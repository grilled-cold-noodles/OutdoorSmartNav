package com.example.magi.map;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

//方向传感器类
public class MyOrientationListener implements SensorEventListener {
    private Context context;
    private SensorManager sensorManager;
    private Sensor sensor;
    private float lastX ;

    public MyOrientationListener(Context context)
    {
        this.context = context;
    }

    public interface OnOrientationListener {
        void onOrientationChanged(float x);
    }

    private OnOrientationListener onOrientationListener;

    public void start() {
        sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE); //获得传感器管理器
        if (sensorManager != null) {
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION); //获得方向传感器
        }
        if (sensor != null)
        {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI); //注册
        }
    }

    public void stop() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) { //接受方向感应器的类型
            float x = event.values[SensorManager.DATA_X]; //这里我们可以得到数据，然后根据需要来处理
            if( Math.abs(x- lastX) > 1.0 ) {
                onOrientationListener.onOrientationChanged(x);
            }
            lastX = x ;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void setOnOrientationListener(OnOrientationListener onOrientationListener) {
        this.onOrientationListener = onOrientationListener ;
    }
}
