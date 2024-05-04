package uk.co.sullenart.photoalbum.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import org.koin.androidx.compose.koinViewModel
import uk.co.sullenart.photoalbum.R
import uk.co.sullenart.photoalbum.auth.User

@Composable
fun SettingsScreen(
    viewModel: SettingsViewmodel = koinViewModel(),
) {
    val user: User? = viewModel.userFlow.collectAsStateWithLifecycle(initialValue = null).value

    Card(
        modifier = Modifier
            .padding(dimensionResource(R.dimen.paddingM))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            if (viewModel.loading) {
                CircularProgressIndicator()
            }
            Column(
                modifier = Modifier
                    .padding(dimensionResource(R.dimen.paddingM))
            ) {
                Greeting(
                    modifier = Modifier
                        .padding(bottom = dimensionResource(R.dimen.paddingM)),
                )
                if (user != null) {
                    UserDetails(
                        modifier = Modifier
                            .padding(bottom = dimensionResource(R.dimen.paddingM)),
                        name = user.name,
                        email = user.email,
                    )
                }
                AuthButtons(
                    signInOptions = viewModel.signInOptions,
                    completeAuth = viewModel::completeAuth,
                    signOut = viewModel::handleSignOut,
                    isSignedIn = user != null,
                    refresh = viewModel::refresh,
                )
            }
        }
    }
}

@Composable
private fun Greeting(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Welcome",
            style = MaterialTheme.typography.headlineLarge,
        )
    }
}

@Composable
private fun UserDetails(
    modifier: Modifier = Modifier,
    name: String,
    email: String,
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = email,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun AuthButtons(
    signInOptions: GoogleSignInOptions,
    completeAuth: (GoogleSignInAccount) -> Unit,
    signOut: () -> Unit,
    refresh: () -> Unit,
    isSignedIn: Boolean,
) {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        // TODO Check for error
        val account: GoogleSignInAccount = GoogleSignIn.getSignedInAccountFromIntent(it.data).result
        completeAuth(account)
    }
    val context = LocalContext.current
    val client = remember { GoogleSignIn.getClient(context, signInOptions) }

    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(
            modifier = Modifier
                .widthIn(200.dp),
            onClick = {
                val signInIntent = client.signInIntent
                launcher.launch(signInIntent)
            },
            enabled = !isSignedIn,
        ) {
            Text(stringResource(R.string.sign_in))
        }
        Button(
            modifier = Modifier
                .widthIn(200.dp),
            onClick = {
                client.signOut()
                signOut()
            },
            enabled = isSignedIn,
        ) {
            Text(stringResource(R.string.sign_out))
        }
        Button(
            modifier = Modifier
                .widthIn(200.dp),
            onClick = {
                client.signOut()
                refresh()
            },
            enabled = isSignedIn,
        ) {
            Text(stringResource(R.string.refresh))
        }
    }
}