package com.example.magi.map;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.overlayutil.OverlayManager;

import java.util.ArrayList;
import java.util.List;

public class MyPoiOverlay extends OverlayManager {
    private PoiList mPoiList = null;
    private BitmapDrawable[] mBitmapDrawable;

    public MyPoiOverlay(BaiduMap baiduMap, BitmapDrawable[] mBitmapDrawable) {
        super(baiduMap);
        this.mBitmapDrawable = mBitmapDrawable;
    }

    public void setData(PoiList mPoiList) {
        this.mPoiList = mPoiList;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public boolean onPolylineClick(Polyline polyline){
        return false;
    }

    @Override
    public List<OverlayOptions> getOverlayOptions() {
        if ((this.mPoiList == null) || (this.mPoiList.getCurrSize() == 0))
            return null;
        ArrayList<OverlayOptions> arrayList = new ArrayList<>();
        List<Storage> lst = mPoiList.getStorageList();
        for (int i = 0; i < lst.size(); i++) {
            if (lst.get(i).getLocation() == null)
                continue;
            Bundle bundle = new Bundle();
            bundle.putInt("index", i);
            bundle.putParcelable("info", lst.get(i));
            arrayList.add(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromBitmap(setNumToIcon(i + 1, lst.get(i)))).extraInfo(bundle).zIndex(6)
                    .position(lst.get(i).getLocation()).extraInfo(bundle));
        }
        return arrayList;
    }

    //绘制文字
    private Bitmap setNumToIcon(int num, Storage storage) {
        BitmapDrawable bd;
        Paint paint = new Paint();
        double empty = storage.getEmpty();
        double total = storage.getTotal();
        if(empty <= 0.2 * total){
            bd = mBitmapDrawable[0];
            paint.setColor(Color.WHITE);
        }
        else {
            if(empty <= 0.5 * total){
                bd = mBitmapDrawable[1];
            }
            else {
                bd = mBitmapDrawable[2];
            }
            paint.setColor(Color.BLACK);
        }
        Bitmap bitmap = bd.getBitmap().copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bitmap);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setAntiAlias(true);
        Paint paintForPark = new Paint();
        paintForPark.setColor(Color.BLACK);
        paintForPark.setAntiAlias(true);
        paintForPark.setTextSize(36);
        paintForPark.setTextAlign(Paint.Align.CENTER);
        if(storage.getStyle().equals("露天")){
            paintForPark.setColor(Color.BLACK);
        }
        else {
            paintForPark.setColor(Color.BLUE);
        }
        int heightY = 10;
        if (num < 10) {
            paint.setTextSize(33);
            heightY = 15;
        } else {
            paint.setTextSize(23);
        }
        canvas.drawText(String.valueOf(num), bitmap.getWidth() / 2, ((bitmap.getHeight() / 2) + heightY), paint);
        canvas.drawText(storage.getEmpty() + "/" + storage.getTotal(), bitmap.getWidth() / 2, (bitmap.getHeight() - 94), paintForPark );
        return bitmap;
    }
}
