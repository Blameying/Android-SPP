package com.example.blame.bluetoothkey;

public interface SocketHolder {
    public void close();
    public void open();
    public void register(BluetoothProxy proxy);
    public int getTYPE();
    public void update();
    public boolean isOpen();
}
