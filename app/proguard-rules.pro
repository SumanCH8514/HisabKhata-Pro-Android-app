-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.room.* <fields>;
    @androidx.room.* <methods>;
}
-keep class * extends androidx.room.RoomDatabase
-dontwarn okio.**
-dontwarn javax.annotation.**
-keepclassmembers enum * { *; }
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclasseswithmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}
-keepclasseswithmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}
