package com.example.studyblocks.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.studyblocks.R
import com.example.studyblocks.auth.AuthState
import com.example.studyblocks.auth.AuthViewModel
import com.example.studyblocks.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(LocalContext.current as androidx.activity.ComponentActivity)
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    
    val authState by authViewModel.authState.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                navController.navigate(Screen.Today.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }
            else -> {}
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "StudyBlocks",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "AI-Powered Study Scheduler",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 48.dp)
        )
        
        OutlinedTextField(
            value = email,
            onValueChange = { 
                email = it
                emailError = null
            },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            isError = emailError != null,
            supportingText = emailError?.let { { Text(it) } }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = password,
            onValueChange = { 
                password = it
                passwordError = null
            },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            isError = passwordError != null,
            supportingText = passwordError?.let { { Text(it) } }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = {
                keyboardController?.hide()
                
                emailError = when {
                    email.isBlank() -> "Email is required"
                    !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Invalid email format"
                    else -> null
                }
                
                passwordError = when {
                    password.isBlank() -> "Password is required"
                    password.length < 6 -> "Password must be at least 6 characters"
                    else -> null
                }
                
                if (emailError == null && passwordError == null) {
                    authViewModel.signInWithEmail(email, password)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Sign In")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        TextButton(
            onClick = { authViewModel.sendPasswordResetEmail(email) },
            enabled = email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        ) {
            Text("Forgot Password?")
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Don't have an account? ",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(
                onClick = { navController.navigate(Screen.SignUp.route) }
            ) {
                Text("Sign Up")
            }
        }
        
        val errorState = authState
        if (errorState is AuthState.Error) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = errorState.message,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}