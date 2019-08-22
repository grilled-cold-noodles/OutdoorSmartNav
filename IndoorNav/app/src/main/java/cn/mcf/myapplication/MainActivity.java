package cn.mcf.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity{


    private MyPointAndLineView myPointAndLineView;  //绘制路径和用户点的自定义控件
    private MyLotView myLotView;  //绘制停车位信息的自定义控件
    private ImageView map;  //绘制底层停车场地图的控件

    private MapPointAndLine mapPointAndLine = new MapPointAndLine();  //关于导航点的自定义控件

    private Button findPath;//寻路按钮，点击后自动寻找最近车位并绘制路径
    private Button getLot;    //刷新车位信息，便于测试本项目效果为给所有车位随机刷新状态（未连接数据库）
    private Button simulateGO;    //模拟导航按钮
    private Button resetLoc;    //随机重置用户位置按钮
    private Button randomCars;    //随机车位
    private Button bluetoothStart;    //蓝牙定位start
    private Button bluetoothStop;     //蓝牙定位stop

    private int[] coordinate = new int[2];    //蓝牙位置坐标
    private MyReceiver receiver = null;    //广播接收
    private Handler myHandler;    //处理子线程信息的类

    float scale;  //用于缩放功能的变量


    /************************************ onCreate方法 *********************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*------------获取布局中的各个控件----------*/
        map = this.findViewById(R.id.map);   //背景,现暂时定为不可见
        myLotView = this.findViewById(R.id.myLotView);  //车位
        myPointAndLineView = this.findViewById(R.id.myPointAndLineView); //路线
        findPath = this.findViewById(R.id.findPath);  //寻找车位
        getLot = this.findViewById(R.id.getLot);  //刷新车位
        simulateGO = this.findViewById(R.id.simulateGO);  //开始导航
        resetLoc = this.findViewById(R.id.resetLoc); //随机位置
        randomCars = this.findViewById(R.id.randomCars);  //随机车辆
        bluetoothStart= this.findViewById(R.id.bluetoothStart);  //开始蓝牙
        bluetoothStop = this.findViewById(R.id.bluetoothStop);   //结束蓝牙

        myPointAndLineView.setMyLotView(myLotView);
        myPointAndLineView.init(); //初始化导航点和车位的坐标，并配置导航点的邻接点

        //处理子线程传过来的信息
        myHandler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                AlertDialog.Builder builder;  //提示框
                //看传过来的message.what中是值是什么，通过case语句分支
                switch(msg.what){
                    //导航结束，弹窗提示
                    case MyPointAndLineView.SIMULATIONEND:
                        builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("提示");
                        builder.setMessage("导航结束");
                        builder.setPositiveButton("确定",null);
                        builder.show();
                        if(myPointAndLineView.getMe().getLotOccupying() == -1)  //-1说明空闲
                            findPath.setText("寻找车位");
                        else
                            findPath.setText("寻车");
                        myPointAndLineView.setFindPathSignal(0);
                        myPointAndLineView.invalidate();
                        break;
                    //车位全满，弹窗提示
                    case MyPointAndLineView.ALL_OCCUPIED:
                        builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("提示");
                        builder.setMessage("该停车场已无空余车位");
                        builder.setPositiveButton("确定",null);
                        builder.show();
                        break;
                    //向服务器获取车位信息完成，子线程发送FRESH_LOT_OK ，主线程刷新UI
                    case MyLotView.FRESH_LOT_OK:
                        myLotView.invalidate(); //重绘
                        break;
                    case MyPointAndLineView.RANDOM_CARS_OK:
                        myLotView.invalidate(); //重绘
                        break;
                }
                super.handleMessage(msg);
            }
        };

        myPointAndLineView.setMyHandler(myHandler);
        myLotView.setMyHandler(myHandler);

        //设置点击事件，实现相应按键 功能

        //点击寻找车位
        findPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myPointAndLineView.findLot(); //寻找最近的空闲车位
                myPointAndLineView.setFindPathSignal(1); //是否已经得出路径信息的信号,设置为1，表明点了“寻找车位”
                myPointAndLineView.invalidate();
            }
        });

        //点击开始导航
        simulateGO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //还没点“寻找车位”，即 还没规划路径，则弹出提示框
                if(myPointAndLineView.getFindPathSignal() == 0){
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("提示");
                    builder.setMessage("请先寻找车位");
                    builder.setPositiveButton("确定",null);
                    builder.show();
                }
                //否则，就开始导航
                else{
                    myPointAndLineView.simulateGO();
                }
            }
        });

        //点击刷新车位
        getLot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myPointAndLineView.resetLot();//重置车位信息
                myPointAndLineView.getMe().setLotOccupying(-1);
                myPointAndLineView.setFindPathSignal(0);
                myPointAndLineView.invalidate();
            }
        });

        //随机重置位置
        resetLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myPointAndLineView.getMe().resetLoc();
                myPointAndLineView.setFindPathSignal(0);
                myPointAndLineView.invalidate();
            }
        });

        //随机车位
        randomCars.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myPointAndLineView.randomCars();
                myPointAndLineView.setFindPathSignal(0);
                myPointAndLineView.resetLot();//重置车位信息
                myLotView.invalidate();
            }
        });

        //开始蓝牙
        bluetoothStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startIntent = new Intent(MainActivity.this, BluetoothService.class);
                startService(startIntent);//启动服务
            }
        });

        //结束蓝牙
        bluetoothStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent stopIntent = new Intent(MainActivity.this, BluetoothService.class);
                stopService(stopIntent);

            }
        });


        //注册广播接收器
        receiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.example.servicetest.RECEIVER");
        MainActivity.this.registerReceiver(receiver, filter);





    }

    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            coordinate[0] = intent.getIntExtra("coordinate[0]", 0);
            coordinate[1] = intent.getIntExtra("coordinate[1]", 0);
            myPointAndLineView.setMe((float)coordinate[0],(float)coordinate[1]); //重设用户点的位置
            myPointAndLineView.invalidate();
            Toast.makeText(MainActivity.this, "您的位置已更新", Toast.LENGTH_SHORT).show();
            Log.d("outout", " " + coordinate[0] + "," + coordinate[1]);

        }
    }

    //疑似缩放功能
    private class TouchListener implements View.OnTouchListener {

        int length = 68;int halfLength = 34;//车位轮廓在地图上长68单位
        int width = 28;int halfWidth = 14;//宽28单位

        /** 记录是拖拉照片模式还是放大缩小照片模式 */
        private int mode = 0;// 初始状态
        /** 拖拉照片模式 */
        private static final int MODE_DRAG = 1;
        /** 放大缩小照片模式 */
        private static final int MODE_ZOOM = 2;

        /** 用于记录开始时候的坐标位置 */
        private PointF startPoint = new PointF();
        /** 用于记录拖拉图片移动的坐标位置 */
        private Matrix matrix = new Matrix();
        private Matrix lotsMatrix = new Matrix();
        /** 用于记录图片要进行拖拉时候的坐标位置 */
        private Matrix currentMatrix = new Matrix();

        private Matrix currentLotsMatrix = new Matrix();

        /** 两个手指的开始距离 */
        private float startDis;
        /** 两个手指的中间点 */
        private PointF midPoint;
        /** 放大倍数 */
        @Override
        public boolean onTouch(View v, MotionEvent event){
            /** 通过与运算保留最后八位 MotionEvent.ACTION_MASK = 255 */
            switch(event.getAction() & MotionEvent.ACTION_MASK) {
                //手指下压屏幕
                case MotionEvent.ACTION_DOWN:
                    mode = MODE_DRAG;
                    //记录ImageView当前移动位置
                    currentMatrix.set(map.getImageMatrix());
                    currentLotsMatrix.set(myLotView.getImageMatrix());
                    startPoint.set(event.getX(), event.getY());
                    break;
                // 手指在屏幕上移动，该事件会被不断触发
                case MotionEvent.ACTION_MOVE:
                    //拖拉图片
                    if (mode == MODE_DRAG) {
                        float dx = event.getX() - startPoint.x;
                        float dy = event.getY() - startPoint.y;
                        //在没有移动之前的位置上进行移动
                        //Log.i("drag_test","drag");
                        matrix.set(currentMatrix);
                        matrix.postTranslate(dx, dy);
                        lotsMatrix.set(currentLotsMatrix);
                        lotsMatrix.postTranslate(dx,dy);
//                        for(MyRectF lot : lots){
//                            lot.set(lot.left - startPoint.x,lot.top - startPoint.y,lot.right-startPoint.x,lot.bottom-startPoint.y);
//                        }
//                        myLotView.invalidate();

                    }
                    //放大缩小图片
                    else if (mode == MODE_ZOOM) {
                        float endDis = distance(event);//结束距离
                        //Log.i("zoom_test","zoom");
                        if (endDis > 10f) {
                            scale = endDis / startDis;
                            matrix.set(currentMatrix);
                            matrix.postScale(scale, scale, midPoint.x, midPoint.y);
                        }
                    }
                    break;
                //手指离开屏幕
                case MotionEvent.ACTION_UP:
                    // 当触点离开屏幕，但是屏幕上还有触点(手指)
                case MotionEvent.ACTION_POINTER_UP:
                    mode = 0;
                    break;
                // 当屏幕上已经有触点(手指)，再有一个触点压下屏幕
                case MotionEvent.ACTION_POINTER_DOWN:
                    //Log.i("mode_drag","mode_drag");
                    mode = MODE_ZOOM;
                    /** 计算两个手指间的距离 */
                    startDis = distance(event);
                    if (startDis > 10f) { // 两个手指并拢在一起的时候像素大于10
                        midPoint = mid(event);
                        //记录当前ImageView的缩放倍数
                        currentMatrix.set(map.getImageMatrix());
                    }
                    break;

            }
            map.setImageMatrix(matrix);
            myLotView.setImageMatrix(lotsMatrix);
            return true;
        }
        /** 计算两个手指间的距离 */
        private float distance(MotionEvent event) {
            float dx = event.getX(1) - event.getX(0);
            float dy = event.getY(1) - event.getY(0);
            /** 使用勾股定理返回两点之间的距离 */
            return (float)Math.sqrt(dx * dx + dy * dy);
        }

        /** 计算两个手指间的中间点 */
        private PointF mid(MotionEvent event) {
            float midX = (event.getX(1) + event.getX(0)) / 2;
            float midY = (event.getY(1) + event.getY(0)) / 2;
            return new PointF(midX, midY);
        }
    }
}
