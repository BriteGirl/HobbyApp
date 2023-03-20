package pl.com.britenet.hobbyapp.user

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import pl.com.britenet.hobbyapp.MainActivity
import pl.com.britenet.hobbyapp.R
import pl.com.britenet.hobbyapp.databinding.ActivityLoginBinding

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()
    private val startGoogleSignIn = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.onLoginOrRegisterWithGoogleComplete(it.data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // observe LiveData from the ViewModel
        viewModel.purpose.observe(this) { purpose -> onPurposeToggled(purpose) }
        viewModel.userLoggedIn.observe(this) { goToMainActivity() }
        viewModel.loginException.observe(this) { errorMessage -> showException(errorMessage) }
        viewModel.intentToStartForResult.observe(this) { startLoginOrRegister(it) }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        binding.loginOrRegisterLink.setOnClickListener { viewModel.togglePurpose() }
        binding.submitBtn.setOnClickListener { loginOrRegister() }
        binding.googleSignInBtn.setOnClickListener { viewModel.loginOrRegisterWithGoogle() }

        setContentView(binding.root)
    }

    private fun startLoginOrRegister(intent: Intent?) {
        if (intent != null) {
            startGoogleSignIn.launch(intent)
        }
    }

    private fun loginOrRegister() {
        val login: String = binding.emailInput.text.toString()
        val password: String = binding.passwordInput.text.toString()

        viewModel.loginOrRegisterWithEmail(login, password)
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun onPurposeToggled(purpose: LoginViewModel.Companion.Purpose) {
        if (purpose == LoginViewModel.Companion.Purpose.LOGIN) {
            binding.loginOrRegisterLabel.text = getString(R.string.sign_in_label)
            binding.loginOrRegisterLink.text = getString(R.string.register)
            binding.accountQuestion.text = getString(R.string.no_account_tv)
            binding.submitBtn.text = getString(R.string.login_btn)
            val button: Button = binding.googleSignInBtn.getChildAt(0) as Button
            button.text = getString(R.string.sign_in_with_google)
        } else {
            binding.loginOrRegisterLabel.text = getString(R.string.register_label)
            binding.loginOrRegisterLink.text = getString(R.string.log_in)
            binding.accountQuestion.text = getString(R.string.have_account_tv)
            binding.submitBtn.text = getString(R.string.register_btn)
            val button: Button = binding.googleSignInBtn.getChildAt(0) as Button
            button.text = getString(R.string.sign_up_with_google)
        }
    }

    private fun showException(exception: Exception?) {
        if (exception != null) {
            val message: String? = exception.message
            Toast.makeText(this, message, Toast.LENGTH_SHORT)
                .show()
            viewModel.onErrorDisplayed()
        }
    }
}
