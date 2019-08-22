package cn.mcf.myapplication;

import android.graphics.RectF;

/**
 * Created by McF on 2018/9/23.
 */

public class MyRectF extends RectF {//RectF  矩形float
    //车位序号
    private int number;

    //对应的导航点序号
    private int mapPoint;

    //车位状态
    private int state;

    public static final int OCCUPIED = 1;//占据
    public static final int AVAILABLE = 0;//可用
    public static final int UNKNOWN = 2;

    public MyRectF(float left,float top,float right,float bottom,int number,int mapPoint){
        super(left,top,right,bottom);
        this.number = number;
        this.mapPoint = mapPoint;
        state = UNKNOWN;
    }

    //取0到1随机数，重置状态
    public void resetState(){
        state = (int)(1+Math.random()*(1-0+1));
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getMapPoint() {
        return mapPoint;
    }

    public void setMapPoint(int mapPoint) {
        this.mapPoint = mapPoint;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }


}
