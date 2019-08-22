package cn.mcf.myapplication;

import android.util.Log;

/**
 * Created by McF on 2018/9/29.
 */

public class Self extends MapPointAndLine {
    private String account;
    private String password;

    //占据的车位序号，-1为未占据
    private int lotOccupying;
    public Self(){  //构造时，默认为-1，即空闲
        super();
        lotOccupying = -1;
    }

    public Self(float firstX,float firstY,int number){ //构造时，默认为-1，即空闲
        super(firstX,firstY,number);
        lotOccupying = -1;
    }
    public int getLotOccupying() {
        return lotOccupying;
    }

    public void setLotOccupying(int lotOccupying) {
        this.lotOccupying = lotOccupying;
    }



    //重置位置
    public void resetLoc(){
        int random = (int)(1+Math.random()*5);
        Log.i("randomrandom",Integer.toString(random));
        switch(random){
            case 1:
                this.setViewX((float)(200+Math.random()*600));
                this.setViewY(460);
                break;
            case 2:
                this.setViewX((float)(200+Math.random()*600));
                this.setViewY(380);
                break;
            case 3:
                this.setViewX((float)(200+Math.random()*600));
                this.setViewY(300);
                break;
            case 4:
                this.setViewX((float)(200+Math.random()*600));
                this.setViewY(220);
                break;
            case 5:
                this.setViewX(300);
                this.setViewY((float)(230+Math.random()*280));
        }
    }

}
