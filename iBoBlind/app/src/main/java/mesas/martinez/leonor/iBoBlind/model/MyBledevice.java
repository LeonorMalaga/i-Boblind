package mesas.martinez.leonor.iBoBlind.model;

/**
 * Created by root on 29/01/15.
 */

import android.bluetooth.BluetoothDevice;

import java.io.Serializable;

public class MyBledevice implements Serializable {
    public BluetoothDevice device=null;
    public int rssi=0;
    public MyBledevice(BluetoothDevice mdevice, int mRssi){
        device=mdevice;
        rssi=mRssi;
    }
}
