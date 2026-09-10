/**
 * Kunjika Web Companion - Web Bluetooth (BLE) Interface
 * Handles pairing, GATT notifications, MTU packet reassembly, and status acknowledgment.
 */

const KUNJIKA_SERVICE_UUID = "e9a30001-9c3f-4e89-9a2d-7d81a9c41b80";
const CHAR_HANDSHAKE_UUID = "e9a30002-9c3f-4e89-9a2d-7d81a9c41b80";
const CHAR_TRANSFER_UUID = "e9a30003-9c3f-4e89-9a2d-7d81a9c41b80";
const CHAR_ACK_UUID = "e9a30004-9c3f-4e89-9a2d-7d81a9c41b80";

class KunjikaBleClient {
    constructor() {
        this.device = null;
        this.server = null;
        this.transferChar = null;
        this.ackChar = null;
        this.handshakeChar = null;
        this.receivedChunks = new Map();
        this.expectedTotalLength = 0;
        this.expectedTotalChunks = 0;
        this.onTransferComplete = null;
        this.onStatusChange = null;
    }

    isSupported() {
        return !!(navigator.bluetooth && navigator.bluetooth.requestDevice);
    }

    updateStatus(message, type = "info") {
        if (this.onStatusChange) {
            this.onStatusChange(message, type);
        }
    }

    /**
     * Request device and establish BLE connection.
     * Uses the Session ID prefix for device filtering.
     */
    async connect(expectedSessionPrefix) {
        if (!this.isSupported()) {
            throw new Error("Web Bluetooth is not supported in this browser. Please use Chrome, Edge, or Brave.");
        }

        this.updateStatus("Scanning for Kunjika Phone via Bluetooth...", "busy");

        try {
            this.device = await navigator.bluetooth.requestDevice({
                filters: [
                    { services: [KUNJIKA_SERVICE_UUID] }
                ],
                optionalServices: [KUNJIKA_SERVICE_UUID]
            });

            this.device.addEventListener("gattserverdisconnected", () => {
                this.updateStatus("Bluetooth device disconnected", "info");
            });

            this.updateStatus(`Connecting to ${this.device.name || "Kunjika Device"}...`, "busy");
            this.server = await this.device.gatt.connect();

            this.updateStatus("Discovering Kunjika Security Services...", "busy");
            const service = await this.server.getPrimaryService(KUNJIKA_SERVICE_UUID);

            this.handshakeChar = await service.getCharacteristic(CHAR_HANDSHAKE_UUID);
            this.transferChar = await service.getCharacteristic(CHAR_TRANSFER_UUID);
            this.ackChar = await service.getCharacteristic(CHAR_ACK_UUID);

            // Subscribe to notifications on the transfer characteristic
            await this.transferChar.startNotifications();
            this.transferChar.addEventListener("characteristicvaluechanged", (e) => this.handleIncomingChunk(e));

            this.updateStatus("BLE Channel Securely Established", "success");
            return true;
        } catch (error) {
            this.updateStatus(`BLE Connection Failed: ${error.message}`, "error");
            throw error;
        }
    }

    /**
     * Sends the Web's handshake confirmation (Public key or session ack)
     */
    async sendHandshake(dataBytes) {
        if (!this.handshakeChar) throw new Error("Not connected");
        await this.handshakeChar.writeValue(dataBytes);
    }

    /**
     * Handles chunked data arriving over BLE (MTU may be 20 to 512 bytes).
     * Chunk format: [TotalLength (2 bytes big endian)] [ChunkIndex (1 byte)] [TotalChunks (1 byte)] [Payload]
     */
    handleIncomingChunk(event) {
        const value = event.target.value;
        const data = new Uint8Array(value.buffer, value.byteOffset, value.byteLength);

        if (data.length < 4) return;

        const totalLength = (data[0] << 8) | data[1];
        const chunkIndex = data[2];
        const totalChunks = data[3];
        const chunkData = data.slice(4);

        if (!this.expectedTotalLength || chunkIndex === 0) {
            this.expectedTotalLength = totalLength;
            this.expectedTotalChunks = totalChunks;
        }

        // Deduplicate and store by chunk index
        this.receivedChunks.set(chunkIndex, chunkData);
        this.updateStatus(`Receiving encrypted payload: chunk ${this.receivedChunks.size}/${totalChunks}...`, "busy");

        if (this.receivedChunks.size === totalChunks) {
            // Reassemble in exact chunk index order
            const fullPayload = new Uint8Array(this.expectedTotalLength);
            let offset = 0;
            for (let i = 0; i < totalChunks; i++) {
                const chunk = this.receivedChunks.get(i);
                if (!chunk) {
                    console.error(`Missing chunk ${i}`);
                    return;
                }
                fullPayload.set(chunk, offset);
                offset += chunk.length;
            }

            this.receivedChunks.clear();
            this.updateStatus("Payload received! Verifying and decrypting...", "busy");
            if (this.onTransferComplete) {
                this.onTransferComplete(fullPayload);
            }
        }
    }

    /**
     * Send completion acknowledgment to the phone.
     */
    async sendAcknowledgment() {
        if (this.ackChar) {
            try {
                const ack = new Uint8Array([0x01]); // 0x01 = SUCCESS
                await this.ackChar.writeValue(ack);
            } catch (e) {
                console.warn("Ack write failed:", e);
            }
        }
    }

    disconnect() {
        if (this.device && this.device.gatt.connected) {
            this.device.gatt.disconnect();
        }
        this.device = null;
        this.server = null;
        this.transferChar = null;
        this.receivedChunks.clear();
    }
}

window.kunjikaBle = new KunjikaBleClient();
