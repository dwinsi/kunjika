/**
 * Kunjika Web Companion - Cryptographic Core
 * Ephemeral ECDH (P-256), HKDF-SHA-256, pairing code, and AES-256-GCM.
 */

class WebDropCrypto {
    constructor() {
        this.keyPair = null;
        this.rawPublicKeyHex = null;
        this.sessionId = null;
        this.serviceUuid = null;
        this.timestamp = null;
        this.sessionKey = null;
    }

    /**
     * Generate an ephemeral ECDH keypair on Curve P-256 (secp256r1).
     */
    async generateEphemeralKeyPair() {
        this.keyPair = await window.crypto.subtle.generateKey(
            {
                name: "ECDH",
                namedCurve: "P-256"
            },
            false, // private key is non-extractable from RAM
            ["deriveKey", "deriveBits"]
        );

        // Export public key in uncompressed format (65 bytes: 0x04 + X + Y)
        const exported = await window.crypto.subtle.exportKey("raw", this.keyPair.publicKey);
        this.rawPublicKeyHex = this.buf2hex(exported);
        this.sessionId = this.generateRandomHex(16);
        this.timestamp = Math.floor(Date.now() / 1000);
        const uuidBytes = new Uint8Array(16);
        window.crypto.getRandomValues(uuidBytes);
        uuidBytes[6] = (uuidBytes[6] & 0x0f) | 0x40;
        uuidBytes[8] = (uuidBytes[8] & 0x3f) | 0x80;
        this.serviceUuid = [...uuidBytes].map((b, i) => `${[4, 6, 8, 10].includes(i) ? "-" : ""}${b.toString(16).padStart(2, "0")}`).join("").replace(/^-/, "");
        return {
            sessionId: this.sessionId,
            publicKeyHex: this.rawPublicKeyHex,
            timestamp: this.timestamp,
            serviceUuid: this.serviceUuid
        };
    }

    /**
     * Derives a shared secret from the Phone's uncompressed P-256 Public Key (hex).
     */
    async deriveSharedSecret(phonePublicKeyHex) {
        const keyBytes = this.hex2buf(phonePublicKeyHex);
        const importedPhoneKey = await window.crypto.subtle.importKey(
            "raw",
            keyBytes,
            {
                name: "ECDH",
                namedCurve: "P-256"
            },
            false,
            []
        );

        // Derive AES-GCM / HMAC key
        const derivedBits = await window.crypto.subtle.deriveBits(
            {
                name: "ECDH",
                public: importedPhoneKey
            },
            this.keyPair.privateKey,
            256
        );

        const transcript = new TextEncoder().encode(`kunjika-web-drop-v2|${this.sessionId}|${this.rawPublicKeyHex}|${phonePublicKeyHex}`);
        const salt = await window.crypto.subtle.digest("SHA-256", transcript);
        const ikm = await window.crypto.subtle.importKey("raw", derivedBits, "HKDF", false, ["deriveKey"]);
        this.sessionKey = await window.crypto.subtle.deriveKey(
            { name: "HKDF", hash: "SHA-256", salt, info: new TextEncoder().encode("kunjika-web-drop-v2/aes-gcm") },
            ikm, { name: "AES-GCM", length: 256 }, false, ["decrypt"]
        );
        this.pairingCodeKey = await window.crypto.subtle.deriveKey(
            { name: "HKDF", hash: "SHA-256", salt, info: new TextEncoder().encode("kunjika-web-drop-v2/pairing-code") },
            ikm, { name: "HMAC", hash: "SHA-256", length: 256 }, false, ["sign"]
        );
        return derivedBits;
    }

    /**
     * Derives a six-digit short authentication string for transcript comparison.
     */
    async computePairingCode() {
        if (!this.pairingCodeKey) throw new Error("Session key not derived");
        const signature = await window.crypto.subtle.sign("HMAC", this.pairingCodeKey, new TextEncoder().encode("kunjika-web-drop-v2/pairing-code"));
        const sigBytes = new Uint8Array(signature);

        // Dynamic truncation (RFC 6238 / RFC 4226)
        const offset = sigBytes[sigBytes.length - 1] & 0x0f;
        const binary =
            ((sigBytes[offset] & 0x7f) << 24) |
            ((sigBytes[offset + 1] & 0xff) << 16) |
            ((sigBytes[offset + 2] & 0xff) << 8) |
            (sigBytes[offset + 3] & 0xff);

        const otp = binary % 1000000;
        return otp.toString().padStart(6, "0");
    }

    /**
     * Decrypts an AES-256-GCM encrypted payload.
     * Expected format: IV (12 bytes) + CipherText with 16-byte auth tag.
     */
    async decryptPayload(encryptedBytes) {
        if (!this.sessionKey) throw new Error("No session key available");

        const iv = encryptedBytes.slice(0, 12);
        const cipherText = encryptedBytes.slice(12);

        const decryptedBuffer = await window.crypto.subtle.decrypt(
            {
                name: "AES-GCM",
                iv: iv,
                tagLength: 128
            },
            this.sessionKey,
            cipherText
        );

        const decoder = new TextDecoder();
        const jsonString = decoder.decode(decryptedBuffer);
        return JSON.parse(jsonString);
    }

    /**
     * Drop references to session material. JavaScript/browser memory is not guaranteed to be zeroed.
     */
    wipe() {
        this.keyPair = null;
        this.sessionKey = null;
        this.pairingCodeKey = null;
        this.rawPublicKeyHex = null;
        this.sessionId = null;
        this.serviceUuid = null;
        this.timestamp = null;
    }

    // --- Helper Utilities ---

    buf2hex(buffer) {
        return [...new Uint8Array(buffer)]
            .map(x => x.toString(16).padStart(2, "0"))
            .join("");
    }

    hex2buf(hexString) {
        const bytes = new Uint8Array(hexString.length / 2);
        for (let i = 0; i < hexString.length; i += 2) {
            bytes[i / 2] = parseInt(hexString.substr(i, 2), 16);
        }
        return bytes.buffer;
    }

    generateRandomHex(bytesLength) {
        const bytes = new Uint8Array(bytesLength);
        window.crypto.getRandomValues(bytes);
        return this.buf2hex(bytes.buffer);
    }
}

// Export singleton
window.webDropCrypto = new WebDropCrypto();
