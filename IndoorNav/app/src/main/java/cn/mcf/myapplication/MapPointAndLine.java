package cn.mcf.myapplication;



import java.util.ArrayList;

/**
 * Created by McF on 2018/9/13.
 */

public class MapPointAndLine{
    //初始坐标
    private float firstX;
    private float firstY;

    //显示坐标
    private float viewX;
    private float viewY;

    //邻接导航点
    private ArrayList<MapPointAndLine> next;

    //序号
    private int number;

    public MapPointAndLine(){
        next = new ArrayList<>();
        number = 0;//用户
    }

    public MapPointAndLine(float firstX,float firstY,int number){
        this.firstX = firstX;
        this.viewX = firstX;
        this.firstY = firstY;
        this.viewY = firstY;
        this.number = number;
        next = new ArrayList<>();
    }

    public float getFirstX() {
        return firstX;
    }

    public void setFirstX(float firstX) {
        this.firstX = firstX;
    }

    public float getFirstY() {
        return firstY;
    }

    public void setFirstY(float firstY) {
        this.firstY = firstY;
    }

    public float getViewX() {
        return viewX;
    }

    public void setViewX(float viewX) {
        this.viewX = viewX;
    }

    public float getViewY() {
        return viewY;
    }

    public void setViewY(float viewY) {
        this.viewY = viewY;
    }

    public ArrayList<MapPointAndLine> getNext() {
        return next;
    }

    public void setNext(ArrayList<MapPointAndLine> next) {
        this.next = next;
    }

    public void addNext(MapPointAndLine nextPoint){
        this.next.add(nextPoint);
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
