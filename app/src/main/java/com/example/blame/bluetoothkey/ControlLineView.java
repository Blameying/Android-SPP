package com.example.blame.bluetoothkey;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

public class ControlLineView extends LinearLayout {

    private TextView title;
    private RadioButton left;
    private RadioButton right;
    private RadioGroup radioGroup;
    private ImageButton status_switch;
    private ImageButton clean;
    private boolean status;
    public boolean isHex;

    public ControlLineView(Context context, AttributeSet attrs){
        super(context,attrs);
        LayoutInflater.from(context).inflate(R.layout.control_line, this);

        status = false;
        title = (TextView) findViewById(R.id.title);
        left = (RadioButton) findViewById(R.id.hex);
        right = (RadioButton) findViewById(R.id.ascii);
        radioGroup = (RadioGroup) findViewById(R.id.radio_group);
        status_switch = (ImageButton) findViewById(R.id.status_switch);
        clean = (ImageButton) findViewById(R.id.clean);
        isHex = true;

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.hex:
                        isHex = true;
                        break;
                    case R.id.ascii:
                        isHex =false;
                        break;
                }
            }
        });
    }

    //getter
    public TextView getTitleView(){
        return title;
    }

    public RadioButton getLeftRadioButton(){
        return left;
    }

    public RadioButton getRightRadioButton(){
        return right;
    }

    public ImageButton getStatus_switch() {
        return status_switch;
    }

    public ImageButton getClean(){
        return clean;
    }

    public RadioGroup getRadioGroup(){
        return radioGroup;
    }


    public void setTitle(String text){
        this.title.setText(text);
    }

    public String getTitle(){
        return this.title.getText().toString();
    }

    public int getSelected(){
        int id = radioGroup.getCheckedRadioButtonId();
        switch (id){
            case R.id.hex:
                return 1;
            case R.id.ascii:
                return 2;
        }
        return 0;
    }

    public void setSwitchOnClickListener(View.OnClickListener listener){
        this.status_switch.setOnClickListener(listener);
    }

    public void setCleanOnClickListener(View.OnClickListener listener){
        this.clean.setOnClickListener(listener);
    }

    public void changeSwitchStatusIcon(boolean status){
        if(status){
            this.status_switch.setImageResource(R.mipmap.pause);
        }else {
            this.status_switch.setImageResource(R.mipmap.start);
        }
        this.status = status;
    }

    public boolean getStatus(){
        return this.status;
    }

    public void hideRadioGroup(){
        this.radioGroup.setVisibility(INVISIBLE);
    }

    public void showRadioGroup(){
        this.radioGroup.setVisibility(VISIBLE);
    }
}