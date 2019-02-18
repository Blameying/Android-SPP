package com.example.blame.bluetoothkey;

import android.bluetooth.BluetoothSocket;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class BluetoothProxy{

    private BluetoothSocket socket=null;
    private InputStream inputStream = null;
    private OutputStream outputStream = null;
    private List<SocketHolder> componentsList = null;
    private SocketHolder currentReadSocketHolder = null;
    private SocketHolder currentWriteHolder = null;
    public RingByte ringByte;
    public static final int WRITE_HOLDER = 1;
    public static final int READ_HOLDER = 0;


    public BluetoothProxy(BluetoothSocket socket) {
        componentsList = new ArrayList<SocketHolder>();
        this.socket = socket;
        try {
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
        }catch (IOException e){
            Log.e("BluetoothProxy","connect error",e);
            Log.d("BluetoothProxy","connect error");
        }
        ringByte = new RingByte(10*1024);
        new GetData().start();
    }

    public void attachSocketHolder(SocketHolder socketHolder){
        this.componentsList.add(socketHolder);
    }


    public void disattachSocketHolder(SocketHolder socketHolder){
        this.componentsList.remove(socketHolder);
    }

    public void setCurrentSocketHolder(SocketHolder socketHolder){
        for(SocketHolder sh:componentsList){
            if(sh == socketHolder){
                if(socketHolder.getTYPE()==WRITE_HOLDER){
                    this.currentWriteHolder = sh;
                }else {
                    this.currentReadSocketHolder = sh;
                }
            }else{
                if(sh.getTYPE()==socketHolder.getTYPE()) {
                    sh.close();
                }
            }
        }
    }

    public void sendToDevice(final int len, final byte[] buffer){
        try {
            outputStream.write(buffer,0,len);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    class GetData extends Thread{
        @Override
        public void run() {
            super.run();
            while (true){
                int data = 0;
                try {
                    data = inputStream.read();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (data != -1) {
                        ringByte.write((byte) data);
                    }else {
                        break;
                    }
                if (currentReadSocketHolder != null && currentReadSocketHolder.isOpen() == true) {
                    currentReadSocketHolder.update();
                }
            }
        }
    }
}
