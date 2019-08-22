package cn.mcf.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;

import java.io.IOException;
import java.util.Arrays;
import java.util.Queue;
import java.util.LinkedList;
import java.util.ArrayList;
import java.lang.Thread;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by McF on 2018/9/13.
 */

public class MyPointAndLineView extends AppCompatImageView {
    //所有导航点
    private ArrayList<MapPointAndLine> mapPointAndLines;


    //存储A*算法中每个选取的点的父节点
    private int[] chosenPoints;

    //用户点
    private Self me;

    //目的地车位序号
    private int destination;

    //目的地车位对应的导航点序号
    private int endNumber;

    //模拟导航中用户点预计到达的下一个导航点的序号
    private int first;
    private Queue<Integer> queue = new LinkedList<Integer>();

    //是否已经得出路径信息的信号，绘图时使用
    private int findPathSignal;

    //车位视图类，连接导航点和车位时使用
    private MyLotView myLotView;

    //发送反馈信息的目的地
    private Handler myHandler;

    //随机改变的车位信息
    private ArrayList<Integer> number1;
    private int endLotNumber;


    public static final int SIMULATIONEND = 1;//导航结束信号
    public static final int ALL_OCCUPIED = 2;//车位全满信号
    public static final int RANDOM_CARS_OK = 7;

    public boolean flag = true;


    public MyPointAndLineView(Context context) {
        super(context);
        mapPointAndLines = new ArrayList<>();
        me = new Self();
        destination = 0;
    }

    public MyPointAndLineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mapPointAndLines = new ArrayList<>();
        me = new Self(1150, 400, 0);
        destination = 0;
    }


    public Self getMe() {
        return me;
    }

    public void setMyLotView(MyLotView myLotView) {
        this.myLotView = myLotView;
    }

    public void setMyHandler(Handler myHandler) {
        this.myHandler = myHandler;
    }

    public void setFindPathSignal(int signal) {
        this.findPathSignal = signal;
    }

    public int getFindPathSignal() {
        return findPathSignal;
    }

    //重置车位信息
    public void resetLot() {
//        for(MyRectF lot:myLotView.getLots())
//            lot.resetState();
        myLotView.getParkLots();//向服务器获取停车场车位信息
    }

    //模拟导航，实现逻辑为 若用户点与终点的坐标差不在5个单位以内，将用户点的X、Y坐标分别向下一个路径点靠近2单位，并重新绘制路径
    public void simulateGO() {
        new Thread(myRunnable).start();
    }

    public Runnable myRunnable = new Runnable() {
        @Override
        public void run() {
            while (Math.abs(me.getViewX() - mapPointAndLines.get(endNumber - 1).getViewX()) > 5
                    || Math.abs(me.getViewY() - mapPointAndLines.get(endNumber - 1).getViewY()) > 5) {
                Log.i("checkcheck", me.getViewX() + " " + me.getViewY());
                for (MapPointAndLine next : me.getNext())
                    Log.i("checkcheck", Integer.toString(next.getNumber()));
                try {
                    while (chosenPoints == null)
                        Thread.sleep(100);
                    Log.e("firstfirst", String.valueOf(first));
                    //int myfirst = queue.poll();
                    //Log.e("myfirst",""+myfirst);
                    if (Math.abs(me.getViewX() - mapPointAndLines.get(first - 1).getViewX()) > 0) {
                        float dX = me.getViewX() - mapPointAndLines.get(first - 1).getViewX();  //x坐标差
                        me.setViewX(me.getViewX() + (-2) * dX / Math.abs(dX));  //x方向向终点靠近2单位
                    }
                    if (Math.abs(me.getViewY() - mapPointAndLines.get(first - 1).getViewY()) > 0) {
                        float dY = me.getViewY() - mapPointAndLines.get(first - 1).getViewY();  //y 坐标差
                        me.setViewY(me.getViewY() + (-2) * dY / Math.abs(dY));  //y方向向终点靠近2单位
                    }
                    postInvalidate();
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (me.getLotOccupying() == -1) {// 未停，将要停
                me.setLotOccupying(destination);  //设置好 目的地 的车位序号
                myLotView.getLots().get(destination - 1).setState(MyRectF.OCCUPIED);  //状态变为“占据”
            } else {   //停过了
                me.setLotOccupying(-1);  //将要空闲
                myLotView.getLots().get(destination - 1).setState(MyRectF.AVAILABLE);  //状态变为“空闲”
            }
            Message msg = Message.obtain(myHandler);
            msg.what = SIMULATIONEND;
            msg.sendToTarget();
        }
    };

    //A*公式 f(n) = g(n) + h(n)
    //计算h(n)
    public int calH(MapPointAndLine self, MapPointAndLine end) {
        return (int) Math.abs(self.getViewX() - end.getViewX() + self.getViewY() - end.getViewY());
    }

    //计算 g(n) 和 g(n+1) 的差值
    public int calDg(MapPointAndLine parent, MapPointAndLine self) {
        return (int) Math.abs(self.getViewX() - parent.getViewX() + self.getViewY() - parent.getViewY());
    }

    //A*算法 初始激活点为用户点，每次选取激活点中f(n)最小的，将其加入选取点列表，并将它的邻接点加入激活点列表，周而复始直至终点加入选取点列表
    public void AStar(int[] parent, ArrayList<MapPointAndLine> checking, int[] fForStar, int[] gForStar, MapPointAndLine end) {
        while (checking.size() != 0) {
            //循环，找到下一步的导航点
            MapPointAndLine best = checking.get(0);  //checking是导航点
            for (MapPointAndLine inCheck : checking) {
                if (fForStar[inCheck.getNumber()] < fForStar[best.getNumber()]) {
                    best = inCheck;
                }
            }
            checking.remove(best);
            //终点加入选取点列表
            if (best == end) {
                return;
            }
            for (MapPointAndLine bestNext : best.getNext()) {
                if (fForStar[bestNext.getNumber()] == 0) {
                    gForStar[bestNext.getNumber()] = gForStar[best.getNumber()] + calDg(best, bestNext);
                    fForStar[bestNext.getNumber()] = gForStar[bestNext.getNumber()] + calH(bestNext, end);
                    checking.add(bestNext);
                    Log.e("getNumber", "" + best.getNumber());
                    parent[bestNext.getNumber()] = best.getNumber();
                }
            }
        }
    }

    //跟距用户点当前位置配置用户点的邻接点 选取原则为：选取上下左右方向各一个距离最近的点
    public void findNearestOfMe() {
        if (me.getNext().size() > 0)
            me.getNext().clear();
        for (MapPointAndLine near : mapPointAndLines) {
            if (Math.abs(near.getViewX() - me.getViewX()) < 5 && Math.abs(near.getViewY() - me.getViewY()) < 168) {
                MapPointAndLine nearestY = new MapPointAndLine(-1, -1, -1);
                float disYIn, disYOut;
                disYIn = 0;
                disYOut = me.getViewY() - near.getViewY();
                if (disYOut > 0) {
                    for (MapPointAndLine findNearestYOfMe : me.getNext()) {
                        if (Math.abs(findNearestYOfMe.getViewX() - me.getViewX()) < 5) {
                            disYIn = me.getViewY() - findNearestYOfMe.getViewY();
                            if (disYIn > 0)
                                nearestY = findNearestYOfMe;
                        }
                    }
                    if (nearestY.getNumber() == -1)
                        me.getNext().add(near);
                    else if (disYIn > disYOut) {
                        me.getNext().remove(nearestY);
                        me.getNext().add(near);
                    }
                } else if (disYOut < 0) {
                    for (MapPointAndLine findNearestYOfMe : me.getNext()) {
                        if (Math.abs(findNearestYOfMe.getViewX() - me.getViewX()) < 5) {
                            disYIn = me.getViewY() - findNearestYOfMe.getViewY();
                            if (disYIn < 0)
                                nearestY = findNearestYOfMe;
                        }
                    }
                    if (nearestY.getNumber() == -1)
                        me.getNext().add(near);
                    else if (disYIn < disYOut) {
                        me.getNext().remove(nearestY);
                        me.getNext().add(near);
                    }
                }
            } else if (Math.abs(near.getViewY() - me.getViewY()) < 5 && Math.abs(near.getViewX() - me.getViewX()) < 168) {
                MapPointAndLine nearestX = new MapPointAndLine(-1, -1, -1);
                float disXIn, disXOut;
                disXIn = 0;
                disXOut = me.getViewX() - near.getViewX();
                if (disXOut > 0) {
                    for (MapPointAndLine findNearestXOfMe : me.getNext()) {
                        if (Math.abs(findNearestXOfMe.getViewY() - me.getViewY()) < 5) {
                            disXIn = me.getViewX() - findNearestXOfMe.getViewX();
                            if (disXIn > 0)
                                nearestX = findNearestXOfMe;
                        }
                    }
                    if (nearestX.getNumber() == -1)
                        me.getNext().add(near);
                    else if (disXIn > disXOut) {
                        me.getNext().remove(nearestX);
                        me.getNext().add(near);
                    }
                } else if (disXOut < 0) {
                    for (MapPointAndLine findNearestXOfMe : me.getNext()) {
                        if (Math.abs(findNearestXOfMe.getViewY() - me.getViewY()) < 5) {
                            disXIn = me.getViewX() - findNearestXOfMe.getViewX();
                            if (disXIn < 0)
                                nearestX = findNearestXOfMe;
                        }
                    }
                    if (nearestX.getNumber() == -1)
                        me.getNext().add(near);
                    else if (disXIn < disXOut) {
                        me.getNext().remove(nearestX);
                        me.getNext().add(near);
                    }
                }
            }
        }
    }

    //将上述函数整合实现路径规划，参数为终点导航点序号
    public void findPath(int number) {
        int[] fForAStar = new int[114];//f(n)数组
        int[] gForAStar = new int[114];//g(n)数组
        int[] parent = new int[114];//父节点数组
        ArrayList<MapPointAndLine> checking = new ArrayList<>();
        checking.add(me);
        gForAStar[me.getNumber()] = 0;
        fForAStar[me.getNumber()] = calH(me, mapPointAndLines.get(number - 1));
        findNearestOfMe();
        AStar(parent, checking, fForAStar, gForAStar, mapPointAndLines.get(number - 1));
        chosenPoints = parent;
    }

    public void findPath(int number, ArrayList<Integer> availableLotsDaoHang) {
        int[] parent = new int[20];//父节点数组,原114
        Queue<MapPointAndLine> checking = new LinkedList<>();
        checking.offer(me);
        findNearestOfMe();
        BFS(parent, checking, availableLotsDaoHang);
        chosenPoints = parent;
    }

    public void BFS(int[] parent, Queue<MapPointAndLine> checking, ArrayList<Integer> availableLotsDaoHang) {
        while (checking.size() != 0) {
            MapPointAndLine mrf = checking.poll();
            if (availableLotsDaoHang.contains(mrf.getNumber())) {
                endNumber = mrf.getNumber();
                for (MyRectF des : myLotView.getLots()) {
                    if (des.getMapPoint() == endNumber && des.getState() == MyRectF.AVAILABLE) {
                        destination = des.getNumber();
                        break;
                    }
                }
                return;
            }
            for (MapPointAndLine next : mrf.getNext()) {
                checking.offer(next);
                parent[next.getNumber()] = mrf.getNumber();
            }
        }
    }

    //寻找最近的空闲车位
    public void findLot() {

        //找车，找到后，直接返回
        if (me.getLotOccupying() != -1) {  //-1为未占据
            findPath(endNumber);  //利用A*算法选路
            return;
        }

        destination = 0;
        endNumber = 0;
//        float distance = 0;
//        for(MyRectF lot:myLotView.getLots()){
//            if(lot.getState() == MyRectF.AVAILABLE){
//                if(destination == 0){
//                    destination = lot.getNumber();
//                    endNumber = lot.getMapPoint();
//                    distance = Math.abs(me.getViewX() - mapPointAndLines.get(lot.getMapPoint() - 1).getViewX()) +
//                            Math.abs(me.getViewY() - mapPointAndLines.get(lot.getMapPoint() - 1).getViewY());
//                }
//                else{
//                    float checkDistance = Math.abs(me.getViewX() - mapPointAndLines.get(lot.getMapPoint() - 1).getViewX()) +
//                            Math.abs(me.getViewY() - mapPointAndLines.get(lot.getMapPoint() - 1).getViewY());
//                    if(checkDistance < distance){
//                        destination = lot.getNumber();
//                        endNumber = lot.getMapPoint();
//                        distance = checkDistance;
//                    }
//                }
//            }
//        }
        ArrayList<Integer> availableLotsDaoHang = new ArrayList<>();
//        //对全部车位进行搜索
//        for (MyRectF lot : myLotView.getLots()) {
//            if (lot.getState() == MyRectF.AVAILABLE)
//                availableLotsDaoHang.add(lot.getMapPoint());  //加入数组
//        }

        for(int i = 0; i <12; i++){
            if(myLotView.getLots().get(i).getState()==MyRectF.AVAILABLE){
                availableLotsDaoHang.add(myLotView.getLots().get(i).getMapPoint());  //加入数组
            }
        }

        if (availableLotsDaoHang.size() == 0) {  //数组为空，说明车位已满
            Message msg = Message.obtain(myHandler);
            msg.what = ALL_OCCUPIED;
            msg.sendToTarget();
        } else {
            findPath(endNumber, availableLotsDaoHang);
        }
    }

    //初始化导航点和车位的坐标，并配置导航点的邻接点
    public void init() {
        int number = 1;
        int lotNumber = 1;//分别为导航点序号和车位序号
        int length = 68;
        int halfLength = 34;//车位轮廓在地图上长68单位
        int width = 28;
        int halfWidth = 14;//宽28单位

        mapPointAndLines.add(new MapPointAndLine(700, 460, number));
        myLotView.getLots().add(new MyRectF(700 - halfLength, 500 + halfWidth, 700 + halfLength, 500 - halfWidth, lotNumber, number));
        number++;
        lotNumber++;
        mapPointAndLines.add(new MapPointAndLine(550, 460, number));
        myLotView.getLots().add(new MyRectF(550 - halfLength, 500 + halfWidth, 550 + halfLength, 500 - halfWidth, lotNumber, number));
        number++;
        lotNumber++;
        mapPointAndLines.add(new MapPointAndLine(400, 460, number));
        myLotView.getLots().add(new MyRectF(400 - halfLength, 500 + halfWidth, 400 + halfLength, 500 - halfWidth, lotNumber, number));
        number++;
        lotNumber++;
        mapPointAndLines.add(new MapPointAndLine(300, 460, number));
        number++;//拐点
        //以上 4排 三个+导航点
        mapPointAndLines.add(new MapPointAndLine(700, 380, number));
        myLotView.getLots().add(new MyRectF(700 - halfLength, 420 + halfWidth, 700 + halfLength, 420 - halfWidth, lotNumber, number));
        number++;
        lotNumber++;
        mapPointAndLines.add(new MapPointAndLine(550, 380, number));
        myLotView.getLots().add(new MyRectF(550 - halfLength, 420 + halfWidth, 550 + halfLength, 420 - halfWidth, lotNumber, number));
        number++;
        lotNumber++;
        mapPointAndLines.add(new MapPointAndLine(400, 380, number));
        myLotView.getLots().add(new MyRectF(400 - halfLength, 420 + halfWidth, 400 + halfLength, 420 - halfWidth, lotNumber, number));
        number++;
        lotNumber++;
        mapPointAndLines.add(new MapPointAndLine(300, 380, number));
        number++;//拐点
        //以上 3排 三个+导航点
        mapPointAndLines.add(new MapPointAndLine(700, 300, number));
        myLotView.getLots().add(new MyRectF(700 - halfLength, 340 + halfWidth, 700 + halfLength, 340 - halfWidth, lotNumber, number));
        number++;
        lotNumber++;
        mapPointAndLines.add(new MapPointAndLine(550, 300, number));
        myLotView.getLots().add(new MyRectF(550 - halfLength, 340 + halfWidth, 550 + halfLength, 340 - halfWidth, lotNumber, number));
        number++;
        lotNumber++;
        mapPointAndLines.add(new MapPointAndLine(400, 300, number));
        myLotView.getLots().add(new MyRectF(400 - halfLength, 340 + halfWidth, 400 + halfLength, 340 - halfWidth, lotNumber, number));
        number++;
        lotNumber++;
        mapPointAndLines.add(new MapPointAndLine(300, 300, number));
        number++;//拐点
        //以上 2排 三个+导航点
        mapPointAndLines.add(new MapPointAndLine(700, 220, number));
        myLotView.getLots().add(new MyRectF(700 - halfLength, 260 + halfWidth, 700 + halfLength, 260 - halfWidth, lotNumber, number));
        number++;
        lotNumber++;
        mapPointAndLines.add(new MapPointAndLine(550, 220, number));
        myLotView.getLots().add(new MyRectF(550 - halfLength, 260 + halfWidth, 550 + halfLength, 260 - halfWidth, lotNumber, number));
        number++;
        lotNumber++;
        mapPointAndLines.add(new MapPointAndLine(400, 220, number));
        myLotView.getLots().add(new MyRectF(400 - halfLength, 260 + halfWidth, 400 + halfLength, 260 - halfWidth, lotNumber, number));
        number++;
        mapPointAndLines.add(new MapPointAndLine(300, 220, number));//拐点
        //以上 1排 三个+导航点

        //配置邻接点
        int i;
        for (i = 0; i < 3; i++) {
            mapPointAndLines.get(i).addNext(mapPointAndLines.get(i + 1));
            mapPointAndLines.get(i + 1).addNext(mapPointAndLines.get(i));

            mapPointAndLines.get(i + 4).addNext(mapPointAndLines.get(i + 5));
            mapPointAndLines.get(i + 5).addNext(mapPointAndLines.get(i + 4));

            mapPointAndLines.get(i + 8).addNext(mapPointAndLines.get(i + 9));
            mapPointAndLines.get(i + 9).addNext(mapPointAndLines.get(i + 8));

            mapPointAndLines.get(i + 12).addNext(mapPointAndLines.get(i + 13));
            mapPointAndLines.get(i + 13).addNext(mapPointAndLines.get(i + 12));
        }
        for (i = 3; i <= 11; i += 4) {
            mapPointAndLines.get(i).addNext(mapPointAndLines.get(i + 4));
            mapPointAndLines.get(i + 4).addNext(mapPointAndLines.get(i));
        }

        //日志打印车位总个数:146
        Log.d("allLots", String.valueOf(lotNumber));
        //向服务器获取车位信息
        myLotView.getParkLots();
    }

    public void setMe(float x,float y){
        me.setViewX(x);
        me.setViewY(y);
    }

    //绘制【路径】和【用户点的圈圈】
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();//创建“画笔”
        paint.setStrokeWidth((float) 5.0);//设置笔刷的粗细度
        paint.setColor(Color.RED);//设置绘制的颜色
        if (findPathSignal == 1) {
            //从终点开始倒推绘制路线(起点连向终点)
            if (destination != 0) {
                findPath(endNumber);//参数为终点导航点序号
                int now = endNumber;
                Log.e("nownow", "" + Arrays.toString(chosenPoints));
                Log.e("nownow", "endnumber:" + now);

                while (chosenPoints[now] != 0) {
                    //由起始坐标画向终止坐标
                    canvas.drawLine(mapPointAndLines.get(now - 1).getViewX(), mapPointAndLines.get(now - 1).getViewY(),
                            mapPointAndLines.get(chosenPoints[now] - 1).getViewX(), mapPointAndLines.get(chosenPoints[now] - 1).getViewY(), paint);
                    now = chosenPoints[now];
                    if (flag) {
                        queue.offer(now);
                        flag = false;
                    }

                    Log.e("nownow", " " + now);
                }
                //first表示下一导航点
                first = now;
                //绘制连接用户点与第一个路径导航点的直线（从用户连向起点）
                canvas.drawLine(me.getViewX(), me.getViewY(), mapPointAndLines.get(now - 1).getViewX(), mapPointAndLines.get(now - 1).getViewY(), paint);

                //绘制连接终点导航点和对应的车位的直线（从终点连向车位）
                canvas.drawLine(myLotView.getLots().get(destination - 1).centerX(), myLotView.getLots().get(destination - 1).centerY(), mapPointAndLines.get(endNumber - 1).getViewX(), mapPointAndLines.get(endNumber - 1).getViewY(), paint);
            }
        }
        //到了以后在车位里画一个圈圈
        if (me.getLotOccupying() != -1) {
            canvas.drawCircle(myLotView.getLots().get(me.getLotOccupying() - 1).centerX(), myLotView.getLots().get(me.getLotOccupying() - 1).centerY(), 10, paint);
        }
        //用户所在位置的圆圈
        canvas.drawCircle(me.getViewX(), me.getViewY(), 10, paint);
    }


    //yj所加：
    //寻找isAvailable
    public int findLotNumber(int number)//从mapPoint中获取车位信息Lotnumber
    {
        for (MyRectF lot1 : myLotView.getLots()) {
            if (lot1.getMapPoint() == number) {
                return lot1.getNumber();
            }
        }
        return 200;
    }

    public int setIsAvailable(int endNumber) {
        if (endLotNumber == 200) return -1;//如果找到了车位信息
        else {
            return endLotNumber;
        }
    }

    public void randomCars() {
        new Thread(randomCarsRunnable).start();
    }
    public Runnable randomCarsRunnable = new Runnable() {
        @Override
        public void run() {
            try {//get
                number1 = new ArrayList<Integer>();
                //随机选取几个点setIsAvailable
                for (int i = 1; i <= 20; i++) {
                    number1.add(findLotNumber(i));
                    OkHttpClient client = new OkHttpClient();
                    RequestBody paramsBody = new FormBody.Builder().add("needChange", number1.get(i - 1).toString()).build();
                    Request request = new Request.Builder().url("http://" + netInf.ipAli + ':' + netInf.portAli + "/changeLotAvail")
                            .post(paramsBody)//传参数、文件或者混合，改一下就行请求体就行
                            .build();
                    Call call = client.newCall(request);

                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        Log.i("getResponse", response.body().toString());
                    } else {
                        throw new IOException("Unexpected code " + response);
                    }
                }
                Message msg = Message.obtain(myHandler);
                msg.what = RANDOM_CARS_OK;
                msg.sendToTarget();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
}