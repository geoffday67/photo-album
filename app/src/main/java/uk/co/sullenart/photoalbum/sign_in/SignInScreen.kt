package uk.co.sullenart.photoalbum.sign_in

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import org.koin.androidx.compose.koinViewModel

@Composable
fun SignInScreen(
    viewModel: SignInViewModel = koinViewModel()
) {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        // TODO Check for error
        val account: GoogleSignInAccount = GoogleSignIn.getSignedInAccountFromIntent(it.data).result
        viewModel.completeAuth(account)
    }
    val client = GoogleSignIn.getClient(LocalContext.current, viewModel.signInOptions)

    Column(
        Modifier.fillMaxSize(),
    ) {
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                val signInIntent = client.signInIntent
                launcher.launch(signInIntent)
            }) {
            Text("Sign in")
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                client.signOut()
            }) {
            Text("Sign out")
        }
    }
}
