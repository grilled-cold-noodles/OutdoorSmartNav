package com.example.magi.map;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.DrivingRouteOverlay;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.navisdk.adapter.BNCommonSettingParam;
import com.baidu.navisdk.adapter.BNOuterTTSPlayerCallback;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BNaviSettingManager;
import com.baidu.navisdk.adapter.BaiduNaviManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    //显示控件变量
    private TextView tv_weather; //显示天气信息（某些版本安卓显示不出来）
    private MapView bmapView; //百度地图控件
    private TextView tv_info_name; //底部信息栏名称
    private TextView tv_info_address; //底部信息栏地址
    private ListView lv_searchResult; //搜索结果列表
    private LinearLayout ll_searchResult; //包含停车场搜索结果列表和关闭列表按钮的布局
    private AlertDialog.Builder mBuilder; //确认框
    private LinearLayout ll_detail; //停车场详情框
    private Button btn_detail; //停车场详情按钮
    private Button btn_go; //导航前往按钮
    private LinearLayout ll_suggestionResult; //包含建议列表和关闭列表按钮的布局
    private ListView lv_suggestionResult; //建议列表
    private InputMethodManager inputManager; //输入法弹出框
    private Button btn_delete; //搜索输入框清空按钮
    private EditText et_search; //搜索输入框
    private Button btn_speak;

    //控制器
    private BaiduMap mBaiduMap; //百度地图控件控制器
    private LocationClient mLocationClient; //定位控制器
    private GeoCoder mGeoCoder; //地理编码控制器
    private MyOrientationListener myOrientationListener; //方向控制器
    private RoutePlanSearch mRoutePlanSearch; //路线规划控制器

    //自定义变量
    private boolean isSatelliteOpen = false; //卫星地图是否开启
    private boolean isTrafficOpen = false; //实时交通是否开启
    private long firstTime = 0; //记录第一次按下返回键
    private boolean isFirstLocate = true; //是否是第一次定位
    private String weather; //天气信息
    private String mSdcardPath; //SD卡路径
    private BDLocation myLocation; //我的位置
    private PoiList parkPoiList; //保存从服务器返回的停车场搜索结果
    private int currPage = 0; //当前搜索结果页数
    private boolean isLoadData = false; //是否需要加载下一页数据
    private List<Map<String, Object>> parkList; //停车场列表数据
    private int currItem = 0; //列表当前显示的条目位置
    private Storage touchPoint; //保存目标地址信息
    private Overlay mOverlay; //点击位置覆盖物
    private PoiList suggestionPoiList; //保存从服务器返回的建议搜索结果
    private List<Map<String, Object>> suggestionList; //建议列表数据
    private LatLng startLatlng; //地图状态改变前中心坐标
    private LatLng endLatlng; //地图状态改变后中心坐标
    private boolean isNeedSearchPark; //地图状态改变后是否需要搜索停车场
    private DrivingRouteOverlay mDrivingRouteOverlay; //驾车路线覆盖物
    private ArrayList<CarsInfo> carsInfos;  //从数据库获取的车辆信息
    private ArrayList<CarsInfo> testCars = new ArrayList<>();
    private ArrayList<Marker> markers = new ArrayList<>();  //存储各个marker，重绘时便于清除
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();  // 用HashMap存储听写结果

    //Handlers
    private Handler weatherHandler;
    private Handler searchHandler;
    private Handler suggestionHandler;
    private Handler netHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext()); //在使用百度地图控件前必须先初始化
        setContentView(R.layout.activity_main);

        initMembers();  //从布局获取控件等
        initMapView(isSatelliteOpen, isTrafficOpen);   //参数：是否开启卫星图、是否开启交通图
        //initCarsInfo();

        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());   //注册监听函数
        mLocationClient.setLocOption(getLocationOption());
        mLocationClient.start();
        initOrientationListener();
        mGeoCoder = GeoCoder.newInstance();  //GeoCoder类将一段地址描述转换为一个坐标
        mRoutePlanSearch = RoutePlanSearch.newInstance();  //路径规划搜索接口
        getWeatherHandler();
        getSearchHandler();
        getSuggestionHandler();
        mDrivingRouteOverlay = new DrivingRouteOverlay(mBaiduMap);
        if (initDir()) { //初始化语音缓存路径
            initNaviPath();
        }
        initSpeech();  //初始化讯飞语音
    }




    @Override
    protected void onStart() {
        super.onStart();
        myOrientationListener.start();
        initButtonListener();  //初始化按钮监听
        initListListener();    //初始化列表监听
        initMapListener();     //初始化地图监听
        mBaiduMap.setOnMarkerClickListener(mDrivingRouteOverlay);
        //mBaiduMap.setOnMarkerClickListener(markerListener);
        mBuilder = new AlertDialog.Builder(this);
        mBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        mLocationClient.stop();
        myOrientationListener.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bmapView.onDestroy();
        mGeoCoder.destroy();
        mRoutePlanSearch.destroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bmapView.onResume();
        mLocationClient.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        bmapView.onPause();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_location:
                mLocationClient.start();
                mLocationClient.requestLocation();
                LatLng latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                setMapCenterAndZoom(latLng, 17.0f, false);
                setBottomInfo("我的位置", myLocation.getAddrStr());
                showBottomInfo(true, false);
                saveTouchPoint("我的位置", latLng, myLocation.getAddrStr());
                //searchPark(touchPoint.getLocation());
                getWeather();
                break;
            case R.id.btn_traffic:
                if (!isTrafficOpen) {
                    if (isSatelliteOpen) {
                        initMapView(true, true);
                    } else {
                        initMapView(false, true);
                    }
                    isTrafficOpen = true;
                    showToast("开启实时交通");
                } else {
                    if (isSatelliteOpen) {
                        initMapView(true, false);
                    } else {
                        initMapView(false, false);
                    }
                    isTrafficOpen = false;
                    showToast("关闭实时交通");
                }
                break;
            case R.id.btn_satellite:
                if (!isSatelliteOpen) {
                    if (isTrafficOpen) {
                        initMapView(true, true);
                    } else {
                        initMapView(true, false);
                    }
                    isSatelliteOpen = true;
                } else {
                    if (isTrafficOpen) {
                        initMapView(false, true);
                    } else {
                        initMapView(false, false);
                    }
                    isSatelliteOpen = false;
                }
                break;
            case R.id.btn_close:
                if (ll_searchResult.getVisibility() != View.INVISIBLE) {
                    ll_searchResult.setVisibility(View.INVISIBLE);
                }
                break;
            case R.id.btn_add:
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(new MapStatus.Builder().zoom(mBaiduMap.getMapStatus().zoom + 1).build()));
                break;
            case R.id.btn_minus:
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(new MapStatus.Builder().zoom(mBaiduMap.getMapStatus().zoom - 1).build()));
                break;
            case R.id.btn_empty:
                if (parkList.size() == 0) {
                    showToast("搜索列表结果为空");
                    break;
                }
                mBuilder.setTitle("提示").setMessage("是否清空搜索结果和地图覆盖物？").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ll_searchResult.setVisibility(View.INVISIBLE);
                        ll_suggestionResult.setVisibility(View.INVISIBLE);
                        ll_detail.setVisibility(View.INVISIBLE);
                        mBaiduMap.clear();
                        if (mOverlay != null) {
                            mOverlay.remove();
                        }
                        parkList.clear();
                        showBottomInfo(false, false);
                    }
                }).show();
                break;
            case R.id.btn_list:
                if (parkList.size() == 0) {
                    showToast("搜索结果列表为空");
                    break;
                }
                lv_searchResult.setSelection(currItem);
                ll_searchResult.setVisibility(View.VISIBLE);
                ll_suggestionResult.setVisibility(View.INVISIBLE);
                ll_detail.setVisibility(View.INVISIBLE);
                break;
            case R.id.btn_go:
                if (touchPoint == null || touchPoint.getLocation() == null) {
                    showToast("请先选择目标");
                    break;
                }
                //初始化导航路线
                initBNRoutePlan(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), touchPoint.getLocation(), touchPoint.getName());
                break;
            case R.id.btn_detail:
                showDetail(ll_detail);
                break;
            case R.id.btn_closeDetail:
                ll_detail.setVisibility(View.INVISIBLE);
                break;
            case R.id.btn_search:
                if (((TextView) findViewById(R.id.et_search)).getText().toString().equals("")) {
                    showToast("搜索内容不能为空");
                }
                getSuggestion(et_search.getText().toString().trim());
                inputManager.hideSoftInputFromWindow(et_search.getWindowToken(), 0);
                break;
            case R.id.btn_closeSuggestion:
                ll_suggestionResult.setVisibility(View.INVISIBLE);
                break;
            case R.id.btn_delete:
                et_search.setText("");
                break;
            case R.id.btn_speech:
                //语音输入
                speechInput();
                break;
            case R.id.btn_speak:
                //语音输出
                speechOutput();
                break;
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                long secondTime = System.currentTimeMillis();
                if (secondTime - firstTime > 2000) {
                    Toast.makeText(MainActivity.this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                    firstTime = secondTime;
                    return true;
                } else {
                    System.exit(0);
                }
                break;
        }
        return super.onKeyUp(keyCode, event);
    }

    //显示Toast弹窗提示，持续时间短
    private void showToast(final String str) {
        Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
    }

    //详情页容器，暂存View，进行控件复用
    static class ViewHolder {
        TextView tv_detail_name;
        TextView tv_detail_address;
        TextView tv_detail_type;
        TextView tv_detail_empty;
        TextView tv_detail_total;
        TextView tv_detail_distance;
        TextView tv_detail_price;
    }

    //显示详情页
    private void showDetail(LinearLayout linearLayout) {
        ViewHolder viewHolder = null;
        if (linearLayout.getTag() == null) {
            viewHolder = new ViewHolder();
            viewHolder.tv_detail_name = (TextView) findViewById(R.id.tv_detail_name);
            viewHolder.tv_detail_address = (TextView) findViewById(R.id.tv_detail_address);
            viewHolder.tv_detail_type = (TextView) findViewById(R.id.tv_detail_type);
            viewHolder.tv_detail_empty = (TextView) findViewById(R.id.tv_detail_empty);
            viewHolder.tv_detail_total = (TextView) findViewById(R.id.tv_detail_total);
            viewHolder.tv_detail_distance = (TextView) findViewById(R.id.tv_detail_distance);
            viewHolder.tv_detail_price = (TextView) findViewById(R.id.tv_detail_price);
            linearLayout.setTag(viewHolder);
        }
        viewHolder = (ViewHolder) linearLayout.getTag();
        viewHolder.tv_detail_name.setText(touchPoint.getName());
        viewHolder.tv_detail_address.setText(touchPoint.getAddress());
        viewHolder.tv_detail_type.setText(touchPoint.getStyle());
        viewHolder.tv_detail_empty.setText(touchPoint.getEmpty() + " 个");
        viewHolder.tv_detail_total.setText(touchPoint.getTotal() + " 个");
        if (touchPoint.getDistance() < 1000) {
            viewHolder.tv_detail_distance.setText(touchPoint.getDistance() + "米");
        } else {
            viewHolder.tv_detail_distance.setText(touchPoint.getDistance() / 1000 + "千米");
        }
        viewHolder.tv_detail_price.setText(touchPoint.getTime1() + " 元/时");
        ll_detail.setVisibility(View.VISIBLE);
    }

    //控制底部详情按钮的显示
    private void showBottomInfo(boolean go, boolean detail) {
        if (go) {
            btn_go.setVisibility(View.VISIBLE);
            if (detail) {
                btn_detail.setVisibility(View.VISIBLE);
            } else {
                btn_detail.setVisibility(View.INVISIBLE);
            }
        } else {
            btn_go.setVisibility(View.INVISIBLE);
            btn_detail.setVisibility(View.INVISIBLE);
        }
    }

    //设置底部信息栏
    private void setBottomInfo(String name, String address) {
        tv_info_name.setText(name);
        tv_info_address.setText(address);
    }

    //设置地图中心以及缩放级别
    private void setMapCenterAndZoom(LatLng center, float zoomLevel, boolean isNeedSearchParks) {
        isNeedSearchPark = isNeedSearchParks;
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLngZoom(center, zoomLevel); //设置地图中心以及缩放级别
        mBaiduMap.animateMapStatus(mapStatusUpdate); //动画更新地图
    }

    //保存目标地址信息
    private void saveTouchPoint(String name, LatLng location, String address) {
        touchPoint.setName(name);
        touchPoint.setLocation(location);
        touchPoint.setAddress(address);
    }

    //为touchPoint设置覆盖物
    private void setCenterMarker() {
        if (mOverlay != null) {
            mOverlay.remove();
        }
        //设置中心点图钉图案
        BitmapDescriptor descriptor = BitmapDescriptorFactory.fromBitmap(martix(BitmapFactory.decodeResource(getResources(), R.drawable.icon_center), 200, 200));
        OverlayOptions options = new MarkerOptions().position(touchPoint.getLocation()).icon(descriptor).zIndex(5);
        mOverlay = mBaiduMap.addOverlay(options);
        //showToast(touchPoint.getLocation().latitude+"--"+touchPoint.getLocation().longitude);
    }


    //requestUrl 请求的地址
    public void getRequest(String requestUrl) {
        try {
            Log.d("tracetrace", "getRequest()进入");
            OkHttpClient mOkHttpClient = new OkHttpClient();  //得到OkHttpClient对象
            Request.Builder builder = new Request.Builder();              //构造Request对象
            Request request = builder.get().url(requestUrl).build();
            Call mCall = mOkHttpClient.newCall(request);              //将Request封装为Call
            //开始执行Call
            //Response mResponse= mCall.execute();//同步方法
            Log.d("tracetrace", "66666");
            mCall.enqueue(new Callback() {//异步执行方法
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d("outout", "请求失败");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.d("outout", "请求成功");
                    String result = response.body().string();
                    Log.d("outout", result);
                    //GSON反序列化,通过构造函数来获取
                    Gson gson = new Gson();
                    carsInfos = gson.fromJson(result, new TypeToken<List<CarsInfo>>() {
                    }.getType());
                    Log.d("outout", "carsInfos.size: " + carsInfos.size());
                    for (CarsInfo car : carsInfos) {
                        Log.d("outout", "id: " + car.getId() + " number: " + car.getNumber() + " latitude: " + car.getLatitude() + " longitude: " + car.getLongitude() + " speed: " + car.getSpeed());
                    }
                    generateMarker();
//                    //画没有信息的图钉
//                    BitmapDescriptor carPoint = BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding);
//                    for(CarsInfo car:carsInfos){
//                        Log.d("outout","纬度 "+car.getLatitude());
//                        LatLng latLng = new LatLng(car.getLatitude(),car.getLongitude());
//                        OverlayOptions options = new MarkerOptions().position(latLng).icon(carPoint).zIndex(9);//设置位置、设置图标样式、设置marker所在层级
//                        Marker marker = (Marker) mBaiduMap.addOverlay(options);
//
//                        Bundle mBundle = new Bundle();
//                        mBundle.putInt("id", car.getId());
//
//                        marker.setExtraInfo(mBundle);
//
//                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //向数据库查询车辆信息的线程
    public void showSurroundingCars() {
        Log.d("tracetrace", "getSurroundingCars()进入");
        new Thread(myRunnable).start();
    }

    public Runnable myRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d("tracetrace", "run进入");
            try {
                getRequest("http://" + NetInf.ipAli + ':' + NetInf.portAli + "/getcarsInfo");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    //获取自定义的marker位图
    private Bitmap getViewBitmap(View addViewContent) {
        addViewContent.setDrawingCacheEnabled(true);
        addViewContent.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        addViewContent.layout(0, 0, addViewContent.getMeasuredWidth(), addViewContent.getMeasuredHeight());
        addViewContent.buildDrawingCache();
        Bitmap cacheBitmap = addViewContent.getDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);
        return bitmap;
    }

    //生成自定义位图
    public void generateMarker() {
        //先清除之前的marker
        for (Marker marker : markers) {
            marker.remove();
            Log.d("outout", "清除中...");
        }
        markers.clear();
        for (CarsInfo car : carsInfos) {
            LatLng latLng = new LatLng(car.getLatitude(), car.getLongitude());
            Log.d("meme", myLocation.getLatitude() + "," + myLocation.getLongitude());
            LatLng me = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            //与我相距4公里以内才显示
            //if (getKiloMeter(me, latLng) < 4) {
            View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.mybitmap, null);
            TextView tv_carinfo = (TextView) view.findViewById(R.id.tv_carinfo);
            //            tv_carinfo.setBackgroundResource(R.drawable.corner_view);
            tv_carinfo.setText(new StringBuilder().append(car.getSpeed()).append("km/h"));
            //调用getViewBitmap函数生成位图
            BitmapDescriptor markerIcon = BitmapDescriptorFactory.fromBitmap(getViewBitmap(view));
            //创建一个bundle来辨别各个marker
            Bundle mBundle = new Bundle();
            mBundle.putInt("id", car.getId());
            OverlayOptions oo = new MarkerOptions().position(latLng).icon(markerIcon).zIndex(9).extraInfo(mBundle);//.draggable(true);
            Marker marker = (Marker) mBaiduMap.addOverlay(oo);
            marker.setExtraInfo(mBundle);
            markers.add(marker);

            //}
        }
    }

//    private void showSurroundingCars() {
//        Log.d("tracetrace", "showSurroundingCars()进入");
//        //mBaiduMap.clear();
//        getSurroundingCars();
//        BitmapDescriptor carPoint = BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding);
//        testCars.clear();
//        Log.d("tracetrace", "testCars.size(): " + testCars.size());
//        testCars.add(new CarsInfo(1, "苏A10086", 32.0245, 118.8683, 50));
//        testCars.add(new CarsInfo(2, "苏A18086", 32.0265, 118.8673, 66));
//        testCars.add(new CarsInfo(3, "浙B43586", 32.0248, 118.8609, 78));
//        testCars.add(new CarsInfo(4, "闽J12388", 32.0321, 118.8587, 82));
//        testCars.add(new CarsInfo(5, "京V12027", 32.0345, 118.8656, 98));
//
//        testCars.add(new CarsInfo(6, "苏P10086", 32.0472, 118.7906, 83));
//        testCars.add(new CarsInfo(7, "苏G10086", 31.9741, 118.8050, 55));
//        testCars.add(new CarsInfo(8, "浙K43586", 32.0223, 118.7903, 28));
//    }

    //初始化成员变量
    private void initMembers() {
        tv_weather = (TextView) findViewById(R.id.tv_weather);
        tv_info_name = (TextView) findViewById(R.id.tv_info_name);
        tv_info_address = (TextView) findViewById(R.id.tv_info_address);
        lv_searchResult = (ListView) findViewById(R.id.lv_searchResult);
        ll_searchResult = (LinearLayout) findViewById(R.id.ll_searchResult);
        parkList = new ArrayList<>();
        touchPoint = new Storage();
        btn_detail = (Button) findViewById(R.id.btn_detail);
        btn_go = (Button) findViewById(R.id.btn_go);
        ll_detail = (LinearLayout) findViewById(R.id.ll_detail);
        ll_suggestionResult = (LinearLayout) findViewById(R.id.ll_suggesionResult);
        lv_suggestionResult = (ListView) findViewById(R.id.lv_suggestionResult);
        suggestionList = new ArrayList<>();
        inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        btn_delete = (Button) findViewById(R.id.btn_delete);
        et_search = (EditText) findViewById(R.id.et_search);
        btn_speak = (Button) findViewById(R.id.btn_speak);
        Log.d("tracetrace", "initMembers()完成");
    }

    //初始化控件（按钮、输入框）监听
    private void initButtonListener() {
        Log.d("tracetrace", "initButtonListener()进入");
        findViewById(R.id.btn_location).setOnClickListener(this);
        findViewById(R.id.btn_traffic).setOnClickListener(this);
        findViewById(R.id.btn_satellite).setOnClickListener(this);
        findViewById(R.id.btn_add).setOnClickListener(this);
        findViewById(R.id.btn_minus).setOnClickListener(this);
        findViewById(R.id.btn_close).setOnClickListener(this);
        findViewById(R.id.btn_empty).setOnClickListener(this);
        findViewById(R.id.btn_list).setOnClickListener(this);
        findViewById(R.id.btn_speak).setOnClickListener(this);
        findViewById(R.id.btn_speech).setOnClickListener(this);
        findViewById(R.id.btn_speak).setOnClickListener(this);
        findViewById(R.id.btn_closeDetail).setOnClickListener(this);
        findViewById(R.id.btn_search).setOnClickListener(this);
        findViewById(R.id.btn_closeSuggestion).setOnClickListener(this);
//      findViewById(R.id.tv_sideText).setOnClickListener(this);
        btn_go.setOnClickListener(this);
        btn_detail.setOnClickListener(this);
        btn_delete.setOnClickListener(this);
        //搜索框焦点监听器
        et_search.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (btn_delete.getVisibility() == View.INVISIBLE) {
                    btn_delete.setVisibility(View.VISIBLE);
                    Toast.makeText(MainActivity.this, "delete可见！", Toast.LENGTH_SHORT).show();
                } else {
                    btn_delete.setVisibility(View.INVISIBLE);
                    Toast.makeText(MainActivity.this, "delete消失！", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //初始化地图界面
    private void initMapView(boolean isSatelliteOpen, boolean isTrafficMapOpen) {
        Log.d("tracetrace", "initMapView()进入");
        bmapView = (MapView) findViewById(R.id.bmapView);
        bmapView.showScaleControl(false);    //隐藏比例尺
        bmapView.showZoomControls(false);    //隐藏缩放控件
        View child = bmapView.getChildAt(1);
        if (child != null && (child instanceof ImageView || child instanceof ZoomControls)) {  //隐藏logo
            child.setVisibility(View.INVISIBLE);
        }
        mBaiduMap = bmapView.getMap();         //获取地图实例
        mBaiduMap.setMyLocationEnabled(true);  //开启定位
        mBaiduMap.setCompassEnable(false);     //关闭左上角指南针
        if (!isSatelliteOpen) {
            mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);  //设置地图类型：普通地图
            mBaiduMap.setTrafficEnabled(isTrafficMapOpen);   //是否开启实时交通
        } else {
            mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);  //设置地图类型：卫星地图
            mBaiduMap.setTrafficEnabled(isTrafficMapOpen);      //是否开启实时交通
        }
        UiSettings settings = mBaiduMap.getUiSettings();
        settings.setRotateGesturesEnabled(false);    //屏蔽地图旋转
//        settings.setAllGesturesEnabled(false);   //关闭一切手势操作
//        settings.setOverlookingGesturesEnabled(false);//屏蔽双指下拉时变成3D地图
//        settings.setZoomGesturesEnabled(false);  //获取是否允许缩放手势返回:是否允许缩放手势
        Log.d("tracetrace", "initMapView()完成");
    }

    //初始化地图监听
    private void initMapListener() {
        Log.d("tracetrace", "initMapListener()进入");
        //设置地图状态改变监听
        mBaiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {
            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus) {
                startLatlng = mapStatus.target;
            }

            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus, int i) {

            }

            @Override
            public void onMapStatusChange(MapStatus mapStatus) {

            }

            @Override
            //拖动地图完成
            public void onMapStatusChangeFinish(MapStatus mapStatus) {
                endLatlng = mapStatus.target;
                //showToast("纬度:"+endLatlng.latitude+"经度："+endLatlng.longitude);
                if (isNeedSearchPark && (startLatlng.latitude != endLatlng.latitude || startLatlng.longitude != endLatlng.longitude)) {
                    mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(endLatlng));
                    saveTouchPoint("", endLatlng, "");
                    //mBaiduMap.clear();
                    setCenterMarker();
                    //searchPark(touchPoint.getLocation());
                    ll_detail.setVisibility(View.INVISIBLE);
                    ll_suggestionResult.setVisibility(View.INVISIBLE);
                    ll_searchResult.setVisibility(View.INVISIBLE);
                    showBottomInfo(true, false);
                }
                isNeedSearchPark = true;
            }
        });

        //设置地图点击监听
        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
                saveTouchPoint("", latLng, "");
                setCenterMarker();
                //searchPark(latLng);
                showBottomInfo(true, false);
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                tv_info_name.setText(mapPoi.getName());
                mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(mapPoi.getPosition()));
                saveTouchPoint(mapPoi.getName(), mapPoi.getPosition(), "");
                setCenterMarker();
                //searchPark(touchPoint.getLocation());
                showBottomInfo(true, false);
                return false;
            }
        });


//        BaiduMap.OnMarkerClickListener markerListener = new BaiduMap.OnMarkerClickListener() {
//            @Override
//            public boolean onMarkerClick(Marker marker) {
//                showToast("2323");
//                //mBaiduMap.removeMarkerClickListener(markerListener);//不remove此marker会被执行多次
//                return true;
//            }
//        };
//        mBaiduMap.setOnMarkerClickListener(markerListener);
















        //设置Marker点击监听
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Log.d("tracetrace","marker被按了一下");
                showToast("不要再按我了啦");
                if (marker.equals(mOverlay)) {
                    return false;
                }
                Storage info = marker.getExtraInfo().getParcelable("info"); //获取存储在marker中的信息
                tv_info_name.setText(info.getName());
                tv_info_address.setText(info.getAddress());
                touchPoint = info;
                showBottomInfo(true, true);
                mRoutePlanSearch.drivingSearch(new DrivingRoutePlanOption()
                        .from(PlanNode.withLocation(new LatLng(myLocation.getLatitude(), myLocation.getLongitude())))
                        .to(PlanNode.withLocation(info.getLocation()))
                        .policy(DrivingRoutePlanOption.DrivingPolicy.ECAR_DIS_FIRST));
                return false;
            }
        });


        //设置驾车路线结果监听
        mRoutePlanSearch.setOnGetRoutePlanResultListener(new OnGetRoutePlanResultListener() {
            @Override
            public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {

            }

            @Override
            public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {

            }

            @Override
            public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {

            }

            @Override
            public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {
                mDrivingRouteOverlay.removeFromMap();
                DrivingRouteLine d = drivingRouteResult.getRouteLines().get(0);
                int distance = d.getDistance();
                touchPoint.setDistance(distance);
                mDrivingRouteOverlay.setData(d);
                mDrivingRouteOverlay.addToMap();
                showDetail(ll_detail);
            }

            @Override
            public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

            }

            @Override
            public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {

            }
        });

        //设置我的位置监听
        mBaiduMap.setOnMyLocationClickListener(new BaiduMap.OnMyLocationClickListener() {
            @Override
            public boolean onMyLocationClick() {
                tv_info_name.setText("我的位置");
                tv_info_address.setText(myLocation.getAddrStr());
                saveTouchPoint("我的位置", new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), myLocation.getAddrStr());
                setCenterMarker();
                showBottomInfo(false, false);
                return false;
            }
        });

        //设置地理编码结果监听
        mGeoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
            @Override
            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

            }

            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
                if (reverseGeoCodeResult == null) {
                    showToast("没有得到地理位置信息");
                } else {
                    if (touchPoint.getName() == "") {
                        tv_info_name.setText(reverseGeoCodeResult.getAddressDetail().district + reverseGeoCodeResult.getAddressDetail().street + reverseGeoCodeResult.getAddressDetail().streetNumber);
                        touchPoint.setName(tv_info_name.getText().toString());
                    }
                    tv_info_address.setText(reverseGeoCodeResult.getAddress());
                    touchPoint.setAddress(reverseGeoCodeResult.getAddress());
                    showBottomInfo(true, false);
                }
            }
        });
    }

    //初始化传感器控制器
    private void initOrientationListener() {
        myOrientationListener = new MyOrientationListener(getApplicationContext());
        myOrientationListener.setOnOrientationListener(new MyOrientationListener.OnOrientationListener() {
            @Override
            public void onOrientationChanged(float x) {
                if (myLocation != null) {
                    // 构造定位数据
                    MyLocationData locData = new MyLocationData.Builder()
                            .accuracy(myLocation.getRadius())
                            .direction(x)
                            .latitude(myLocation.getLatitude())
                            .longitude(myLocation.getLongitude())
                            .build();
                    // 设置定位数据
                    mBaiduMap.setMyLocationData(locData);
                    // 设置定位显示方式：normal
                    mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
                            MyLocationConfiguration.LocationMode.NORMAL, true, null));
                }
            }
        });
    }

    //初始化列表监听
    private void initListListener() {
        Log.d("tracetrace", "initListListener()进入");
        lv_searchResult.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {  //监听ListView的滑动状态改变
                if (isLoadData && scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {  //SCROLL_STATE_IDLE：滑动后静止
                    currPage++;
                    mBaiduMap.clear();
                    //searchPark(touchPoint.getLocation());
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                isLoadData = ((firstVisibleItem + visibleItemCount) == totalItemCount);
                currItem = firstVisibleItem;
            }
        });

        lv_searchResult.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Storage storage = parkPoiList.getStorageList().get(position);
                setMapCenterAndZoom(storage.getLocation(), 17.0f, false);
                setBottomInfo(storage.getName(), storage.getAddress());
                showBottomInfo(true, true);
                touchPoint = storage;
                showDetail(ll_detail);
                mRoutePlanSearch.drivingSearch(new DrivingRoutePlanOption().from(PlanNode.withLocation(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()))).to(PlanNode.withLocation(storage.getLocation())).policy(DrivingRoutePlanOption.DrivingPolicy.ECAR_DIS_FIRST));
                ll_searchResult.setVisibility(View.INVISIBLE);
            }
        });

        lv_suggestionResult.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Storage storage = suggestionPoiList.getStorageList().get(position);
                saveTouchPoint(storage.getName(), storage.getLocation(), storage.getAddress());
                setMapCenterAndZoom(storage.getLocation(), 17.0f, false);
                setBottomInfo(storage.getName(), storage.getAddress());
                showBottomInfo(true, false);
                mBaiduMap.clear();
                //searchPark(storage.getLocation());
                ll_suggestionResult.setVisibility(View.INVISIBLE);
            }
        });
    }

    //初始化讯飞语音
    private void initSpeech() {
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=5d197925");
    }


    //语音输出
    private void speechOutput() {
        //1. 创建 SpeechSynthesizer 对象 , 第二个参数： 本地合成时传 InitListener
        SpeechSynthesizer mTts = SpeechSynthesizer.createSynthesizer(this, null);
        //2.合成参数设置
        mTts.setParameter(SpeechConstant.VOICE_NAME, "vixyun"); // 设置发音人
        mTts.setParameter(SpeechConstant.SPEED, "50");// 设置语速
        mTts.setParameter(SpeechConstant.VOLUME, "80");// 设置音量，范围 0~100
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); //设置云端
        //设置合成音频保存位置（可自定义保存位置），保存在 “./sdcard/iflytek.pcm”
        //保存在 SD 卡需要在 AndroidManifest.xml 添加写 SD 卡权限
        //仅支持保存为 pcm 和 wav 格式， 如果不需要保存合成音频，注释该行代码
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, "./sdcard/iflytek.pcm");
        //3.开始合成
        mTts.startSpeaking(et_search.getText().toString(), new MySynthesizerListener());

    }

    //语音播报生命周期的类
    class MySynthesizerListener implements SynthesizerListener {

        @Override
        public void onSpeakBegin() {
            showToast(" 开始播放 ");
        }

        @Override
        public void onSpeakPaused() {
            showToast(" 暂停播放 ");
        }

        @Override
        public void onSpeakResumed() {
            showToast(" 继续播放 ");
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos, String info) {
            // 合成进度
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            // 播放进度
        }

        @Override
        public void onCompleted(SpeechError error) {
            if (error == null) {
                showToast("播放完成 ");
            } else if (error != null) {
//                showToast(error.getPlainDescription(true));
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话 id，当业务出错时将会话 id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话 id为null
            //if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //     String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //}
        }

    }

    //语音输入
    private void speechInput() {
        //1. 创建RecognizerDialog对象
        RecognizerDialog mDialog = new RecognizerDialog(this, new MyInitListener());
        //2. 设置accent、 language等参数
        mDialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");// 设置中文
        mDialog.setParameter(SpeechConstant.ACCENT, "mandarin");
        // 若要将UI控件用于语义理解，必须添加以下参数设置，设置之后 onResult回调返回将是语义理解
        // 结果
        // mDialog.setParameter("asr_sch", "1");
        // mDialog.setParameter("nlp_version", "2.0");
        //3.设置回调接口
        mDialog.setListener(new MyRecognizerDialogListener());
        //4. 显示dialog，接收语音输入
        mDialog.show();
    }

    //监听初始化
    class MyInitListener implements InitListener {

        @Override
        public void onInit(int code) {
            if (code != ErrorCode.SUCCESS) {
                showToast("初始化失败 ");
            }
        }
    }

    class MyRecognizerDialogListener implements RecognizerDialogListener {

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            if(!isLast) {  //去掉末尾的句号
                String result = results.getResultString(); //未解析的
                String text = JsonParser.parseIatResult(result);//解析过后的
                String sn = null;
                // 读取json结果中的 sn字段
                try {
                    JSONObject resultJson = new JSONObject(results.getResultString());
                    sn = resultJson.optString("sn");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mIatResults.put(sn, text);//没有得到一句，添加到
                StringBuffer resultBuffer = new StringBuffer();
                for (String key : mIatResults.keySet()) {
                    resultBuffer.append(mIatResults.get(key));
                }

                et_search.setText(resultBuffer.toString());// 设置输入框的文本
                et_search.setSelection(et_search.length());//把光标定位末尾
                if (((TextView) findViewById(R.id.et_search)).getText().toString().equals("")) {
                    showToast("搜索内容不能为空");
                }
                getSuggestion(et_search.getText().toString().trim());
                inputManager.hideSoftInputFromWindow(et_search.getWindowToken(), 0);
            }
        }

        @Override
        public void onError(SpeechError speechError) {

        }

    }

    //获取天气信息
    private void getWeather() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                weather = Weather.getWeather(myLocation.getCity());
                if (weather != null && weather == "") {
                    Message message = new Message();
                    message.what = 0;
                    weatherHandler.sendMessage(message);
                } else {
                    Message message = new Message();
                    message.what = 1;
                    message.obj = weather;
                    weatherHandler.sendMessage(message);
                }
            }
        });
        try {
            thread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //监听天气信息获取
    private void getWeatherHandler() {
        if (weatherHandler == null) {
            weatherHandler = new Handler(getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what == 1) {
                        tv_weather.setText((String) msg.obj);
                    } else {
                        tv_weather.setText("加载失败，点击定位图标重试");
                    }
                }
            };
        }
    }

    //获取定位设置
    private LocationClientOption getLocationOption() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setIsNeedAddress(true);
        option.setOpenGps(true);
        option.setCoorType("bd09ll");
        option.setWifiCacheTimeOut(300000);
        option.setScanSpan(3000);
        return option;
    }

    //监听定位信息获取,位置一旦有所改变就会调用这个方法
    private class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            myLocation = location; //保存定位信息
            Log.d("tracetrace", "meLocation保存");
            MyLocationData locData = new MyLocationData.Builder().accuracy(location.getRadius()).latitude(location.getLatitude()).longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData); //设置地图上我的坐标
            //一旦位置变化就重新获取周边车辆图钉
            showSurroundingCars();
            if (isFirstLocate) {  //这个判断是为了防止每次定位都重新设置中心点和marker
                LatLng myLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                setMapCenterAndZoom(myLatLng, 17.0f, false);
                setBottomInfo("我的位置", location.getAddrStr());
                showBottomInfo(false, false);
                saveTouchPoint("我的位置", myLatLng, location.getAddrStr());
                getWeather();
                //searchPark(myLatLng);
                isFirstLocate = false;
            }
        }
    }

    //创建一个文件夹用于保存在路线导航过程中语音导航语音文件的缓存，防止用户再次开启同样的导航直接从缓存中读取即可
    private boolean initDir() {
        //判断外部存储（SD卡）状态
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {  //MEDIA_MOUNTED：SD卡正常挂载
            mSdcardPath = Environment.getExternalStorageDirectory().toString();
        } else {
            mSdcardPath = null;
        }
        if (mSdcardPath == null) {
            return false;
        }
        File file = new File(mSdcardPath, "Map");  //file路径为mSdcardPath/Map
        if (!file.exists()) {
            try {
                file.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    //初始化导航引擎
    private void initNaviPath() {
        Bundle bundle = new Bundle();
        bundle.putString(BNCommonSettingParam.TTS_APP_ID, "10159100");
        BNaviSettingManager.setNaviSdkParam(bundle);
        BNOuterTTSPlayerCallback bnOuterTTSPlayerCallback = null;
        BaiduNaviManager.getInstance().init(MainActivity.this, mSdcardPath, "Map", new BaiduNaviManager.NaviInitListener() {
            @Override
            public void onAuthResult(int i, String s) {
            }

            @Override
            public void initStart() {
            }

            @Override
            public void initSuccess() {
            }

            @Override
            public void initFailed() {
            }
        }, bnOuterTTSPlayerCallback);
    }

    //初始化导航路线
    private void initBNRoutePlan(LatLng source, LatLng destination, String destinationName) {
        BNRoutePlanNode startNode = new BNRoutePlanNode(source.longitude, source.latitude, "我我我", null, BNRoutePlanNode.CoordinateType.BD09LL);
        BNRoutePlanNode endNode = new BNRoutePlanNode(destination.longitude, destination.latitude, destinationName, null, BNRoutePlanNode.CoordinateType.BD09LL);
        if (startNode != null && endNode != null) {
            ArrayList<BNRoutePlanNode> lst = new ArrayList<>();
            lst.add(startNode);
            lst.add(endNode);
            MyRoutePlanListener myRoutePlanListener = new MyRoutePlanListener(lst);
            BaiduNaviManager.getInstance().launchNavigator(MainActivity.this, lst, 1, true, myRoutePlanListener);
        }
    }

    //监听导航路线
    class MyRoutePlanListener implements BaiduNaviManager.RoutePlanListener {
        private ArrayList<BNRoutePlanNode> mList = null;

        public MyRoutePlanListener(ArrayList<BNRoutePlanNode> lst) {
            mList = lst;
        }

        @Override
        public void onJumpToNavigator() {
            Intent intent = new Intent(MainActivity.this, NavagationActivity.class);
            intent.putExtra("routePlanNode", mList);
            startActivity(intent);
        }

        @Override
        public void onRoutePlanFailed() {
            showToast("路线规划失败");
        }

    }

    //搜索指定坐标点周围2公里的停车场信息
    private void searchPark(final LatLng latLng) {
        Thread parkThread = new Thread(new Runnable() {
            @Override
            public void run() { //每次搜索新建一个线程
                parkPoiList = Park.getPark(latLng, currPage);
                if (parkPoiList == null || parkPoiList.getCurrSize() == 0) {
                    Message message = new Message();
                    message.what = 0;
                    searchHandler.sendMessage(message);
                } else {
                    Message message = new Message();
                    message.what = 1;
                    message.obj = parkPoiList;
                    searchHandler.sendMessage(message);
                }
            }
        });
        try {
            parkThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //监听停车场搜索结果的获取
    private void getSearchHandler() {
        if (searchHandler == null) {
            searchHandler = new Handler(getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what == 1) {
                        List<Storage> mStorageList = parkPoiList.getStorageList();
                        parkList.clear();
                        for (int i = 0; i < mStorageList.size(); i++) {
                            Map<String, Object> item = new HashMap<>();
                            item.put("name", mStorageList.get(i).getName());
                            item.put("address", mStorageList.get(i).getAddress());
                            item.put("type", mStorageList.get(i).getStyle());
                            item.put("price", mStorageList.get(i).getTime1() + "元/时");
                            parkList.add(item);     //把该停车场的信息add进parkList
                        }
                        SimpleAdapter simpleAdapter = new SimpleAdapter(
                                getApplicationContext(),   //上下文对象
                                parkList,                  //数据源是含有Map的一个集合
                                R.layout.park_list_item,   //每一个item的布局文件
                                new String[]{"name", "address", "type", "price"},  //数组里的每一项要与parkList集合的key值一一对应
                                new int[]{R.id.tv_name, R.id.tv_address, R.id.tv_type, R.id.tv_price}  //第三个参数里面的控件id。
                        );
                        lv_searchResult.setAdapter(simpleAdapter);
                        setListViewHeightBasedOnChildren(lv_searchResult, 8);
                        setCenterMarker();
                        addOverlays();
                    } else {
                        showToast("停车场搜不到啊");
                    }
                }
            };
        }
    }

    //在地图上增加覆盖物，覆盖物颜色根据a=空车位/总车位，当a<=0.2为红色，当0.2<a<=0.5为黄色，当0.5<a<1为绿色
    public void addOverlays() {
        MyPoiOverlay overlay = new MyPoiOverlay(mBaiduMap, new BitmapDrawable[]{new BitmapDrawable(martix(BitmapFactory.decodeResource(getResources(), R.drawable.icon_marker_busy), 93, 122)), new BitmapDrawable(martix(BitmapFactory.decodeResource(getResources(), R.drawable.icon_marker_middle), 93, 122)), new BitmapDrawable(martix(BitmapFactory.decodeResource(getResources(), R.drawable.icon_marker_free), 93, 122))});
        overlay.setData(parkPoiList);
        overlay.addToMap();
    }

    //获取地名建议
    private void getSuggestion(final String query) {
        Thread parkThread = new Thread(new Runnable() {
            @Override
            public void run() { //每次搜索新建一个线程
                suggestionPoiList = Suggestion.getSuggestion(query, myLocation.getCity());
                if (suggestionPoiList == null || suggestionPoiList.getCurrSize() == 0) {
                    Message message = new Message();
                    message.what = 0;
                    suggestionHandler.sendMessage(message);
                } else {
                    Message message = new Message();
                    message.what = 1;
                    message.obj = suggestionPoiList;
                    suggestionHandler.sendMessage(message);
                }
            }
        });
        try {
            parkThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //监听地名建议信息获取
    private void getSuggestionHandler() {
        if (suggestionHandler == null) {
            suggestionHandler = new Handler(getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what == 1) {
                        List<Storage> mStorageList = suggestionPoiList.getStorageList();
                        suggestionList = new ArrayList<>();
                        for (int i = 0; i < mStorageList.size(); i++) {
                            Map<String, Object> item = new HashMap<>();
                            item.put("name", mStorageList.get(i).getName());
                            item.put("address", mStorageList.get(i).getAddress());
                            suggestionList.add(item);
                        }
                        SimpleAdapter simpleAdapter = new SimpleAdapter(getApplicationContext(), suggestionList, R.layout.suggestion_list_item, new String[]{"name", "address"}, new int[]{R.id.tv_name, R.id.tv_address});
                        lv_suggestionResult.setAdapter(simpleAdapter);
                        setListViewHeightBasedOnChildren(lv_suggestionResult, 8);
                        ll_suggestionResult.setVisibility(View.VISIBLE);
                        if (ll_searchResult.getVisibility() == View.VISIBLE) {
                            ll_searchResult.setVisibility(View.INVISIBLE);
                        }
                    } else {
                        showToast("搜不到结果");
                    }
                }
            };
        }
    }

    //根据列表项目的高度和数目自动调整列表高度
    private void setListViewHeightBasedOnChildren(ListView listView, int limit) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int totalHeight = 0;
        if (listAdapter.getCount() < limit) {
            limit = listAdapter.getCount();
        }
        for (int i = 0; i < limit; i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (limit - 1));
        listView.setLayoutParams(params);
    }

    //Bitmap缩放
    private Bitmap martix(Bitmap bitmap, int newWidth, int newHeight) {
        int widtth = bitmap.getWidth();
        int height = bitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale((float) newWidth / widtth, (float) newHeight / height);
        return Bitmap.createBitmap(bitmap, 0, 0, widtth, height, matrix, true);
    }

    //计算两点之间的真实距离
    public double getKiloMeter(LatLng start, LatLng end) {

        double lon1 = (Math.PI / 180) * start.longitude;
        double lon2 = (Math.PI / 180) * end.longitude;
        double lat1 = (Math.PI / 180) * start.latitude;
        double lat2 = (Math.PI / 180) * end.latitude;
        // 地球半径
        double R = 6371;
        // 两点间距离 km，如果想要米的话，结果*1000就可以了
        double d = Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1))
                * R;
        return d;
    }
}