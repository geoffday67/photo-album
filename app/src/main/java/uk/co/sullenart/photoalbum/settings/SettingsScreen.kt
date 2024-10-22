package uk.co.sullenart.photoalbum.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import org.koin.androidx.compose.koinViewModel
import uk.co.sullenart.photoalbum.R
import uk.co.sullenart.photoalbum.auth.User
import uk.co.sullenart.photoalbum.ui.theme.Pink80

@Composable
fun SettingsScreen(
    viewModel: SettingsViewmodel = koinViewModel(),
    onDismiss: () -> Unit,
) {
    val user: User? = viewModel.userFlow.collectAsStateWithLifecycle(initialValue = null).value

    Dialog(
        onDismissRequest = onDismiss,
    ) {
        Card {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = dimensionResource(R.dimen.paddingM), start = dimensionResource(R.dimen.paddingM), end = dimensionResource(R.dimen.paddingM))
                    .padding(dimensionResource(R.dimen.paddingM))
            ) {
                Greeting(
                    modifier = Modifier
                        .padding(bottom = dimensionResource(R.dimen.paddingM)),
                )
                Battery(
                    level = viewModel.batteryLevel,
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
                ClearButton(viewModel::clearData)
                LockButton(viewModel::enableLockMode)
                DialogButtons(onDismiss)
            }
        }

        if (viewModel.loading) {
            if (viewModel.totalPhotos > 0) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth(),
                    progress = { viewModel.processedPhotos.toFloat() / viewModel.totalPhotos.toFloat() },
                    color = Pink80,
                )
            } else {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth(),
                    color = Pink80,
                )
            }
        }
    }
}

@Composable
private fun Battery(
    level: Int,
) {
    Text("${stringResource(R.string.battery)} $level%")
}

@Composable
private fun DialogButtons(
    onDismiss: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {
        TextButton(
            onClick = onDismiss,
        ) {
            Text("Close")
        }
    }
}

@Composable
private fun ClearButton(
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(

            modifier = Modifier
                .widthIn(200.dp),
            onClick = onClick
        ) {
            Text("Clear data")
        }
    }
}

@Composable
private fun LockButton(
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(

            modifier = Modifier
                .widthIn(200.dp),
            onClick = onClick
        ) {
            Text("Enable lock mode")
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