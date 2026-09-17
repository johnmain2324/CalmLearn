package com.education.calmlearn.ui.start

/** Man hinh nen mo dau khi ung dung khoi dong. */
enum class StartDestination {
    ONBOARDING,
    LOGIN,
    HOME
}

/**
 * Quyet dinh man hinh mo dau, tach thanh ham thuan (khong phu thuoc Context/Android) de unit test
 * bang JUnit thuong. Xem MainActivity de biet cach anh xa sang id trong nav_graph.xml.
 *
 * Quy tac (dung yeu cau da thong nhat):
 * - Chua xem/bo qua Onboarding -> Onboarding.
 * - Da xem Onboarding nhung chua co phien dang nhap hop le -> Dang nhap.
 * - Da xem Onboarding VA dang co phien dang nhap hop le (tu AuthRepository that, khong phai co
 *   lai local) -> Trang chu.
 */
object StartDestinationResolver {
    fun resolve(hasCompletedOnboarding: Boolean, isAuthenticated: Boolean): StartDestination = when {
        !hasCompletedOnboarding -> StartDestination.ONBOARDING
        isAuthenticated -> StartDestination.HOME
        else -> StartDestination.LOGIN
    }
}
