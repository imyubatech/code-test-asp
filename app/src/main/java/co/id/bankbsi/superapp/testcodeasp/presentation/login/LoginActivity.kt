package co.id.bankbsi.superapp.testcodeasp.presentation.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import co.id.bankbsi.superapp.testcodeasp.R
import co.id.bankbsi.superapp.testcodeasp.databinding.ActivityLoginBinding
import co.id.bankbsi.superapp.testcodeasp.presentation.core.BaseActivity
import co.id.bankbsi.superapp.testcodeasp.presentation.dashboard.DashboardActivity

class LoginActivity : BaseActivity(), LoginContract.View {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var presenter: LoginPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (app.sessionManager.isActive()) {
            openDashboard()
            return
        }
        if (app.sessionManager.isLoggedIn()) app.sessionManager.clear()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        presenter = LoginPresenter(this, app.sessionManager)

        binding.inputUsername.doAfterTextChanged { binding.inputUsername.error = null }
        binding.inputPassword.doAfterTextChanged { binding.inputPassword.error = null }
        binding.buttonLanguage.setOnClickListener { toggleLanguage() }
        binding.buttonLogin.setOnClickListener {
            presenter.submit(
                binding.inputUsername.text?.toString().orEmpty(),
                binding.inputPassword.text?.toString().orEmpty()
            )
        }
        if (intent.getBooleanExtra(EXTRA_TIMED_OUT, false) && savedInstanceState == null) {
            Toast.makeText(this, R.string.session_expired, Toast.LENGTH_LONG).show()
        }
    }

    override fun showValidation(result: LoginValidator.Result) {
        binding.inputUsername.error = if (result.usernameMissing) getString(R.string.username_required) else null
        binding.inputPassword.error = if (result.passwordMissing) getString(R.string.password_required) else null
    }

    override fun openDashboard() {
        startActivity(Intent(this, DashboardActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    override fun onDestroy() {
        if (::presenter.isInitialized) presenter.detach()
        super.onDestroy()
    }

    companion object { const val EXTRA_TIMED_OUT = "extra_timed_out" }
}
