package com.sumanonline.hisabkhata.feature.auth

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.sumanonline.hisabkhata.R
import com.sumanonline.hisabkhata.core.common.CertificateUtils
import com.sumanonline.hisabkhata.core.common.Constants
import com.sumanonline.hisabkhata.core.common.Resource
import com.sumanonline.hisabkhata.core.designsystem.BrandBlue
import com.sumanonline.hisabkhata.core.designsystem.GiveRed
import com.sumanonline.hisabkhata.core.designsystem.GiveRedLight
import com.sumanonline.hisabkhata.core.designsystem.GiveRedStroke
import com.sumanonline.hisabkhata.core.designsystem.PageBackground
import com.sumanonline.hisabkhata.core.designsystem.SurfaceWhite
import com.sumanonline.hisabkhata.core.designsystem.TextPrimary
import com.sumanonline.hisabkhata.core.designsystem.TextSecondary
import com.sumanonline.hisabkhata.core.designsystem.TextTertiary

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    var showConfigHelpDialog by remember { mutableStateOf(false) }
    var configHelpTitle by remember { mutableStateOf("") }
    var configHelpMessage by remember { mutableStateOf("") }
    var configHelpSha1 by remember { mutableStateOf("") }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        if (data != null) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (!idToken.isNullOrBlank()) {
                    viewModel.signInWithGoogleIdToken(
                        googleIdToken = idToken,
                        fallbackDisplayName = account.displayName,
                        fallbackEmail = account.email,
                        fallbackPhotoUrl = account.photoUrl?.toString()
                    )
                } else {
                    viewModel.onGoogleSignInError("Google account selected but ID token was missing. Please try again.")
                }
            } catch (e: ApiException) {
                when (e.statusCode) {
                    GoogleSignInStatusCodes.SIGN_IN_CANCELLED, 16 -> {
                        // User cancelled / dismissed the account picker; do nothing
                    }
                    10 -> {
                        val sha1 = CertificateUtils.getSigningSha1(context)
                        val pkg = context.packageName
                        configHelpTitle = "Google Sign-In Setup Required (Code 10)"
                        configHelpSha1 = sha1
                        configHelpMessage = "Google Sign-In rejected the request with DEVELOPER_ERROR (Code 10).\n\n" +
                                "This occurs when the signing certificate SHA-1 fingerprint is not added to Firebase Console under your Android app ($pkg).\n\n" +
                                "• Package Name: $pkg\n" +
                                "• SHA-1 Fingerprint:\n$sha1\n\n" +
                                "How to resolve:\n" +
                                "1. Open Firebase Console → Project Settings → General\n" +
                                "2. Select your Android app ($pkg)\n" +
                                "3. Click 'Add fingerprint' and paste this SHA-1"
                        showConfigHelpDialog = true
                        viewModel.onGoogleSignInError("Google Sign-In Code 10: SHA-1 fingerprint must be added to Firebase Console.")
                    }
                    12500 -> {
                        val sha1 = CertificateUtils.getSigningSha1(context)
                        val pkg = context.packageName
                        configHelpTitle = "Google Sign-In Failed (Code 12500)"
                        configHelpSha1 = sha1
                        configHelpMessage = "Google Sign-In returned SIGN_IN_FAILED (Code 12500).\n\n" +
                                "Please ensure your Firebase Console has Google Sign-In provider enabled and this SHA-1 added:\n\n" +
                                "• Package Name: $pkg\n" +
                                "• SHA-1 Fingerprint:\n$sha1"
                        showConfigHelpDialog = true
                        viewModel.onGoogleSignInError("Google Sign-In Code 12500: Check Firebase Console Google Sign-In provider & SHA-1.")
                    }
                    7 -> {
                        viewModel.onGoogleSignInError("Network error during Google Sign-In. Please check your internet connection.")
                    }
                    else -> {
                        val msg = e.localizedMessage ?: e.message ?: "Status ${e.statusCode}"
                        viewModel.onGoogleSignInError("Google Sign-In error (Code ${e.statusCode}): $msg")
                    }
                }
            } catch (e: Exception) {
                viewModel.onGoogleSignInError(e.localizedMessage ?: "Failed to sign in with Google")
            }
        } else if (result.resultCode != Activity.RESULT_CANCELED) {
            viewModel.onGoogleSignInError("Google Sign-In returned with no data (Result: ${result.resultCode})")
        }
    }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is AuthEvent.LoginSuccess -> onLoginSuccess()
                is AuthEvent.Error -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
                else -> Unit
            }
        }
    }

    if (showConfigHelpDialog) {
        AlertDialog(
            onDismissRequest = { showConfigHelpDialog = false },
            title = {
                Text(
                    text = configHelpTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color(0xFF0F172A)
                )
            },
            text = {
                Text(
                    text = configHelpMessage,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = Color(0xFF334155)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("SHA-1 Fingerprint", configHelpSha1)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "SHA-1 fingerprint copied to clipboard!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text(
                        text = "Copy SHA-1",
                        fontWeight = FontWeight.Bold,
                        color = BrandBlue
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfigHelpDialog = false }) {
                    Text(text = "Close", color = TextSecondary)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = SurfaceWhite
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
            .statusBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_logo_round),
                contentDescription = "HisabKhata Logo",
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "HisabKhata PRO",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = BrandBlue
            )

            Text(
                text = "Smart Digital Ledger for Merchants",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceWhite)
                    .padding(24.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Merchant Login",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (uiState is Resource.Error) {
                        Surface(
                            color = GiveRedLight,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, GiveRedStroke),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = GiveRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = (uiState as Resource.Error).message,
                                    color = GiveRed,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            if (uiState is Resource.Error) viewModel.clearError()
                        },
                        label = { Text("Email Address") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = TextTertiary)
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandBlue,
                            unfocusedBorderColor = TextTertiary.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            if (uiState is Resource.Error) viewModel.clearError()
                        },
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = TextTertiary)
                        },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = TextTertiary
                                )
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { viewModel.login(email, password) }
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandBlue,
                            unfocusedBorderColor = TextTertiary.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Forgot Password?",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BrandBlue,
                            modifier = Modifier.clickable(onClick = onNavigateToForgotPassword)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { viewModel.login(email, password) },
                        enabled = uiState !is Resource.Loading,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        if (uiState is Resource.Loading) {
                            CircularProgressIndicator(color = SurfaceWhite, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = "Sign In",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = SurfaceWhite
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = Color(0xFFE2E8F0),
                            thickness = 1.dp
                        )
                        Text(
                            text = "OR",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8),
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(horizontal = 14.dp)
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = Color(0xFFE2E8F0),
                            thickness = 1.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedButton(
                        onClick = {
                            try {
                                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestIdToken(Constants.GOOGLE_WEB_CLIENT_ID)
                                    .requestEmail()
                                    .requestProfile()
                                    .build()
                                val googleSignInClient = GoogleSignIn.getClient(context, gso)
                                googleSignInClient.signOut().addOnCompleteListener {
                                    googleSignInLauncher.launch(googleSignInClient.signInIntent)
                                }
                            } catch (e: Exception) {
                                viewModel.onGoogleSignInError(e.localizedMessage ?: "Failed to launch Google Sign-In")
                            }
                        },
                        enabled = uiState !is Resource.Loading,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.2.dp, Color(0xFFE2E8F0)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = SurfaceWhite,
                            contentColor = Color(0xFF1E293B)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_google_logo),
                                contentDescription = "Google Logo",
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Continue with Google",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1E293B)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "New to HisabKhata? ",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
                Text(
                    text = "Create Account",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandBlue,
                    modifier = Modifier.clickable(onClick = onNavigateToRegister)
                )
            }
        }
    }
}
