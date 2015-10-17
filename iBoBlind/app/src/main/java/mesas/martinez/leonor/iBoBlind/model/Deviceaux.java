package mesas.martinez.leonor.iBoBlind.model;

import android.util.Log;

/**
 * Created by root on 20/08/15.
 */
//---------------------Device Aux-------------------------//
public class Deviceaux {

    //state=0 acercandose, state=1 alejandose
    private long last_update;
    private int close;
    private int count;
    private int a;
    private int diferTime;//min secons betwen anounces
    private double outOfRegion;
    private double dBmRSSI;
    private boolean okaRssi;
    private boolean okaRssiOld;

    private int length=8;
    private double[] aRssi;
   // private double[] aRssiOld={0,0,0};
    private String address;
    private String text;

    public Deviceaux(double rssi, String address, String text) {
        this.address = address;
        this.text = text;
        this.dBmRSSI = rssi;
        this.outOfRegion = -85.0;
        this.count = 0;
        this.last_update=0;
        this.close=0;
        this.a = 0;
        this.okaRssi=false;
        this.okaRssiOld=false;
        this.aRssi=new double[length];
        this.push(rssi);
        this.diferTime=4;
        this.last_update=System.currentTimeMillis();
    }
    public Deviceaux(double rssi, String address) {
        this.address = address;
        this.text = "leonormartinezmesas@gmail";
        this.dBmRSSI = rssi;
        this.outOfRegion = -85.0;
        this.count = 0;
        this.last_update=0;
        this.close=0;
        this.a = 0;
        this.okaRssi=false;
        this.okaRssiOld=false;
        aRssi=new double[length];
        push(rssi);
        this.diferTime=4;
        this.last_update=System.currentTimeMillis();
    }
    public Deviceaux(double rssi, String address,int diferTime) {
        this.address = address;
        this.text = "leonormartinezmesas@gmail";
        this.dBmRSSI = rssi;
        this.outOfRegion = -85.0;
        this.count = 0;
        this.last_update=0;
        this.close=0;
        this.a = 0;
        this.okaRssi=false;
        this.okaRssiOld=false;
        aRssi=new double[length];
        push(rssi);
        this.diferTime=diferTime;
        this.last_update=System.currentTimeMillis();
    }
   public Deviceaux(String address) {
        this.address = address;
        this.count = 0;
        this.last_update=0;
        this.close=0;
       this.okaRssi=false;
       this.okaRssiOld=false;
        this.a = 0;
        this.dBmRSSI = -85.0;
        this.outOfRegion = -85.0;
        this.text = "leonormartinezmesas@gmail";
        this.diferTime=4;
       this.last_update=System.currentTimeMillis();
    }

  public Deviceaux(String address,String text) {
        this.address = address;
        this.count = 0;
        this.last_update=0;
        this.close=0;
        this.a = 0;
        this.okaRssi=false;
        this.okaRssiOld=false;
        this.dBmRSSI = -85.0;
        this.outOfRegion = -85.0;
        this.text = "leonormartinezmesas@gmail";
        this.diferTime=4;
      this.last_update=System.currentTimeMillis();
    }
   public Deviceaux(Device device,Double rssi) {
        this.address = device.getmDeviceAddress();
        this.count = 0;
        this.last_update=0;
        this.close=0;
        this.okaRssi=false;
        this.okaRssiOld=false;
        this.dBmRSSI = rssi;
        this.outOfRegion = Double.valueOf(device.getMaxRSSI());
        this.text = device.getDeviceSpecification();
        this.a = 0;
        aRssi=new double[length];
        push(rssi);
        this.diferTime=4;
       this.last_update=System.currentTimeMillis();
    }
  public Deviceaux(String address,int maxRssi, String deviceSpecification,int rssi) {
        this.address = address;
        this.count = 0;
        this.last_update=0;
        this.close=0;
        this.dBmRSSI =rssi;
        this.okaRssi=false;
        this.okaRssiOld=false;
        this.outOfRegion = maxRssi;
        this.text = deviceSpecification;
        this.a = 0;
        aRssi=new double[length];
        push(rssi);
        this.diferTime=4;
      this.last_update=System.currentTimeMillis();
    }
    public Deviceaux(String address,int maxRssi, String deviceSpecification,int rssi, int diferTime) {
        this.address = address;
        this.count = 0;
        this.last_update=0;
        this.close=0;
        this.dBmRSSI =rssi;
        this.okaRssi=false;
        this.okaRssiOld=false;
        this.outOfRegion = maxRssi;
        this.text = deviceSpecification;
        this.a = 0;
        aRssi=new double[length];
        push(rssi);
        this.diferTime=diferTime;
        this.last_update=System.currentTimeMillis();
    }
    public int getClose() {
        return close;
    }

    public void setClose(int close) {
        this.close = close;
    }
    public int getCount() {
        return count;
    }
    public int getDiferTime() {
        return diferTime;
    }
    public void setCounttocero() {
        count=0;
    }

    public void plusOneCount() {
        count++;
    }
    public void minusOneCount() {
        if(count>1){count--;}else{count=0;}
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

    public double getdBmRSSI() {
        return dBmRSSI;
    }

    public void setdBmRSSI(double rssi) {
        this.dBmRSSI = rssi;
        push(rssi);
    }

    public double getOutOfRegion() {
        return outOfRegion;
    }

    public void setOutOfRegion(double outOfRegion) {
        this.outOfRegion = outOfRegion;
    }

    private void push(double value) {
        double auxm=length/2;
        int m=(int)auxm;
        //Log.d("---------------PUSH---------------    ","    Mitad array=    "+m);
        try {
            if(value!=0.0f){
            if (a >0) {
                for (int i = (length - 1); i >0; i--) {
                    //Log.d("----------------PUSH------------   in 0  ", value+"  in i= "+i+"moving r[i-1]= "+aRssi[i-1]);
                    double aux = aRssi[i - 1];
                    aRssi[i] = aux;
                }
            }
                aRssi[0] = value;
            a++;
            if(a>m){okaRssi=true;}
            if(a>aRssi.length){okaRssiOld=true;}}
        }catch(NullPointerException e){
            Log.e("DEVICEAUX PUSK", "NULL POINT EXCEPTION"+e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    public double average() {
        double auxm=length/2;
        int m=(int)auxm;
        double d = 0;
        double sum = 0;
        double result=0f;
        if(okaRssi){
        for (int i = 0; i< m; i++) {
            sum = sum + aRssi[i];
            if(aRssi[i]!=0.0f){
            d++;}
        }
        result = sum/d;}
        Log.d("Average", "SUM= "+sum+"/ por d= "+d+" result= "+result);
        return result;
    }
    public double oldAverage() {
        double auxm=length/2;
        int m=(int)auxm;
        double d = 0;
        double sum = 0;
        double result=0f;
        if(okaRssiOld){
            for (int i = m; i< length; i++) {
                sum = sum + aRssi[i];

                if(aRssi[i]!=0.0f){
                    d++;}
            }
            result = sum/d;}
        Log.d("Average", "SUM= "+sum+"/ por d= "+d+" result= "+result);
        return result;
    }
    public boolean RangeOfTime(){
        boolean result=false;
        long currentime=System.currentTimeMillis();
        long difer=currentime-last_update;
        if(difer>(diferTime*1000)){
        last_update=currentime;
        result=true;
        }
        return result;
    }
    public double difer(){
        double result=0.0f;
        if(okaRssi && okaRssiOld ){
            double aux=average();
            double Oldaux=oldAverage();
            result=aux-Oldaux;
        }
        return result;
    }
    @Override
    public boolean equals(Object o) {
        Deviceaux aux = (Deviceaux) o;
        String address = aux.getAddress();
        return this.address.equals(address);
    }
}
//------------------------------Device aux--------------------//
