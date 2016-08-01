package com.connor.myapplication.util;

import com.connor.myapplication.data.PointBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by meitu on 2016/7/7.
 */
public class BezierUtil {
    //存放手指滑动屏幕时的坐标
    public static List<PointBean> mScreenPoints = new ArrayList<>();
    //存放屏幕的点计算出的中点和接下来的屏幕点
    public static List<PointBean> mBezierPoints = new ArrayList<>();
    //贝塞尔曲线上的点
    public static ArrayList<PointBean> mBezierResult = new ArrayList<>();
    //添加点的次数，用来判断计算贝塞尔控制点
    public static int addTimes = 0;

    /**
     * 添加手指滑动屏幕时的坐标
     */
    public static void addScreenPoint(float X, float Y) {
        mScreenPoints.add(new PointBean(X, Y));
        addTimes++;
        if (addTimes == 2) {
            addMidPoint();
            releaseScreenPoints();
            addTimes = 1;
            mScreenPoints.add(new PointBean(X, Y));
        }
    }

    /**
     * 用两个点算他们的中点，完成一次贝塞尔曲线计算
     */
    public static void addMidPoint() {
        PointBean one = mScreenPoints.get(0);
        PointBean three = mScreenPoints.get(1);
        PointBean mid = new PointBean(((one.getX() + three.getX()) / 2), (one.getY() + three.getY()
        ) / 2);

        if (mBezierPoints.size() == 2) {//前面已经完成一次中点计算了
            mBezierPoints.add(mid);
            calculateBezier();
            mBezierPoints.clear();//计算完之后，要将这一次的中点和屏幕点记下来，给下一个中点完成贝塞尔曲线
            mBezierPoints.add(mid);
            mBezierPoints.add(three);
        } else {//第一次完成中点计算
            mBezierPoints.add(one);//第一个点，以线性的贝塞尔曲线连接
            mBezierPoints.add(mid);
            mBezierPoints.add(three);
            calculateBezier();
            releasePoints();
            //
            mBezierPoints.add(mid);
            mBezierPoints.add(three);
        }

    }


    /**
     * 计算贝塞尔曲线点
     */
    public static void calculateBezier() {
        PointBean mControlPoint;//贝塞尔控制点
        PointBean mBezierPoint;//贝塞尔曲线上的点
        PointBean mStartPoint;//贝塞尔曲线起点
        PointBean mEndPoint;//贝塞尔曲线终点
        float t = 0.01f;//默认t取0.01
        float BezierX;
        float BezierY;
        mControlPoint = mBezierPoints.get(1);
        mStartPoint = mBezierPoints.get(0);
        mEndPoint = mBezierPoints.get(2);

        mBezierResult.add(mStartPoint);

        //先计算两点之间距离，根据距离选择t
        float distance;//起点和终点两点间距离
        distance = calculateDistance(mStartPoint, mEndPoint);
        t = 1 / distance;//再根据距离选取t的取值 100->0.01,   10->0.1,   1->1
        float step = t / 2;//除以2再取多一点的点

        if (t > 0 && t < 1) {//t取值范围
            //公式 (1-t)^2 * x1 + 2 * t * (1-t) * x0 + t^2 * x2 = x
            for (; t <= 1.0f; t += step) {
                BezierX = (float) Math.pow(1 - t, 2) * mBezierPoints.get(0).getX() + 2 * t * (1 -
                        t) *
                        mControlPoint.getX() + (float) Math.pow(t, 2) * mBezierPoints.get(2).getX();

                BezierY = (float) Math.pow(1 - t, 2) * mBezierPoints.get(0).getY() + 2 * t * (1 -
                        t) *
                        mControlPoint.getY() + (float) Math.pow(t, 2) * mBezierPoints.get(2).getY();

                mBezierPoint = new PointBean(BezierX, BezierY);
                mBezierResult.add(mBezierPoint);
            }
        }

        mBezierResult.add(mEndPoint);

        ObjectUtil.createBezierLine(mBezierResult);
    }


    /**
     * 计算两点间距离,结果取数的级别
     */
    public static float calculateDistance(PointBean start, PointBean end) {
        int result = 1;
        float x = Math.abs(start.getX() - end.getX());
        float y = Math.abs(start.getY() - end.getY());
        String dis = (int) Math.sqrt(x * x + y * y) + "";
        int level = dis.trim().length();
        for (int i = 1; i < level; ++i) {
            result *= 10;
        }
        return (float) result;
    }

    /**
     * 释放坐标对象
     */
    public static void releaseScreenPoints() {
        if (mScreenPoints.size() != 0 && mScreenPoints != null) {
            for (PointBean p : mScreenPoints) {
                p = null;
            }
            mScreenPoints.clear();
        }

        if (mBezierResult.size() != 0 && mBezierResult != null) {
            for (PointBean p : mBezierResult) {
                p = null;
            }
            mBezierResult.clear();
        }

        addTimes = 0;
    }

    public static void releasePoints() {
        releaseScreenPoints();

        if (mBezierPoints.size() != 0 && mBezierPoints != null) {
            for (PointBean p : mBezierPoints) {
                p = null;
            }
            mBezierPoints.clear();
        }
    }
}