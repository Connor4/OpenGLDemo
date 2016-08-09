package com.connor.myapplication.data;

/**
 * Created by meitu on 2016/7/7.
 */
public class PointBean {
    private float x;
    private float y;
    private float z;


    public PointBean(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public PointBean(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }
}
