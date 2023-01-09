package com.lakshyagupta7089.controller20

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.lakshyagupta7089.controller20.model.PairedDevice
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class BluetoothService(
    private val activity: AppCompatActivity,
    private val mHandler: Handler
    ) {

    private val TAG = "BluetoothService"

    private var mBluetoothManager: BluetoothManager = activity.getSystemService(BluetoothManager::class.java)
    private var mBluetoothAdapter: BluetoothAdapter? = mBluetoothManager.adapter
    private var mPairedDeviceList: ArrayList<PairedDevice>? = null

    private var mConnectThread: ConnectThread? = null
    private var mConnectedThread: ConnectedThread? = null

    init {
        if (mBluetoothAdapter == null) {
            mHandler.obtainMessage(DEVICE_NOT_SUPPORTS_BLUETOOTH).sendToTarget()

        } else {
            if (mBluetoothAdapter?.isEnabled == false) {
                mHandler.obtainMessage(ENABLE_BLUETOOTH).sendToTarget()
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun getPairedDeviceList(isWantRefreshedList: Boolean): ArrayList<PairedDevice> {
        if (mPairedDeviceList == null || isWantRefreshedList) {
            mPairedDeviceList = ArrayList()


            mBluetoothAdapter?.bondedDevices?.forEach { bluetoothDevice ->
                mPairedDeviceList?.add(PairedDevice(bluetoothDevice, bluetoothDevice.uuids[0].uuid))
            }
        }

        return mPairedDeviceList ?: ArrayList()
    }

    fun connectWithDevice(bluetoothDevice: BluetoothDevice?, uuid: UUID?) {
        if (mConnectThread == null) {
            mConnectThread = ConnectThread(bluetoothDevice, uuid)
            mConnectThread?.start()
        }
    }

    private fun connected(socket: BluetoothSocket?) {
        Log.d(TAG, "connected successfully")
        mConnectedThread = ConnectedThread(socket)
        mConnectedThread?.start()
        mHandler.obtainMessage(10).sendToTarget()

        write("green")
        Handler(activity.mainLooper).postDelayed({
            write("red")
        }, 200)
    }

    fun write(msg: String) {
        if (mConnectedThread != null) {
            val out = msg.toByteArray()
            mConnectedThread?.write(out)
        }
    }

    private fun connectionLost() {

    }

    @SuppressLint("MissingPermission")
    private inner class ConnectThread(device: BluetoothDevice?, uuid: UUID?) : Thread() {

        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
                device?.createRfcommSocketToServiceRecord(uuid)
        }

        override fun run() {
            mmSocket?.let { socket ->
                try {
                    socket.connect()
                } catch (e: IOException) {
                    Log.e(TAG, "Could not close the client socket", e)
                    mHandler.obtainMessage(RECEIVED_AN_ERROR, e.toString()).sendToTarget()
                    return
                }

                synchronized(this@BluetoothService) {
                    mConnectThread = null
                }

                connected(socket)
            }
        }

        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the client socket", e)
            }
        }
    }

    private inner class ConnectedThread(socket: BluetoothSocket?): Thread() {
        private val mmSocket = socket
        private val mmInStream: InputStream = socket?.inputStream!!
        private val mmOutStream: OutputStream = socket?.outputStream!!

        override fun run() {
            val buffer = ByteArray(1024)
            var bytes: Int

            // Keep listening to the InputStream while connected

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer)

                    // Send the obtained bytes to the UI Activity
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                        .sendToTarget()
                } catch (e: IOException) {
                    Log.e(TAG, "disconnected", e)
                    connectionLost()
                    break
                }
            }
        }

        fun write(buffer: ByteArray?) {
            try {
                mmOutStream.write(buffer)

                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(MESSAGE_WRITE, -1, -1, buffer)
                    .sendToTarget()
            } catch (e: IOException) {
                Log.e(TAG, "Exception during write", e)
            }
        }

        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "close() of connect socket failed", e)
            }
        }
    }
}