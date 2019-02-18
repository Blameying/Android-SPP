package com.example.blame.bluetoothkey;

public class RingByte {
    private byte[] text;
    private int readPos;
    private int writePos;
    private int length;

    public RingByte(int length){
        text = new byte[length];
        for (int i=0;i<length;i++){
            text[i]=0;
        }
        readPos = writePos = 0;
        this.length = length;
    }

    public synchronized boolean write(byte data){
        text[writePos] = data;
        if((writePos+1)%length!=readPos) {
            writePos = (writePos + 1) % length;
        }else {
            return false;
        }
        return true;
    }

    public byte[] readAll(){
        if(readPos < writePos){
            byte[] left = new byte[writePos-readPos];
            System.arraycopy(text,readPos,left,0,writePos-readPos);
            readPos = writePos%length;
            return left;
        }else{
            byte[] right = new byte[length-readPos+writePos];
            System.arraycopy(text,readPos,right,0,length-readPos);
            System.arraycopy(text,0,right,length-readPos,writePos);
            readPos = writePos%length;
            return right;
        }
    }

    public byte read(){
        if(readPos%length!=writePos) {
            byte result = text[readPos];
            readPos = (readPos+1)%length;
            return result;
        }else {
            return 0;
        }
    }

    public boolean hasNext(){
        if(readPos%length!=writePos)
            return true;
        else
            return false;
    }
}
