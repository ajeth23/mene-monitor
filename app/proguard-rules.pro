# Mene Monitor ProGuard / R8 Optimization Rules

# Preserve line numbers for stack trace de-obfuscation
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Kotlin Coroutines
-keepclassmembers class kotlinx.coroutines.android.HandlerDispatcher {
    public <init>(...);
}
-dontwarn kotlinx.coroutines.**

# Room Persistence Library
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.**

# Moshi JSON Codegen
-keepclassmembers class * {
    @com.squareup.moshi.Json *;
}
-keep class * implements com.squareup.moshi.JsonAdapter
-dontwarn com.squareup.moshi.**

# Retrofit & OkHttp
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-dontwarn retrofit2.**
-dontwarn okhttp3.**

# Jetpack Compose
-keepclassmembers class * extends androidx.compose.ui.node.LayoutNode {
    public *;
}
-dontwarn androidx.compose.**
