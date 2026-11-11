package com.sumanonline.hisabkhata.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sumanonline.hisabkhata.core.common.ImageUtils
import com.sumanonline.hisabkhata.core.designsystem.BrandBlue
import com.sumanonline.hisabkhata.core.designsystem.BrandBlueLight

@Composable
fun CustomerAvatar(
    name: String,
    photoUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = 46.dp,
    shape: Shape = CircleShape,
    backgroundColor: Color = BrandBlueLight,
    textColor: Color = BrandBlue,
    fontSize: TextUnit = 18.sp,
    showActiveBadge: Boolean = false
) {
    val sanitizedSource = remember(photoUrl) { ImageUtils.sanitizeImageSource(photoUrl) }
    val initial = remember(name) { (name.trim().firstOrNull() ?: 'P').uppercaseChar() }
    var loadFailed by remember(sanitizedSource) { mutableStateOf(false) }

    Box(
        modifier = modifier.size(size)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            if (sanitizedSource != null && !loadFailed) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(sanitizedSource)
                        .crossfade(true)
                        .build(),
                    contentDescription = name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    onError = {
                        loadFailed = true
                    }
                )
            } else {
                Text(
                    text = initial.toString(),
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
        }

        if (showActiveBadge) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(11.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF16A34A))
                    .border(1.5.dp, Color.White, CircleShape)
            )
        }
    }
}
