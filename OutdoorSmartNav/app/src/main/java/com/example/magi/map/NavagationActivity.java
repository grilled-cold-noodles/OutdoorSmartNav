package com.example.magi.map;

import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.baidu.navisdk.adapter.BNRouteGuideManager;
import com.baidu.navisdk.adapter.BNRouteGuideManager.CustomizedLayerItem;
import com.baidu.navisdk.adapter.BNRoutePlanNode;

import java.util.ArrayList;
import java.util.List;

public class NavagationActivity extends AppCompatActivity {
    private BNRoutePlanNode mBNRoutePlanNode;
    private static final int MSG_SHOW = 1;
    private static final int MSG_HIDE = 2;
    private static final int MSG_RESET_NODE = 3;
    private ArrayList<BNRoutePlanNode> list;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navagation);
        getHandler();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {}
        View view = BNRouteGuideManager.getInstance().onCreate(NavagationActivity.this, new BNRouteGuideManager.OnNavigationListener() {
            @Override
            public void onNaviGuideEnd() {
                finish();
            }

            @Override
            public void notifyOtherAction(int i, int i1, int i2, Object o) {

            }
        });
        if(view != null){
            setContentView(view);
        }
        Intent intent = getIntent();
        if(intent != null){
            list = (ArrayList<BNRoutePlanNode>)intent.getSerializableExtra("routePlanNode");
            mBNRoutePlanNode = list.get(0);
        }
    }

    @Override
    protected void onResume() {
        BNRouteGuideManager.getInstance().onResume();
        super.onResume();
        if(mHandler != null){
            mHandler.sendEmptyMessageAtTime(MSG_SHOW, 2000);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        BNRouteGuideManager.getInstance().onPause();
    }

    @Override
    protected void onDestroy() {
        BNRouteGuideManager.getInstance().onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        BNRouteGuideManager.getInstance().onStop();
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        BNRouteGuideManager.getInstance().onBackPressed(false);
    }

    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        BNRouteGuideManager.getInstance().onConfigurationChanged(newConfig);
        super.onConfigurationChanged(newConfig);
    }

    //监控导航信息
    private void getHandler() {
        if (mHandler == null) {
            mHandler = new Handler(getMainLooper()){
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what == MSG_SHOW) {
                        addCustomizedLayerItems();
                    }else if (msg.what == MSG_HIDE) {
                        BNRouteGuideManager.getInstance().showCustomizedLayer(false);
                    }else if (msg.what == MSG_RESET_NODE) {
                        BNRouteGuideManager.getInstance().resetEndNodeInNavi(list.get(1));
                    }
                }
            };
        }
    }

    //添加自定义的图层，可以更换导航的中心的图标
    private void addCustomizedLayerItems() {
        List<CustomizedLayerItem> items = new ArrayList<>();
        CustomizedLayerItem customizedLayerItem;
        if (mBNRoutePlanNode != null) {
            customizedLayerItem = new CustomizedLayerItem(mBNRoutePlanNode.getLongitude(), mBNRoutePlanNode.getLatitude(),
                    BNRoutePlanNode.CoordinateType.BD09LL, getResources().getDrawable(R.drawable.icon_launcher), CustomizedLayerItem.ALIGN_CENTER);
            items.add(customizedLayerItem);
            BNRouteGuideManager.getInstance().setCustomizedLayerItems(items);
        }
        BNRouteGuideManager.getInstance().showCustomizedLayer(true);
    }
}
