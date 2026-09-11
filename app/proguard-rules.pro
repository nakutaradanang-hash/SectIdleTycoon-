# Add project specific ProGuard / R8 rules here.
# Optimization passes and aggressive member merging
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# Aggressive optimizations
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

# Keep game models and data classes serialized via Moshi or JSON
-keepclassmembers class com.sect.idle.models.** { *; }
-keep class com.sect.idle.models.** { *; }
-keep class com.sect.idle.gameplay.SectData { *; }

# Keep Room entities / DAOs
-keep class androidx.room.** { *; }

# Keep custom views and game core
-keep class com.sect.idle.ui.GameView { *; }
-keep class com.sect.idle.ui.GameActivity { *; }
-keep class com.sect.idle.MainActivity { *; }

# Strip verbose debug logging from production release builds for performance
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
}
