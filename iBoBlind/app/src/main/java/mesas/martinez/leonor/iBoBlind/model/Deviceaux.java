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
    private boolean firstime;
    private int length=10;//min length 4
    private double[] aRssi;
    private int[] tendence={0,0,0,0,0,0,0,0,0,0};
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
        this.last_update=0;
        this.firstime=true;
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
        this.last_update=0;
        this.firstime=true;
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
        this.last_update=0;
        this.firstime=true;
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
       this.last_update=0;
       this.firstime=true;
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
      this.last_update=0;
      this.firstime=true;
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
       this.last_update=0;
       this.firstime=true;
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
      this.last_update=0;
      this.firstime=true;
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
        this.last_update=0;
        if(rssi>maxRssi){this.firstime=false;}else{this.firstime=true;}
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

    public void plusTwoCount() {
        count=count+2;
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

    public void setDiferTime(int diferTime) {
        this.diferTime = diferTime;
    }

    public void setLast_update(long last_update) {
        this.last_update = last_update;
    }

    public long getLast_update() {
        return last_update;
    }

    public double getdBmRSSI() {
        return dBmRSSI;
    }

    public void setdBmRSSI(double rssi) {
        this.dBmRSSI = rssi;
        push(rssi);
    }

    public void setFirstime(boolean firstime) {
        this.firstime = firstime;
        this.last_update=System.currentTimeMillis();
    }

    public boolean isFirstime() {
        return firstime;
    }

    public double getOutOfRegion() {
        return outOfRegion;
    }

    public void setOutOfRegion(double outOfRegion) {
        this.outOfRegion = outOfRegion;
    }
    public void pushTendence(int value) {
        try {
            if(value!=0.0f){
                    for (int i = (length - 1); i >0; i--) {
                        //Log.d("----------------PUSH------------   in 0  ", value+"  in i= "+i+"moving r[i-1]= "+aRssi[i-1]);
                        double aux = aRssi[i - 1];
                        aRssi[i] = aux;
                    }
                }
                tendence[0] = value;
        }catch(NullPointerException e){
            Log.e("DEVICEAUX PUSK", "NULL POINT EXCEPTION"+e.getLocalizedMessage());
            e.printStackTrace();
        }
    }
    public int tendence(){
        int sum=0;
        for(int i=0; i<tendence.length;i++){
            sum=sum+tendence[i];
        }
        return sum;
    }

    private void push(double value) {
        int auxm=(int)(length/4);
        int m=(int)auxm*2;
        int n=(int)auxm*3;

        //Log.d("---------------PUSH---------------    ","    Mitad array=    "+m);
        try {

            if(value!=0.0f){
                if((length<5) || a > (length-1)){
                    if (a > 0) {
                        for (int i = (length - 1); i > 0; i--) {
                            //Log.d("----------------PUSH------------   in 0  ", value+"  in i= "+i+"moving r[i-1]= "+aRssi[i-1]);
                            double aux = aRssi[i - 1];
                            aRssi[i] = aux;
                        }

                    }
                    aRssi[0] = value;
                    a++;
                    if(a>m){okaRssi=true;}
                    if(a>aRssi.length){okaRssiOld=true;}
                }else{
                    double aux;
                switch(a) {
                    case (0):
                        for (int i =0; i > length; i++) {
                            aRssi[i]=value;
                        }
                    break;
                    case(1):
                        for (int i =0; i > auxm; i++) {
                            aRssi[i]=value;
                        }
                        okaRssi=true;
                    break;
                    case(2):
                        aux = aRssi[auxm];
                        for (int i =auxm; i > n; i++) {
                            aRssi[i]=aux;
                        }
                        for (int i =0; i > auxm; i++) {
                            aRssi[i]=value;
                        }
                        okaRssiOld=true;
                        break;
                    case(3):
                        aux = aRssi[auxm];
                        for (int i =auxm; i > m; i++) {
                            aRssi[i]=aux;
                        }
                        for (int i =0; i > auxm; i++) {
                            aRssi[i]=value;
                        }
                        okaRssiOld=true;
                        break;
                    default:
                        int old=(int)(length/a);
                        int p=(int)(length/(a+1));
                        int init=(int)(length-p);
                        int fin=(int)length;
                        for(int k=0;k<(a+1);k++){

                            if(k==(a)){
                              aux=value;
                            }else{
                               aux=aRssi[(length-1-(old*k))];
                            }
                            for(int j=init;j<fin;j++){
                                aRssi[j]=aux;
                            }
                            fin=init;
                            init=fin-p;
                        }
                    break;
                }
                   a++;

                }
}
        }catch(NullPointerException e){
            Log.e("DEVICEAUX PUSH", "NULL POINT EXCEPTION"+e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    public double average() {
        double d = 0;
        double sum = 0;
        double result=0f;
        if(okaRssi){
        double auxm=(length/2)-1;
        int m=(int)auxm;
        for (int i = 0; i< m; i++) {
            sum = sum + aRssi[i];
            if(aRssi[i]!=0.0f){
            d++;}
        }
        result = sum/d;}
        //Log.d("AverageNEW", "SUM= "+sum+"/ por d= "+d+" result= "+result);
        return result;
    }
    public double oldAverage() {
        double d = 0;
        double sum = 0;
        double result=0f;
        if(okaRssiOld){
            double auxm=(length/2)+1;
            int m=(int)auxm;
            for (int i = m; i< length; i++) {
                sum = sum + aRssi[i];

                if(aRssi[i]!=0.0f){
                    d++;}
            }
            result = sum/d;}
        //Log.d("AverageOLD", "SUM= "+sum+"/ por d= "+d+" result= "+result);
        return result;
    }
    public boolean RangeOfTime(){
        boolean result=false;
        long currentime=System.currentTimeMillis();
        long difer=currentime-last_update;
        if(difer>(diferTime*1000)){
        result=true;
        }
            Log.d("  ----DEVICEAUX RANGE OFF TIME-----"+result+"----for device--:  ",address+" difer= "+difer+" < "+(diferTime*1000));

        return result;
    }
    public double difer(){
        double result=0.0f;
        if(okaRssi && okaRssiOld ){
            double aux=average();
            double Oldaux=oldAverage();
            result=aux-Oldaux;
            Log.d("Average for : ", address+"  is NewAverage " + aux+" - OldAverage  " +Oldaux);
        }
        Log.d("Average for : ", address+"is  = "+result);
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
