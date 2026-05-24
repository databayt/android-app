# Hogwarts Android - ProGuard/R8 Rules
# PROD-001: Production-ready minification rules

#-------------------------------------------------
# General Android
#-------------------------------------------------
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses

# Keep application class
-keep class org.hogwarts.android.HogwartsApplication { *; }

#-------------------------------------------------
# Kotlin
#-------------------------------------------------
-dontwarn kotlin.**
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

#-------------------------------------------------
# kotlinx.serialization
#-------------------------------------------------
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Keep serializers
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep companion object serializer() of serializable classes
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}

-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static ** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep all DTOs
-keep class org.hogwarts.android.**.dto.** { *; }
-keep class org.hogwarts.android.**.model.** { *; }

#-------------------------------------------------
# Retrofit
#-------------------------------------------------
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

# Keep Retrofit API interfaces
-keep,allowobfuscation interface org.hogwarts.android.**.remote.*Api
-keep,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**

#-------------------------------------------------
# OkHttp
#-------------------------------------------------
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# OkHttp platform used only on JVM and when Conscrypt and other security
# providers are available.
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

#-------------------------------------------------
# Room
#-------------------------------------------------
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

#-------------------------------------------------
# Hilt / Dagger
#-------------------------------------------------
-dontwarn dagger.**
-keep class dagger.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

#-------------------------------------------------
# Jetpack Compose
#-------------------------------------------------
-dontwarn androidx.compose.**

#-------------------------------------------------
# Firebase
#-------------------------------------------------
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Crashlytics
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

#-------------------------------------------------
# CameraX + ML Kit
#-------------------------------------------------
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**
-keep class com.google.mlkit.** { *; }

#-------------------------------------------------
# Coil
#-------------------------------------------------
-dontwarn coil.**

#-------------------------------------------------
# Timber
#-------------------------------------------------
-assumenosideeffects class timber.log.Timber {
    public static void v(...);
    public static void d(...);
    public static void i(...);
}

#-------------------------------------------------
# WorkManager
#-------------------------------------------------
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

#-------------------------------------------------
# Biometric
#-------------------------------------------------
-keep class androidx.biometric.** { *; }

#-------------------------------------------------
# Enum classes
#-------------------------------------------------
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

#-------------------------------------------------
# Parcelable
#-------------------------------------------------
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

#-------------------------------------------------
# R8 full mode adjustments
#-------------------------------------------------
-allowaccessmodification
-repackageclasses ''
