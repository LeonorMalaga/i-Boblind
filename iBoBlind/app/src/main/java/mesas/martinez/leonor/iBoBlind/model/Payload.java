package mesas.martinez.leonor.iBoBlind.model;

import java.io.Serializable;

/**
 * Created by Leonor Martinez Mesas on 24/10/2014.
 */
public class Payload implements Serializable {
    private int _id;
    private int device_id;
    private String mdate = null;//The date at which the value was obtained
    private String mtype = null;//type of value, example Integer, double, String, Long ..ect
    private String mvalue = null;//The value collected by the sensor
    /**
     * Constructor
     */
    public Payload(){

    }

    public Payload( String type, String value) {
        this.mdate = String.valueOf(System.currentTimeMillis());
        this.mtype = type;
        this.mvalue = value;
    }
    public Payload( String type, String value, int device_id) {
        this.mdate = String.valueOf(System.currentTimeMillis());
        this.mtype = type;
        this.mvalue = value;
        this.device_id=device_id;
    }
    public Payload(String date, String type, String value,int device_id) {
        this.mdate = date;
        this.mtype = type;
        this.mvalue = value;
        this.device_id=device_id;
    }
    public Payload(String date, String type, String value) {
        this.mdate = date;
        this.mtype = type;
        this.mvalue = value;
    }

    /**GETTER-SETTER
     *This object can´t change after it was created, y will implement only getter
     */
    public int getdevice_id() {
        return device_id;
    }

    public void setdevice_id(int device_id) {
        this.device_id = device_id;
    }

    public String getDate() {
        return mdate;
    }

    public String getType() {
        return mtype;
    }

    public String getValue() {
        return mvalue;
    }

    public void setMdate(String mdate) {
        this.mdate = mdate;
    }

    public void setMtype(String mtype) {
        this.mtype = mtype;
    }

    public void setMvalue(String mvalue) {
        this.mvalue = mvalue;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }
    /*
  *OTHER
  */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Payload)) return false;
        Payload payload = (Payload) o;
        if (!mdate.equals(payload.mdate)) return false;
        if (!mtype.equals(payload.mtype)) return false;
        if (!mvalue.equals(payload.mvalue)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = mdate.hashCode();
        result = 31 * result + mtype.hashCode();
        result = 31 * result + mvalue.hashCode();
        return result;
    }
}