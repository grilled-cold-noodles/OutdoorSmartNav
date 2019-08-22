package cn.mcf.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.List;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by McF on 2018/9/25.
 */
public class MyLotView extends AppCompatImageView {

    private ArrayList<MyRectF> lots; //停车场内的所有车位
    private Handler myHandler;
    public static final int FRESH_LOT_OK = 3;

    //在onDraw的外面放对象
    private Paint paint =new Paint();
    private Matrix matrix1 = new Matrix();
    private Matrix matrix2 = new Matrix();
    private Bitmap car = BitmapFactory.decodeResource(getResources(),R.mipmap.car);
    private Bitmap park = BitmapFactory.decodeResource(getResources(),R.mipmap.park);

    private class lotJson{
        String area_no;
        String area_name;
        List<Integer> data;
    }

    private class lotJsonAli{
        int _id;
        boolean isAvailable;
    }

    //必须实现两个构造方法
    public MyLotView(Context context){
        super(context);
        lots = new ArrayList<>();
    }
    //必须实现两个构造方法
    public MyLotView(Context context, AttributeSet attrs){
        super(context,attrs);
        lots = new ArrayList<>();
    }

    public void setMyHandler(Handler myHandler) {
        this.myHandler = myHandler;
    }

    public ArrayList<MyRectF> getLots() {
        return lots;
    }

    public void setLots(ArrayList<MyRectF> lots) {
        this.lots = lots;
    }
    //绘制车位信息
    @Override
    public void onDraw(Canvas canvas){
        super.onDraw(canvas);
        paint.setStrokeWidth((float)5.0);//设置笔刷的粗细度
        paint.setColor(Color.RED);//设置绘制的颜色

        //for(MyRectF lot:lots){
        for(int i=0;i<12;i++){
            Log.i("lotslist", String.valueOf(lots.get(i)));
            if(lots.get(i).getState() == MyRectF.OCCUPIED ){
                Log.d("mycar","是OCCUPIED");
                matrix1.setRotate(90);  //旋转
                Bitmap carToDraw = Bitmap.createBitmap(car, 0, 0, car.getWidth(), car.getHeight(), matrix1, true);
                canvas.drawBitmap(carToDraw,null,lots.get(i),paint);
            }
            else if(lots.get(i).getState() == MyRectF.AVAILABLE){
                Log.d("mycar","是AVAILABLE");
                matrix2.setRotate(90);  //旋转
                Bitmap carToDraw2 = Bitmap.createBitmap(park, 0, 0, park.getWidth(), park.getHeight(), matrix2, true);
                canvas.drawBitmap(carToDraw2,null,lots.get(i),paint);
            }
            else if(lots.get(i).getState() == MyRectF.UNKNOWN){
                Log.e("mycar","是UNKNOWN");
            }
            else{
                Log.e("mycar","既不OCCUPIED，也不AVAILABLE");
            }
        }
    }

    //重置车位信息的时候被调用
    //向服务器获取车位信息
    public void getParkLots(){
        new Thread(myRunnable).start();
    }
    public Runnable myRunnable = new Runnable() {
            @Override
        public void run() {
            try{
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("http://" + netInf.ipAli + ':' + netInf.portAli + "/getParkLots?parkID=park1")
                        .build();
                Response response = null;
                response = client.newCall(request).execute();
                if(response.isSuccessful()){
                    Gson gson = new Gson();
                    String lotsArray = response.body().string();
                    //字符串形式改为列表形式存入
                    List<lotJsonAli> occupiedLots = gson.fromJson(lotsArray, new TypeToken<List<lotJsonAli>>(){}.getType());

                    //判断所有车位的信息
                    for(lotJsonAli i : occupiedLots){
                        Log.e("occupiedLots","_id: "+i._id);
                        if(i._id > 12){  //对于数据库中>12的id号，则跳过
                            continue;
                        }
                        if(i.isAvailable){
                            lots.get(i._id - 1).setState(MyRectF.AVAILABLE);
                        }
                        else
                            lots.get(i._id - 1).setState(MyRectF.OCCUPIED);
                    }
                }
//                if(response.isSuccessful()){
//                    Gson gson = new Gson();
//                    String resStr = response.body().string();
//                    lotJson lot = gson.fromJson(resStr,lotJson.class);
//                    for(int i = 0; i < lot.data.size();i++){
//                        if(lot.data.get(i) == 2){
//                            lots.get(i).setState(MyRectF.AVAILABLE);
//                        }
//                        else if(lot.data.get(i) == 3){
//                            lots.get(i).setState(MyRectF.OCCUPIED);
//                        }
//                        else{
//                            lots.get(i).setState(MyRectF.UNKNOWN);
//                        }
//                    }
//                }
                Message msg =Message.obtain(myHandler);
                msg.what = FRESH_LOT_OK;
                msg.sendToTarget();
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    };
}
