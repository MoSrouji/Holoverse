# Moshi rules
-keep class com.squareup.moshi.** { *; }
-keep interface com.squareup.moshi.** { *; }
-keep class * extends com.squareup.moshi.JsonAdapter { *; }
-keep class * implements com.squareup.moshi.JsonAdapter { *; }
-keepattributes Signature, *Annotation*, EnclosingMethod
-keep class com.example.holoverse.threedmodel.data.remote.dto.** { *; }

# Kotlin Serialization rules
-keepattributes *Annotation*, InnerClasses
-keepclassmembers class com.example.holoverse.** {
    *** Companion;
}
-keepclasseswithmembers class com.example.holoverse.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep class com.example.holoverse.auth.domain.entities.** { *; }
-keep class com.example.holoverse.chatsystem.data.remote.** { *; }

# Retrofit rules
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepclassmembers,allowshrinking,allowoptimization interface * {
    @retrofit2.http.* <methods>;
}
-keep class retrofit2.** { *; }

# OkHttp3 rules
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Cloudinary rules
-keep class com.cloudinary.** { *; }

# SceneView / Filament rules
-keep class io.github.sceneview.** { *; }
-keep class com.google.android.filament.** { *; }
