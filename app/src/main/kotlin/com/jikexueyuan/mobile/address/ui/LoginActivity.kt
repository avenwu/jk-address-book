package com.jikexueyuan.mobile.address.ui

import android.accounts.Account
import android.accounts.AccountManager
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.inputmethod.EditorInfo
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.TextView
import com.jikexueyuan.mobile.address.*
import com.jikexueyuan.mobile.address.api.AppService
import com.jikexueyuan.mobile.address.extention.startActivitySafely
import kotlinx.android.synthetic.content_login.email_sign_in_button

/**
 * A login screen that offers login via email/password.
 */
class LoginActivity : AppCompatActivity() {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private var mAuthTask: AsyncTask<*, *, *> ? = null

    // UI references.
    private var mEmailView: AutoCompleteTextView? = null
    private var mPasswordView: EditText? = null
    private var mProgressView: View? = null
    private var mLoginFormView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        mEmailView = findViewById(R.id.email) as AutoCompleteTextView

        mPasswordView = findViewById(R.id.password) as EditText
        mPasswordView!!.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(textView: TextView, id: Int, keyEvent: KeyEvent): Boolean {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin()
                    return true
                }
                return false
            }
        })

        email_sign_in_button.setOnClickListener(object : OnClickListener {
            override fun onClick(view: View) {
                attemptLogin()
            }
        })

        mLoginFormView = findViewById(R.id.login_form)
        mProgressView = findViewById(R.id.login_progress)
        if (!intent.hasExtra(Authenticator.PARAM_AUTHTOKEN_TYPE)) {
            trySkipLogin()
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private fun attemptLogin() {
        if (mAuthTask != null) {
            return
        }

        // Reset errors.
        mEmailView!!.error = null
        mPasswordView!!.error = null

        // Store values at the time of the login attempt.
        val email = mEmailView!!.text.toString()
        val password = mPasswordView!!.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView!!.error = getString(R.string.error_invalid_password)
            focusView = mPasswordView
            cancel = true
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView!!.error = getString(R.string.error_field_required)
            focusView = mEmailView
            cancel = true
        } else if (!isEmailValid(email)) {
            mEmailView!!.error = getString(R.string.error_invalid_email)
            focusView = mEmailView
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView!!.requestFocus()
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true)
            var name = mEmailView!!.text.toString()
            var pwd = mPasswordView!!.text.toString()

            mAuthTask = AppService.login(name, pwd, { success ->
                mAuthTask = null
                showProgress(false)

                if (success) {
                    val account = Account(name, Authenticator.PARAM_ACCOUNT_TYPE)
                    val bundle = Bundle()
                    AccountManager.get(this).addAccountExplicitly(account, pwd, bundle)
                    updateLoginTimestamp(this, System.currentTimeMillis())
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    if (isNetWorkAvailable(this)) {
                        mPasswordView!!.error = getString(R.string.error_incorrect_password)
                        mPasswordView!!.requestFocus()
                    } else {
                        Snackbar.make(mProgressView, R.string.network_is_unavailable, Snackbar.LENGTH_LONG)
                                .setAction(R.string.open_setting, {
                                    startActivitySafely(Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS), {
                                        Snackbar.make(mProgressView, R.string.failed_open_setting, Snackbar.LENGTH_SHORT).show()
                                    })
                                }).show()
                    }
                }
            })

        }
    }

    /**
     * 1. 3分钟内免登陆; 2.有网自动登录；3.无网络有缓存，跳过登录
     */
    fun trySkipLogin() {
        var justLogin = (System.currentTimeMillis() - getLoginTimestamp(this)) < 3 * 60 * 1000
        if (justLogin) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            val manager = AccountManager.get(this)
            val accounts = manager.getAccountsByType(Authenticator.PARAM_ACCOUNT_TYPE)
            if (accounts != null && accounts.size > 0) {
                val name = accounts[0].name
                val pwd = manager.getPassword(accounts[0])
                if (!TextUtils.isEmpty(name)) {
                    mEmailView?.setText(name)
                }
                if (!TextUtils.isEmpty(pwd)) {
                    mPasswordView?.setText(pwd)
                }
                if (isNetWorkAvailable(this)) {
                    email_sign_in_button.performClick()
                } else {
                    checkUserListCache(this, {
                        if (it) {
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                    })
                }
            }
        }
    }

    private fun isEmailValid(email: String): Boolean {
        return true
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length > 6
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private fun showProgress(show: Boolean) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime)

            mLoginFormView!!.visibility = if (show) View.GONE else View.VISIBLE
            mLoginFormView!!.animate().setDuration(shortAnimTime.toLong()).alpha(
                    (if (show) 0 else 1).toFloat()).setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    mLoginFormView!!.visibility = if (show) View.GONE else View.VISIBLE
                }
            })

            mProgressView!!.visibility = if (show) View.VISIBLE else View.GONE
            mProgressView!!.animate().setDuration(shortAnimTime.toLong()).alpha(
                    (if (show) 1 else 0).toFloat()).setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    mProgressView!!.visibility = if (show) View.VISIBLE else View.GONE
                }
            })
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView!!.visibility = if (show) View.VISIBLE else View.GONE
            mLoginFormView!!.visibility = if (show) View.GONE else View.VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mAuthTask?.cancel(true)
    }
}

