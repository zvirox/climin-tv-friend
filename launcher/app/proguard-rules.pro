# Your TV Home R8 / ProGuard Optimization Rules
# Ensures minimal APK footprint (<500 KB) and fast boot time on low-memory Android TV devices.

-keepattributes *Annotation*
-dontwarn java.lang.invoke.**
-dontwarn javax.annotation.**

# Keep models and adapters
-keep class io.yourtv.home.model.** { *; }
-keep class io.yourtv.home.service.** { *; }
-keep class io.yourtv.home.tif.** { *; }
-keep class io.yourtv.home.theme.** { *; }

# DynamicAnimation keeps
-keep class androidx.dynamicanimation.animation.** { *; }
-keepclassmembers class androidx.dynamicanimation.animation.DynamicAnimation {
    public *;
}
