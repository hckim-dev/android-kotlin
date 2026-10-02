package com.hocheol.githubrepository

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.hocheol.githubrepository.databinding.ActivitySignInBinding
import com.hocheol.githubrepository.utillity.AuthTokenProvider
import com.hocheol.githubrepository.utillity.RetrofitUtil
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class SignInActivity : AppCompatActivity(), CoroutineScope {

    private lateinit var binding: ActivitySignInBinding
    private val authTokenProvider by lazy { AuthTokenProvider(this) }
    private val job: Job = Job()
    private var loginJob: Job? = null
    private var verificationDialog: AlertDialog? = null
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (checkAuthCodeExist()) {
            launchMainActivity()
        } else {
            binding.loginButton.setOnClickListener { loginGithub() }
        }
    }

    private fun checkAuthCodeExist(): Boolean = authTokenProvider.token.isNullOrEmpty().not()

    private fun launchMainActivity() = startActivity(
        Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    )

    private fun loginGithub() {
        loginJob?.cancel()
        loginJob = launch {
            showProgress()
            try {
                // Device flow authenticates a public client without an embedded client secret.
                val response = RetrofitUtil.authApiService.getDeviceCode(BuildConfig.GITHUB_CLIENT_ID)
                val device = response.body()
                check(response.isSuccessful && device != null) { "Device authorization failed" }
                check(device.deviceCode.isNotBlank() && device.userCode.isNotBlank())
                check(device.expiresIn in 1..900 && device.interval in 1..900)
                val uri = Uri.parse(device.verificationUri)
                check(uri.scheme == "https" && uri.host == "github.com" && uri.path == "/login/device")

                verificationDialog = AlertDialog.Builder(this@SignInActivity)
                    .setTitle("GitHub 로그인")
                    .setMessage("인증 페이지에서 다음 코드를 입력하세요: ${device.userCode}")
                    .setPositiveButton("인증 페이지 열기") { _, _ ->
                        CustomTabsIntent.Builder().build().launchUrl(this@SignInActivity, uri)
                    }
                    .setNegativeButton("취소") { _, _ -> loginJob?.cancel() }
                    .setOnCancelListener { loginJob?.cancel() }
                    .show()

                withTimeout(device.expiresIn * 1000) {
                    var intervalSeconds = device.interval.coerceAtLeast(5)
                    while (isActive) {
                        delay(intervalSeconds * 1000)
                        val tokenResponse = RetrofitUtil.authApiService.getAccessToken(
                            clientId = BuildConfig.GITHUB_CLIENT_ID,
                            deviceCode = device.deviceCode
                        )
                        check(tokenResponse.isSuccessful) { "Token request failed" }
                        val token = tokenResponse.body() ?: error("Empty token response")
                        when (token.error) {
                            "authorization_pending" -> Unit
                            "slow_down" -> intervalSeconds += 5
                            null -> {
                                val accessToken = token.accessToken
                                check(!accessToken.isNullOrBlank()) { "Missing access token" }
                                authTokenProvider.updateToken(accessToken)
                                launchMainActivity()
                                return@withTimeout
                            }
                            else -> error("Device authorization denied or expired")
                        }
                    }
                }
            } catch (e: TimeoutCancellationException) {
                Toast.makeText(this@SignInActivity, "인증 시간이 만료되었습니다", Toast.LENGTH_SHORT).show()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Toast.makeText(this@SignInActivity, "로그인할 수 없습니다. 다시 시도해 주세요", Toast.LENGTH_SHORT).show()
            } finally {
                verificationDialog?.dismiss()
                verificationDialog = null
                if (!isFinishing && !isDestroyed) dismissProgress()
            }
        }
    }

    private fun showProgress() = with(binding) {
        loginButton.isVisible = false
        progressBar.isGone = false
    }

    private fun dismissProgress() = with(binding) {
        loginButton.isVisible = true
        progressBar.isGone = true
    }

    override fun onDestroy() {
        verificationDialog?.dismiss()
        job.cancel()
        super.onDestroy()
    }
}
