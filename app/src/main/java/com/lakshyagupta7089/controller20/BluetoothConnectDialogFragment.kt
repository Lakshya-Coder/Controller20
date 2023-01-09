package com.lakshyagupta7089.controller20

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.lakshyagupta7089.controller20.model.PairedDevice

class BluetoothConnectDialogFragment(
    private val pairedDeviceList: ArrayList<PairedDevice>?,
    private val mHandler: Handler
): DialogFragment() {
    private val TAG = "BluetoothConnectDialog"

    private var dialogPairedDeviceRecyclerView: RecyclerView? = null
    private var dialogSwipeRefreshLayout: SwipeRefreshLayout? = null

    override fun onViewCreated(bluetoothConnectDialogView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(bluetoothConnectDialogView, savedInstanceState)

        dialogPairedDeviceRecyclerView = bluetoothConnectDialogView.findViewById(R.id.dialogPairedDeviceRecyclerView)
        dialogSwipeRefreshLayout = bluetoothConnectDialogView.findViewById(R.id.dialogSwipeRefreshLayout)

        dialogSwipeRefreshLayout?.setOnRefreshListener {
            mHandler.obtainMessage(NEED_REFRESHED_PAIRED_DEVICE_LIST).sendToTarget()
        }

        dialogPairedDeviceRecyclerView?.adapter = PairedDeviceAdapter(pairedDeviceList, mHandler)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        return inflater.inflate(R.layout.bluetooth_connect_dialog, container, false)
    }

    fun updatePairedDevicesRecyclerView(pairedDeviceList: ArrayList<PairedDevice>?) {
        dialogPairedDeviceRecyclerView?.adapter = null
        dialogPairedDeviceRecyclerView?.adapter = PairedDeviceAdapter(pairedDeviceList, mHandler)
        dialogSwipeRefreshLayout?.isRefreshing = false
    }
}