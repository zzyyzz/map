package share.s10;

import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;



public class MainActivity extends AppCompatActivity {

    private MapView mapView;
    private BaiduMap baiduMap;
    //定位相关
    private LocationClient mLocationClient;
    private MyLocationListener mLocationListener;
    private boolean isFirstIn = true;

   //下面两个图片相关
    private ImageView im1;
    private ImageView im2;

    //三个按钮
    private Button bt4;
    private Button bt5;
    private Button bt6;
    PendingIntent paIntent;
    SmsManager smsManager;

    //回复相关
     private String longitude;
     private String latitude;

    //新加的数据库相关
    private NotesDB notesDB;
    private SQLiteDatabase dbReader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //百度地图相关
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        initView();
        initLocation();

        //图片点击相关
        friendsonClickListener listen2=new friendsonClickListener();
        im2=(ImageView)findViewById(R.id.im2);
        im2.setOnClickListener(listen2);

        //地图标记相关
        //定义Maker坐标点
        LatLng point = new LatLng(22.25517,131.54);
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.biaoji3);
        OverlayOptions option = new MarkerOptions()
                .position(point)
                .icon(bitmap);
        baiduMap.addOverlay(option);

        //3个按钮相关
        bt4=(Button)findViewById(R.id.bt4);
        bt5=(Button)findViewById(R.id.bt5);
        bt6=(Button)findViewById(R.id.bt6);
        bt4OnClickListener bt4On=new bt4OnClickListener();
        bt5OnClickListener bt5On=new bt5OnClickListener();
        bt6OnClickListener bt6On=new bt6OnClickListener();
        bt4.setOnClickListener(bt4On);
        bt5.setOnClickListener(bt5On);
        bt6.setOnClickListener(bt6On);

        //短信相关
        paIntent = PendingIntent.getBroadcast(this, 0, new Intent(), 0);
        smsManager = SmsManager.getDefault();

        //数据库相关
        notesDB=new NotesDB(this);
        dbReader=notesDB.getReadableDatabase();
    }

    class  friendsonClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            Intent intent2=new Intent();
            intent2.setClass(MainActivity.this,friends.class);
            startActivity(intent2);
        }
    }


    private void initLocation() {

        mLocationClient = new LocationClient(this);
        mLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(mLocationListener);

        LocationClientOption option = new LocationClientOption();
        option.setCoorType("bd09ll");
        option.setIsNeedAddress(true);
        option.setOpenGps(true);
        option.setScanSpan(1000);
        mLocationClient.setLocOption(option);
    }

    private void initView() {
        mapView= (MapView)findViewById(R.id.bmapView);
        baiduMap=mapView.getMap();
    }

    @Override
    protected void onStart() {
        super.onStart();
        baiduMap.setMyLocationEnabled(true);
        if(!mLocationClient.isStarted()){
            mLocationClient.start();
        }
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
    protected void onStop() {
        super.onStop();
        baiduMap.setMyLocationEnabled(false);
        mLocationClient.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    private class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {


            MyLocationData data = new MyLocationData.Builder()//
                    .accuracy(bdLocation.getRadius())//
                    .latitude(bdLocation.getLatitude())//
                    .longitude(bdLocation.getLongitude())//
                    .build();
            baiduMap.setMyLocationData(data);
            longitude= String.valueOf(bdLocation.getLongitude());
            latitude= String.valueOf(bdLocation.getLatitude());
            if(isFirstIn){
                LatLng latLng=new LatLng(bdLocation.getLatitude(),bdLocation.getLongitude());
                MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);
                baiduMap.animateMapStatus(msu);
                isFirstIn=false;

            }
        }
    }


   //3个按钮相关的OnClickListener
    class bt4OnClickListener implements View.OnClickListener{

       @Override
       public void onClick(View view) {
           int i=0;
           String str1=null;
           for (i=0;i<2;i++) {
               Cursor cursor1 = dbReader.query(NotesDB.TABLE_NAME, null, null, null, null, null, null);
               cursor1.moveToPosition(i);
               str1 = cursor1.getString(cursor1.getColumnIndex("content"));
               smsManager.sendTextMessage(str1, null, "where are you？", paIntent,
                       null);
           }
       }
   }


    class bt5OnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            sendSMS("");
        }
    }

    private void sendSMS(String number)
    {

        String smsBody=longitude+"/"+latitude;
        Uri smsToUri = Uri.parse("smsto:"+number);
        Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);
        intent.putExtra("sms_body", smsBody);
        startActivity(intent);
    }
    class bt6OnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {

        }
    }


}
