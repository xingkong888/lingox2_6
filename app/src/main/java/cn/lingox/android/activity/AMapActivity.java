package cn.lingox.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.Tip;

import java.util.ArrayList;
import java.util.List;

import cn.lingox.android.R;
import cn.lingox.android.entity.User;
import cn.lingox.android.helper.CacheHelper;


public class AMapActivity extends Activity implements AMap.OnMarkerClickListener, GeocodeSearch.OnGeocodeSearchListener,
        AMap.InfoWindowAdapter, Inputtips.InputtipsListener, AMap.OnInfoWindowClickListener {
    //声明变量
    private MapView mapView;
    private AMap aMap;

    private String address = new String();

    private User user = CacheHelper.getInstance().getSelfInfo();
    private Marker marker;
    private LatLng latLng;

    private GeocodeSearch geocoderSearch;
    private LatLonPoint latLonPoint;
    private double lat, lng;
    private Intent intent = new Intent();

    private EditText editText;
    private Button btn;

    private ListView listView;
    private ArrayList<String> arrayList = new ArrayList<>();
    private ArrayList<Tip> tipList = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private Inputtips inputtips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在onCreat方法中给aMap对象赋值
        setContentView(R.layout.activity_map);
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);// 必须要写
        aMap = mapView.getMap();

        lat = user.getLoc()[0];
        lng = user.getLoc()[1];
        latLng = new LatLng(lat, lng);
        latLonPoint = new LatLonPoint(lat, lng);

        aMap.setOnMarkerClickListener(this);// 设置点击marker事件监听器
        aMap.setInfoWindowAdapter(this);
        aMap.setOnInfoWindowClickListener(this);// 设置点击infoWindow事件监听器

        geocoderSearch = new GeocodeSearch(this);
        geocoderSearch.setOnGeocodeSearchListener(this);
        getAddress(latLonPoint);
        initView();
    }


    private void initView() {
        editText = (EditText) findViewById(R.id.map_search);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (listView.getVisibility() == View.GONE) {
                    listView.setVisibility(View.VISIBLE);
                }
                if (s.toString().trim().length() < 2) {
//                 tipList.clear();
                    arrayList.clear();
                    adapter.notifyDataSetChanged();
                }
                // 发送输入提示请求
                inputtips = new Inputtips(AMapActivity.this, AMapActivity.this);
                try {
                    // newText表示提示关键字，第二个参数默认代表全国，也可以为城市区号
                    inputtips.requestInputtips(s.toString().trim(), "");
                } catch (Exception e) {

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btn = (Button) findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 发送输入提示请求
                inputtips = new Inputtips(AMapActivity.this, AMapActivity.this);
                try {
                    // newText表示提示关键字，第二个参数默认代表全国，也可以为城市区号
                    inputtips.requestInputtips(editText.getText().toString().trim(), "");
                } catch (Exception e) {

                }
            }
        });

        listView = (ListView) findViewById(R.id.list);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Tip tip = tipList.get(position);
                getAddress(tip.getName(), tip.getAdcode());
                listView.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (listView.getVisibility() == View.VISIBLE) {
            listView.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }

    //根据坐标，获取地址描述
    private void getAddress(LatLonPoint latLonPoint) {
//        latLonPoint参数表示一个Latlng，第二参数表示范围多少米，GeocodeSearch.AMAP表示是国测局坐标系还是GPS原生坐标系
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200, GeocodeSearch.AMAP);
//        Toast.makeText(this, latLonPoint.toString(), Toast.LENGTH_LONG).show();
        geocoderSearch.getFromLocationAsyn(query);
    }

    //根据地址获取坐标
    private void getAddress(String address, String cityCode) {
        // name表示地址，第二个参数表示查询城市，中文或者中文全拼，citycode、adcode
        GeocodeQuery query = new GeocodeQuery(address, cityCode);
        geocoderSearch.getFromLocationNameAsyn(query);
//        Toast.makeText(this, cityCode, Toast.LENGTH_LONG).show();
    }

    //创建标记
    private void makeMarker(LatLng latLng1, String address) {
        this.address = address;
        aMap.clear();
        aMap.moveCamera(CameraUpdateFactory.zoomTo(15));
        aMap.moveCamera(CameraUpdateFactory.changeLatLng(latLng1));
        marker = aMap.addMarker(new MarkerOptions()
                .position(latLng1)
                .title(address)
                .icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .draggable(true));
        marker.showInfoWindow();// 设置默认显示一个infowinfow
    }

    /**
     * @param list 返回的结果集合
     * @param i    返回的状态码
     */
    @Override
    public void onGetInputtips(List<Tip> list, int i) {
        if (i == 0) {
            tipList.addAll(list);
            for (Tip tip : list) {
                arrayList.add(tip.getName());
            }
            adapter.notifyDataSetChanged();
        }
    }

    //逆地理编码时将地理坐标转换为中文地址（地名描述） 的回调接口

    /**
     * @param result 返回的具体数据
     * @param rCode  返回码
     */
    @Override
    public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
        if (rCode == 0) {
            if (result != null && result.getRegeocodeAddress() != null
                    && result.getRegeocodeAddress().getFormatAddress() != null) {
                String addressName = result.getRegeocodeAddress().getFormatAddress()
                        + "附近";
//                Toast.makeText(this, addressName, Toast.LENGTH_LONG).show();
                makeMarker(latLng, addressName);
            } else {
                Toast.makeText(this, "No data", Toast.LENGTH_LONG).show();
            }
        } else if (rCode == 27) {
            Toast.makeText(this, R.string.network_unavailable, Toast.LENGTH_LONG).show();
        } else if (rCode == 32) {
//            Toast.makeText(this,R.string.network_unavailable,Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "An unknown error", Toast.LENGTH_LONG).show();
        }
    }

    //地理编码是将中文地址(或地名描述)转换为地理坐标 的回调接口

    /**
     * @param result
     * @param rCode
     */
    @Override
    public void onGeocodeSearched(GeocodeResult result, int rCode) {
        if (rCode == 0) {
            if (result != null && result.getGeocodeAddressList() != null
                    && result.getGeocodeAddressList().size() > 0) {
                GeocodeAddress address = result.getGeocodeAddressList().get(0);
                LatLonPoint point = address.getLatLonPoint();
//                Toast.makeText(this,point.getLatitude()+">>>"+point.getLongitude(), Toast.LENGTH_LONG).show();
                makeMarker(new LatLng(point.getLatitude(), point.getLongitude()), address.getFormatAddress());
            } else {
                Toast.makeText(this, "No data", Toast.LENGTH_LONG).show();
            }
        } else if (rCode == 27) {
            Toast.makeText(this, R.string.network_unavailable, Toast.LENGTH_LONG).show();
        } else if (rCode == 32) {
//            Toast.makeText(this,R.string.network_unavailable,Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "An unknown error", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker1) {
        if (marker1.equals(marker)) {
            if (aMap != null) {
                returnResult();
            }
        }
        return false;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        if (this.marker.equals(marker)) {
            if (aMap != null) {
                returnResult();
            }
        }
    }

    //返回结果到上一级
    private void returnResult() {
//        Toast.makeText(this, "ABCD", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        double[] doubles = {latLonPoint.getLongitude(), latLonPoint.getLatitude()};
        intent.putExtra(PathEditActivity.SELECTDETIALLAT, doubles);//坐标
        intent.putExtra(PathEditActivity.SELECTDETIALADD, address);//地址
        setResult(PathEditActivity.SELECTDETIAL, intent);
        finish();
    }

    /**
     * 监听自定义infowindow窗口的infowindow事件回调
     */
    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    /**
     * 监听自定义infowindow窗口的infocontents事件回调
     */
    @Override
    public View getInfoContents(Marker marker) {
        return null;
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
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            setResult(PathEditActivity.SELECTDETIAL, intent);
        }
        return super.onKeyDown(keyCode, event);
    }
}