package com.motawfik.minigram.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.motawfik.minigram.data.FACEBOOK_LOGIN_STATUS
import com.motawfik.minigram.data.FirebaseAuth
import com.motawfik.minigram.data.GOOGLE_LOGIN_STATUS
import com.motawfik.minigram.data.LOGIN_STATUS
import kotlinx.coroutines.*

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    var email = MutableLiveData<String>()
    var password = MutableLiveData<String>()

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel() // to cancel all coroutines when the view model is terminated
    }

    private var _loggedIn = MutableLiveData<LOGIN_STATUS>()
    val loggedIn: LiveData<LOGIN_STATUS>
        get() = _loggedIn

    var loginMessage = Transformations.map(_loggedIn) {
        when (it) {
            LOGIN_STATUS.SUCCESS -> "LOGGED IN"
            LOGIN_STATUS.NO_USER -> "NO USER FOUND"
            LOGIN_STATUS.INVALID_CREDENTIALS -> "INVALID USERNAME/PASSWORD"
            LOGIN_STATUS.UNKNOWN_ERROR -> "UNKNOWN ERROR FOUND"
            else -> ""
        }
    }

    private var _facebookStatus = MutableLiveData<FACEBOOK_LOGIN_STATUS>()
    val facebookStatus: LiveData<FACEBOOK_LOGIN_STATUS>
        get() = _facebookStatus

    var facebookLoginMessage = Transformations.map(_facebookStatus) {
        when (it) {
            FACEBOOK_LOGIN_STATUS.SUCCESS -> "LOGGED IN USING FACEBOOK"
            FACEBOOK_LOGIN_STATUS.DUPLICATE_EMAIL -> "ACCOUNT ALREADY EXIST WITH THE SAME EMAIL"
            FACEBOOK_LOGIN_STATUS.UNKNOWN_ERROR -> "UNKNOWN ERROR OCCURRED"
            else -> ""
        }
    }

    private var _googleStatus = MutableLiveData<GOOGLE_LOGIN_STATUS>()
    val googleStatus: LiveData<GOOGLE_LOGIN_STATUS>
        get() = _googleStatus

    var googleLoginMessage = Transformations.map(_googleStatus) {
        when (it) {
            GOOGLE_LOGIN_STATUS.SUCCESS -> "LOGGED IN USING GOOGLE"
            GOOGLE_LOGIN_STATUS.UNKNOWN_ERROR -> "UNKNOWN ERROR OCCURRED"
            else -> ""
        }
    }

    private var _loginLoading = MutableLiveData<Boolean>()
    val loginLoading: LiveData<Boolean>
        get() = _loginLoading

    private var _googleLogin = MutableLiveData<Boolean>()
    val googleLogin: LiveData<Boolean>
        get() = _googleLogin

    private val firebaseAuth = FirebaseAuth()

    init {
        email.value = ""
        password.value = ""

    }

    fun login() {
        uiScope.launch {
            _loginLoading.value = true
            withContext(Dispatchers.IO) {
                _loggedIn.postValue(firebaseAuth.loginWithEmailAndPassword(email.value!!, password.value!!))
            }
            _loginLoading.value = false
        }
    }

    fun loginWithGoogleFlag() {
        _googleLogin.value = true
    }
    fun finishLoggingWithGoogle() {
        _googleStatus.value = GOOGLE_LOGIN_STATUS.NONE
        _googleLogin.value = false
    }

    fun finishFacebookLogin() {
        _facebookStatus.value = FACEBOOK_LOGIN_STATUS.NONE
    }

    fun loginWithGoogle(task: Task<GoogleSignInAccount>) {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                _googleStatus.postValue(firebaseAuth.loginWithGoogle(task))
            }
        }

    }

    fun finishedLoggingIn() {
        _loggedIn.value = LOGIN_STATUS.NONE
    }

    fun loginWithFacebook(result: LoginResult) {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                _facebookStatus.postValue(firebaseAuth.loginWithFacebook(result.accessToken))
            }
        }
    }
}