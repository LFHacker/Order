// 最小单测运行器：不引入第三方框架，保证离线可编译。
#include <cstdio>
#include <string_view>

#include "order/version.hpp"

namespace {

int g_failed = 0;

void check(bool cond, const char* expr, const char* file, int line) {
    if (!cond) {
        std::printf("FAIL  %s:%d  %s\n", file, line, expr);
        ++g_failed;
    }
}

}  // namespace

#define CHECK(cond) check((cond), #cond, __FILE__, __LINE__)

int main() {
    CHECK(!order::version().empty());
    CHECK(order::version() == std::string_view{"0.1.0"});
    CHECK(order::kCoreName == "order-core");

    if (g_failed == 0) {
        std::printf("all checks passed (version=%s)\n", order::version().data());
        return 0;
    }
    std::printf("%d check(s) failed\n", g_failed);
    return 1;
}
