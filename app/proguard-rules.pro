# ProGuard rules for Digital Signage CMS Android Application

# Keep WebView JavaScript interfaces (do not obfuscate)
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep Gson model classes for reflection-based JSON parsing
-keepclassmembers class * {
    <fields>;
}
-keepattributes Signature
-keepattributes *Annotation*

# Keep Retrofit interfaces
-keep interface retrofit2.** { *; }
-keep class retrofit2.** { *; }

# Optimization for performance
-optimizations !code/simplification/arithmetic

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

# Keep essential Android framework components
-keep class android.support.** { *; }
-keep class androidx.** { *; }

# Security: Prevent obfuscation of entry points
-keep class com.signage.cms.** { *; } 