# Keep SQLCipher and AndroidX Security Crypto rules
-keep class net.zetetic.database.sqlcipher.** { *; }
-dontwarn net.zetetic.database.sqlcipher.**

# Room Database rules
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Biometric & Keystore
-keep class androidx.biometric.** { *; }
-keep class androidx.security.crypto.** { *; }

# Gson & Data Models (Keep serialized fields from being renamed by R8 in release build)
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-keepclassmembers class * implements java.io.Serializable { *; }
-keep class com.google.gson.** { *; }
-keep class com.kunjika.app.core.webdrop.** { *; }
-keep class com.kunjika.app.data.** { *; }

# ML Kit Barcode Scanning Component Registrars & Metadata Rules (Crucial for Release .aab build)
-keep class com.google.mlkit.vision.barcode.** { *; }
-keep class com.google.mlkit.common.** { *; }
-keep class com.google.mlkit.common.internal.** { *; }
-keep class com.google.mlkit.common.sdks.** { *; }
-keep class * implements com.google.mlkit.common.sdks.** { *; }
-keep class * extends com.google.mlkit.common.sdks.** { *; }
-keep class * implements com.google.firebase.components.ComponentRegistrar { *; }
-keep class * implements com.google.mlkit.common.internal.MlKitComponentRegistrar { *; }
-keepclassmembers class * implements com.google.mlkit.common.internal.MlKitComponentRegistrar {
    public <init>();
}
-dontwarn com.google.mlkit.**
-dontwarn com.google.android.gms.**

# CameraX Rules
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**

# Play Integrity
-keep class com.google.android.play.core.integrity.** { *; }

# Kunjika Security Hardening
-keepclassmembers class com.kunjika.app.core.security.** { *; }
-keepclassmembers class com.kunjika.app.core.blockchain.** { *; }
-keepclassmembers class com.kunjika.app.core.qr.** { *; }

# Ensure no logging reaches production via R8 stripping
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}
