package mesas.martinez.leonor.iBoBlind.model;

import java.io.Serializable;

/**
 * Created by leonorMartinezMesas on 20/01/15.
 * This class represent a BLE device
 * identificate by the bluettoth adress(with ejem:<Adress>:AE1234CD,<device_name>:"LUIS", <RSSI>:"-51dBm")
 */
public class Project implements Serializable {
    private int _id=-1;
    private String mdate = null;//The date at which the class is generate
    private String mprojectName = null;//"LUIS"
    private String mprojectSpecification = null;//message to show
//    private List<Device> mDeviceList = new LinkedList<Device>();//List of measures values with his date and type(dBm) of value(-51)

    @Override
    public String toString() {
        return "Name:"+mprojectName+", "+mprojectSpecification;
    }

    /**
     * Constructor
     */
    public Project(){}
    public Project(String mprojectName,String mprojectSpecification) {
        this.mprojectSpecification = mprojectSpecification;
        this.mprojectName=mprojectName;
        this.mdate = String.valueOf(System.currentTimeMillis());
    }
    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }
    /**
     * Metod
     * /
     /**GETTER-SETTER
     *  I build all getter and setter, except set mDeviceAddress,because,it is the identifier
     * public void setmDeviceAddress(String mDeviceAddress) {
     *this.mDeviceAddress = mDeviceAddress;
     *}
     */
    public String getDate() {
        return mdate;
    }

    public void setDate(String mdate) {
        this.mdate = mdate;
    }
    public String getprojectSpecification() {
        return mprojectSpecification;
    }

    public void setprojectSpecification(String mprojectSpecification) {
        this.mprojectSpecification = mprojectSpecification;
    }

    public String getmprojectName() {
        return mprojectName;
    }

    public void setmprojectName(String mprojectName) {
        this.mprojectName = mprojectName;
    }
    //
//    public List<Device> getDeviceList() {
//        return mDeviceList;
//    }
//
//    public void setDeviceList(List<Device> mDeviceList) {
//        this.mDeviceList = mDeviceList;
//    }
//    /*
//*OTHER
//*/
//    public void addDevice(Device Device) {
//        mDeviceList.add(Device);
//    }
//    public Device getDevice(int i) {
//        return mDeviceList.get(i);
//    }
//    public int getDeviceListSize(){
//        return mDeviceList.size();
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Project)) return false;
        Project that = (Project) o;
       if (mprojectName != null ? !mprojectName.equals(that.mprojectName) : that.mprojectName != null)
            return false;
        if (mprojectSpecification != null ? !mprojectSpecification.equals(that.mprojectSpecification) : that.mprojectSpecification!= null)
            return false;
        return true; }

    @Override
    public int hashCode() {
        int result = mprojectName.hashCode();
        result = 31 * result + (mprojectSpecification != null ? mprojectSpecification.hashCode() : 0);
        return result;  }
}
