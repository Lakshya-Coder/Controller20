package com.lakshyagupta7089.controller20.model

import android.bluetooth.BluetoothDevice
import java.util.UUID

class PairedDevice(
    bluetoothDevice: BluetoothDevice?,
    uuid: UUID?
) {
    var bluetoothDevice: BluetoothDevice? = bluetoothDevice
        get() = field
        private set

    var uuid: UUID? = uuid
        get() = field
        private set
}