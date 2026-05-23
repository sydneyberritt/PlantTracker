package com.example.planttracker

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import kotlinx.coroutines.launch

@Composable
fun SignUpScreen(onSignUpSuccess: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordCopy by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.signup_title),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = stringResource(R.string.signup_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            CozyTextField(
                value = email,
                onValueChange = {
                    email = it
                    error = null
                    prefs.edit { putString("username", it) }
                },
                label = stringResource(R.string.email_label),
                icon = R.drawable.email
            )

            Spacer(Modifier.height(16.dp))

            CozyTextField(
                value = password,
                onValueChange = {
                    password = it
                    error = null
                },
                label = stringResource(R.string.password_label),
                icon = R.drawable.lock,
                isPassword = true
            )

            Spacer(Modifier.height(16.dp))

            CozyTextField(
                value = passwordCopy,
                onValueChange = {
                    passwordCopy = it
                    error = null
                },
                label = stringResource(R.string.confirm_password_label),
                icon = R.drawable.lock,
                isPassword = true
            )

            Spacer(Modifier.height(32.dp))

            val registrationFailedMessage = stringResource(R.string.registration_failed)
            val welcomeMessage = "Welcome to the family!"
            val passMatchMessage = stringResource(R.string.passwords_do_not_match)

            Button(
                onClick = {
                    if (password != passwordCopy) {
                        Toast.makeText(context, passMatchMessage, Toast.LENGTH_SHORT).show()
                    } else {
                        isLoading = true
                        error = null

                        scope.launch {
                            try {
                                AuthRepository.register(email, password)
                                Toast.makeText(context, welcomeMessage, Toast.LENGTH_LONG).show()
                                onSignUpSuccess()
                            } catch (e: Exception) {
                                error = e.message ?: registrationFailedMessage
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                enabled = !isLoading && email.isNotBlank() && password.length >= 6
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(text = stringResource(R.string.create_account_button), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(24.dp))

            TextButton(
                onClick = { onSignUpSuccess() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.already_have_account),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            error?.let {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}
