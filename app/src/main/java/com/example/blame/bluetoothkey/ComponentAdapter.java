package com.example.blame.bluetoothkey;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.List;

public class ComponentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ComponentItem> components;
    private BluetoothProxy proxy;

    public static enum ComponentType {
        MESSAGE_SENDER,
        MESSAGE_RECIEVER,
        CHART,
        BITMAP
    }

    public ComponentAdapter(List<ComponentItem> components,BluetoothProxy proxy){
        super();
        this.components = components;
        this.proxy = proxy;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.component_item,viewGroup,false);
        RecyclerView.ViewHolder viewHolder = null;
        if(i==ComponentType.MESSAGE_RECIEVER.ordinal()){
            viewHolder = new MessageReceiverViewHolder(view);
        }else if(i==ComponentType.MESSAGE_SENDER.ordinal()){
            viewHolder = new MessageSenderViewHolder(view);
        }else if(i==ComponentType.CHART.ordinal()){
            viewHolder = new ChartViewHolder(view);
        }else if(i==ComponentType.BITMAP.ordinal()){
            viewHolder = new BitmapViewHolder(view);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

    }

    @Override
    public int getItemViewType(int position) {
        return components.get(position).getType();
    }

    @Override
    public int getItemCount() {
        return components.size();
    }

    public class MessageSenderViewHolder extends RecyclerView.ViewHolder{

        public MessageSender messageSender;

        public MessageSenderViewHolder(View itemView){
            super(itemView);
            messageSender = new MessageSender(itemView.getContext());
            messageSender.register(proxy);
            LinearLayout linearLayout = itemView.findViewById(R.id.item_block);
            linearLayout.addView(messageSender);
        }
    }

    public class MessageReceiverViewHolder extends RecyclerView.ViewHolder{

        public MessageReciever messageReciever;

        public MessageReceiverViewHolder(View itemView){
            super(itemView);
            messageReciever = new MessageReciever(itemView.getContext());
            messageReciever.register(proxy);
            LinearLayout linearLayout = itemView.findViewById(R.id.item_block);
            linearLayout.addView(messageReciever);
        }
    }

    public class ChartViewHolder extends RecyclerView.ViewHolder{

        public Chart chart;

        public ChartViewHolder(View itemView){
            super(itemView);
            chart = new Chart(itemView.getContext());
            chart.register(proxy);
            LinearLayout linearLayout = itemView.findViewById(R.id.item_block);
            linearLayout.addView(chart);
        }
    }

    public class BitmapViewHolder extends RecyclerView.ViewHolder{
        public BitmapComponent bitmapComponent;

        public BitmapViewHolder(View itemView){
            super(itemView);
            bitmapComponent = new BitmapComponent(itemView.getContext());
            bitmapComponent.register(proxy);
            LinearLayout linearLayout = itemView.findViewById(R.id.item_block);
            linearLayout.addView(bitmapComponent);
        }
    }


}
