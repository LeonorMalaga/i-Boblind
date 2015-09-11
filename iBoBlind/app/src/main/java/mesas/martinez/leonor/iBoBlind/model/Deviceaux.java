package mesas.martinez.leonor.iBoBlind.model;

/**
 * Created by root on 20/08/15.
 */
//---------------------Device Aux-------------------------//
public class Deviceaux {
    //state=0 acercandose, state=1 alejandose
    private int state;
    private int count;
    private double outOfRegion;
    private double dBmAverage;
    private double lastdBmAverage;
    private String address;
    private String text;

    public Deviceaux(double dBmAverage, String address, String text) {
        this.address = address;
        this.text = text;
        this.dBmAverage = dBmAverage;
        this.lastdBmAverage = -77.0;
        this.outOfRegion = -85.0;
        this.state = 0;
        this.count = 0;
    }
    public Deviceaux(double dBmAverage, String address) {
        this.address = address;
        this.text = "leonormartinezmesas@gmail";
        this.dBmAverage = dBmAverage;
        this.lastdBmAverage = -77.0;
        this.outOfRegion = -85.0;
        this.state = 0;
        this.count = 0;
    }
   public Deviceaux(String address) {
        this.address = address;
        this.state = 0;
        this.count = 0;
        this.dBmAverage = -85.0;
        this.lastdBmAverage = -77.0;
        this.outOfRegion = -85.0;
        this.text = "leonormartinezmesas@gmail";
    }

  public Deviceaux(String address,String text) {
        this.address = address;
        this.state = 0;
        this.count = 0;
        this.dBmAverage = -85.0;
        this.lastdBmAverage = -77.0;
        this.outOfRegion = -85.0;
        this.text = "leonormartinezmesas@gmail";
    }
   public Deviceaux(Device device,Double rssi) {
        this.address = device.getmDeviceAddress();
        this.state = 0;
        this.count = 0;
        this.dBmAverage = Double.valueOf(device.getMaxRSSI());
        this.lastdBmAverage = rssi;
        this.outOfRegion = Double.valueOf(device.getMaxRSSI())-3;
        this.text = device.getDeviceSpecification();
    }
  public Deviceaux(String address,int maxRssi, String deviceSpecification,int rssi) {
        this.address = address;
        this.state = 0;
        this.count = 0;
        this.dBmAverage =maxRssi;
        this.lastdBmAverage = rssi;
        this.outOfRegion = maxRssi-3;
        this.text = deviceSpecification;
    }
    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getAddress() {
        return address;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public double getdBmAverage() {
        return dBmAverage;
    }

    public void setdBmAverage(double dBmAverage) {
        this.dBmAverage = dBmAverage;
    }

    public double getOutOfRegion() {
        return outOfRegion;
    }

    public void setOutOfRegion(double outOfRegion) {
        this.outOfRegion = outOfRegion;
    }

    public double getLastdBmAverage() {
        return lastdBmAverage;
    }

    public void setLastdBmAverage(double lastdBmAverage) {
        this.lastdBmAverage = lastdBmAverage;
    }

    @Override
    public boolean equals(Object o) {
        Deviceaux aux = (Deviceaux) o;
        String address = aux.getAddress();
        return this.address.equals(address);
    }
}
//------------------------------Device aux--------------------//
