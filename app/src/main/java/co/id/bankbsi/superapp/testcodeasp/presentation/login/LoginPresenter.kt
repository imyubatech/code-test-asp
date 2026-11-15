package co.id.bankbsi.superapp.testcodeasp.presentation.login

import co.id.bankbsi.superapp.testcodeasp.utils.session.SessionManager

class LoginPresenter(
    private var view: LoginContract.View?,
    private val session: SessionManager
) {
    fun submit(username: String, password: String) {
        val result = LoginValidator.validate(username, password)
        view?.showValidation(result)
        if (!result.isValid) return

        session.login(username.trim())
        view?.openDashboard()
    }

    fun detach() { view = null }
}
