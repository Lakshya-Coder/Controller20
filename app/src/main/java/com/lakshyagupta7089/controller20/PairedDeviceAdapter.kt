package com.lakshyagupta7089.controller20

import android.annotation.SuppressLint
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.*
import com.lakshyagupta7089.controller20.model.PairedDevice

class PairedDeviceAdapter(
    private val pairedDevicesList: ArrayList<PairedDevice>?,
    private val mHandler: Handler
): Adapter<PairedDeviceAdapter.PairedDeviceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PairedDeviceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_paired_device, parent, false)
        return PairedDeviceViewHolder(view)
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: PairedDeviceViewHolder, position: Int) {
        val pairedDevice = pairedDevicesList?.get(position)

        holder.rowDeviceHardwareAddressTextView.text = pairedDevice?.uuid.toString()
        holder.rowDeviceNameTextView.text = pairedDevice?.bluetoothDevice?.name
        holder.rowPairedDeviceLinearLayout.setOnClickListener {
            mHandler.obtainMessage(CONNECT_WITH_DEVICE, pairedDevice).sendToTarget()
        }
    }

    override fun getItemCount(): Int = pairedDevicesList?.size!!

    inner class PairedDeviceViewHolder(itemView: View) : ViewHolder(itemView) {
        val rowDeviceNameTextView: TextView = itemView.findViewById(R.id.rowDeviceNameTextView)
        val rowDeviceHardwareAddressTextView: TextView = itemView.findViewById(R.id.rowDeviceHardwareAddressTextView)
        val rowPairedDeviceLinearLayout: LinearLayout = itemView.findViewById(R.id.rowPairedDeviceLinearLayout)
    }

}