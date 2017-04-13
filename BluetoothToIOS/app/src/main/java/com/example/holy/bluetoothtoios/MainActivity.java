package com.example.holy.bluetoothtoios;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.DataSetObserver;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;


import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    ListView blueToothLv;



    private BluetoothAdapter mBluetoothAdapter;

    private static final int REQUEST_ENABLE = 1001;

    private List<String> nameList = new ArrayList<String>();
    private MyAdapter myAdapter;
    private MyCallBack myCallBack;

    private BluetoothManager bluetoothManager;
    private BluetoothLeScanner bluetoothLeScanner;
    private BluetoothDevice device;
    private BluetoothGatt mBluetoothGatt;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
//           mBluetoothAdapter.stopLeScan(myCallBack2);
            bluetoothLeScanner.stopScan(myCallBack);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        blueToothLv = (ListView)findViewById(R.id.bluetooth_lv);
        myAdapter = new MyAdapter();
        blueToothLv.setAdapter(myAdapter);
        blueToothLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
        bluetoothManager =  (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if(mBluetoothAdapter.isEnabled()){
            //弹出对话框提示用户是后打开
            Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enabler, REQUEST_ENABLE);
        }
        //获取本机蓝牙名称
        String name = mBluetoothAdapter.getName();
        Logger.d(name);
        //获取本机蓝牙地址
        String address = mBluetoothAdapter.getAddress();
        Logger.d(address);

        myCallBack = new MyCallBack();
        bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        bluetoothLeScanner.stopScan(myCallBack);
        bluetoothLeScanner.startScan(myCallBack);
        mHandler.sendEmptyMessageDelayed(101,5000);

//获取已配对蓝牙设备0
//        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
//        Logger.d( "bonded device size ="+devices.size());
//        for(BluetoothDevice bonddevice:devices){
//            Logger.d( "bonded device name ="+bonddevice.getName()+" address"+bonddevice.getAddress());
//        }
//        UUID uuid = UUID.fromString("7905F431-B5CE-4E99-A40F-4B1E122D00D0");
//
//        try {
//            BluetoothServerSocket servicesocket = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("ANCS",uuid);
//            BluetoothSocket socket = servicesocket.accept();
//            socket.connect();
//            InputStream stream = socket.getInputStream();
//            byte[] b = new byte[16];
//            int i = 0;
//            while((i = stream.read(b)) != -1){
//                String str = new String(b);
//                Logger.d(str);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    class MyAdapter extends BaseAdapter {


        @Override
        public int getCount() {
            return nameList.size();
        }

        @Override
        public Object getItem(int position) {
            return nameList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if(convertView == null){
                convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.list_item,null);
                viewHolder = new ViewHolder();
                viewHolder.titleTv = (TextView)convertView.findViewById(R.id.list_item_tv);
                convertView.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.titleTv.setText((String)getItem(position));
            return convertView;
        }

        @Override
        public int getItemViewType(int position) {
            return 1;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }


    }

    class ViewHolder{
        TextView titleTv;
    }

    class MyCallBack extends ScanCallback{
        public MyCallBack() {
            super();
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            Logger.d(result.toString());
            if(result.getDevice().getName()== null){
                Logger.d(result.getDevice().getAddress());
                nameList.add(result.getDevice().getAddress());
            }else{
                Logger.d(result.getDevice().getName()+"*****"+result.getDevice().getAddress());
                nameList.add(result.getDevice().getName());
            }

            myAdapter.notifyDataSetChanged();
            if(result.getDevice().getAddress().equals("49:56:89:5C:B1:8E")){
                mHandler.sendEmptyMessage(101);
                device = result.getDevice();
                mBluetoothGatt = device.connectGatt(MainActivity.this,false,mGattCallback);
            }



        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            Logger.d(results.size());
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Logger.d(errorCode);
        }
    }

    class  MyCallBack2 implements BluetoothAdapter.LeScanCallback {

        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            Logger.d(device.toString());

        }
    }


    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, final int status, final int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            mHandler.post(new Runnable() {
                public void run() {
                    if (status != BluetoothGatt.GATT_SUCCESS) { // 连接失败判断
                        Logger.d("连接失败");
                        return;
                    }
                    if (newState == BluetoothProfile.STATE_CONNECTED) { // 连接成功判断
                        Logger.d("连接成功");
                        mBluetoothGatt.discoverServices(); // 发现服务
                        return;
                    }
                    if (newState == BluetoothProfile.STATE_DISCONNECTED) {  // 连接断开判断
                        Logger.d("连接失败");
                        return;
                    }
                }
            });
        }

                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, final int status) {
                    super.onServicesDiscovered(gatt, status);
                    if (status != BluetoothGatt.GATT_SUCCESS) { // 发现服务失败
                        Logger.d("发现服务失败");
                        return;
                    }
                    //不是失败的情况就是成功
                    Logger.d("发现服务成功"+gatt.getServices().toString());





                    for (BluetoothGattService service:gatt.getServices()) {
                        Logger.d(service.getUuid().toString());
                        if(service.getUuid().toString().equals("7905f431-b5ce-4e99-a40f-4b1e122d00d0")){

                            if(service.getIncludedServices().size() > 0){
                                for(BluetoothGattService service2:service.getIncludedServices()){
                                    Logger.d(service2.getUuid().toString());
                                }
                            }


                            BluetoothGattCharacteristic txd_charact = service.getCharacteristic(UUID.fromString("7905f431-b5ce-4e99-a40f-4b1e122d00d0")); // 再通过BluetoothGattService获取BluetoothGattCharacteristic特征值
                            if (0 != (txd_charact.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY)) { // 查看是否带有可通知属性notify
                                mBluetoothGatt.setCharacteristicNotification(txd_charact, true);
                                BluetoothGattDescriptor descriptor = txd_charact.getDescriptor(UUID.fromString("9fbf120d-6301-42d9-8c58-25e699a21dbd")); // 这包数据什么意思，可以不用管，反正是固定这包数据就是了。
                                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                mBluetoothGatt.writeDescriptor(descriptor);
                            } else if (0 != (txd_charact.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE)) {  // 查看是否带有indecation属性
                                mBluetoothGatt.setCharacteristicNotification(txd_charact, true);
                                BluetoothGattDescriptor descriptor = txd_charact.getDescriptor(UUID.fromString("9fbf120d-6301-42d9-8c58-25e699a21dbd"));
                                descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                                mBluetoothGatt.writeDescriptor(descriptor);
                            }
                            mBluetoothGatt.setCharacteristicNotification(txd_charact, true);


                        }



                    }


                }

                @Override
                public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, final int status) {
                    super.onDescriptorWrite(gatt, descriptor, status);
                    if (status != BluetoothGatt.GATT_SUCCESS) {  // 写Descriptor失败
                        Logger.d("写Descriptor失败");
                        return;
                    }
                    //不是失败的情况就是成功
                    Logger.d("写Descriptor成功");
                }

                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                    super.onCharacteristicChanged(gatt, characteristic);
                    //BLE设备主动向手机发送的数据时收到的数据回调

                    Logger.d(getHexString(characteristic.getValue()));
                }
                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    super.onCharacteristicWrite(gatt, characteristic, status);
                    if (status != BluetoothGatt.GATT_SUCCESS) {  // 写数据失败
                        return;
                    }
                }
// 还有很多其他回调方法，这里就不一一介绍了
            };


    public static String getHexString(byte[] src){
        StringBuilder sb = new StringBuilder();
        if(src.length == 0){
            return "空字符串";
        }
        for(int i=0;i<src.length;i++){
            int a = src[i]&0xff;
            sb.append(Integer.toHexString(a));
        }
        return sb.toString();
    }


}
