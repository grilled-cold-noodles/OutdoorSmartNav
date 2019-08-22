package cn.mcf.myapplication;

public class Location {
    public int getRSSI() {
        return RSSI;
    }

    public void setRSSI(int RSSI) {
        this.RSSI = RSSI;
    }

    public int getIdX() {
        return idX;
    }

    public void setIdX(int idX) {
        this.idX = idX;
    }

    public int getIdY() {
        return idY;
    }

    public void setIdY(int idY) {
        this.idY = idY;
    }

    private int RSSI;
    private int idX;
    private int idY;

    public Location(int RSSI,int idX,int idY){
        this.RSSI = RSSI;
        this.idX = idX;
        this.idY = idY;
    }



}
