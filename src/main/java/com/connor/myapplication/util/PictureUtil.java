package com.connor.myapplication.util;

import android.util.Log;

import com.connor.myapplication.data.Constant;
import com.connor.myapplication.data.PointBean;
import com.connor.myapplication.program.TextureHelper;

import java.util.Random;

/**
 * Created by meitu on 2016/7/15.
 */
public class PictureUtil {
    //显示纹理大小间隔,4个
    public static float mStrideX;
    public static float mStrideY;
    public static float mFBOStrideX;
    public static float mFBOStrideY;
    //偏移量
    public static float mXOffset;
    public static float mYOffset;
    //投影矩阵，用于计算偏移量
    public static float[] projectionMatrix = new float[16];

    /**
     * 根据相片比例计算纹理所在坐标
     */
    public static float[] calculateVertices() {
        float width = 1.0f;
        float height = 1.0f;
        float mPicRatio = TextureHelper.getBitmapOptions();
        //确定是横屏还是竖屏,对应的方向应该占满屏幕,另外一边按照相片比例和SurfaceView比例计算
        if (mPicRatio > 1) {
            height = 1.0f;
            //对应的一边所占比
            // 1 / SurfaceHeight = x / (SurfaceHeight / ratio)
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
            //SurfaceWidth所占为1.0f,图片宽高比乘以SurfaceWidth就是图片高度
            //在屏幕上所占大小,再跟SurfaceHeight除以,得到的就是当高也为1.0f时
            //相片的高应该所占比例
            height = (((float) Constant.mSurfaceViewWidth * mPicRatio) / (float) Constant
                    .mSurfaceViewHeight);

            //设置纹理大小
            Constant.TextureWidth = Constant.mSurfaceViewWidth;
            Constant.TextureHeight = (int) (height * Constant.mSurfaceViewHeight);
        }

        //图片区域大小
        Constant.AreaWidth = width;
        Constant.AreaHeight = height;
        //第一次进来也要计算间隔
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
     * 这个方法是因为在横屏状态下，FBO上转回屏幕Y轴会压缩，对应的点Y轴距离就会变小
     * 这里计算补偿所压缩距离
     */
    public static float[] calculateOppositePointsArea(PointBean p) {
        float strideX = mFBOStrideX;
        float strideY = mFBOStrideY;
        if (Constant.CURRENT_USE_TYPE == Constant.WALLPAPER) {
            float addStride = changeStride();
            strideX += addStride;
            float ratio = (float) Constant.mSurfaceViewHeight / (float) Constant.mSurfaceViewWidth;
            //因为两种方向的照片需要在Y轴补偿不同的量，这个判断条件应该具有通用性
            if (Constant.AreaHeight < Constant.AreaWidth) {
                strideY += addStride * ratio;
            } else {
                strideY += addStride / ratio;
            }
        }

        calculateOffset(p);

        float[] vertices = new float[]
                {       //X,Y,S,T
                        mXOffset, mYOffset, 0.5f, 0.5f,//XY,ST
                        mXOffset - strideX, mYOffset - strideY, //XY
                        0.0f, 1.0f,//ST
                        mXOffset + strideX, mYOffset - strideY, //XY
                        1.0f, 1.0f,//ST
                        mXOffset + strideX, mYOffset + strideY, //XY
                        1.0f, 0.0f,//ST
                        mXOffset - strideX, mYOffset + strideY, //XY
                        0.0f, 0.0f,//ST
                        mXOffset - strideX, mYOffset - strideY, //XY
                        0.0f, 1.0f,//ST
                };
        return vertices;
    }

    /**
     * 给烟花笔计算顶点用，跟{@link #calculateOppositePointsArea} 不同的是这里需要随机旋转
     * 公式x' = x * cos(angle) + y * sin(angle), y' = y * sin(angle) - x * cos(angle)
     * 以mXOffset，mYOffset为原点计算出旋转后的x',y'的位置，再跟上面一样放置
     *
     * @param p
     * @return
     */
    public static float[] calculateFireWorkPointArea(PointBean p) {
        float strideX = mFBOStrideX;
        float strideY = mFBOStrideY;
        strideX *= 4;//变宽一点
        strideY *= 4;
        calculateOffset(p);
        //生成随机角度
        Random random = new Random();
        double angle = random.nextInt(360) % (360 + 1) ;
        angle = Math.toRadians(angle);
        //以正方形四个角顺时针方向排布
        float OneX = strideX * (float) Math.cos(angle) + strideY * (float) Math.sin(angle);
        float OneY = strideY * (float) Math.sin(angle) - strideX * (float) Math.cos(angle);
        float TwoX = strideX * (float) Math.cos(angle) + strideY * (float) Math.sin(angle);
        float TwoY = strideY * (float) Math.sin(angle) - strideX * (float) Math.cos(angle);
        float ThreeX = strideX * (float) Math.cos(angle) + strideY * (float) Math.sin(angle);
        float ThreeY = strideY * (float) Math.sin(angle) - strideX * (float) Math.cos(angle);
        float FourX = strideX * (float) Math.cos(angle) + strideY * (float) Math.sin(angle);
        float FourY = strideY * (float) Math.sin(angle) - strideX * (float) Math.cos(angle);

        float[] vertices = new float[]
                {       //X,Y,S,T
                        mXOffset, mYOffset, 0.5f, 0.5f,//XY,ST
                        mXOffset - ThreeX, mYOffset - ThreeY, 0.0f, 1.0f,//
                        mXOffset + TwoX, mYOffset - TwoY, 1.0f, 1.0f,//
                        mXOffset + OneX, mYOffset + OneY, 1.0f, 0.0f,//
                        mXOffset - FourX, mYOffset + FourY, 0.0f, 0.0f,//
                        mXOffset - ThreeX, mYOffset - ThreeY, 0.0f, 1.0f,//
                };
        return vertices;
    }


    /**
     * 计算FBO上面需要的效果区域,这里包含两个纹理。
     * 第一个ST坐标是点击位置所对应的原图位置，即取一定位置的ST坐标，取出来的是那个位置的纹理
     * 第二个ST坐标是放置黑白圆圈那张图的ST
     * 具体的忘了，是用原图纹理和点的纹理重合。看看program和脚本
     */
    public static float[] calculateOppositeEffectArea(PointBean p) {
        float strideX = mFBOStrideX;
        float strideY = mFBOStrideY;
        calculateOffset(p);
        float[] vertices = new float[]
                {
                        mXOffset, mYOffset,//XY
                        (mXOffset + 1) / 2, (mYOffset - 1) / 2, 0.5f, 0.5f,//ST,ST
                        mXOffset - strideX, mYOffset - strideY, //XY
                        (mXOffset - strideX + 1) / 2, ((mYOffset - strideY) - 1) / 2, 0.0f, 0.0f,
                        //ST,ST
                        mXOffset + strideX, mYOffset - strideY, //XY
                        (mXOffset + strideX + 1) / 2, ((mYOffset - strideY) - 1) / 2, 1.0f, 0.0f,
                        //ST,ST
                        mXOffset + strideX, mYOffset + strideY, //XY
                        (mXOffset + strideX + 1) / 2, ((mYOffset + strideY) - 1) / 2, 1.0f, 1.0f,
                        //ST,ST
                        mXOffset - strideX, mYOffset + strideY, //XY
                        (mXOffset - strideX + 1) / 2, ((mYOffset + strideY) - 1) / 2, 0.0f, 1.0f,
                        //ST,ST
                        mXOffset - strideX, mYOffset - strideY, //XY
                        (mXOffset - strideX + 1) / 2, ((mYOffset - strideY) - 1) / 2, 0.0f, 0.0f
                        //ST,ST
                };
        return vertices;
    }

    /**
     * 计算比例，使笔触变圆
     * 按照纹理比例在Y轴乘以比例就可以得到Y轴应该变的大小
     */
    private static void calculateStride() {
        float ratio = (float) Constant.mSurfaceViewHeight / (float) Constant.mSurfaceViewWidth;
        mStrideX = 0.03f;
        mStrideY = 0.03f;
        mStrideY /= ratio;
    }

    /**
     * FBO的因为横屏竖屏的ST坐标都是（-1,1）所以算比例时用SurfaceView高度
     * 公式为 :  strideY / TextureHeight =  FBOStrideY / SurfaceViewHeight
     */
    private static void calculateFBOStride() {
        mFBOStrideX = mStrideX;
        mFBOStrideY = (mStrideY * Constant.mSurfaceViewHeight) / (float) Constant
                .TextureHeight;
    }

    /**
     * 当点需要使用大小不同纹理时，在这里改变点的大小
     * 随机生成一些结果，使得stride变大或者变小
     */
    private static float changeStride() {
        Random random = new Random();
        float result = random.nextFloat();
        return result / 10;
    }

    /**
     * 改变画笔之后，需要重新设置当前的笔画间隔
     */
    public static void reSetStride() {
        calculateStride();
        calculateFBOStride();
        //重新设置投影矩阵
        projectionMatrix[0] = 1;
        projectionMatrix[12] = 0;
        projectionMatrix[13] = 0;
    }

    /**
     * 修改缩放平移后偏移量
     * 对于投影矩阵，0,5位是X,Y方向上缩放量,12,13位是X，Y方向上平移量
     * 做这个的思路：画一个（-1,1）的，再画一个（-2,2）的，两个都是原点作为中心。
     * 缩放的就取屏幕上的坐标（-0.5，-0.5），（-2,2）即放大的会显示在哪里（就是（-1,1）等比例的位置），
     * 如果要显示在点击位置（缩放的应该在（（-0.25，-0.25））），应该坐标怎么变换
     * 公式为：点击位置/ratio = 应该在的位置/1（即屏幕XY最大值）
     * 平移的，单独拿出来的话，就是x-offset，y+offset。
     * 但是平移和缩放的一起来，就必须选计算平移的，再处理缩放的。
     */
    private static void calculateOffset(PointBean p) {
        float ratio = projectionMatrix[0];//缩放倍数
        float Xoffset = projectionMatrix[12];//X轴偏移量
        float Yoffset = projectionMatrix[13];//Y轴偏移量
        float x = p.getX();
        float y = p.getY();
        if (ratio != 0) {
            mXOffset = (x - Xoffset) / ratio;
            mYOffset = (y + Yoffset) / ratio;
        } else {
            mXOffset = x;
            mYOffset = y;
        }
    }

}
