# Keep SQLCipher and AndroidX Security Crypto rules
-keep class net.zetetic.database.sqlcipher.** { *; }
-dontwarn net.zetetic.database.sqlcipher.**

# Room Database rules
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Biometric & Keystore
-keep class androidx.biometric.** { *; }
-keep class androidx.security.crypto.** { *; }
