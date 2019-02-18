package com.example.blame.bluetoothkey;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private BluetoothAdapter bluetoothAdapter;
    private TextView title_text;
    private Button title_button;
    private Set<BluetoothDevice> pairedDevices;
    private RecyclerView recyclerView;
    private MyBluetoothAdapter myBluetoothAdapter=null;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        title_text = findViewById(R.id.main_title_text);
        title_button = findViewById(R.id.main_title_button);
        title_button.setOnClickListener(this);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        handler = new Handler();
        recyclerView = findViewById(R.id.bluetoothList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        switchStatus(bluetoothAdapter.isEnabled());
        if(!bluetoothAdapter.isEnabled()){
            if(bluetoothAdapter.enable()){
                switchStatus(true);
            }else{
                Toast.makeText(this,"打开蓝牙失败",Toast.LENGTH_LONG).show();
            }
        }
    }

    private void switchStatus(Boolean status){
        if(status){
            title_text.setText(R.string.open_status);
            title_text.setTextColor(ContextCompat.getColor(this,R.color.green));
            title_button.setText(R.string.close_button);
        }else{
            title_text.setText(R.string.close_status);
            title_text.setTextColor(ContextCompat.getColor(this,R.color.red));
            title_button.setText(R.string.open_button);
        }
        createRecycleView(status);
    }

    private void createRecycleView(Boolean status){
        if(status) {
            new Thread(){
                @Override
                public void run() {
                    super.run();
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            pairedDevices = bluetoothAdapter.getBondedDevices();
                            if(myBluetoothAdapter==null){
                                myBluetoothAdapter = new MyBluetoothAdapter(MainActivity.this,pairedDevices);
                                recyclerView.setAdapter(myBluetoothAdapter);
                            }else{
                                myBluetoothAdapter.setDevices(pairedDevices);
                                myBluetoothAdapter.notifyDataSetChanged();
                            }
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                    };
                    handler.post(runnable);
                }
            }.start();
        }else{
            recyclerView.setVisibility(View.INVISIBLE);
        }
    }

    public void startControlActivity(BluetoothDevice device){
        Intent intent = new Intent(MainActivity.this,ContriolActivity.class);
        intent.putExtra("mac",device.getAddress());
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.main_title_button:
                if(bluetoothAdapter.isEnabled()){
                    if(bluetoothAdapter.disable()){
                        switchStatus(false);
                    }else
                    {
                        Toast.makeText(this,"关闭蓝牙失败",Toast.LENGTH_LONG).show();
                    }
                }else{
                    if(bluetoothAdapter.enable()){
                        switchStatus(true);
                    }else{
                        Toast.makeText(this,"打开蓝牙失败",Toast.LENGTH_LONG).show();
                    }
                }
                break;
                default:
                    break;
        }
    }
}
