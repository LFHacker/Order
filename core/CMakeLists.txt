cmake_minimum_required(VERSION 3.20)
project(order_core VERSION 0.1.0 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

if(NOT CMAKE_BUILD_TYPE AND NOT CMAKE_CONFIGURATION_TYPES)
  set(CMAKE_BUILD_TYPE Release CACHE STRING "" FORCE)
endif()

option(ORDER_BUILD_TESTS "Build core unit tests" ON)

add_library(order_core STATIC
  src/version.cpp
)

target_include_directories(order_core PUBLIC ${CMAKE_CURRENT_SOURCE_DIR}/include)

# 版本号由 CMake 注入，避免代码里硬编码
target_compile_definitions(order_core PUBLIC
  ORDER_CORE_VERSION="${PROJECT_VERSION}"
)

if(MSVC)
  target_compile_options(order_core PRIVATE /W4 /permissive- /utf-8)
else()
  target_compile_options(order_core PRIVATE -Wall -Wextra -Wpedantic)
endif()

if(ORDER_BUILD_TESTS)
  enable_testing()
  add_executable(order_core_tests tests/test_version.cpp)
  target_link_libraries(order_core_tests PRIVATE order_core)
  if(MSVC)
    target_compile_options(order_core_tests PRIVATE /utf-8)
  endif()
  add_test(NAME core_tests COMMAND order_core_tests)
endif()
