/**
 * Kunjika Web Companion - Main Application Controller
 * Handles Session LifeCycle, UI States, Clipboard Management, Theme Modes, and Event Flow.
 */

class WebCompanionApp {
    constructor() {
        this.sessionTimer = null;
        this.sessionTimeRemaining = 60;
        this.clipboardTimer = null;
        this.clipboardTimeRemaining = 30;
        this.currentPassword = null;
        this.currentPayload = null;
        this.lastQrPayloadStr = null;

        this.initElements();
        this.initTheme();
        this.initEvents();
        this.startNewSession();
    }

    initElements() {
        this.qrCanvas = document.getElementById("qr-canvas");
        this.statusBadge = document.getElementById("status-badge");
        this.timerCircle = document.getElementById("timer-circle");
        this.timerSeconds = document.getElementById("timer-seconds");
        this.bleButton = document.getElementById("ble-connect-btn");
        this.bleStatus = document.getElementById("ble-status");
        this.totpCard = document.getElementById("totp-card");
        this.totpCodeEl = document.getElementById("totp-code");
        this.credentialCard = document.getElementById("credential-card");
        this.qrSection = document.getElementById("qr-section");

        // Credential card elements
        this.itemTitle = document.getElementById("item-title");
        this.itemUsername = document.getElementById("item-username");
        this.itemPassword = document.getElementById("item-password");
        this.togglePasswordBtn = document.getElementById("toggle-password-btn");
        this.copyPasswordBtn = document.getElementById("copy-password-btn");
        this.itemUrl = document.getElementById("item-url");
        this.itemNotes = document.getElementById("item-notes");
        this.clipboardProgress = document.getElementById("clipboard-progress");
        this.clipboardNotice = document.getElementById("clipboard-notice");

        // Theme elements
        this.themeToggleBtn = document.getElementById("theme-toggle");
        this.sunIcon = document.getElementById("theme-icon-sun");
        this.moonIcon = document.getElementById("theme-icon-moon");
    }

    initTheme() {
        const savedTheme = localStorage.getItem("kunjika_theme");
        const prefersDark = window.matchMedia("(prefers-color-scheme: dark)").matches;
        const initialTheme = savedTheme || (prefersDark ? "dark" : "light");

        this.setTheme(initialTheme, false);

        if (this.themeToggleBtn) {
            this.themeToggleBtn.addEventListener("click", () => {
                const currentTheme = document.documentElement.getAttribute("data-theme") || "dark";
                const nextTheme = currentTheme === "dark" ? "light" : "dark";
                this.setTheme(nextTheme, true);
            });
        }

        // Listen for OS system theme changes if user has not explicitly chosen
        window.matchMedia("(prefers-color-scheme: dark)").addEventListener("change", (e) => {
            if (!localStorage.getItem("kunjika_theme")) {
                this.setTheme(e.matches ? "dark" : "light", false);
            }
        });
    }

    setTheme(theme, savePreference = false) {
        document.documentElement.setAttribute("data-theme", theme);
        if (savePreference) {
            localStorage.setItem("kunjika_theme", theme);
        }

        if (this.sunIcon && this.moonIcon) {
            if (theme === "light") {
                this.sunIcon.style.display = "none";
                this.moonIcon.style.display = "block";
            } else {
                this.sunIcon.style.display = "block";
                this.moonIcon.style.display = "none";
            }
        }

        // Re-render QR code in theme colors if active
        if (this.lastQrPayloadStr && this.qrSection && this.qrSection.style.display !== "none") {
            this.drawQrCode(this.lastQrPayloadStr);
        }
    }

    initEvents() {
        this.bleButton.addEventListener("click", () => this.handleBleConnect());

        this.togglePasswordBtn.addEventListener("click", () => {
            const isMasked = this.itemPassword.dataset.masked === "true";
            if (isMasked) {
                this.itemPassword.textContent = this.currentPassword;
                this.itemPassword.dataset.masked = "false";
                this.togglePasswordBtn.textContent = "Hide";
            } else {
                this.itemPassword.textContent = "•".repeat(Math.max(10, this.currentPassword.length));
                this.itemPassword.dataset.masked = "true";
                this.togglePasswordBtn.textContent = "Show";
            }
        });

        this.copyPasswordBtn.addEventListener("click", async () => {
            if (this.currentPassword) {
                await navigator.clipboard.writeText(this.currentPassword);
                this.showToast("Password re-copied to clipboard!");
            }
        });

        document.getElementById("btn-reset-session").addEventListener("click", () => {
            this.resetToScanner();
        });

        // Setup BLE status listener
        window.kunjikaBle.onStatusChange = (msg, type) => {
            this.bleStatus.textContent = msg;
            this.bleStatus.className = `ble-status ${type}`;
            if (type === "success" || type === "busy") {
                // Pause session renewal countdown so ephemeral keys are locked during transfer
                clearInterval(this.sessionTimer);
            }
        };

        // Setup BLE incoming payload listener
        window.kunjikaBle.onTransferComplete = (fullPayload) => {
            this.processReceivedPayload(fullPayload);
        };
    }

    drawQrCode(payloadStr) {
        if (!this.qrCanvas || !payloadStr) return;

        try {
            const qr = qrcode(0, 'M');
            qr.addData(payloadStr);
            qr.make();

            const moduleCount = qr.getModuleCount();
            const size = 240;
            const margin = 10;
            const cellSize = (size - margin * 2) / moduleCount;
            const ctx = this.qrCanvas.getContext('2d');
            this.qrCanvas.width = size;
            this.qrCanvas.height = size;

            const isDark = document.documentElement.getAttribute("data-theme") !== "light";
            // In Dark Mode: Deep Obsidian Surface + Sovereign Amber Gold modules
            // In Light Mode: Clean Platinum Surface + Deep Obsidian High-Contrast modules
            const bgColor = isDark ? "#0F141C" : "#FFFFFF";
            const dotColor = isDark ? "#FBBF24" : "#0F172A";

            ctx.fillStyle = bgColor;
            ctx.fillRect(0, 0, size, size);

            ctx.fillStyle = dotColor;
            for (let r = 0; r < moduleCount; r++) {
                for (let c = 0; c < moduleCount; c++) {
                    if (qr.isDark(r, c)) {
                        ctx.fillRect(
                            Math.round(margin + c * cellSize),
                            Math.round(margin + r * cellSize),
                            Math.ceil(cellSize),
                            Math.ceil(cellSize)
                        );
                    }
                }
            }
        } catch (e) {
            console.error("QR Code drawing error:", e);
        }
    }

    async startNewSession() {
        clearInterval(this.sessionTimer);
        this.sessionTimeRemaining = 60;
        this.updateTimerDisplay();

        this.statusBadge.textContent = "Generating PoW & Keys...";
        this.statusBadge.className = "status-badge busy";

        try {
            // 1. Generate Ephemeral ECDH Key
            const session = await window.webDropCrypto.generateEphemeralKeyPair();

            // 2. Solve Micro Proof of Work locally (Zero Gas)
            const pow = await window.webDropCrypto.solveProofOfWork(4);

            // 3. Construct QR payload
            const payloadObj = {
                v: 1,
                sid: session.sessionId,
                pk: session.publicKeyHex,
                nonce: pow.nonce,
                t: session.timestamp
            };
            const payloadStr = JSON.stringify(payloadObj);
            this.lastQrPayloadStr = payloadStr;

            // 4. Render to Canvas with theme awareness
            this.drawQrCode(payloadStr);

            this.statusBadge.textContent = "Ready to Scan (Air-Gapped)";
            this.statusBadge.className = "status-badge ready";

            // Start 60s countdown
            this.sessionTimer = setInterval(() => {
                this.sessionTimeRemaining--;
                this.updateTimerDisplay();
                if (this.sessionTimeRemaining <= 0) {
                    this.startNewSession(); // Auto-renew expired session
                }
            }, 1000);

        } catch (error) {
            console.error("Session generation failed:", error);
            this.statusBadge.textContent = "Init Error";
            this.statusBadge.className = "status-badge error";
        }
    }

    updateTimerDisplay() {
        this.timerSeconds.textContent = `${this.sessionTimeRemaining}s`;
        const total = 60;
        const progress = (this.sessionTimeRemaining / total);
        const strokeDash = 283; // 2 * PI * 45
        this.timerCircle.style.strokeDashoffset = strokeDash - (strokeDash * progress);
    }

    async handleBleConnect() {
        try {
            await window.kunjikaBle.connect(window.webDropCrypto.sessionId);
        } catch (err) {
            console.warn("User cancelled or BLE error:", err);
        }
    }

    async processReceivedPayload(fullPayload) {
        try {
            if (fullPayload.length < 65 + 12 + 16) {
                throw new Error("Payload too short or invalid format");
            }

            // 1. Extract Phone's uncompressed P-256 public key (first 65 bytes)
            const phonePubKeyBytes = fullPayload.slice(0, 65);
            const phonePubKeyHex = window.webDropCrypto.buf2hex(phonePubKeyBytes);

            // 2. Extract Encrypted Payload (remaining bytes: IV 12B + CipherText)
            const encryptedBytes = fullPayload.slice(65);

            // 3. Derive Shared Secret via ECDH
            await window.webDropCrypto.deriveSharedSecret(phonePubKeyHex);

            // 4. Compute TOTP SAS verification code
            try {
                const totpCode = await window.webDropCrypto.computeTotpVerificationCode();
                this.totpCodeEl.textContent = `${totpCode.slice(0, 3)} ${totpCode.slice(3)}`;
                this.totpCard.style.display = "block";
            } catch (e) {
                console.warn("TOTP SAS computation note:", e);
            }

            // 5. Decrypt using derived shared key
            const credential = await window.webDropCrypto.decryptPayload(encryptedBytes);
            this.currentPassword = credential.password;
            this.currentPayload = credential;

            // Populate UI
            this.itemTitle.textContent = credential.title || "Untitled Credential";
            this.itemUsername.textContent = credential.username || "(No username)";
            this.itemPassword.textContent = "•".repeat(Math.max(10, credential.password.length));
            this.itemPassword.dataset.masked = "true";
            this.togglePasswordBtn.textContent = "Show";

            if (credential.websiteUrl) {
                this.itemUrl.textContent = credential.websiteUrl;
                this.itemUrl.parentElement.style.display = "flex";
            } else {
                this.itemUrl.parentElement.style.display = "none";
            }

            if (credential.notes) {
                this.itemNotes.textContent = credential.notes;
                this.itemNotes.parentElement.style.display = "flex";
            } else {
                this.itemNotes.parentElement.style.display = "none";
            }

            // Switch view
            this.qrSection.style.display = "none";
            this.credentialCard.style.display = "block";
            clearInterval(this.sessionTimer);

            // Auto copy password to clipboard!
            await navigator.clipboard.writeText(credential.password);
            this.showToast("Password copied to clipboard!");

            // Send BLE Ack
            await window.kunjikaBle.sendAcknowledgment();
            window.kunjikaBle.disconnect();

            // Start 30s auto-clear clipboard countdown
            this.startClipboardWipeTimer();

        } catch (error) {
            console.error("Decryption failed:", error);
            this.statusBadge.textContent = "Decryption Failed (Integrity Mismatch)";
            this.statusBadge.className = "status-badge error";
        }
    }

    startClipboardWipeTimer() {
        clearInterval(this.clipboardTimer);
        this.clipboardTimeRemaining = 30;

        this.clipboardTimer = setInterval(async () => {
            this.clipboardTimeRemaining--;
            const progress = (this.clipboardTimeRemaining / 30) * 100;
            this.clipboardProgress.style.width = `${progress}%`;
            this.clipboardNotice.textContent = `Clipboard auto-clears in ${this.clipboardTimeRemaining}s`;

            if (this.clipboardTimeRemaining <= 0) {
                clearInterval(this.clipboardTimer);
                try {
                    await navigator.clipboard.writeText("");
                    this.showToast("Clipboard wiped for security.");
                } catch (e) {
                    console.warn("Could not wipe clipboard automatically:", e);
                }
                this.resetToScanner();
            }
        }, 1000);
    }

    resetToScanner() {
        clearInterval(this.clipboardTimer);
        this.currentPassword = null;
        this.currentPayload = null;
        window.webDropCrypto.wipe();
        this.credentialCard.style.display = "none";
        this.totpCard.style.display = "none";
        this.qrSection.style.display = "flex";
        this.startNewSession();
    }

    showToast(message) {
        const toast = document.getElementById("toast");
        toast.textContent = message;
        toast.className = "toast show";
        setTimeout(() => {
            toast.className = "toast";
        }, 3000);
    }
}

document.addEventListener("DOMContentLoaded", () => {
    window.app = new WebCompanionApp();
});
