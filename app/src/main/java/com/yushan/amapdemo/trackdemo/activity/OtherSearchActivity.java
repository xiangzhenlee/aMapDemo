package com.yushan.amapdemo.trackdemo.activity;

import android.app.Activity;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.TextureMapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.track.AMapTrackClient;
import com.amap.api.track.query.entity.Point;
import com.amap.api.track.query.model.DistanceRequest;
import com.amap.api.track.query.model.DistanceResponse;
import com.amap.api.track.query.model.LatestPointRequest;
import com.amap.api.track.query.model.LatestPointResponse;
import com.amap.api.track.query.model.QueryTerminalRequest;
import com.amap.api.track.query.model.QueryTerminalResponse;
import com.yushan.amapdemo.R;
import com.yushan.amapdemo.trackdemo.util.Constants;
import com.yushan.amapdemo.trackdemo.util.SimpleOnTrackListener;

import java.util.Date;
import java.util.Locale;

/**
 * 除轨迹点查询以外的简单查询接口示例
 */
public class OtherSearchActivity extends Activity {

    private AMapTrackClient aMapTrackClient;
    private TextView logText;
    private TextureMapView mapView;

    private Marker locationMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_search);

        aMapTrackClient = new AMapTrackClient(getApplicationContext());

        logText = findViewById(R.id.activity_other_search_log);
        mapView = findViewById(R.id.activity_other_search_map);
        mapView.onCreate(savedInstanceState);

        // 查询实时位置
        findViewById(R.id.activity_other_search_location).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aMapTrackClient.queryTerminal(new QueryTerminalRequest(Constants.SERVICE_ID, Constants.TERMINAL_NAME), new SimpleOnTrackListener() {
                    @Override
                    public void onQueryTerminalCallback(QueryTerminalResponse queryTerminalResponse) {
                        if (queryTerminalResponse.isSuccess()) {
                            long terminalId = queryTerminalResponse.getTid();
                            if (terminalId > 0) {
                                aMapTrackClient.queryLatestPoint(new LatestPointRequest(Constants.SERVICE_ID, terminalId), new SimpleOnTrackListener() {
                                    @Override
                                    public void onLatestPointCallback(LatestPointResponse latestPointResponse) {
                                        if (latestPointResponse.isSuccess()) {
                                            Point point = latestPointResponse.getLatestPoint().getPoint();
                                            appendLogText("查询实时位置成功，实时位置：" + pointToString(point));
                                            showLocationOnMap(new LatLng(point.getLat(), point.getLng()));
                                        } else {
                                            appendLogText("查询实时位置失败，" + latestPointResponse.getErrorMsg());
                                        }
                                    }
                                });
                            } else {
                                appendLogText("终端不存在，请先使用轨迹上报示例页面创建终端和上报轨迹");
                            }
                        } else {
                            appendLogText("终端查询失败，" + queryTerminalResponse.getErrorMsg());
                        }
                    }
                });
            }
        });

        // 查询行驶里程
        findViewById(R.id.activity_other_search_distance).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aMapTrackClient.queryTerminal(new QueryTerminalRequest(Constants.SERVICE_ID, Constants.TERMINAL_NAME), new SimpleOnTrackListener() {
                    @Override
                    public void onQueryTerminalCallback(QueryTerminalResponse queryTerminalResponse) {
                        if (queryTerminalResponse.isSuccess()) {
                            long terminalId = queryTerminalResponse.getTid();
                            if (terminalId > 0) {
                                long curr = System.currentTimeMillis();
                                DistanceRequest distanceRequest = new DistanceRequest(
                                        Constants.SERVICE_ID,
                                        terminalId,
                                        curr - 12 * 60 * 60 * 1000, // 开始时间
                                        curr,   // 结束时间
                                        -1  // 轨迹id
                                );
                                aMapTrackClient.queryDistance(distanceRequest, new SimpleOnTrackListener() {
                                    @Override
                                    public void onDistanceCallback(DistanceResponse distanceResponse) {
                                        if (distanceResponse.isSuccess()) {
                                            appendLogText("行驶里程查询成功，共行驶: " + distanceResponse.getDistance() + "m");
                                        } else {
                                            appendLogText("行驶里程查询失败，" + distanceResponse.getErrorMsg());
                                        }
                                    }
                                });
                            } else {
                                appendLogText("终端不存在，请先使用轨迹上报示例页面创建终端和上报轨迹");
                            }
                        } else {
                            appendLogText("终端查询失败，" + queryTerminalResponse.getErrorMsg());
                        }
                    }
                });

            }
        });
    }

    private void showLocationOnMap(LatLng latLng) {

        if (locationMarker != null) {
            locationMarker.remove();
        }

        mapView.getMap().moveCamera(CameraUpdateFactory.newLatLng(latLng));
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        locationMarker = mapView.getMap().addMarker(markerOptions);
    }

    private String pointToString(Point point) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return "{lat: " + point.getLat() + ", lng: " + point.getLng() +
                ", 上传时间: " + sdf.format(new Date(point.getTime())) +
                ", 定位精度" + point.getAccuracy() + ", 其他属性参考文档...}";
    }

    private void appendLogText(String text) {
        logText.append(text + "\n");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }


}
