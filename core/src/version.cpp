#include "order/version.hpp"

namespace order {

std::string_view version() noexcept {
    return ORDER_CORE_VERSION;
}

}  // namespace order
