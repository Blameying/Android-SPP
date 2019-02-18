package com.example.blame.bluetoothkey;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.Arrays;
import java.util.Random;

public class Chart extends LinearLayout implements SocketHolder{

    private ControlLineView controlLineView;
    private LineChart lineChart;
    private BluetoothProxy bluetoothProxy;
    private boolean openStatus = false;
    public static final int TYPE = BluetoothProxy.READ_HOLDER;
    private ListenerForSelf listener;
    private double point_count = 0;
    private String value = "";
    private boolean begin = false;
    private int count = 0;
    private Random random;

    private final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    String[] result = (String[]) msg.obj;
                    try {
                        addEntry(result[0],result[1]);
                    }catch (ArrayIndexOutOfBoundsException e){
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };

    public Chart(Context context){
        super(context);
        View view = LayoutInflater.from(context).inflate(R.layout.chart_component, this);
        controlLineView = view.findViewById(R.id.chart_control_line);
        controlLineView.setTitle("折线图");
        controlLineView.hideRadioGroup();
        lineChart = view.findViewById(R.id.chart);
        listener = new ListenerForSelf();
        this.controlLineView.setSwitchOnClickListener(listener);
        this.controlLineView.setCleanOnClickListener(listener);
        random = new Random();
        initLineChart();
        this.setFocusable(true);
        this.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    getParent().requestDisallowInterceptTouchEvent(true);
                }else {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
            }
        });
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
                    cleanChart();
                    break;
            }
        }
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

    private void initLineChart(){
        lineChart.setNoDataText("没有可用数据=.=");
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setScaleXEnabled(true);
        lineChart.setScaleYEnabled(true);
        lineChart.setDoubleTapToZoomEnabled(true);
        lineChart.setVisibleXRangeMaximum(8);
    }

    public void addEntry(String tag,String value){
        LineData lineData = lineChart.getLineData();
        String[] tags;
        if(lineData!=null) {
            tags = lineData.getDataSetLabels();
        }else {
            lineData = new LineData();
            lineChart.setData(lineData);
            tags = lineData.getDataSetLabels();
        }
        LineDataSet lineDataSet;
        if(Arrays.asList(tags).contains(tag)){
            lineDataSet = (LineDataSet) lineData.getDataSetByLabel(tag,false);
        }else {
            lineDataSet = new LineDataSet(null,tag);
            lineDataSet.setHighlightEnabled(true);
            lineDataSet.setColor(Color.argb(0xff,random.nextInt(256),random.nextInt(256),random.nextInt(256)));
            lineData.addDataSet(lineDataSet);
        }
        int i_value = 0;
        try {
            i_value = Integer.parseInt(value);
        }catch (NumberFormatException e){
            return;
        }
        Entry entry = new Entry(lineDataSet.getEntryCount(),i_value);
        lineData.addEntry(entry,lineData.getIndexOfDataSet(lineDataSet));
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();
    }

    public void cleanChart(){
        LineData lineData = lineChart.getLineData();
        lineData.clearValues();
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.openStatus = false;
    }

    public boolean isOpen(){
        return openStatus;
    }

    @Override
    public void update() {
        while (bluetoothProxy.ringByte.hasNext()){
            byte y = bluetoothProxy.ringByte.read();
            if(y=='{'){
                value = "";
                begin = true;
            }else if(begin && y!='}'){
                value += (char)y;
                count++;
                if(count>10){
                    begin = false;
                    value = "";
                    count = 0;
                }
            }else if(begin && y=='}'){
                begin = false;
                count = 0;
                Message msg = new Message();
                msg.what = 1;
                msg.obj = value.split(":");
                handler.sendMessage(msg);
            }
        }
    }
}
