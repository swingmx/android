# Retrofit
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class retrofit2.adapter.kotlin.coroutines.** { *; }
-keep class com.squareup.okhttp.** { *; }

# Gson
-keep class com.google.gson.** { *; }
-keep class com.google.gson.annotations.** { *; }
-dontwarn com.google.gson.**

# Keep core classes for Retrofit
-keep class com.android.swingmusic.auth.** { *; }
-keep class com.android.swingmusic.core.** { *; }
-keep class com.android.swingmusic.network.** { *; }
-keep class com.android.swingmusic.database.** { *; }

# Timber - strip logging calls from release builds
-assumenosideeffects class timber.log.Timber* {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
    public static *** wtf(...);
    public static *** tag(...);
}
