package com.example.blame.bluetoothkey;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ContriolActivity extends AppCompatActivity implements View.OnClickListener {

    private BluetoothSocket socket;
    private RecyclerView recyclerView =null;
    private List<ComponentItem> componentItemList;
    private ComponentAdapter componentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contriol);
        Intent intent = getIntent();
        String deviceMac = intent.getStringExtra("mac");
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter.isEnabled()){
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceMac);
            try {
                // 蓝牙串口服务对应的UUID。如使用的是其它蓝牙服务，需更改下面的字符串
                UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                socket = device.createRfcommSocketToServiceRecord(MY_UUID);
                try {
                    socket.connect();
                }catch (IOException e){
                    try{
                        Method method = device.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
                        socket = (BluetoothSocket) method.invoke(device,1);
                        socket.connect();
                    }catch (IOException exp){
                        Log.e("msg","connect failed");
                        Toast.makeText(ContriolActivity.this,"串口获取失败",Toast.LENGTH_LONG).show();
                        Intent intent_to_main = new Intent(ContriolActivity.this,MainActivity.class);
                        startActivity(intent_to_main);
                        finish();
                    }
                }
            } catch (Exception e) {
                Toast.makeText(ContriolActivity.this,"串口获取失败",Toast.LENGTH_LONG).show();
                Intent intent_to_main = new Intent(ContriolActivity.this,MainActivity.class);
                startActivity(intent_to_main);
                finish();
                return;
            }
        }else{
            Intent intent1 = new Intent(ContriolActivity.this,MainActivity.class);
            startActivity(intent1);
            finish();
        }

        recyclerView = findViewById(R.id.components_list);
        componentItemList = new ArrayList<>();
        componentItemList.add(new ComponentItem(ComponentAdapter.ComponentType.MESSAGE_RECIEVER.ordinal()));
        componentItemList.add(new ComponentItem(ComponentAdapter.ComponentType.MESSAGE_SENDER.ordinal()));
        componentItemList.add(new ComponentItem(ComponentAdapter.ComponentType.CHART.ordinal()));
        componentItemList.add(new ComponentItem(ComponentAdapter.ComponentType.BITMAP.ordinal()));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        componentAdapter = new ComponentAdapter(componentItemList,new BluetoothProxy(socket));
        recyclerView.setAdapter(componentAdapter);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(socket.isConnected()){
            try {
                socket.close();
            }catch (IOException e){
                Log.d("ControlActivity","OnDestroy socket.close failed");
            }
        }
    }

    @Override
    public void onClick(View v) {

    }

}
