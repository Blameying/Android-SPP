package com.example.blame.bluetoothkey;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MyBluetoothAdapter extends RecyclerView.Adapter<MyBluetoothAdapter.ViewHolder> {

    private List<BluetoothDevice> pairedDevices;
    private MainActivity parent;

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        TextView mac;

        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.bluetooth_device_name_item);
            mac = itemView.findViewById(R.id.bluetooth_device_mac_item);
        }
    }

    public MyBluetoothAdapter(MainActivity parent,Set<BluetoothDevice> devices){
        pairedDevices = new ArrayList<BluetoothDevice>();
        pairedDevices.addAll(devices);
        this.parent = parent;
    }

    public void setDevices(Set<BluetoothDevice> devices){
        pairedDevices.clear();
        pairedDevices.addAll(devices);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.bluetooth_device_item,viewGroup,false);
        final ViewHolder viewHolder = new ViewHolder(view);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = viewHolder.getAdapterPosition();
                BluetoothDevice btDevice = pairedDevices.get(position);
                parent.startControlActivity(btDevice);
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        BluetoothDevice btDevice = pairedDevices.get(i);
        viewHolder.name.setText(btDevice.getName());
        viewHolder.mac.setText(btDevice.getAddress());
    }

    @Override
    public int getItemCount() {
        return pairedDevices.size();
    }
}
