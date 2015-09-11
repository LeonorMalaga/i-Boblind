package mesas.martinez.leonor.iBoBlind.model;

import java.io.Serializable;

/**
 * Created by leonorMartinezMesas on 20/01/15.
 * This class represent a BLE device
 * identificate by the bluettoth adress(with ejem:<Adress>:AE1234CD,<device_name>:"LUIS", <RSSI>:"-51dBm")
 */
public class Device implements Serializable {
    private int _id=-1;
    private int projecto_id=-1;
    private String mdate = null;//The date at which the class is generate
    private String mDeviceAddress = null;// lE1234CD
    private String mDeviceName = null;//"LUIS"
    private String latitude = null;
    private String longitude = null;
    private String maxRSSI = null;
    private String mDeviceSpecification = null;//message to show

    @Override
    public String toString() {
        return "Name:" + mDeviceName + ", address:" + mDeviceAddress + ", message "+this.mDeviceSpecification+", latitude:" + latitude + ", longitude:" + longitude + ",Proyect_id: "+", MAXRSSI:" +maxRSSI;
    }

    /**
     * Constructor
     */
    public Device() {
    }

    public Device(int idprojecto, String mDeviceAddress, String latitude, String longitude, String mDeviceName, String mDeviceSpecification, String maxRssi) {
        this.projecto_id = idprojecto;
        this.mDeviceAddress = mDeviceAddress;
        this.mDeviceName = mDeviceName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.mDeviceSpecification = mDeviceSpecification;
        this.mdate = String.valueOf(System.currentTimeMillis());
        this.maxRSSI = maxRssi;
    }

    /**
     * Method
     * /
     * /**GETTER-SETTER
     * I build all getter and setter, except set mDeviceAddress,because,it is the identifier
     * public void setmDeviceAddress(String mDeviceAddress) {
     * this.mDeviceAddress = mDeviceAddress;
     * }
     */
    public String getMaxRSSI() {
        return maxRSSI;
    }

    public int getprojecto_id() {
        return projecto_id;
    }

    public int get_id() {
        return _id;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }
    public String getDate() {
        return mdate;
    }


    public String getDeviceId() {
        return mDeviceAddress;
    }

    public String getDeviceSpecification() {
        return mDeviceSpecification;
    }

    public String getmDeviceAddress() {
        return mDeviceAddress;
    }

    public String getmDeviceName() {
        return mDeviceName;
    }

    /*SETTERS*/
    public void setDeviceSpecification(String mDeviceSpecification) {
        this.mDeviceSpecification = mDeviceSpecification;
    }
    public void setmDeviceName(String mDeviceName) {
        this.mDeviceName = mDeviceName;
    }
    public void setmDeviceAddress(String mDeviceAddress) {
        this.mDeviceAddress = mDeviceAddress;
    }
    public void setDate(String mdate) {
        this.mdate = mdate;
    }
    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
    public void setMaxRSSI(String maxRSSI) {
        this.maxRSSI = maxRSSI;
    }
    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }
    public void set_id(int _id) {
        this._id = _id;
    }
    public void setprojecto_id(int projecto_id) {
        this.projecto_id = projecto_id;
    }
//    //overloading Methods to avoid exception Problems
//    public void setLongitude() {
//
//    }
//    public void setMaxRSSI() {
//
//    }
//    public void setLatitude() {
//
//    }
//    public void set_id() {
//
//    }
//    public void setprojecto_id() {
//
//    }
//    public void setmDeviceName() {
//
//    }
//    public void setmDeviceAddress() {
//
//    }
//    public void setDate() {
//
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Device)) return false;

        Device device = (Device) o;

        if (!mDeviceAddress.equals(device.mDeviceAddress)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return mDeviceAddress.hashCode();
    }
}
