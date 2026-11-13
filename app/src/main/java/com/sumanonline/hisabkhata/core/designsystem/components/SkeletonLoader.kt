package com.sumanonline.hisabkhata.core.designsystem.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sumanonline.hisabkhata.core.designsystem.BorderLight
import com.sumanonline.hisabkhata.core.designsystem.SurfaceWhite

/**
 * Creates an animated linear gradient shimmer brush for skeleton loading states.
 */
@Composable
fun shimmerBrush(
    targetValue: Float = 1300f,
    shimmerColors: List<Color> = listOf(
        Color(0xFFE2E8F0),
        Color(0xFFF1F5F9),
        Color(0xFFCBD5E1),
        Color(0xFFF1F5F9),
        Color(0xFFE2E8F0)
    )
): Brush {
    val transition = rememberInfiniteTransition(label = "skeletonShimmerTransition")
    val translateAnimation by transition.animateFloat(
        initialValue = 0f,
        targetValue = targetValue,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "skeletonShimmerTranslate"
    )
    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnimation - 600f, translateAnimation - 600f),
        end = Offset(translateAnimation, translateAnimation)
    )
}

/**
 * Applies a clipped background with the shimmer brush to any composable box.
 */
fun Modifier.shimmerBackground(
    brush: Brush,
    shape: Shape = RoundedCornerShape(8.dp)
): Modifier = this
    .clip(shape)
    .background(brush)

/**
 * A basic reusable box element representing a shimmering skeleton placeholder.
 */
@Composable
fun SkeletonBox(
    brush: Brush,
    width: Dp? = null,
    height: Dp,
    shape: Shape = RoundedCornerShape(6.dp),
    modifier: Modifier = Modifier
) {
    val baseModifier = if (width != null) modifier.width(width) else modifier.fillMaxWidth()
    Box(
        modifier = baseModifier
            .height(height)
            .shimmerBackground(brush, shape)
    )
}

/**
 * Shimmer placeholder row for a single customer in CustomerListScreen.
 */
@Composable
fun CustomerListItemSkeleton(brush: Brush) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceWhite)
            .padding(horizontal = 16.dp, vertical = 13.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular Avatar Shimmer
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(brush)
            )

            Spacer(modifier = Modifier.width(14.dp))

            // Center details column
            Column(modifier = Modifier.weight(1f)) {
                SkeletonBox(brush = brush, width = 140.dp, height = 15.dp, shape = RoundedCornerShape(4.dp))
                Spacer(modifier = Modifier.height(7.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(brush))
                    Spacer(modifier = Modifier.width(5.dp))
                    SkeletonBox(brush = brush, width = 80.dp, height = 11.dp, shape = RoundedCornerShape(3.dp))
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Right amount column
            Column(horizontalAlignment = Alignment.End) {
                SkeletonBox(brush = brush, width = 76.dp, height = 16.dp, shape = RoundedCornerShape(4.dp))
                Spacer(modifier = Modifier.height(6.dp))
                SkeletonBox(brush = brush, width = 64.dp, height = 13.dp, shape = RoundedCornerShape(4.dp))
            }
        }
    }
    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 0.5.dp)
}

/**
 * Shimmer placeholder for the summary balance cards on the home page.
 */
@Composable
fun SummaryBalanceCardsSkeleton(brush: Brush) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                SkeletonBox(brush = brush, width = 80.dp, height = 11.dp)
                Spacer(modifier = Modifier.height(8.dp))
                SkeletonBox(brush = brush, width = 110.dp, height = 22.dp)
                Spacer(modifier = Modifier.height(6.dp))
                SkeletonBox(brush = brush, width = 70.dp, height = 11.dp)
            }
        }

        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                SkeletonBox(brush = brush, width = 80.dp, height = 11.dp)
                Spacer(modifier = Modifier.height(8.dp))
                SkeletonBox(brush = brush, width = 110.dp, height = 22.dp)
                Spacer(modifier = Modifier.height(6.dp))
                SkeletonBox(brush = brush, width = 70.dp, height = 11.dp)
            }
        }
    }
}

/**
 * Full skeleton feed for the customer home list screen.
 */
@Composable
fun CustomerListSkeleton(
    brush: Brush,
    itemCount: Int = 6
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SummaryBalanceCardsSkeleton(brush)

        // Net Receivable Banner Shimmer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .height(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceWhite)
                .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SkeletonBox(brush = brush, width = 160.dp, height = 14.dp)
                Spacer(modifier = Modifier.weight(1f))
                SkeletonBox(brush = brush, width = 50.dp, height = 14.dp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Filter pills row shimmer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SkeletonBox(brush = brush, width = 72.dp, height = 30.dp, shape = CircleShape)
            SkeletonBox(brush = brush, width = 105.dp, height = 30.dp, shape = CircleShape)
            SkeletonBox(brush = brush, width = 105.dp, height = 30.dp, shape = CircleShape)
            SkeletonBox(brush = brush, width = 90.dp, height = 30.dp, shape = CircleShape)
        }

        Spacer(modifier = Modifier.height(6.dp))
        HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.5.dp)

        // Customer item rows shimmer
        repeat(itemCount) {
            CustomerListItemSkeleton(brush)
        }
    }
}

/**
 * Shimmer placeholder row for a ledger table entry.
 */
@Composable
fun LedgerRowSkeleton(brush: Brush) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceWhite)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1.4f)
                .padding(start = 14.dp, end = 10.dp)
        ) {
            SkeletonBox(brush = brush, width = 110.dp, height = 13.dp)
            Spacer(modifier = Modifier.height(6.dp))
            SkeletonBox(brush = brush, width = 72.dp, height = 16.dp, shape = RoundedCornerShape(4.dp))
        }

        Box(
            modifier = Modifier
                .weight(0.8f)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            SkeletonBox(brush = brush, width = 60.dp, height = 16.dp)
        }

        Box(
            modifier = Modifier
                .weight(0.8f)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            SkeletonBox(brush = brush, width = 60.dp, height = 16.dp)
        }
    }
    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 0.5.dp)
}

/**
 * Shimmer placeholder for CustomerLedgerScreen.
 */
@Composable
fun LedgerScreenSkeleton(brush: Brush, rowCount: Int = 6) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Balance Card Shimmer
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SkeletonBox(brush = brush, width = 100.dp, height = 12.dp)
                Spacer(modifier = Modifier.height(8.dp))
                SkeletonBox(brush = brush, width = 150.dp, height = 28.dp)
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SkeletonBox(brush = brush, width = 90.dp, height = 18.dp)
                    SkeletonBox(brush = brush, width = 90.dp, height = 18.dp)
                }
            }
        }

        // Action buttons shimmer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            repeat(5) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(brush))
                    Spacer(modifier = Modifier.height(4.dp))
                    SkeletonBox(brush = brush, width = 38.dp, height = 9.dp)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Table header shimmer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF1F5F9))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            SkeletonBox(brush = brush, width = 80.dp, height = 12.dp, modifier = Modifier.weight(1.4f))
            SkeletonBox(brush = brush, width = 50.dp, height = 12.dp, modifier = Modifier.weight(0.8f))
            SkeletonBox(brush = brush, width = 50.dp, height = 12.dp, modifier = Modifier.weight(0.8f))
        }

        // Ledger rows
        repeat(rowCount) {
            LedgerRowSkeleton(brush)
        }
    }
}

/**
 * Shimmer placeholder for ReportsScreen.
 */
@Composable
fun ReportsScreenSkeleton(brush: Brush) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(4) {
                SkeletonBox(brush = brush, width = 80.dp, height = 32.dp, shape = RoundedCornerShape(8.dp))
            }
        }

        // Summary Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SkeletonBox(brush = brush, width = 85.dp, height = 30.dp)
                    SkeletonBox(brush = brush, width = 85.dp, height = 30.dp)
                    SkeletonBox(brush = brush, width = 85.dp, height = 30.dp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SkeletonBox(brush = brush, height = 44.dp, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f))
                    SkeletonBox(brush = brush, height = 44.dp, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Transaction cards
        repeat(5) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        SkeletonBox(brush = brush, width = 120.dp, height = 14.dp)
                        Spacer(modifier = Modifier.height(5.dp))
                        SkeletonBox(brush = brush, width = 90.dp, height = 11.dp)
                    }
                    SkeletonBox(brush = brush, width = 65.dp, height = 16.dp)
                }
            }
        }
    }
}
