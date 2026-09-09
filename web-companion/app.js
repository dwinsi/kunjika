/**
 * Kunjika Web Companion - Main Application Controller
 * Handles Session LifeCycle, UI States, Clipboard Management, and Event Flow.
 */

class WebCompanionApp {
    constructor() {
        this.sessionTimer = null;
        this.sessionTimeRemaining = 60;
        this.clipboardTimer = null;
        this.clipboardTimeRemaining = 30;
        this.currentPassword = null;
        this.currentPayload = null;

        this.initElements();
        this.initEvents();
        this.startNewSession();
    }

    initElements() {
        this.qrCanvas = document.getElementById("qr-canvas");
        this.statusBadge = document.getElementById("status-badge");
        this.statusText = document.getElementById("status-text");
        this.timerCircle = document.getElementById("timer-circle");
        this.timerSeconds = document.getElementById("timer-seconds");
        this.bleButton = document.getElementById("ble-connect-btn");
        this.bleStatus = document.getElementById("ble-status");
        this.totpCard = document.getElementById("totp-card");
        this.totpCodeEl = document.getElementById("totp-code");
        this.credentialCard = document.getElementById("credential-card");
        this.vaultSection = document.getElementById("vault-section");
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
        };

        // Setup BLE incoming payload listener
        window.kunjikaBle.onTransferComplete = (fullPayload) => {
            this.processReceivedPayload(fullPayload);
        };
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
            // Format: JSON string containing:
            // { v: 1, sid: "...", pk: "...", nonce: "...", t: 1234567890 }
            const payloadObj = {
                v: 1,
                sid: session.sessionId,
                pk: session.publicKeyHex,
                nonce: pow.nonce,
                t: session.timestamp
            };
            const payloadStr = JSON.stringify(payloadObj);

            // 4. Draw to Canvas using qrcode-generator (auto-selects version 1-40)
            const qr = qrcode(0, 'M');
            qr.addData(payloadStr);
            qr.make();

            const moduleCount = qr.getModuleCount();
            const size = 240;
            const margin = 8;
            const cellSize = (size - margin * 2) / moduleCount;
            const ctx = this.qrCanvas.getContext('2d');
            this.qrCanvas.width = size;
            this.qrCanvas.height = size;
            ctx.fillStyle = "#11161d";
            ctx.fillRect(0, 0, size, size);
            ctx.fillStyle = "#00e676";
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

    async processReceivedPayload(encryptedBytes) {
        try {
            // Decrypt using derived shared key
            const credential = await window.webDropCrypto.decryptPayload(encryptedBytes);
            this.currentPassword = credential.password;
            this.currentPayload = credential;

            // Compute TOTP SAS verification code
            try {
                const totpCode = await window.webDropCrypto.computeTotpVerificationCode();
                this.totpCodeEl.textContent = `${totpCode.slice(0, 3)} ${totpCode.slice(3)}`;
                this.totpCard.style.display = "block";
            } catch (e) {
                console.warn("TOTP SAS computation note:", e);
            }

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
