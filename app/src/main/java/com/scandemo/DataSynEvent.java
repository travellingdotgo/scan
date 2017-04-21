package com.scandemo;

public class DataSynEvent {
    private int count;
    float score;

    public DataSynEvent(int i, float score){
        this.count = i;
        this.score = score;
    }

    public int getCount() {
        return count;
    }
    public float getFloat() {
        return score;
    }

    public void setCount(int count) {
        this.count = count;
    }
}