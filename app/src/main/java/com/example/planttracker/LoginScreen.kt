package com.example.planttracker

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit, onSignUp: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE) }
    var email by remember {
        mutableStateOf(prefs.getString("username", "") ?: "")
    }
    var password by remember {
        mutableStateOf(prefs.getString("password", "") ?: "")
    }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var checked by remember {
        mutableStateOf(prefs.getBoolean("remember_me", false))
    }
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
                text = stringResource(R.string.login_title),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = stringResource(R.string.welcome_back),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            CozyTextField(
                value = email,
                onValueChange = {
                    email = it
                    error = null
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Checkbox(
                    checked = checked,
                    onCheckedChange = { checked = it },
                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                )
                Text(
                    text = stringResource(R.string.remember_login),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    isLoading = true
                    error = null
                    scope.launch {
                        try {
                            AuthRepository.login(email, password)
                            prefs.edit {
                                putBoolean("remember_me", checked)
                                if (checked) {
                                    putString("username", email)
                                    putString("password", password)
                                } else {
                                    remove("username")
                                    remove("password")
                                }
                            }
                            onLoginSuccess()
                        } catch (e: Exception) {
                            error = e.message ?: "Login failed"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(stringResource(R.string.login_button), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(24.dp))

            TextButton(onClick = onSignUp) {
                Text(
                    text = stringResource(R.string.no_account_signup),
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

@Composable
fun CozyTextField(
    value: String, 
    onValueChange: (String) -> Unit, 
    label: String, 
    icon: Int, 
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        },
        singleLine = true,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            cursorColor = MaterialTheme.colorScheme.primary
        )
    )
}
