package com.example.blame.bluetoothkey;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;


public class MessageSender extends LinearLayout implements SocketHolder{

    private ControlLineView controlLineView;
    private EditText editText;
    private BluetoothProxy bluetoothProxy;
    private boolean openStatus = false;
    private ListenerForSelf listener;
    private TextView time_display;
    private int time_ms = 0;
    public static final int TYPE = BluetoothProxy.WRITE_HOLDER;

    private final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 100:
                    Toast.makeText(getContext(),"输入的字符串不是16进制",Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    public MessageSender(Context context){
        super(context);
        View view = LayoutInflater.from(context).inflate(R.layout.message_component, this);
        controlLineView = view.findViewById(R.id.message_control_line);
        editText = view.findViewById(R.id.edit);
        controlLineView.setTitle("发送");
        listener = new ListenerForSelf();
        this.controlLineView.setSwitchOnClickListener(listener);
        this.controlLineView.setCleanOnClickListener(listener);
        time_display = view.findViewById(R.id.time_display);
        time_display.setText("定时： "+String.valueOf(time_ms));
        time_display.setVisibility(VISIBLE);
        time_display.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                showTimeSelectorDialog();
            }
        });
    }


    class SendDataRunable implements Runnable {
        @Override
        public void run() {
            String text = editText.getText().toString();
            int length = text.length();
            if(length!=0){
                while(openStatus){
                    synchronized (bluetoothProxy.ringByte) {
                        if (controlLineView.isHex) {
                            text = text.replaceAll("\\s*","");
                            if(length%2!=0){
                                text+="0";
                            }
                            try {
                                bluetoothProxy.sendToDevice(text.length() / 2, stringToHex(text));
                            }catch (NumberFormatException e){
                                Message msg = new Message();
                                msg.what = 100;
                                handler.sendMessage(msg);
                                break;
                            }
                        } else {
                            bluetoothProxy.sendToDevice(text.length(), text.getBytes(StandardCharsets.US_ASCII));
                        }
                    }
                    if(time_ms!=0){
                        try {
                            Thread.sleep(time_ms);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }else{
                        break;
                    }
                }
            }else{
                close();
            }
            close();
        }
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
                    close();
                    break;
            }
        }
    }

    public byte[] stringToHex(String text){
        byte[] bytes = new byte[text.length()/2];
        byte[] text_bytes = text.getBytes();
        for(int i=0;i<bytes.length;i+=1){
            bytes[i] |= Integer.parseInt(""+(char)text_bytes[i*2]+(char)text_bytes[i*2+1],16);
        }
        return bytes;
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
        new Thread(new SendDataRunable()).start();
    }

    @Override
    public void register(BluetoothProxy proxy) {
        this.bluetoothProxy = proxy;
        proxy.attachSocketHolder(this);
    }

    private void setNumberPickerDividerColor(NumberPicker numberPicker) {
        Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    //设置分割线的颜色值
                    pf.set(numberPicker, new ColorDrawable(0xff6ec4fa));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    public static boolean setNumberPickerTextColor(NumberPicker numberPicker) {
        final int count = numberPicker.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = numberPicker.getChildAt(i);
            if (child instanceof EditText) {
                Field selectorWheelPaintField;
                try {
                    selectorWheelPaintField = numberPicker.getClass().getDeclaredField("mSelectorWheelPaint");
                    selectorWheelPaintField.setAccessible(true);
                    try {
                        ((Paint) selectorWheelPaintField.get(numberPicker)).setColor(0xff6ec4fa);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    ((EditText) child).setTextColor(0xff6ec4fa);
                    numberPicker.invalidate();
                    return true;
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }


    public void showTimeSelectorDialog(){
        final AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        final View view = LayoutInflater.from(getContext()).inflate(R.layout.number_selector_dialog,null);
        final NumberPicker number = (NumberPicker)view.findViewById(R.id.number);
        final NumberPicker time_type = (NumberPicker)view.findViewById(R.id.time_type);
        final Button button_ok = (Button)view.findViewById(R.id.ok);
        String[] types = {"ms","s","min"};
        time_type.setDisplayedValues(types);
        time_type.setMinValue(0);
        time_type.setMaxValue(2);
        time_type.setValue(0);
        time_type.setWrapSelectorWheel(true);
        time_type.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        setNumberPickerDividerColor(time_type);
        setNumberPickerTextColor(time_type);

        number.setMaxValue(1000);
        number.setMinValue(0);
        number.setValue(0);
        number.setWrapSelectorWheel(false);
        setNumberPickerDividerColor(number);
        setNumberPickerTextColor(number);

        dialog.setView(view);
        final AlertDialog alertDialog = dialog.show();
        button_ok.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                int selector_number = number.getValue();
                int selector_type = time_type.getValue();

                switch (selector_type){
                    case 0:
                        time_ms = selector_number;
                        break;
                    case 1:
                        time_ms = selector_number*1000;
                        break;
                    case 2:
                        time_ms = selector_number*1000*60;
                        break;
                }
                time_display.setText("定时： "+String.valueOf(time_ms));
                alertDialog.dismiss();
            }
        });
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

    }
}
