# CliminTV Home R8 / ProGuard Optimization Rules
# Ensures minimal APK footprint (<500 KB) and fast boot time on low-memory Android TV devices.

-keepattributes *Annotation*
-dontwarn java.lang.invoke.**
-dontwarn javax.annotation.**

# Keep models and adapters
-keep class io.climintv.home.model.** { *; }
-keep class io.climintv.home.service.** { *; }
-keep class io.climintv.home.tif.** { *; }
-keep class io.climintv.home.theme.** { *; }

# DynamicAnimation keeps
-keep class androidx.dynamicanimation.animation.** { *; }
-keepclassmembers class androidx.dynamicanimation.animation.DynamicAnimation {
    public *;
}
