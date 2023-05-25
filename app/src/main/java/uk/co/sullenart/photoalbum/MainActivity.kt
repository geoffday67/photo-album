package uk.co.sullenart.photoalbum

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import uk.co.sullenart.photoalbum.ui.theme.PhotoAlbumTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = PhotosViewModel()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestServerAuthCode("623200176730-43pm5mfljjfj5unb63m75tdhhlt2jcdt.apps.googleusercontent.com", true)
            .requestScopes(Scope("https://www.googleapis.com/auth/photoslibrary.readonly"))
            .build()
        val client = GoogleSignIn.getClient(this, gso)

        setContent {
            val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                val account: GoogleSignInAccount = GoogleSignIn.getSignedInAccountFromIntent(it.data).result
                val authCode = account.serverAuthCode ?: ""
                viewModel.completeAuth(authCode)
            }
            PhotoAlbumTheme {
                Column {
                    Button(onClick = {
                        val signInIntent = client.signInIntent
                        launcher.launch(signInIntent)
                    }) {
                        Text("Sign in")
                    }
                    Button(onClick = {
                        client.signOut()
                    }) {
                        Text("Sign out")
                    }
                    Button(onClick = {
                        try{
                            val adminName = ComponentName(this@MainActivity, DeviceAdmin::class.java)
                            val dpm = getSystemService(DevicePolicyManager::class.java)
                            dpm.setLockTaskPackages(adminName, arrayOf(packageName))
                            Log.d("Photo", "Lock task mode enabled")
                        } catch (e: Exception) {
                            Log.e("Photo", "Error enabling lock task mode", e)
                        }
                    }) {
                        Text("Lock task mode")
                    }
                    LazyColumn {
                        items(viewModel.albums) {
                            Text(it.title)
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val dpm = getSystemService(DevicePolicyManager::class.java)
        if (dpm.isLockTaskPermitted(packageName)) {
            Log.d("Photo", "Starting in lock task mode")
            startLockTask()
        }
    }
}
