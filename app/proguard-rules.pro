# SQLCipher (JNI bindings need stable names)
-keep class net.zetetic.database.sqlcipher.** { *; }
-dontwarn net.zetetic.database.sqlcipher.**

# Room: generated code + library consumer rules handle the rest
-dontwarn androidx.room.paging.**

# Gson: keep only the fields/constructors of models that are (de)serialized.
# Classes themselves can still be shrunk/optimized; field names are kept for JSON mapping.
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-keepclassmembers class com.kunjika.app.core.webdrop.** {
    <init>(...);
    <fields>;
}
-keepclassmembers class com.kunjika.app.data.** {
    <init>(...);
    <fields>;
}

# ML Kit / CameraX / Play Integrity / Biometric / Security Crypto ship their own
# consumer ProGuard rules, so broad -keep rules are no longer needed.
-dontwarn com.google.mlkit.**
-dontwarn com.google.android.gms.**
-dontwarn androidx.camera.**

# Ensure no logging reaches production via R8 stripping
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}
