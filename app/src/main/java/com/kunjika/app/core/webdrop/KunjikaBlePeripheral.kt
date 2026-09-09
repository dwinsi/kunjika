package com.kunjika.app.core.webdrop

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import java.util.UUID

/**
 * 100% Air-Gapped Bluetooth Low Energy (BLE) Peripheral for Kunjika.
 * Transfers encrypted credential packets directly to laptop browser via GATT.
 */
class KunjikaBlePeripheral(
    private val context: Context,
    private val onConnected: () -> Unit,
    private val onTransferSuccess: () -> Unit,
    private val onError: (String) -> Unit
) {
    companion object {
        private const val TAG = "KunjikaBle"
        val SERVICE_UUID: UUID = UUID.fromString("e9a30001-9c3f-4e89-9a2d-7d81a9c41b80")
        val CHAR_HANDSHAKE_UUID: UUID = UUID.fromString("e9a30002-9c3f-4e89-9a2d-7d81a9c41b80")
        val CHAR_TRANSFER_UUID: UUID = UUID.fromString("e9a30003-9c3f-4e89-9a2d-7d81a9c41b80")
        val CHAR_ACK_UUID: UUID = UUID.fromString("e9a30004-9c3f-4e89-9a2d-7d81a9c41b80")
        val CLIENT_CONFIG_DESCRIPTOR_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter
    private var advertiser: BluetoothLeAdvertiser? = null
    private var gattServer: BluetoothGattServer? = null
    private var connectedDevice: BluetoothDevice? = null
    private var isAdvertising = false

    private var transferCharacteristic: BluetoothGattCharacteristic? = null

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            isAdvertising = true
            Log.d(TAG, "BLE Advertising started successfully")
        }

        override fun onStartFailure(errorCode: Int) {
            isAdvertising = false
            Log.e(TAG, "BLE Advertising failed: $errorCode")
            onError("Bluetooth Advertising failed (code $errorCode)")
        }
    }

    private val gattServerCallback = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "Device connected: ${device.address}")
                connectedDevice = device
                onConnected()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "Device disconnected")
                connectedDevice = null
            }
        }

        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            if (responseNeeded) {
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value)
            }

            if (characteristic.uuid == CHAR_ACK_UUID) {
                Log.d(TAG, "Transfer ACK received from web companion!")
                onTransferSuccess()
                stop()
            }
        }

        override fun onDescriptorWriteRequest(
            device: BluetoothDevice,
            requestId: Int,
            descriptor: android.bluetooth.BluetoothGattDescriptor,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            if (responseNeeded) {
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun start(): Boolean {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            onError("Bluetooth is disabled. Please enable Bluetooth.")
            return false
        }

        advertiser = bluetoothAdapter.bluetoothLeAdvertiser
        if (advertiser == null) {
            onError("Bluetooth LE Advertising not supported on this device.")
            return false
        }

        // Setup GATT Server
        try {
            gattServer = bluetoothManager?.openGattServer(context, gattServerCallback)
            val service = BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)

            val handshakeChar = BluetoothGattCharacteristic(
                CHAR_HANDSHAKE_UUID,
                BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_WRITE
            )

            transferCharacteristic = BluetoothGattCharacteristic(
                CHAR_TRANSFER_UUID,
                BluetoothGattCharacteristic.PROPERTY_NOTIFY or BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ
            ).apply {
                val descriptor = android.bluetooth.BluetoothGattDescriptor(
                    CLIENT_CONFIG_DESCRIPTOR_UUID,
                    android.bluetooth.BluetoothGattDescriptor.PERMISSION_WRITE or android.bluetooth.BluetoothGattDescriptor.PERMISSION_READ
                )
                addDescriptor(descriptor)
            }

            val ackChar = BluetoothGattCharacteristic(
                CHAR_ACK_UUID,
                BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_WRITE
            )

            service.addCharacteristic(handshakeChar)
            service.addCharacteristic(transferCharacteristic!!)
            service.addCharacteristic(ackChar)

            gattServer?.addService(service)

            // Start Advertising
            val settings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(true)
                .setTimeout(60000) // 60-second timeout
                .build()

            val data = AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .addServiceUuid(ParcelUuid(SERVICE_UUID))
                .build()

            advertiser?.startAdvertising(settings, data, advertiseCallback)
            return true
        } catch (e: SecurityException) {
            onError("Bluetooth permissions required: ${e.message}")
            return false
        } catch (e: Exception) {
            onError("Error starting BLE service: ${e.message}")
            return false
        }
    }

    /**
     * Sends encrypted payload split into MTU-safe chunks.
     */
    @SuppressLint("MissingPermission")
    fun sendEncryptedPayload(payload: ByteArray): Boolean {
        val device = connectedDevice
        val char = transferCharacteristic
        if (device == null || char == null || gattServer == null) {
            onError("Laptop browser is not connected yet.")
            return false
        }

        val chunkSize = 180 // Safe MTU chunk size
        val totalChunks = ((payload.size + chunkSize - 1) / chunkSize)
        val totalLength = payload.size

        for (i in 0 until totalChunks) {
            val start = i * chunkSize
            val end = (start + chunkSize).coerceAtMost(payload.size)
            val chunkBytes = payload.copyOfRange(start, end)

            // Header: [TotalLength (2 bytes)] [ChunkIndex (1 byte)] [TotalChunks (1 byte)]
            val packet = ByteArray(4 + chunkBytes.size)
            packet[0] = ((totalLength shr 8) and 0xff).toByte()
            packet[1] = (totalLength and 0xff).toByte()
            packet[2] = i.toByte()
            packet[3] = totalChunks.toByte()
            System.arraycopy(chunkBytes, 0, packet, 4, chunkBytes.size)

            char.value = packet
            gattServer?.notifyCharacteristicChanged(device, char, false)
            Thread.sleep(15) // Brief inter-packet delay for BLE buffer flush
        }
        return true
    }

    @SuppressLint("MissingPermission")
    fun stop() {
        try {
            if (isAdvertising) {
                advertiser?.stopAdvertising(advertiseCallback)
                isAdvertising = false
            }
            gattServer?.close()
            gattServer = null
            connectedDevice = null
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping BLE: ${e.message}")
        }
    }
}
