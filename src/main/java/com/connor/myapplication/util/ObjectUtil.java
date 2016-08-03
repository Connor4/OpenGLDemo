package com.connor.myapplication.util;

import android.opengl.GLSurfaceView;

import com.connor.myapplication.data.Constant;
import com.connor.myapplication.data.PointBean;
import com.connor.myapplication.home.FBOEffectPoints;
import com.connor.myapplication.home.FBOPoints;
import com.connor.myapplication.home.OpenGLRenderer;
import com.connor.myapplication.home.Points;

import java.util.ArrayList;

public class ObjectUtil {
    public static GLSurfaceView mView;
    public static OpenGLRenderer mRenderer;

    /**
     * 根据点坐标，先换算再调用其他方法添加点对象
     *
     * @param x 屏幕坐标
     * @param y 屏幕坐标
     */
    public static void setPointCoordinate(float x, float y) {
        //转换成OpenGL坐标
        float glCoorX = CalculateCoordinateX(x);
        float glCoorY = CalculateCoordinateY(y);
        float glOppositeX = CalculateOppositeCoordinateX(glCoorX);
        float glOppositeY = CalculateOppositeCoordinateY(-glCoorY);


        switch (Constant.CURRENT_USE_TYPE) {
            case Constant.PAINT:
                CreateAndAddOppositePoint(glOppositeX, glOppositeY);
                break;

            case Constant.WALLPAPER:
                CreateAndAddOppositePoint(glOppositeX, glOppositeY);
                break;

            case Constant.MOSAIC:
                CreateAndAddOppositeEffect(glOppositeX, glOppositeY);
                break;

            case Constant.ERASER:
                CreateAndAddOppositeEraser(glOppositeX, glOppositeY);
                break;

            default:
                break;
        }
        mView.requestRender();
    }


    /**
     * 生成点对象并加入容器中
     *
     * @param x opengl坐标
     * @param y opengl坐标
     */
    private static void CreateAndAddOppositeEraser(float x, float y) {
        Points points = new Points(new PointBean(x, y));
        mRenderer.addOppositeMesh(points);
    }


    private static void CreateAndAddOppositeEffect(float x, float y) {
        FBOEffectPoints points = new FBOEffectPoints(new PointBean(x, y));
        mRenderer.addOppositeMesh(points);
    }


    /**
     * 生成点对象并加入容器中
     *
     * @param x opengl坐标
     * @param y opengl坐标
     */
    private static void CreateAndAddOppositePoint(float x, float y) {
        FBOPoints points = new FBOPoints(new PointBean(x, y));
        mRenderer.addOppositeMesh(points);
    }


    /**
     * 用点画线
     */
    public static void createBezierLine(ArrayList<PointBean> list) {
        ArrayList<PointBean> frontList = new ArrayList<>();
        ArrayList<PointBean> oppositeList = new ArrayList<>();
        float glLineCoorX;
        float glLineCoorY;
        float glLineOppositeCoorX;
        float glLineOppositeCoorY;
        PointBean frontBean;
        PointBean oppositeBean;

        for (PointBean p : list) {
            glLineCoorX = CalculateCoordinateX(p.getX());
            glLineCoorY = CalculateCoordinateY(p.getY());
            glLineOppositeCoorX = CalculateOppositeCoordinateX(glLineCoorX);
            glLineOppositeCoorY = CalculateOppositeCoordinateY(-glLineCoorY);//FBO的需要取反

            frontBean = new PointBean(glLineCoorX, glLineCoorY);
            frontList.add(frontBean);

            oppositeBean = new PointBean(glLineOppositeCoorX, glLineOppositeCoorY);
            oppositeList.add(oppositeBean);
        }

        if (!frontList.isEmpty() && !oppositeList.isEmpty()) {

            for (PointBean p : oppositeList) {
                switch (Constant.CURRENT_USE_TYPE) {
                    case Constant.PAINT:
                        CreateAndAddOppositePoint(p.getX(), p.getY());
                        break;
                    case Constant.WALLPAPER:
                        CreateAndAddOppositePoint(p.getX(), p.getY());
                        break;
                    case Constant.MOSAIC:
                        CreateAndAddOppositeEffect(p.getX(), p.getY());
                        break;
                    case Constant.ERASER:
                        CreateAndAddOppositeEraser(p.getX(), p.getY());
                        break;
                    default:
                        break;
                }
            }
            mView.requestRender();
        }
    }

    /**
     * 屏幕坐标X换算OpenGL坐标X
     */
    private static float CalculateCoordinateX(float x) {
        float result = (x / (float) Constant.mSurfaceViewWidth) * 2 - 1;

        return result;
    }

    /**
     * 屏幕坐标Y换算OpenGL坐标Y
     */
    private static float CalculateCoordinateY(float y) {
        float result = 1 - (y / (float) Constant.mSurfaceViewHeight) * 2;
        return result;
    }

    /**
     * 好像是根据可画的区域去计算OpenGL坐标
     */
    private static float CalculateOppositeCoordinateX(float x) {
        float result = x / Constant.AreaWidth;
        return result;
    }

    /**
     * 好像是根据可画的区域去计算OpenGL坐标
     */
    private static float CalculateOppositeCoordinateY(float y) {
        float result = y / Constant.AreaHeight;
        return result;
    }

    /**
     * 作引用用
     */
    public static void getViewAndRenderer(GLSurfaceView view, OpenGLRenderer renderer) {
        mRenderer = renderer;
        mView = view;
    }
}
