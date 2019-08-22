package cn.mcf.myapplication;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

public class BluetoothService extends Service {

    public static final String TAG = "MyService";
    private BluetoothBinder mBinder = new BluetoothBinder();
    private BluetoothAdapter bluetoothAdapter;//本地蓝牙适配器
    public static final int AP = 6;//全部蓝牙个数
    private String[] allRssiName = new String[AP];//全部蓝牙名称
    private String[] rssiArrName = new String[3];//蓝牙名称(储存搜索到的前三个)
    private double[] rssiArrDate = new double[3];//单次测量的Rssi数据
    private int[] coordinate;//knn匹配后的坐标信息
    private int searchNum = 0;//搜索到前3个设备即停止
    private boolean kalmanFlag = true;//用于设置kalman滤波的初值
    private Kalman[] kalman = new Kalman[2];//x,y坐标分别用kalman过滤
    private Intent intentPro = new Intent("com.example.servicetest.RECEIVER");//用于广播的发送


    class BluetoothBinder extends Binder {
        public void startBluetooth() {
            Log.d(TAG, "start bluetooth");
        }

        public int[] getResult() {
            Log.d(TAG, "get result");
            return coordinate;
        }
    }

    public void activeBluetooth() {
        Log.d(TAG, "蓝牙模块启动");
        initName();
        initRssi();//初始化Rssi数据
        initBluetooth();//初始化
        openBluetooth();//开启
        registerBoard();//注册
    }


    public void initName() {
        allRssiName[0] = "EWF1341805A";
        allRssiName[1] = "EWD8DA94E6E";
        allRssiName[2] = "EWC6093F0E5";
        allRssiName[3] = "EWC57B49E2E";
        allRssiName[4] = "EWCA5F5B3B9";
        allRssiName[5] = "EWF7588FC2A";
        kalman[0] = new Kalman(1, 5);
        kalman[1] = new Kalman(1, 5);
    }

    public void initRssi() {
        rssiArrName[0] = "null";
        rssiArrName[1] = "null";
        rssiArrName[2] = "null";
        searchNum = 0;
    }

    private void initBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Log.d(TAG, "设备不支持蓝牙");
        } else {
            Log.d(TAG, "设备支持蓝牙");
        }
    }

    private void openBluetooth() {
        boolean openResult = bluetoothAdapter.enable();
        if (openResult) {
            Log.d(TAG, "蓝牙设备启动");
        } else {
            Log.d(TAG, "蓝牙设备未启动");
        }
    }

    public final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String name;//存放当前搜索到的设备名称
            short rssi;//获取的rssi 负值
            double Rssi;//rssi 正值
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    name = device.getName();

                    for (int numAP = 0; numAP < AP; numAP++) {
                        if (searchNum >= 3) {
                            bluetoothAdapter.cancelDiscovery();
                        } else if (allRssiName[numAP].equals(name)) {
                            rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                            Rssi = Math.abs(rssi);
                            Log.d(TAG, "录入: " + name + " = " + Rssi);
                            rssiArrName[searchNum] = name;
                            rssiArrDate[searchNum] = Rssi;
                            searchNum++;//优化搜索，但检测到3个设备时停止搜索
                        }
                    }

                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d(TAG, "全部扫描完成");

                //KNN
                KNN knnKalman = new KNN();
                try {
                    InputStream is = getAssets().open("rssi.txt");
                    knnKalman.init(is);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                knnKalman.Euclid(rssiArrName, rssiArrDate);
                coordinate = knnKalman.minDistance();

                Log.d(TAG, coordinate[0] + "-" + coordinate[1]);

                //Kalman
                if (kalmanFlag = true) {
                    kalman[0].setStartValue(coordinate[0]);
                    kalman[1].setStartValue(coordinate[1]);
                    kalmanFlag = false;
                } else {
                    coordinate[0] = (int) kalman[0].kalmanFilter(coordinate[0]);
                    coordinate[1] = (int) kalman[1].kalmanFilter(coordinate[1]);
                }

                Log.d(TAG, coordinate[0] + "," + coordinate[1]);


                intentPro.putExtra("coordinate[0]", coordinate[0]);
                intentPro.putExtra("coordinate[1]", coordinate[1]);
                sendBroadcast(intentPro);

                bluetoothAdapter.cancelDiscovery();//关闭广播

                startDiscovery();
                context.unregisterReceiver(this);
            }
        }
    };

    private void registerBoard() {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(broadcastReceiver, filter);
    }

    public void startDiscovery() {
        initRssi();
        Log.d(TAG, "开始广播搜索");
        bluetoothAdapter.startDiscovery();

    }

    public BluetoothService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    //创建服务的时候调用
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
    }

    //每次服务启动时调用
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        activeBluetooth();
        startDiscovery();
        return super.onStartCommand(intent, flags, startId);
    }

    //服务销毁时调用
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }


}
