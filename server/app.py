"""Order API —— 认证服务（注册 / 登录 / 续期 / 登出）。

响应信封固定为 {ok, data, server_time} 或 {ok:false, data:null, error:{code,message}, server_time}。
"""
import os
import socket
import sqlite3
import time
import secrets
from datetime import datetime, timezone
from functools import wraps

from flask import Flask, g, jsonify, request
from werkzeug.security import check_password_hash, generate_password_hash

VERSION = os.environ.get("ORDER_VERSION", "debug-0.5.1A-20261122")
DB_PATH = os.environ.get("ORDER_DB", "/srv/order/order.db")
TOKEN_TTL = 30 * 24 * 3600
STARTED = time.time()

app = Flask(__name__)


def now_iso() -> str:
    return (
        datetime.now(timezone.utc)
        .isoformat(timespec="milliseconds")
        .replace("+00:00", "Z")
    )


def db() -> sqlite3.Connection:
    conn = sqlite3.connect(DB_PATH, timeout=10)
    conn.row_factory = sqlite3.Row
    return conn


def init_db() -> None:
    with db() as conn:
        conn.executescript(
            """
            CREATE TABLE IF NOT EXISTS users (
                id            TEXT PRIMARY KEY,
                username      TEXT UNIQUE NOT NULL,
                password_hash TEXT NOT NULL,
                created_at    TEXT NOT NULL
            );
            CREATE TABLE IF NOT EXISTS sessions (
                token      TEXT PRIMARY KEY,
                user_id    TEXT NOT NULL,
                device_id  TEXT,
                created_at TEXT NOT NULL,
                expires_at INTEGER NOT NULL
            );
            CREATE INDEX IF NOT EXISTS idx_sessions_user ON sessions(user_id);
            """
        )


init_db()


def ok(data=None, status: int = 200):
    return jsonify(ok=True, data=data, server_time=now_iso()), status


def fail(code: str, message: str, status: int):
    return (
        jsonify(
            ok=False,
            data=None,
            error={"code": code, "message": message},
            server_time=now_iso(),
        ),
        status,
    )


def iso_from_epoch(ts: int) -> str:
    return datetime.fromtimestamp(ts, timezone.utc).isoformat(timespec="seconds").replace("+00:00", "Z")


def new_token(user_id: str, device_id: str) -> dict:
    """签发 token，返回 token 与到期时间（ISO-8601 UTC）。"""
    token = secrets.token_urlsafe(32)
    expires_at = int(time.time()) + TOKEN_TTL
    with db() as conn:
        conn.execute(
            "INSERT INTO sessions(token, user_id, device_id, created_at, expires_at)"
            " VALUES(?,?,?,?,?)",
            (token, user_id, device_id, now_iso(), expires_at),
        )
    return {"token": token, "expires_at": iso_from_epoch(expires_at)}


def require_auth(fn):
    @wraps(fn)
    def wrapper(*args, **kwargs):
        header = request.headers.get("Authorization", "")
        if not header.startswith("Bearer "):
            return fail("UNAUTHORIZED", "缺少 token", 401)
        token = header[7:].strip()
        with db() as conn:
            row = conn.execute(
                "SELECT s.user_id, s.expires_at, u.username"
                " FROM sessions s JOIN users u ON u.id = s.user_id WHERE s.token = ?",
                (token,),
            ).fetchone()
        if row is None:
            return fail("UNAUTHORIZED", "token 无效", 401)
        if row["expires_at"] < int(time.time()):
            return fail("UNAUTHORIZED", "token 已过期", 401)
        g.user_id = row["user_id"]
        g.username = row["username"]
        g.token = token
        return fn(*args, **kwargs)

    return wrapper


def user_payload(user_id: str, username: str) -> dict:
    return {"user_id": user_id, "username": username}


def valid_username(name: str) -> bool:
    return 3 <= len(name) <= 64


@app.get("/healthz")
def healthz():
    return ok(
        {
            "service": "order-api",
            "version": VERSION,
            "hostname": socket.gethostname(),
            "uptime_s": round(time.time() - STARTED, 1),
        }
    )


@app.get("/")
def index():
    return ok({"service": "order-api", "version": VERSION, "health": "/healthz"})


@app.post("/api/v1/auth/register")
def register():
    body = request.get_json(silent=True) or {}
    username = (body.get("username") or "").strip()
    password = body.get("password") or ""
    device_id = (body.get("device_id") or "").strip()[:64]

    if not valid_username(username):
        return fail("BAD_REQUEST", "账号需 3–64 个字符", 400)
    if len(password) < 8:
        return fail("BAD_REQUEST", "密码至少 8 位", 400)

    user_id = "u_" + secrets.token_hex(8)
    try:
        with db() as conn:
            conn.execute(
                "INSERT INTO users(id, username, password_hash, created_at) VALUES(?,?,?,?)",
                (user_id, username, generate_password_hash(password), now_iso()),
            )
    except sqlite3.IntegrityError:
        return fail("CONFLICT", "该账号已被注册", 409)

    issued = new_token(user_id, device_id)
    return ok(
        {
            "token": issued["token"],
            "expires_at": issued["expires_at"],
            "user": user_payload(user_id, username),
        },
        201,
    )


@app.post("/api/v1/auth/login")
def login():
    body = request.get_json(silent=True) or {}
    username = (body.get("username") or "").strip()
    password = body.get("password") or ""
    device_id = (body.get("device_id") or "").strip()[:64]

    if not username or not password:
        return fail("BAD_REQUEST", "账号和密码不能为空", 400)

    with db() as conn:
        row = conn.execute(
            "SELECT id, username, password_hash FROM users WHERE username = ?", (username,)
        ).fetchone()

    if row is None or not check_password_hash(row["password_hash"], password):
        return fail("UNAUTHORIZED", "账号或密码不正确", 401)

    issued = new_token(row["id"], device_id)
    return ok(
        {
            "token": issued["token"],
            "expires_at": issued["expires_at"],
            "user": user_payload(row["id"], row["username"]),
        }
    )


@app.get("/api/v1/auth/me")
@require_auth
def me():
    with db() as conn:
        row = conn.execute(
            "SELECT expires_at FROM sessions WHERE token = ?", (g.token,)
        ).fetchone()
    expires = iso_from_epoch(row["expires_at"]) if row else None
    return ok({"user": user_payload(g.user_id, g.username), "expires_at": expires})


@app.post("/api/v1/auth/logout")
@require_auth
def logout():
    with db() as conn:
        conn.execute("DELETE FROM sessions WHERE token = ?", (g.token,))
    return ok({"logged_out": True})


@app.errorhandler(404)
def not_found(_):
    return fail("NOT_FOUND", "接口不存在", 404)


@app.errorhandler(500)
def server_error(_):
    return fail("INTERNAL", "服务端错误", 500)


if __name__ == "__main__":
    app.run(host="127.0.0.1", port=8100)
