package uk.co.sullenart.photoalbum.sign_in

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.launch
import uk.co.sullenart.photoalbum.auth.Auth

class SignInViewModel(
    private val auth: Auth,
) : ViewModel() {
    val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestServerAuthCode(CLIENT_ID, true)
        .requestScopes(Scope(SCOPE))
        .build()

    fun completeAuth(account: GoogleSignInAccount) {
        viewModelScope.launch {
            val code = account.serverAuthCode ?: ""
            auth.exchangeCode(code)
        }
    }

    companion object {
        private const val CLIENT_ID = "623200176730-43pm5mfljjfj5unb63m75tdhhlt2jcdt.apps.googleusercontent.com"
        private const val SCOPE = "https://www.googleapis.com/auth/photoslibrary.readonly"
    }
}