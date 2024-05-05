package uk.co.sullenart.photoalbum.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.launch
import uk.co.sullenart.photoalbum.auth.Auth
import uk.co.sullenart.photoalbum.auth.UserRepository
import uk.co.sullenart.photoalbum.background.BackgroundFetcher
import uk.co.sullenart.photoalbum.photos.PhotosRepository

class SettingsViewmodel(
    private val auth: Auth,
    private val userRepository: UserRepository,
    private val backgroundFetcher: BackgroundFetcher,
    private val photosRepository: PhotosRepository,
) : ViewModel() {
    val userFlow = userRepository.userFlow
    var loading by mutableStateOf(false)
    var totalPhotos by mutableIntStateOf(0)
    var processedPhotos by mutableIntStateOf(0)

    val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestServerAuthCode(CLIENT_ID, true)
        .requestScopes(Scope(SCOPE))
        .build()

    fun completeAuth(account: GoogleSignInAccount) {
        viewModelScope.launch {
            auth.exchangeCode(account)
        }
    }

    fun handleSignOut() {
        viewModelScope.launch {
            auth.signOut()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            try {
                loading = true
                totalPhotos = 0
                backgroundFetcher.refresh { total, processed ->
                    totalPhotos = total
                    processedPhotos = processed
                }
            } catch (e: Exception) {

            } finally {
                loading = false
            }
        }
    }

    fun clearCaches() {
      photosRepository.clearCaches()
    }

    companion object {
        private const val CLIENT_ID = "623200176730-43pm5mfljjfj5unb63m75tdhhlt2jcdt.apps.googleusercontent.com"
        private const val SCOPE = "https://www.googleapis.com/auth/photoslibrary.readonly"
    }
}