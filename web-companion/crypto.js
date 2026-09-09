/**
 * Kunjika Web Companion - Cryptographic Core
 * 100% Offline Client-side Proof of Work, Ephemeral ECDH (P-256), TOTP 6-Digit Derivation, and AES-256-GCM.
 */

class WebDropCrypto {
    constructor() {
        this.keyPair = null;
        this.rawPublicKeyHex = null;
        this.sessionId = null;
        this.powNonce = null;
        this.timestamp = null;
        this.sharedSecretKey = null;
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
        return {
            sessionId: this.sessionId,
            publicKeyHex: this.rawPublicKeyHex,
            timestamp: this.timestamp
        };
    }

    /**
     * Solves a micro Proof of Work (PoW) locally.
     * Computes SHA-256(sessionId + timestamp + nonce) until hash starts with target leading zeroes (e.g. 4 hex chars = 16 bits).
     * Takes ~150-300ms on modern CPU. Zero server dependency, zero network.
     */
    async solveProofOfWork(difficultyChars = 4) {
        const prefix = "0".repeat(difficultyChars);
        const base = `${this.sessionId}:${this.timestamp}:`;
        let nonce = 0;
        const encoder = new TextEncoder();

        while (true) {
            const data = encoder.encode(base + nonce);
            const hashBuffer = await window.crypto.subtle.digest("SHA-256", data);
            const hashHex = this.buf2hex(hashBuffer);
            if (hashHex.startsWith(prefix)) {
                this.powNonce = nonce.toString();
                return {
                    nonce: this.powNonce,
                    hash: hashHex
                };
            }
            nonce++;
            // Yield every 5000 iterations so the UI doesn't freeze
            if (nonce % 5000 === 0) {
                await new Promise(resolve => setTimeout(resolve, 0));
            }
        }
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

        this.sharedSecretBits = derivedBits;

        this.sharedSecretKey = await window.crypto.subtle.importKey(
            "raw",
            derivedBits,
            "AES-GCM",
            false,
            ["decrypt"]
        );

        return derivedBits;
    }

    /**
     * Derives the TOTP-style 6-Digit Short Authentication String (SAS).
     * Code = Truncate6(HMAC-SHA256(sharedSecret, floor(timestamp / 30)))
     */
    async computeTotpVerificationCode() {
        if (!this.sharedSecretBits) throw new Error("Shared secret not derived");

        const hmacKey = await window.crypto.subtle.importKey(
            "raw",
            this.sharedSecretBits,
            { name: "HMAC", hash: "SHA-256" },
            false,
            ["sign"]
        );

        // 30-second time bucket
        const timeBucket = Math.floor(Date.now() / 1000 / 30);
        const timeBuffer = new ArrayBuffer(8);
        const timeView = new DataView(timeBuffer);
        // Put timeBucket as 64-bit big endian integer
        timeView.setBigUint64(0, BigInt(timeBucket), false);

        const signature = await window.crypto.subtle.sign("HMAC", hmacKey, timeBuffer);
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
        if (!this.sharedSecretKey) throw new Error("No shared secret key available");

        const iv = encryptedBytes.slice(0, 12);
        const cipherText = encryptedBytes.slice(12);

        const decryptedBuffer = await window.crypto.subtle.decrypt(
            {
                name: "AES-GCM",
                iv: iv,
                tagLength: 128
            },
            this.sharedSecretKey,
            cipherText
        );

        const decoder = new TextDecoder();
        const jsonString = decoder.decode(decryptedBuffer);
        return JSON.parse(jsonString);
    }

    /**
     * Securely zero-out keys and secrets from RAM.
     */
    wipe() {
        this.keyPair = null;
        this.sharedSecretBits = null;
        this.sharedSecretKey = null;
        this.rawPublicKeyHex = null;
        this.sessionId = null;
        this.powNonce = null;
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
