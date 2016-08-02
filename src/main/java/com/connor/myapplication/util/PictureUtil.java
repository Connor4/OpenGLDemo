package com.connor.myapplication.util;

import com.connor.myapplication.data.Constant;
import com.connor.myapplication.data.PointBean;
import com.connor.myapplication.program.TextureHelper;

/**
 * Created by meitu on 2016/7/15.
 */
public class PictureUtil {
    public static float mStrideX = 0.05f;
    public static float mStrideY = 0.05f;

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
            height = (((float) Constant.mSurfaceViewWidth * mPicRatio) / (float) Constant
                    .mSurfaceViewHeight);

            //设置纹理大小
            Constant.TextureWidth = Constant.mSurfaceViewWidth;
            Constant.TextureHeight = (int) (height * Constant.mSurfaceViewHeight);
        }

        //用于设置点击区域大小
        Constant.AreaWidth = width;
        Constant.AreaHeight = height;
        calculateStride();

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

    public static float[] calculateOppositeVertices() {
        float width = 1.0f;
        float height = 1.0f;
        float mPicRatio = TextureHelper.getBitmapOptions();
        //确定是横屏还是竖屏,对应的方向应该占满屏幕,另外一边按照相片比例和SurfaceView比例计算
        if (mPicRatio > 1) {
            height = 1.0f;
            width = ((float) Constant.mSurfaceViewWidth / ((float) Constant.mSurfaceViewHeight /
                    mPicRatio));
            if (width > 1.0f) {//r如果计算出来的宽度大于屏幕的，取1
                width = 1.0f;
            }

        } else {
            width = 1.0f;
            height = (((float) Constant.mSurfaceViewWidth * mPicRatio) / (float) Constant
                    .mSurfaceViewHeight);

        }
        float[] vertices = new float[]
                {
                        // X, Y, S, T
                     /*   0f, 0f, 0.5f, 0.5f,
                        -width, -height, 0.0f, 0.0f,
                        width, -height, 1.0f, 0.0f,
                        width, height, 1.0f, 1.0f,
                        -width, height, 0.0f, 1.0f,
                        -width, -height, 0.0f, 0.0f*/
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
     * 计算橡皮擦的区域
     */
    public static float[] calculateEraseArea(PointBean p) {
        float stride = 0.025f; //这个间距是猜的
        float[] vertices = new float[]
                {
                        p.getX(), p.getY(), (p.getX() + 1) / 2, (1 - p.getY()) / 2, 0.5f, 0.5f,
                        p.getX() - stride, p.getY() - stride, //XY
                        (p.getX() - stride + 1) / 2, (1 - (p.getY() - stride)) / 2, 0.0f, 1.0f,
                        //ST,ST
                        p.getX() + stride, p.getY() - stride, //XY
                        (p.getX() + stride + 1) / 2, (1 - (p.getY() - stride)) / 2, 1.0f, 1.0f,
                        //ST,ST
                        p.getX() + stride, p.getY() + stride, //XY
                        (p.getX() + stride + 1) / 2, (1 - (p.getY() + stride)) / 2, 1.0f, 0.0f,
                        //ST,ST
                        p.getX() - stride, p.getY() + stride, //XY
                        (p.getX() - stride + 1) / 2, (1 - (p.getY() + stride)) / 2, 0.0f, 0.0f,
                        //ST,ST
                        p.getX() - stride, p.getY() - stride, //XY
                        (p.getX() - stride + 1) / 2, (1 - (p.getY() - stride)) / 2, 0.0f, 1.0f//ST,ST
                };
        return vertices;
    }

    /**
     * 计算FBO上面需要的橡皮擦区域
     */
    public static float[] calculateOppositeEraserArea(PointBean p) {
        float stride = 0.025f;
        float[] vertices = new float[]
                {
                        p.getX(), p.getY(), (p.getX() + 1) / 2, (p.getY() - 1) / 2, 0.5f, 0.5f,
                        p.getX() - stride, p.getY() - stride, //XY
                        (p.getX() - stride + 1) / 2, ((p.getY() - stride) - 1) / 2, 0.0f, 0.0f,//ST
                        p.getX() + stride, p.getY() - stride, //XY
                        (p.getX() + stride + 1) / 2, ((p.getY() - stride) - 1) / 2, 1.0f, 0.0f,//ST
                        p.getX() + stride, p.getY() + stride, //XY
                        (p.getX() + stride + 1) / 2, ((p.getY() + stride) - 1) / 2, 1.0f, 1.0f,//ST
                        p.getX() - stride, p.getY() + stride, //XY
                        (p.getX() - stride + 1) / 2, ((p.getY() + stride) - 1) / 2, 0.0f, 1.0f,//ST
                        p.getX() - stride, p.getY() - stride, //XY
                        (p.getX() - stride + 1) / 2, ((p.getY() - stride) - 1) / 2, 0.0f, 0.0f//ST
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
                {//X,Y,S,T
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
     */
    private static void calculateStride() {
        float ratio = TextureHelper.getBitmapOptions();
        if (ratio > 1) {
            mStrideY /= ratio;
        } else {
            mStrideX = 0.025f;
            mStrideY = 0.025f;
            mStrideX /= ratio;
        }
    }

}
