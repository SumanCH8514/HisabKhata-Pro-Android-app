package com.sumanonline.hisabkhata.feature.auth

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sumanonline.hisabkhata.core.common.Resource
import com.sumanonline.hisabkhata.core.designsystem.BrandBlue
import com.sumanonline.hisabkhata.core.designsystem.GiveRed
import com.sumanonline.hisabkhata.core.designsystem.GiveRedLight
import com.sumanonline.hisabkhata.core.designsystem.GiveRedStroke
import com.sumanonline.hisabkhata.core.designsystem.GotGreen
import com.sumanonline.hisabkhata.core.designsystem.GotGreenLight
import com.sumanonline.hisabkhata.core.designsystem.GotGreenStroke
import com.sumanonline.hisabkhata.core.designsystem.PageBackground
import com.sumanonline.hisabkhata.core.designsystem.SurfaceWhite
import com.sumanonline.hisabkhata.core.designsystem.TextPrimary
import com.sumanonline.hisabkhata.core.designsystem.TextSecondary
import com.sumanonline.hisabkhata.core.designsystem.TextTertiary

@Composable
fun ForgotPasswordScreen(
    viewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }
    var emailSent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is AuthEvent.PasswordResetSent -> {
                    emailSent = true
                    Toast.makeText(context, "Password reset link sent to ${event.email}", Toast.LENGTH_LONG).show()
                }
                is AuthEvent.Error -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
                else -> Unit
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = BrandBlue)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Reset Password",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary
            )

            Text(
                text = "Enter your registered email and we'll send you instructions to reset your password.",
                fontSize = 14.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceWhite)
                    .padding(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (emailSent) {
                        Surface(
                            color = GotGreenLight,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, GotGreenStroke),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = GotGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Password reset instructions sent! Check your inbox.",
                                    color = GotGreen,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else if (uiState is Resource.Error) {
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
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandBlue,
                            unfocusedBorderColor = TextTertiary.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { viewModel.sendPasswordReset(email) },
                        enabled = uiState !is Resource.Loading,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        if (uiState is Resource.Loading) {
                            CircularProgressIndicator(color = SurfaceWhite, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = "Send Reset Link",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = SurfaceWhite
                            )
                        }
                    }
                }
            }
        }
    }
}
