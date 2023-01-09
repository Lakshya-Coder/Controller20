package com.lakshyagupta7089.controller20

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.hardware.*
import android.os.*
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.marginLeft
import com.lakshyagupta7089.controller20.model.PairedDevice


class MainActivity : AppCompatActivity(), SensorEventListener {

    private val TAG = "MainActivity"

    private var prevSendData = "no lights"

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.i("Permission: ", "Granted")
                mBluetoothService = BluetoothService(this, mHandler)
                mBluetoothConnectDialog = BluetoothConnectDialogFragment(mBluetoothService?.getPairedDeviceList(false), mHandler)
            } else {
                Log.i("Permission: ", "Denied")
            }
        }
    val enableBtLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(
                    applicationContext,
                    "Please enable bluetooth so app works perfectly.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    private val mHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)

            when(msg.what) {
                DEVICE_NOT_SUPPORTS_BLUETOOTH -> {

                }

                ENABLE_BLUETOOTH -> {
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    enableBtLauncher.launch(enableBtIntent)
                }

                NEED_REFRESHED_PAIRED_DEVICE_LIST -> {
                    mBluetoothConnectDialog?.updatePairedDevicesRecyclerView(mBluetoothService?.getPairedDeviceList(true))
                }

                CONNECT_WITH_DEVICE -> {
                    val pairedDevice = msg.obj as PairedDevice
                    mBluetoothService?.connectWithDevice(pairedDevice.bluetoothDevice, pairedDevice.uuid)
                    mBluetoothConnectDialog?.dismiss()
                }

                MESSAGE_READ -> {

                }

                MESSAGE_WRITE -> {
//                    Toast.makeText(this@MainActivity, (msg.obj as ByteArray).toString(), Toast.LENGTH_LONG).show()
                }

                RECEIVED_AN_ERROR -> {
                    Toast.makeText(this@MainActivity, msg.obj.toString(), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private var mBluetoothService: BluetoothService? = null
    private lateinit var mConnectBtn: Button
    private var mBluetoothConnectDialog: BluetoothConnectDialogFragment? = null

    private lateinit var sensorManager: SensorManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED) {
            requestPermission(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            mBluetoothService = BluetoothService(this, mHandler)
            mBluetoothConnectDialog = BluetoothConnectDialogFragment(mBluetoothService?.getPairedDeviceList(false), mHandler)
        }

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        mConnectBtn = findViewById(R.id.connectBtn)

        mConnectBtn.setOnClickListener {
            mBluetoothConnectDialog?.show(supportFragmentManager, "BluetoothConnectDialogFragment")
        }

//        findViewById<Button>(R.id.redBtn).setOnClickListener {
//            mBluetoothService?.write("red")
//        }
//        findViewById<Button>(R.id.greenBtn).setOnClickListener {
//            mBluetoothService?.write("green")
//        }

        setUpAccelerometerSensor()
    }

    private fun setUpAccelerometerSensor() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { sensor ->
            sensorManager.registerListener(this,
                sensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }


    private fun requestPermission(permission: String) {
        when {
            ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission is granted
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                permission
            ) -> {
                // Additional rational should be displayed
                AlertDialog.Builder(this)
                    .setTitle("Permission required")
                    .setMessage("We need this permission to communicate with bluetooth device.")
                    .setPositiveButton("Ok") { _, _ ->
                        requestPermissionLauncher.launch(permission)
                    }.show()
            }

            else -> {
                // Permission has not been asked yet
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val square: CardView = findViewById(R.id.cardView)
            val helperTextView: TextView = findViewById(R.id.helperTextView)

            square.rotationX = x * 4
            square.rotationY = y * 4

////            findViewById<TextView>(R.id.helperTextView).let {
//            helperTextView.rotationX = x * 4
//            helperTextView.rotationY = y * 4
//            }

            if (x < -2.5) {
                if (y < -2) {
                    Log.d(TAG, "onSensorChanged: left up")
                    return
                } else if (y > 2){
                    Log.d(TAG, "onSensorChanged: right up")
                    return
                }
            }

            if (x > 2.5) {
                if (y < -2) {
                    Log.d(TAG, "onSensorChanged: left down")
                    return
                } else if (y > 2){
                    Log.d(TAG, "onSensorChanged: right down")
                    return
                }
            }


            if (x < -2.5) {
                Log.d(TAG, "Forward")
                square.setBackgroundColor(Color.parseColor("#ECB365"))
                helperTextView.text = "See your car has been moving forward."
                helperTextView.setTextColor(Color.parseColor("#041C32"))
            } else if (x > 2.5) {
                Log.d(TAG, "Backward")
                square.setBackgroundColor(Color.parseColor("#ECB365"))

                helperTextView.text = "See your car has been moving backward"
                helperTextView.setTextColor(Color.parseColor("#041C32"))
            } else if (y < -2.5) {
                Log.d(TAG, "Left")
                helperTextView.text = "See your car has been moving left"
                helperTextView.setTextColor(Color.parseColor("#041C32"))
                square.setBackgroundColor(Color.parseColor("#ECB365"))
                synchronized(this@MainActivity) {
                    if (prevSendData != "red") {
                        mBluetoothService?.write("red")
                        prevSendData = "red"
                    }
                }
            } else if (y > 2.5) {
                Log.d(TAG, "Right")
                helperTextView.text = "See your car has been moving right"
                helperTextView.setTextColor(Color.parseColor("#041C32"))
                square.setBackgroundColor(Color.parseColor("#ECB365"))
                synchronized(this@MainActivity) {
                    if (prevSendData != "green") {
                        mBluetoothService?.write("green")
                        prevSendData = "green"
                    }
                }
            } else {
                square.setBackgroundColor(Color.parseColor("#04293A"))
                synchronized(this@MainActivity) {
                    if (prevSendData == "no lights") {
                        mBluetoothService?.write("no lights")
                    }
                    helperTextView.text = "Please move your phone to move the car."
                    helperTextView.setTextColor(Color.WHITE)
                }
            }
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) { }

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        super.onDestroy()
    }

}