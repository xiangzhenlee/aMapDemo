package com.yushan.amapdemo;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.trace.LBSTraceClient;
import com.amap.api.trace.TraceLocation;
import com.amap.api.trace.TraceStatusListener;
import com.amap.api.track.query.entity.Point;
import com.yushan.amapdemo.trackdemo.util.AMapUtils;
import com.yushan.amapdemo.trackdemo.util.PathSmoothTool;

import org.json.JSONObject;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements AMapService.OnLocationClientChangeListener {

    private MapView mMapView;
    private AMap aMap;
    private MyLocationStyle myLocationStyle;
    private UiSettings uiSettings;
    private AMapLocationClient locationClient;
    private AMapLocationClientOption locationOption;
    private List<Polyline> polylines = new LinkedList<>();
    private List<Marker> endMarkers = new LinkedList<>();


//    TraceStatusListener traceStatusListener = new TraceStatusListener() {
//        @Override
//        public void onTraceStatus(List<TraceLocation> locations, List<LatLng> rectifications, String errorInfo) {
//            //locations 定位得到的轨迹点集，rectifications 纠偏后的点集，errorInfo 轨迹纠偏错误信息
//            Toast.makeText(MapActivity.this, "定位1成功", Toast.LENGTH_SHORT).show();
//            if (rectifications != null && rectifications.size() > 0) {
//
//                LatLng latLng = new LatLng(locations.get(locations.size() - 1).getLatitude(),locations.get(locations.size() - 1).getLongitude());
//                Double dis = AMapUtils.calculateLineDistance(latLng, rectifications.get(rectifications.size() -1));
//                if (dis < 5) {
//                    Toast.makeText(MapActivity.this, "定位成功", Toast.LENGTH_SHORT).show();
//                    drawTrackOnMap(rectifications);
//                }
//            }
//        }
//    };

    private Polyline mOriginPolyline;
    private PathSmoothTool mpathSmoothTool;
    private Polyline mkalmanPolyline;
    private LatLng latLng;
    private boolean endLine = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        initView();
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);
        //初始化地图控制器对象
        if (aMap == null) {
            aMap = mMapView.getMap();
            uiSettings = aMap.getUiSettings();
        }

        initAMap();

        Intent serviceIntent = new Intent(MapActivity.this, AMapService.class);
        bindService(serviceIntent, mServiceConnection, BIND_AUTO_CREATE);
        startService(serviceIntent);


        //初始化LBSTraceClient
//        LBSTraceClient lbsTraceClient = LBSTraceClient.getInstance(this);
//        lbsTraceClient.startTrace(traceStatusListener); //开始采集,需要传入一个状态回调监听。
    }

    //轨迹平滑优化
    public List<LatLng> pathOptimize(List<LatLng> originlist) {
        List<LatLng> pathoptimizeList = mpathSmoothTool.pathOptimize(originlist);
//        mkalmanPolyline = aMap.addPolyline(new PolylineOptions().addAll(pathoptimizeList).color(Color.parseColor("#FFC125")));
        return pathoptimizeList;
    }

//    private LatLngBounds getBounds(List<LatLng> pointlist) {
//        LatLngBounds.Builder b = LatLngBounds.builder();
//        if (pointlist == null) {
//            return b.build();
//        }
//        for (int i = 0; i < pointlist.size(); i++) {
//            b.include(pointlist.get(i));
//        }
//        return b.build();
//
//    }

    /**
     * 绑定service监听
     */
    ServiceConnection mServiceConnection = new ServiceConnection() {

        /*当绑定时执行*/
        public void onServiceConnected(ComponentName name, IBinder service) {  //service的onbind（）中返回值不为null才会触发
            AMapService aMapService = ((AMapService.MyBinder) service).getService();//得到该service实例
            aMapService.setOnLocationClientChangeListener(MapActivity.this);//把回调对象传送给service
        }

        /*当异常结束service时执行，但调用unbindService()时不会触发改方法 测试的话可以在bind时使用Context.BIND_NOT_FOREGROUND  调用stopservice（）可触发*/
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private void drawSmoothLine(List<LatLng> mOriginList) {
        mpathSmoothTool = new PathSmoothTool();
        //设置平滑处理的等级
        mpathSmoothTool.setIntensity(4);
        //未处理轨迹
        if (mOriginList != null && mOriginList.size() > 0) {
            mOriginPolyline = aMap.addPolyline(new PolylineOptions().addAll(mOriginList).color(Color.GREEN));
//            aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(getBounds(mOriginList),0));
//            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mOriginList.get(0),15));
        }
        //平滑处理后的轨迹
        List<LatLng> dealLine = pathOptimize(mOriginList);
        drawTrackOnMap(dealLine);
    }

    private void drawTrackOnMap(List<LatLng> rectifications) {
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.BLUE).width(20);
        if (rectifications.size() > 0) {
            // 起点
            LatLng p = rectifications.get(0);
            LatLng latLng = new LatLng(p.latitude, p.longitude);
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            endMarkers.add(aMap.addMarker(markerOptions));
        }
        if (rectifications.size() > 1 && endLine) {
            // 终点
            LatLng p = rectifications.get(rectifications.size() - 1);
            LatLng latLng = new LatLng(p.latitude, p.longitude);
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            endMarkers.add(aMap.addMarker(markerOptions));
        }
        for (LatLng p : rectifications) {
            LatLng latLng = new LatLng(p.latitude, p.longitude);
            polylineOptions.add(latLng);
            boundsBuilder.include(latLng);
        }
        Polyline polyline = aMap.addPolyline(polylineOptions);
        polylines.add(polyline);
//        aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 30));
    }

    private void initAMap() {
        myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        myLocationStyle.interval(2000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。

        myLocationStyle.strokeColor(Color.argb(0, 0, 0, 0));// 设置圆形的边框颜色
        myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));// 设置圆形的填充颜色
        myLocationStyle.strokeWidth(2);//设置定位蓝点精度圈的边框宽度的方法。

//        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_SHOW);//只定位一次。
//        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE) ;//定位一次，且将视角移动到地图中心点。
//        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW) ;//连续定位、且将视角移动到地图中心点，定位蓝点跟随设备移动。（1秒1次定位）
//        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_MAP_ROTATE);//连续定位、且将视角移动到地图中心点，地图依照设备方向旋转，定位点会跟随设备移动。（1秒1次定位）
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）默认执行此种模式。
        //以下三种模式从5.1.0版本开始提供
//        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);//连续定位、蓝点不会移动到地图中心点，定位点依照设备方向旋转，并且蓝点会跟随设备移动。
//        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW_NO_CENTER);//连续定位、蓝点不会移动到地图中心点，并且蓝点会跟随设备移动。
//        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_MAP_ROTATE_NO_CENTER);//连续定位、蓝点不会移动到地图中心点，地图依照设备方向旋转，并且蓝点会跟随设备移动。

        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        aMap.getUiSettings().setZoomControlsEnabled(false);//去掉地图右下角隐藏的缩放按钮
        aMap.getUiSettings().setMyLocationButtonEnabled(false);//设置默认定位按钮是否显示，非必需设置。
        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        aMap.moveCamera(CameraUpdateFactory.zoomTo(13));

        uiSettings.setMyLocationButtonEnabled(true);//设置默认定位按钮是否显示，非必需设置。
        uiSettings.setScaleControlsEnabled(true);//控制比例尺控件是否显示
    }

    private void initView() {
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.amap);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void locationClientChange(List<LatLng> list) {
        Toast.makeText(MapActivity.this, "lat:" + list.get(list.size() - 1).latitude + "\nlng:" + list.get(list.size() - 1).longitude, Toast.LENGTH_SHORT).show();
        drawSmoothLine(list);
    }

}

