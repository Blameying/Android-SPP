package com.example.blame.bluetoothkey;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class BitmapComponent extends LinearLayout implements SocketHolder {

    private ControlLineView controlLineView;
    private ImageView imageView;
    private BluetoothProxy bluetoothProxy;
    private boolean openStatus = false;
    public static final int TYPE = BluetoothProxy.READ_HOLDER;
    private ListenerForSelf listener;
    private int status = 0;
    private byte[] buffer = new byte[4+1024*768+3];
    private int index = 0;
    private byte[] copy = new byte[4+1024*768];

    private final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    int length = msg.arg1;
                    byte[] data = (byte[])msg.obj;
                    int[] copy;
                    int width = 0;
                    int height = 0;
                    synchronized (data) {
                        width = ((data[1] & 0xff) << 8) | (data[0] & 0xff);
                        height = ((data[3] & 0xff) << 8) | (data[2] & 0xff);
                        copy = new int[width*height];
                        for(int i=4,index = 0;i<width*height+4;i++,index++){
                            copy[index] = Color.argb(0xff, 0xff&data[i], 0xff& data[i], 0xff&data[i]);
                        }
                    }
                    try {
                        flashImg(width,height,copy);
                    }catch (Exception e){
                        e.printStackTrace();
                        Toast.makeText(getContext(),"格式错误",Toast.LENGTH_SHORT).show();
                    }
                    break;

            }
        }
    };

    public BitmapComponent(Context context){
        super(context);
        View view = LayoutInflater.from(context).inflate(R.layout.bitmap_component, this);
        controlLineView = view.findViewById(R.id.bitmap_control_line);
        controlLineView.setTitle("位图");
        controlLineView.hideRadioGroup();
        listener = new ListenerForSelf();
        imageView = view.findViewById(R.id.bitmap_img);
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
                    cleanImg();
                    break;
            }
        }
    }

    public void flashImg(int width,int height,int[] data){
        Bitmap bitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        bitmap.setPixels(data,0,width,0,0,width,height);
        imageView.setImageBitmap(bitmap);
    }

    public void cleanImg(){
        imageView.setImageResource(R.mipmap.xiuxiu);
    }

    @Override
    public void close() {
        this.controlLineView.changeSwitchStatusIcon(false);
        openStatus = false;
    }

    @Override
    public void open() {
        this.controlLineView.changeSwitchStatusIcon(true);
        this.bluetoothProxy.setCurrentSocketHolder(this);
        openStatus = true;
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
    }

    public boolean isOpen(){
        return openStatus;
    }

    @Override
    public void update() {
        synchronized (bluetoothProxy.ringByte){
            while (bluetoothProxy.ringByte.hasNext()){
                byte y = bluetoothProxy.ringByte.read();
                if(y=='[' && status==0){
                    status = 1;
                }else if(y=='[' && status==1){
                    status = 2;
                }else if(y=='[' && status==2){
                    status = 3;
                }else if(y!='[' && status!=3){
                    status = 0;
                }else if(status==3){
                    if(index<1024*768+4+3){
                        buffer[index] = y;
                        if(index>3 && buffer[index]==']'&&buffer[index-1]==']'&&buffer[index-2]==']'){
                            status = 0;
                            index = index-3;
                            Message msg = new Message();
                            synchronized (copy){
                                System.arraycopy(buffer,0,copy,0,index+1);
                                msg.what = 1;
                                msg.obj = copy;
                                msg.arg1 = index+1;
                                handler.sendMessage(msg);
                            }
                            index = 0;
                        }else {
                            index++;
                        }
                    }else {
                        status = 0;
                        index = 0;
                    }
                }
            }
        }
    }
}
