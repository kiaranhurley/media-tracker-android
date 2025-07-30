package com.kiaranhurley.mediatracker.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kiaranhurley.mediatracker.database.entities.User
import com.kiaranhurley.mediatracker.ui.theme.*
import java.util.regex.Pattern

// Email validation regex pattern
private val EMAIL_PATTERN = Pattern.compile(
    "[a-zA-Z0-9+._%-+]{1,256}@[a-zA-Z0-9][a-zA-Z0-9-]{0,64}(\\.[a-zA-Z0-9][a-zA-Z0-9-]{0,25})+"
)

// Validation helper functions
private fun isValidEmail(email: String): Boolean {
    return EMAIL_PATTERN.matcher(email).matches()
}

private fun isValidPassword(password: String): Boolean {
    return password.length >= 6
}

private fun isValidUsername(username: String): Boolean {
    return username.length >= 3 && username.matches(Regex("^[a-zA-Z0-9_]+$"))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onAuthSuccess: (User) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var isLoginMode by remember { mutableStateOf(true) }
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    
    // Shared form state that persists when switching between Sign In/Sign Up
    var username by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    // Animation states
    val logoScale by animateFloatAsState(
        targetValue = if (isLoginMode) 1f else 0.9f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "logoScale"
    )
    
    val contentAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(800),
        label = "contentAlpha"
    )
    
    // Handle auth success
    LaunchedEffect(authState) {
        val currentState = authState
        if (currentState is AuthState.Success) {
            onAuthSuccess(currentState.user)
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        PrimaryGray,
                        SurfaceGray
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
                .alpha(contentAlpha),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            // App Logo & Branding
            Card(
                modifier = Modifier
                    .scale(logoScale)
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            Text(
                text = "MediaTracker",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                ),
                color = TextTertiary
            )
            
            Text(
                text = "Track movies and games like a pro",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp
                ),
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 48.dp)
            )
            
            // Main Auth Card
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 12.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Mode Selector
                    ModeSelector(
                        isLoginMode = isLoginMode,
                        onModeChange = { 
                            isLoginMode = it
                            viewModel.clearError()
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Auth Forms with Animation
                    AnimatedContent(
                        targetState = isLoginMode,
                        transitionSpec = {
                            slideInHorizontally(
                                initialOffsetX = { if (targetState) -it else it },
                                animationSpec = tween(300)
                            ) togetherWith slideOutHorizontally(
                                targetOffsetX = { if (targetState) it else -it },
                                animationSpec = tween(300)
                            )
                        },
                        label = "authForm"
                    ) { loginMode ->
                        if (loginMode) {
                            ModernLoginForm(
                                username = username,
                                onUsernameChange = { username = it },
                                password = password,
                                onPasswordChange = { password = it },
                                passwordVisible = passwordVisible,
                                onPasswordVisibilityChange = { passwordVisible = !passwordVisible },
                                onLogin = viewModel::login,
                                authState = authState,
                                onClearError = viewModel::clearError
                            )
                        } else {
                            ModernRegisterForm(
                                username = username,
                                onUsernameChange = { username = it },
                                displayName = displayName,
                                onDisplayNameChange = { displayName = it },
                                email = email,
                                onEmailChange = { email = it },
                                password = password,
                                onPasswordChange = { password = it },
                                confirmPassword = confirmPassword,
                                onConfirmPasswordChange = { confirmPassword = it },
                                passwordVisible = passwordVisible,
                                onPasswordVisibilityChange = { passwordVisible = !passwordVisible },
                                confirmPasswordVisible = confirmPasswordVisible,
                                onConfirmPasswordVisibilityChange = { confirmPasswordVisible = !confirmPasswordVisible },
                                onRegister = viewModel::register,
                                authState = authState,
                                onClearError = viewModel::clearError
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Footer
            Text(
                text = "Your personal media library awaits",
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ModeSelector(
    isLoginMode: Boolean,
    onModeChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                RoundedCornerShape(16.dp)
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ModeButton(
            text = "Sign In",
            isSelected = isLoginMode,
            onClick = { onModeChange(true) },
            modifier = Modifier.weight(1f)
        )
        ModeButton(
            text = "Sign Up",
            isSelected = !isLoginMode,
            onClick = { onModeChange(false) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ModeButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(200),
        label = "backgroundColor"
    )
    
    val textColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else CardTitleText,
        animationSpec = tween(200),
        label = "textColor"
    )
    
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = backgroundColor,
            contentColor = textColor
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun ModernLoginForm(
    username: String,
    onUsernameChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibilityChange: () -> Unit,
    onLogin: (String, String) -> Unit,
    authState: AuthState,
    onClearError: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        ModernTextField(
            value = username,
            onValueChange = { 
                onUsernameChange(it)
                if (authState is AuthState.Error) onClearError()
            },
            label = "Username or Email",
            isError = authState is AuthState.Error
        )
        
        ModernTextField(
            value = password,
            onValueChange = { 
                onPasswordChange(it)
                if (authState is AuthState.Error) onClearError()
            },
            label = "Password",
            isPassword = true,
            passwordVisible = passwordVisible,
            onPasswordVisibilityChange = onPasswordVisibilityChange,
            isError = authState is AuthState.Error
        )
        
        if (authState is AuthState.Error) {
            ErrorMessage(authState.message)
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        ModernButton(
            text = "Sign In",
            onClick = { onLogin(username, password) },
            enabled = username.isNotBlank() && password.isNotBlank() && authState !is AuthState.Loading,
            isLoading = authState is AuthState.Loading,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ModernRegisterForm(
    username: String,
    onUsernameChange: (String) -> Unit,
    displayName: String,
    onDisplayNameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibilityChange: () -> Unit,
    confirmPasswordVisible: Boolean,
    onConfirmPasswordVisibilityChange: () -> Unit,
    onRegister: (User) -> Unit,
    authState: AuthState,
    onClearError: () -> Unit
) {
    // Validation states
    val isUsernameValid = username.isBlank() || isValidUsername(username)
    val isEmailValid = email.isBlank() || isValidEmail(email)
    val isPasswordValid = password.isBlank() || isValidPassword(password)
    val isPasswordMatch = confirmPassword.isBlank() || password == confirmPassword
    
    val isFormValid = username.isNotBlank() && 
                     displayName.isNotBlank() && 
                     email.isNotBlank() && 
                     password.isNotBlank() && 
                     confirmPassword.isNotBlank() &&
                     isUsernameValid &&
                     isEmailValid &&
                     isPasswordValid &&
                     isPasswordMatch
    
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        ModernTextField(
            value = username,
            onValueChange = { 
                onUsernameChange(it)
                if (authState is AuthState.Error) onClearError()
            },
            label = "Username",
            isError = !isUsernameValid,
            supportingText = if (!isUsernameValid && username.isNotBlank()) {
                "Username must be at least 3 characters and contain only letters, numbers, and underscores"
            } else null
        )
        
        // Display Name with Tooltip
        ModernTextFieldWithTooltip(
            value = displayName,
            onValueChange = { 
                onDisplayNameChange(it)
                if (authState is AuthState.Error) onClearError()
            },
            label = "Display Name",
            helpText = "This is the name other users will see. It can be different from your username and contain spaces (e.g., 'John Doe')."
        )
        
        ModernTextField(
            value = email,
            onValueChange = { 
                onEmailChange(it)
                if (authState is AuthState.Error) onClearError()
            },
            label = "Email",
            keyboardType = KeyboardType.Email,
            isError = !isEmailValid,
            supportingText = if (!isEmailValid && email.isNotBlank()) {
                "Please enter a valid email address"
            } else null
        )
        
        ModernTextField(
            value = password,
            onValueChange = { 
                onPasswordChange(it)
                if (authState is AuthState.Error) onClearError()
            },
            label = "Password",
            isPassword = true,
            passwordVisible = passwordVisible,
            onPasswordVisibilityChange = onPasswordVisibilityChange,
            isError = !isPasswordValid,
            supportingText = if (!isPasswordValid && password.isNotBlank()) {
                "Password must be at least 6 characters long"
            } else null
        )
        
        ModernTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = "Confirm Password",
            isPassword = true,
            passwordVisible = confirmPasswordVisible,
            onPasswordVisibilityChange = onConfirmPasswordVisibilityChange,
            isError = !isPasswordMatch,
            supportingText = if (!isPasswordMatch && confirmPassword.isNotBlank()) {
                "Passwords don't match"
            } else null
        )
        
        if (authState is AuthState.Error) {
            ErrorMessage(authState.message)
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        ModernButton(
            text = "Create Account",
            onClick = { 
                onRegister(
                    User(
                        username = username,
                        displayName = displayName,
                        password = password
                    )
                )
            },
            enabled = isFormValid && authState !is AuthState.Loading,
            isLoading = authState is AuthState.Loading,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordVisibilityChange: (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    supportingText: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        visualTransformation = if (isPassword && !passwordVisible) 
            PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isPassword) KeyboardType.Password else keyboardType
        ),
        trailingIcon = if (isPassword && onPasswordVisibilityChange != null) {
            {
                IconButton(onClick = onPasswordVisibilityChange) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = CardSubtitleText
                    )
                }
            }
        } else null,
        isError = isError,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            cursorColor = MaterialTheme.colorScheme.primary
        ),
        shape = RoundedCornerShape(16.dp),
        supportingText = supportingText?.let {
            { 
                Text(
                    text = it,
                    color = if (isError) MaterialTheme.colorScheme.error else CardBodyText,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    )
}

@Composable
private fun ModernTextFieldWithTooltip(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    helpText: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false
) {
    var showTooltip by remember { mutableStateOf(false) }
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(label) },
                modifier = modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                isError = isError,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(16.dp)
            )
            
            IconButton(
                onClick = { showTooltip = !showTooltip },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Help",
                    tint = CardSubtitleText,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        AnimatedVisibility(
            visible = showTooltip,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = helpText,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

@Composable
private fun ModernButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

@Composable
private fun ErrorMessage(message: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(12.dp)
        )
    }
} 