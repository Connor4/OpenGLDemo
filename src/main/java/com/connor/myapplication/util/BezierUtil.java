package com.connor.myapplication.util;

import com.connor.myapplication.data.Constant;
import com.connor.myapplication.data.PointBean;
import com.connor.myapplication.program.TextureHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by meitu on 2016/7/7.
 */
public class BezierUtil {
    //存放手指滑动屏幕时的坐标
    private static List<PointBean> mScreenPoints = new ArrayList<>();
    //存放屏幕的点计算出的中点和接下来的屏幕点
    private static List<PointBean> mBezierPoints = new ArrayList<>();
    //删除一些点之后的list
    private static ArrayList<PointBean> mDeleteLeftPoints = new ArrayList<>();
    //贝塞尔曲线上的点
    private static ArrayList<PointBean> mBezierResult = new ArrayList<>();
    //记录上一次删除点
    private static PointBean mLastDeletePoint = null;
    //添加点的次数，用来判断计算贝塞尔控制点
    private static int addTimes = 0;

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
    private static void addMidPoint() {
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
            //添加点，等下一次用
            mBezierPoints.add(mid);
            mBezierPoints.add(three);
        }

    }


    /**
     * 计算贝塞尔曲线点
     */
    private static void calculateBezier() {
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

        switch (Constant.CURRENT_USE_TYPE) {
            case Constant.PAINT:
                ObjectUtil.createBezierLine(mBezierResult);
                break;
            case Constant.FIREWORKS:
                //需要每个点位置刚好接上，不能重叠
               mDeleteLeftPoints = splicePoints(mBezierResult);
                ObjectUtil.createBezierLine(mDeleteLeftPoints);
                break;
            case Constant.WALLPAPER:
                //需要先除去一些点
                mDeleteLeftPoints = deletePoints(mBezierResult);
                ObjectUtil.createBezierLine(mDeleteLeftPoints);
                break;
            case Constant.MOSAIC:
                ObjectUtil.createBezierLine(mBezierResult);
                break;
            case Constant.ERASER:
                ObjectUtil.createBezierLine(mBezierResult);
                break;
            default:
                break;
        }

    }

    /**
     * 烟花笔处理，需要将点尽量拼接，连在一起会遮盖.
     * 计算的距离由PictureUtil的固定间隔值得来
     *
     * @param list
     * @return
     */
    private static ArrayList<PointBean> splicePoints(ArrayList<PointBean> list) {
        ArrayList<PointBean> result = new ArrayList<>();
        float distance;
        PointBean first=list.get(0);
        PointBean second = list.get(1);
        result.add(first);

        for (int i = 1; i < list.size()-1; i++) {
            distance = calculateExactDistance(first, second);
            if (distance > 50.0f) {
                first = second;
                result.add(second);
            }
            second = list.get(i + 1);
        }
        return result;
    }

    /**
     * 使用贴纸的时候，除去一些点，使得不连起来
     * 算法就是判断两点距离，够大就要，不够就不要
     */
    private static ArrayList<PointBean> deletePoints(ArrayList<PointBean> list) {
        ArrayList<PointBean> result = new ArrayList<>();
        float distance;
        if (mLastDeletePoint == null) {//第一次mLastDeletePoint会为null
            distance = calculateDistance(list.get(0), list.get(list.size() - 1));
            if (distance > 10) {//距离大于10,两个点都要
                mLastDeletePoint = list.get(list.size() - 1);
                result.add(list.get(0));
                result.add(mLastDeletePoint);
            } else {//距离小于10,只要第一个点
                mLastDeletePoint = list.get(0);
                result.add(mLastDeletePoint);
            }
        } else {
            //这里mLastDeletePoint不为空，即不为第一次；先计算list第一个点和mLastDeletePoint距离是否
            // 符合选取，不符合； 计算list最后一个点和mLastDeletePoint是否可以。
            distance = calculateDistance(mLastDeletePoint, list.get(0));
            if (distance > 10) {
                mLastDeletePoint = list.get(0);
                result.add(mLastDeletePoint);
            } else {
                distance = calculateDistance(mLastDeletePoint, list.get(list.size() - 1));
                if (distance > 10) {
                    mLastDeletePoint = list.get(list.size() - 1);
                    result.add(mLastDeletePoint);
                }
            }
        }
        return result;
    }

    /**
     * 计算两点间距离,结果取数的级别
     */
    private static float calculateDistance(PointBean start, PointBean end) {
        float result = 1;
        float x = Math.abs(start.getX() - end.getX());
        float y = Math.abs(start.getY() - end.getY());
        String dis = (int) Math.sqrt(x * x + y * y) + "";
        int level = dis.trim().length();
        for (int i = 1; i < level; ++i) {
            result *= 10;
        }
        return result;
    }

    /**
     * 也是计算两点间距离，但是这个是结果准确的
     * @param start
     * @param end
     * @return
     */
    private static float calculateExactDistance(PointBean start, PointBean end) {
        float x = Math.abs(start.getX() - end.getX());
        float y = Math.abs(start.getY() - end.getY());
        float distance = (float)Math.sqrt((x * x + y * y));
        return distance;
    }


    /**
     * 释放坐标对象
     */
    public static void releaseScreenPoints() {
        mScreenPoints.clear();
        mBezierResult.clear();
        mDeleteLeftPoints.clear();
        addTimes = 0;
    }

    /**
     * 也是释放坐标，但是比 releaseScreenPoints()多一个释放
     */
    public static void releasePoints() {
        releaseScreenPoints();
        mBezierPoints.clear();
        mLastDeletePoint = null;
    }
}
