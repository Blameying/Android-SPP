package com.example.blame.bluetoothkey;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import com.example.blame.bluetoothkey.BluetoothProxy;
import com.example.blame.bluetoothkey.ControlLineView;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


public class MessageReciever extends LinearLayout implements SocketHolder {

    private ControlLineView controlLineView;
    private EditText editText;
    private BluetoothProxy bluetoothProxy;
    private boolean openStatus = false;
    private ListenerForSelf listener;
    public static final int TYPE = BluetoothProxy.READ_HOLDER;

    private final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    String temp = "";
                    byte[] data = (byte[])msg.obj;
                    if(controlLineView.isHex){
                        for(byte b:data){
                            temp = Integer.toHexString(0xff & b);
                            if(temp.length()==1){
                                temp = "0"+temp;
                            }
                            temp = temp + " ";
                            editText.append(temp);
                        }
                    }else {
                        temp = new String(data, StandardCharsets.US_ASCII);
                        editText.append(temp);
                    }
                    break;
            }
        }
    };

    public MessageReciever(Context context){
        super(context);
        View view = LayoutInflater.from(context).inflate(R.layout.message_component, this);
        controlLineView = view.findViewById(R.id.message_control_line);
        editText = view.findViewById(R.id.edit);
        controlLineView.setTitle("接收");
        listener = new ListenerForSelf();
        this.controlLineView.setSwitchOnClickListener(listener);
        this.controlLineView.setCleanOnClickListener(listener);
    }

    class ListenerForSelf implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id){
                case R.id.status_switch:
                    if(controlLineView.getStatus()){
                        close();
                    }else {
                        open();
                    }
                    break;
                case R.id.clean:
                    editText.setText("");
            }
        }
    }

    @Override
    public void close() {
        this.controlLineView.changeSwitchStatusIcon(false);
        this.openStatus = false;
    }

    @Override
    public void open() {
        this.controlLineView.changeSwitchStatusIcon(true);
        this.bluetoothProxy.setCurrentSocketHolder(this);
        this.openStatus = true;
        this.update();
    }

    @Override
    public void register(BluetoothProxy proxy) {
        this.bluetoothProxy = proxy;
        proxy.attachSocketHolder(this);
    }

    @Override
    public int getTYPE() {
        return TYPE;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        openStatus = false;
    }

    public boolean isOpen(){
        return openStatus;
    }

    @Override
    public void update() {
        Message msg = new Message();
        msg.what = 1;
        synchronized (bluetoothProxy.ringByte) {
            if (bluetoothProxy.ringByte.hasNext()) {
                msg.obj = bluetoothProxy.ringByte.readAll();
                handler.sendMessage(msg);
            }
        }
    }
}
