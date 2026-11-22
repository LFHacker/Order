#pragma once

#include <string_view>

namespace order {

/// 核心库名称，用于日志与 /version 响应。
inline constexpr std::string_view kCoreName = "order-core";

/// 由 CMake 在编译期注入的版本号（当前对应 CMakeLists 的 project VERSION）。
std::string_view version() noexcept;

}  // namespace order
