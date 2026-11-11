package com.sumanonline.hisabkhata.feature.about

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sumanonline.hisabkhata.R
import com.sumanonline.hisabkhata.core.common.CertificateUtils
import com.sumanonline.hisabkhata.core.common.Constants
import com.sumanonline.hisabkhata.core.designsystem.BrandBlue
import com.sumanonline.hisabkhata.core.designsystem.NavyBlueDark
import com.sumanonline.hisabkhata.core.designsystem.NavyBluePrimary
import com.sumanonline.hisabkhata.core.designsystem.PageBackground
import com.sumanonline.hisabkhata.core.designsystem.SurfaceWhite
import com.sumanonline.hisabkhata.core.designsystem.TextPrimary
import com.sumanonline.hisabkhata.core.designsystem.TextSecondary
import com.sumanonline.hisabkhata.core.designsystem.TextTertiary

@Composable
fun AboutProjectScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Top Header Bar
            Surface(
                color = NavyBluePrimary,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = "About Project",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "HisabKhata PRO • SumanOnline",
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // App Branding Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.hisabkhata_logo),
                            contentDescription = "App Logo",
                            modifier = Modifier
                                .size(88.dp)
                                .clip(RoundedCornerShape(20.dp))
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "HisabKhata",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFDC2626))
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "PRO",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        Text(
                            text = "Smart Digital Ledger & Debt Recovery",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Version Pill
                        Surface(
                            color = Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Version 1.1.0 (Build 2) • Stable",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF334155)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Project Overview Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Project Overview",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "HisabKhata PRO is a enterprise-grade digital ledger designed for Indian MSMEs, retailers, and wholesalers. It completely replaces paper bahi-khata notebooks with instant debit/credit tracking, multi-party ledger statements, UPI payment settlements, and automated reminders.",
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                        Spacer(modifier = Modifier.height(14.dp))

                        InfoRow(label = "Maintained By", value = "SumanOnline.Com")
                        InfoRow(label = "Lead Engineer", value = "Suman Chakrabortty")
                        InfoRow(label = "Platform", value = "Android 8.0+ (API 26–35)")
                        InfoRow(label = "Architecture", value = "MVI / MVVM + Clean Outbox")
                        InfoRow(label = "Sync Engine", value = "Firebase RTDB + Cloudflare R2")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Certificate & OAuth Setup Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = BrandBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Firebase & Google Auth Setup",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "To enable Google Sign-In, add this package name and SHA-1 fingerprint under your Android App in Firebase Console:",
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val sha1 = CertificateUtils.getSigningSha1(context)
                        val pkg = context.packageName

                        InfoRow(label = "Package Name", value = pkg)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Active Signing SHA-1:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = sha1,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF0F172A),
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "COPY",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandBlue,
                                    modifier = Modifier
                                        .clickable {
                                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                            val clip = android.content.ClipData.newPlainText("SHA-1", sha1)
                                            clipboard.setPrimaryClip(clip)
                                            android.widget.Toast.makeText(context, "SHA-1 copied!", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(4.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Changelog Card (What's New in v1.1.0)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NewReleases,
                                contentDescription = null,
                                tint = BrandBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "What's New in v1.1.0",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        ChangelogItem(
                            title = "Google Sign-In Integration",
                            desc = "Full native Google OAuth authentication on Login and Signup pages."
                        )
                        ChangelogItem(
                            title = "Self-Healing Cloud Sync",
                            desc = "Automatic Firebase token refresh and retry queue for offline entries."
                        )
                        ChangelogItem(
                            title = "Enhanced Customer Ledger",
                            desc = "Notes truncated to 4 lines with ellipsis, cleaner overdue indicators."
                        )
                        ChangelogItem(
                            title = "Interactive Sync Indicator",
                            desc = "Smooth rotating sync animation with instant status toast notifications."
                        )
                        ChangelogItem(
                            title = "Biometric Security",
                            desc = "Hardware-backed fingerprint and face unlock protection."
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions: GitHub & Web Portal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://github.com/SumanCH8514/HisabKhata-Pro-Android-app")
                            )
                            try { context.startActivity(intent) } catch (_: Exception) {}
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.2.dp, Color(0xFFCBD5E1)),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "GitHub Repo",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Button(
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(Constants.WEB_PORTAL_BASE_URL)
                            )
                            try { context.startActivity(intent) } catch (_: Exception) {}
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Web Portal",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Footer
                Text(
                    text = "© 2026 SumanOnline.Com. All rights reserved.",
                    fontSize = 11.5.sp,
                    color = TextTertiary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 12.5.sp,
            color = TextTertiary,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 12.5.sp,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ChangelogItem(title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(BrandBlue)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = desc,
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 17.sp
            )
        }
    }
}
