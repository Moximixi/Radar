package com.example.radar_project;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * 主界面
 */
public class MainActivity extends AppCompatActivity {
    private TextView title;
    private ImageView refresh;//刷新按钮
    private LocationClient mLocationClient;
    private String receiveMs;
    private boolean isFirstLocate =true;
    private String FromWho;//该短信的来自者
    private MapView mapView;
    private double latitude;//我的位置的纬度
    private double longitude;//我的位置的精度
    private double rece_latitude;//接收到的位置的纬度
    private double rece_longitude;//接收到的位置的精度
    private BaiduMap baiduMap;
    private String tag="MainActivity";//方便调试
    private ImageView findfriends;
    private ImageView locatemyself;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());//监听器注册
        //去除标题栏
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }

        //去除状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //地图的初始化操作一定要在setContentView之前调用
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        //初始化控件
        initView();
        //实例化类
        setListener();
        //申请权限
        definePermission();
        //发送与接收短信
        Send_AccepteMes();
        //设置标题字体样式
        Typeface typeFace = Typeface.createFromAsset(getAssets(),"7thi.ttf");
        title.setTypeface(typeFace);
        //生成地图
        baiduMap=mapView.getMap();
        //删除百度地图logo
        mapView.removeViewAt(1);
        baiduMap.setMyLocationEnabled(true);//开启让"我"显示在地图上的功能
        mapView.showZoomControls(false);//禁用缩放按钮
    }

    private void setListener() {
        findfriends.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,friends_list.class);
                startActivity(intent);
            }
        });
        //回到自己位置上
        myButtonListener myButtonListener=new myButtonListener();
        locatemyself.setOnClickListener(myButtonListener);
    }

    /**
     * 发送与接收短信
     */
    private void Send_AccepteMes() {
        //发送短信
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.SEND_SMS)!=PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.SEND_SMS},1);
                }else{//开始发送短信
                    //sendSMS(toWho,"where are you?");
                    List<Friend_database> friends=DataSupport.findAll(Friend_database.class);
                    for(Friend_database friend:friends){//给数据库里面的每个人发短信
                        sendSMS(friend.getNum(),"where are you?");
                        Toast.makeText(MainActivity.this,"成功发送一条",Toast.LENGTH_SHORT).show();
                    }
                }

                //Toast.makeText(MainActivity.this,"How are you?",Toast.LENGTH_SHORT).show();
            }
        });

        //动态注册并实例化接收短信类
        IntentFilter intentFilter1 = new IntentFilter();
        intentFilter1.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        intentFilter1.addAction("android.provider.Telephony.SMS_RECEIVED");
        MessageReceiver messageReceiver = new MessageReceiver();
        registerReceiver(messageReceiver, intentFilter1);
    }

    /**
     * 初始化控件
     */
    private void initView() {
        mapView=findViewById(R.id.bmapView);//找到百度地图显示的控件
        title=findViewById(R.id.title);
        refresh=findViewById(R.id.refresh);
        findfriends=findViewById(R.id.findFriends);
        locatemyself=findViewById(R.id.locateMyself);
    }

    /**
     * 初始监听事件
     */
    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location){
            //获得自己的经纬度
            latitude=location.getLatitude();
            longitude=location.getLongitude();

            if (location.getLocType() == BDLocation.TypeGpsLocation
                    || location.getLocType() == BDLocation.TypeNetWorkLocation) {//通过网络或者GPS获得定位
                navigateTo(location);
            }
        }
    }

    /**
     * 结束时释放资源
     */
    @Override
    protected void onResume(){
        super.onResume();
        mapView.onResume();
    }
    @Override
    protected void onPause(){
        super.onPause();
        mapView.onPause();
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        mLocationClient.stop();//该活动结束时停止定位
        mapView.onDestroy();
        baiduMap.setMyLocationEnabled(false);//关闭(让"我"显示在图片上)
    }

    /**
     * 动态申请权限
     */
    private void definePermission() {
        List<String> permissionList = new ArrayList<>();//权限集合（将没有被授权的权限添加进来）
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String [] permissions = permissionList.toArray(new String[permissionList.size()]);//集合转为数组
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);//一次性申请还未申请的
        } else {//权限都已经申请
            requestLocation();
        }
    }

    /**
     * 开始定位
     */
    private void requestLocation(){
        mLocationClient.start();//开始定位
        initLocation();
    }

    /**
     * 移到我的位置上
     * @param location
     */
    private void navigateTo(BDLocation location){
        if(isFirstLocate){//第一次定位移到我的位置上
            LatLng ll=new LatLng(location.getLatitude(),location.getLongitude());//获得当前位置的纬度和精度
            MapStatusUpdate update=MapStatusUpdateFactory.newLatLng(ll);
            baiduMap.animateMapStatus(update);
            update=MapStatusUpdateFactory.zoomTo(16f);//缩放比例16倍
            baiduMap.animateMapStatus(update);
            isFirstLocate=false;//表示不是第一次定位了,防止多次调用animateMapStatus()方法
        }
        //将点标记在我的位置上
        MyLocationData.Builder locationBuilder=new MyLocationData.Builder();
        locationBuilder.latitude(location.getLatitude());
        locationBuilder.longitude(location.getLongitude());//将当前位置封装起来
        MyLocationData locationData=locationBuilder.build();//取出来并显示
        baiduMap.setMyLocationData(locationData);
        //标点和连线


    }

    /**
     * 设置定位的时间间隔
     */
    private void initLocation(){
        LocationClientOption option=new LocationClientOption();
        //option.setScanSpan(5000);//每5秒更新一次位置
        mLocationClient.setLocOption(option);
    }

    /**
     * 请求授权的回调
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    //请求授权的结果会回调到下面的函数中，第二个参数为要申请的权限，其中第三个参数就是授权结果
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){
        switch (requestCode){
            case 1:
                if(grantResults.length>0){
                    for(int result:grantResults){
                        if(result!=PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(this,"请你必须同意所有的权限才能使用本程序哟",Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();//没有任何错误就开始定位
                }else{//一条权限都没有申请
                    Toast.makeText(this,"发生未知错误",Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;

        }
    }


    /**
     * 发送短信的方法
     * @param phoneNumber
     * @param message
     */
    public void sendSMS(String phoneNumber,String message){
        //获取短信管理器
        SmsManager smsManager = SmsManager.getDefault();
        //拆分短信内容（手机短信长度限制）
        List<String> divideContents = smsManager.divideMessage(message);
        /**
         * 发送情况的回调函数
         */
        //sendPI
        String SENT_SMS_ACTION = "SENT_SMS_ACTION";
        Intent sentIntent = new Intent(SENT_SMS_ACTION);
        PendingIntent sentPI = PendingIntent.getBroadcast(MainActivity.this, 0, sentIntent, 0);
            // register the Broadcast Receivers
        MainActivity.this.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context _context, Intent _intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(MainActivity.this, "短信发送成功", Toast.LENGTH_SHORT)  .show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        break;
                }
            }
        }, new IntentFilter(SENT_SMS_ACTION));

        /**
         *接收情况的回调函数
         */
        String DELIVERED_SMS_ACTION = "DELIVERED_SMS_ACTION";
        Intent deliverIntent = new Intent(DELIVERED_SMS_ACTION);
        PendingIntent deliverPI = PendingIntent.getBroadcast(MainActivity.this, 0, deliverIntent, 0);
        MainActivity.this.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context _context, Intent _intent) {
                Toast.makeText(MainActivity.this, "收信人已经成功接收", Toast.LENGTH_SHORT).show();
            }
        }, new IntentFilter(DELIVERED_SMS_ACTION));

        for (String text : divideContents) {
            smsManager.sendTextMessage(phoneNumber, null, text, sentPI, deliverPI);
        }
    }
    /**
     * 接收短信的类
     */
    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Toast.makeText(MainActivity.this,"注册成功!",Toast.LENGTH_SHORT).show();
            Bundle bundle = intent.getExtras();
            Object[] pdus = (Object[]) bundle.get("pdus"); // 提取短信消息
            SmsMessage[] messages = new SmsMessage[pdus.length];
            for (int i = 0; i < messages.length; i++) {
                messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
            }
            String address = messages[0].getOriginatingAddress(); // 获取发送方号码
            String fullMessage = "";
            for (SmsMessage message : messages) {
                fullMessage += message.getMessageBody(); // 获取短信内容
            }
            receiveMs=fullMessage;
            FromWho=address;
            //Toast.makeText(MainActivity.this,"address; " + address + ", message: " + fullMessage,Toast.LENGTH_SHORT).show();
            if(receiveMs.equals("where are you?")){//首次问我在哪里
                sendSMS(FromWho,latitude+","+longitude);
            }else if(receiveMs.contains(",")){//得到消息后
                //接收经纬度
                String[] locate= receiveMs.split(",");
                rece_latitude=Double.parseDouble(locate[0]);
                rece_longitude=Double.parseDouble(locate[1]);
//                Log.d(tag,"收到的纬度为"+rece_latitude);
//                Log.d(tag,"收到的精度为"+rece_longitude);
                //然后开始画点
                setMarker(rece_latitude,rece_longitude);
                //并连线
                connectLine(rece_latitude,rece_longitude);


                //回复感谢
                //sendSMS(FromWho,"谢谢你的回复!");
            }else{

            }
            abortBroadcast();//然后截断这个广播
        }
    }
    /**
     * 标点
     */
    private void setMarker(Double latitude,Double longitude){
        //清除地图上所有覆盖物
        baiduMap.clear();
        //定义Maker坐标点
        LatLng point = new LatLng(latitude,longitude);
        //构建Marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.green);
        //构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions().position(point).icon(bitmap);
        //在地图上添加Marker，并显示
        baiduMap.addOverlay(option);
    }
    /**
     * 连线
     */
    private void connectLine(Double de_latitude,Double de_longitude){
        List<LatLng> points = new ArrayList<LatLng>();
        points.add(new LatLng(latitude,longitude));//起点为自己的位置
        points.add(new LatLng(de_latitude,de_longitude));
        List<Integer> colors = new ArrayList<>();
        colors.add(Integer.valueOf(Color.BLUE));
        OverlayOptions ooPolyline = new PolylineOptions().width(10)
                .colorsValues(colors).points(points);
        Polyline mPolyline = (Polyline) baiduMap.addOverlay(ooPolyline);
    }

    /**
     * 移动到最初的位置上的类
     */
    //移动我的位置上的事件
    class myButtonListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            LatLng ll=new LatLng(latitude,longitude);
            MapStatusUpdate update=MapStatusUpdateFactory.newLatLng(ll);
            baiduMap.animateMapStatus(update);
        }
    }
}
