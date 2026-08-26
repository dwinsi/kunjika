# Keep SQLCipher and AndroidX Security Crypto rules
-keep class net.zetetic.database.sqlcipher.** { *; }
-dontwarn net.zetetic.database.sqlcipher.**

# Room Database rules
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Biometric & Keystore
-keep class androidx.biometric.** { *; }
-keep class androidx.security.crypto.** { *; }

# Kunjika Security Hardening
# Obfuscate all core security managers but keep the names of classes used in XML or DI if necessary.
# Since we use manual DI and no reflection-based XML bindings for these, we can obfuscate them.
-keepclassmembers class com.keyfortress.app.core.security.** { *; }
-keepclassmembers class com.keyfortress.app.core.blockchain.** { *; }
-keepclassmembers class com.keyfortress.app.core.qr.** { *; }

# Ensure no logging reaches production via R8 stripping as well
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}
