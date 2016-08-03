package com.connor.myapplication.util;

import android.util.Log;

import com.connor.myapplication.data.Constant;
import com.connor.myapplication.data.PointBean;
import com.connor.myapplication.program.TextureHelper;

/**
 * Created by meitu on 2016/7/15.
 */
public class PictureUtil {
    public static float mStrideX ;
    public static float mStrideY ;
    public static float mFBOStrideX ;
    public static float mFBOStrideY;

    /**
     * 根据相片比例计算纹理所在坐标
     *
     * @return
     */
    public static float[] calculateVertices() {
        float width = 1.0f;
        float height = 1.0f;
        float mPicRatio = TextureHelper.getBitmapOptions();
        //确定是横屏还是竖屏,对应的方向应该占满屏幕,另外一边按照相片比例和SurfaceView比例计算
        if (mPicRatio > 1) {
            height = 1.0f;
            //对应的一边所占比
            // 1 / surfaceheight = x / (sufaceheight / ratio)
            width = ((float) Constant.mSurfaceViewWidth / ((float) Constant.mSurfaceViewHeight /
                    mPicRatio));
            if (width > 1.0f) {//r如果计算出来的宽度大于屏幕的，取1
                width = 1.0f;
            }
            //设置纹理大小
            Constant.TextureWidth = (int) (width * Constant.mSurfaceViewWidth);
            Constant.TextureHeight = Constant.mSurfaceViewHeight;

        } else {
            width = 1.0f;
            //surfacewidth所占为1.0f,图片宽高比乘以surfacewidth就是图片高度
            //在屏幕上所占大小,再跟surfaceheight除以,得到的就是当高也为1.0f时
            //相片的高应该所占比例
            height = (((float) Constant.mSurfaceViewWidth * mPicRatio) / (float) Constant
                    .mSurfaceViewHeight);

            //设置纹理大小
            Constant.TextureWidth = Constant.mSurfaceViewWidth;
            Constant.TextureHeight = (int) (height * Constant.mSurfaceViewHeight);
        }

        //用于设置点击区域大小
        Constant.AreaWidth = width;
        Constant.AreaHeight = height;
        //计算间隔
        calculateStride();
        calculateFBOStride();

        float[] vertices = new float[]
                {
                        // X, Y, S, T
                        0f, 0f, 0.5f, 0.5f,
                        -width, -height, 0.0f, 1.0f,
                        width, -height, 1.0f, 1.0f,
                        width, height, 1.0f, 0.0f,
                        -width, height, 0.0f, 0.0f,
                        -width, -height, 0.0f, 1.0f
                };

        return vertices;
    }

    /**
     * 放置背景图的顶点
     */
    public static float[] calculateOppositeVertices() {
        float[] vertices = new float[]
                {
                        // X, Y, S, T
                        0f, 0f, 0.5f, 0.5f,
                        -1.0f, -1.0f, 0.0f, 0.0f,
                        1.0f, -1.0f, 1.0f, 0.0f,
                        1.0f, 1.0f, 1.0f, 1.0f,
                        -1.0f, 1.0f, 0.0f, 1.0f,
                        -1.0f, -1.0f, 0.0f, 0.0f
                };
        return vertices;
    }

    /**
     * 根据点的坐标，计算出点所要纹理的大小
     *
     * @param p 坐标封装类
     * @return
     */
    public static float[] calculatePointsArea(PointBean p) {
        float strideX = mStrideX;
        float strideY = mStrideY;
        float[] vertices = new float[]
                { //X,Y,S,T
                        p.getX(), p.getY(), 0.5f, 0.5f,
                        p.getX() - strideX, p.getY() - strideY, //XY
                        0.0f, 1.0f,//ST
                        p.getX() + strideX, p.getY() - strideY, //XY
                        1.0f, 1.0f,//ST
                        p.getX() + strideX, p.getY() + strideY, //XY
                        1.0f, 0.0f,//ST
                        p.getX() - strideX, p.getY() + strideY, //XY
                        0.0f, 0.0f,//ST
                        p.getX() - strideX, p.getY() - strideY, //XY
                        0.0f, 1.0f,//ST
                };
        return vertices;
    }

    /*  *
         * 这个方法是因为在横屏状态下，FBO上转回屏幕Y轴会压缩，对应的点Y轴距离就会变小
         * 这里计算补偿所压缩距离
         */
    public static float[] calculateOppositePointsArea(PointBean p) {
        float strideX = mFBOStrideX;
        float strideY = mFBOStrideY;
        float[] vertices = new float[]
                {       //X,Y,S,T
                        p.getX(), p.getY(), 0.5f, 0.5f,
                        p.getX() - strideX, p.getY() - strideY, //XY
                        0.0f, 1.0f,//ST
                        p.getX() + strideX, p.getY() - strideY, //XY
                        1.0f, 1.0f,//ST
                        p.getX() + strideX, p.getY() + strideY, //XY
                        1.0f, 0.0f,//ST
                        p.getX() - strideX, p.getY() + strideY, //XY
                        0.0f, 0.0f,//ST
                        p.getX() - strideX, p.getY() - strideY, //XY
                        0.0f, 1.0f,//ST
                };
        return vertices;
    }

    /**
     * 计算比例，使笔触变圆
     * 按照纹理比例在Y轴乘以比例就可以得到Y轴应该变的大小
     */
    private static void calculateStride() {
        mStrideX = 0.05f;
        mStrideY = 0.05f;
        float ratio = TextureHelper.getBitmapOptions();
        if (ratio > 1) {
            mStrideY /= ratio;
        } else {
            mStrideY *= ratio;
        }
    }

    /**
     * FBO的因为横屏竖屏的ST坐标都是（-1,1）所以算比例时用SurfaceView高度
     * 公式为 :  strideY / TextureHeight =  fbostrideY / SurfaceViewHeight
     */
    private static void calculateFBOStride() {
        mFBOStrideX = mStrideX;
        mFBOStrideY = (mStrideY * Constant.mSurfaceViewHeight) / (float) Constant
                .TextureHeight;
    }

}
