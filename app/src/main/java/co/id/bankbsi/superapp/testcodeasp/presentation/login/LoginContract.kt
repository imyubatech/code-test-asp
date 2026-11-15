package co.id.bankbsi.superapp.testcodeasp.presentation.login

interface LoginContract {
    interface View {
        fun showValidation(result: LoginValidator.Result)
        fun openDashboard()
    }
}
