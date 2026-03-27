# ─── Kotlin ───────────────────────────────────────────────────────────────────
-keepattributes *Annotation*, InnerClasses, Signature, EnclosingMethod
-dontnote kotlin.**
-dontwarn kotlin.**

# ─── Kotlin Serialization ─────────────────────────────────────────────────────
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
# Keep generated serializers for our model/DTO classes
-keep,includedescriptorclasses class com.example.searchprice.**$$serializer { *; }
-keepclassmembers class com.example.searchprice.** {
    *** Companion;
}
-keepclasseswithmembers class com.example.searchprice.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ─── Ktor / OkHttp / Okio ─────────────────────────────────────────────────────
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keep class okio.** { *; }

# ─── Kotlinx Coroutines ───────────────────────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

# ─── Koin ─────────────────────────────────────────────────────────────────────
-keep class org.koin.** { *; }
-keepnames class * implements org.koin.core.module.Module

# ─── Compose ──────────────────────────────────────────────────────────────────
# Compose Compiler generates stable classes; keep them for correct recomposition
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ─── Domain model — never obfuscate data classes used across layers ───────────
-keep class com.example.searchprice.domain.model.** { *; }
-keep class com.example.searchprice.data.model.** { *; }
