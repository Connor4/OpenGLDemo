package com.connor.myapplication.home;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.connor.myapplication.R;
import com.connor.myapplication.data.Constant;
import com.connor.myapplication.util.BezierUtil;
import com.connor.myapplication.util.FBOArrayUtil;
import com.connor.myapplication.util.ObjectUtil;
import com.connor.myapplication.util.PictureUtil;

public class MainActivity extends Activity {
    private static GLSurfaceView mGLSurfaceView;
    private static OpenGLRenderer mRenderer;
    //=======回弹部分start=====
    private Rebound mRebound;
    //=======回弹部分end=====

    private boolean mGestureFlag = false;//是否出现手势操作判断

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = this.getIntent().getExtras();
        int mResourceId = bundle.getInt("id");

        Constant.CURRENT_USE_TYPE = Constant.PAINT;
        Constant.CURRENT_GESTURE_MODE = Constant.GESTURE_MODE_NORMAL;

        setContentView(R.layout.activity_main);

        mGLSurfaceView = new GLSurfaceView(this);
        mRenderer = new OpenGLRenderer(this, mResourceId);
        mGLSurfaceView.setEGLContextClientVersion(2);
        mGLSurfaceView.setRenderer(mRenderer);
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        RelativeLayout rl = (RelativeLayout) findViewById(R.id.main);
        rl.addView(mGLSurfaceView);


        ObjectUtil.getViewAndRenderer(mGLSurfaceView, mRenderer);

        mGLSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event != null) {
                    final int action = MotionEventCompat.getActionMasked(event);

                    final float currentX = event.getX();
                    final float currentY = event.getY();

                    switch (action & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_DOWN:
                            BezierUtil.releasePoints();//再清一次保证容器空了
                            Constant.CURRENT_GESTURE_MODE = Constant.GESTURE_MODE_NORMAL;

                            ObjectUtil.setPointCoordinate(currentX, currentY);
                            BezierUtil.addScreenPoint(currentX, currentY);//添加点，用于贝塞尔曲线

                            break;

                        case MotionEvent.ACTION_POINTER_DOWN:
                            mGestureFlag = true;
                            break;

                        case MotionEvent.ACTION_MOVE:
                            if (mGestureFlag) {
                                Constant.CURRENT_GESTURE_MODE = Constant.GESTURE_MODE_GESTURING;
                            }

                            if (Constant.CURRENT_GESTURE_MODE == Constant.GESTURE_MODE_NORMAL) {
                                //点下来了继续滑动就画线
                                BezierUtil.addScreenPoint(currentX, currentY);

                            } else if (Constant.CURRENT_GESTURE_MODE == Constant
                                    .GESTURE_MODE_GESTURING) {
                                mRenderer.handleDragGesture(event);
                                mRenderer.handlePinchGesture(event);
                                mGLSurfaceView.requestRender();
                            }
                            break;

                        case MotionEvent.ACTION_UP:
                            if (Constant.CURRENT_GESTURE_MODE == Constant.GESTURE_MODE_NORMAL) {
                                //排入SurfaceView队列，在它自己的线程离运行
                                mGLSurfaceView.queueEvent(new Runnable() {
                                    @Override
                                    public void run() {
                                        mRenderer.drawInBackupFBO();
                                    }
                                });
                            }

                            if (mGestureFlag) {
                                mRenderer.freeGestureStatus();
                                checkRebound();
                            }

                            Constant.CURRENT_GESTURE_MODE = Constant.GESTURE_MODE_GONE;
                            BezierUtil.releasePoints();

                            break;

                        case MotionEvent.ACTION_POINTER_UP:
                            mRenderer.freeGestureStatus();
                            checkRebound();

                            Constant.CURRENT_GESTURE_MODE = Constant.GESTURE_MODE_GONE;
                            mGestureFlag = false;


                            break;

                        default:
                            break;
                    }
                }
                return true;
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        calculateSurfaceSize();
        PictureUtil.reSetStride();
    }

    public void FireWorks(View view) {
        Constant.CURRENT_USE_TYPE = Constant.FIREWORKS;
        PictureUtil.reSetStride();
    }

    public void Mosaic(View view) {
        Constant.CURRENT_USE_TYPE = Constant.MOSAIC;
        PictureUtil.reSetStride();
    }

    public void Erase(View view) {
        Constant.CURRENT_USE_TYPE = Constant.ERASER;
        PictureUtil.reSetStride();
    }

    public void Paint(View view) {
        Constant.CURRENT_USE_TYPE = Constant.PAINT;
        PictureUtil.reSetStride();
    }

    public void Star(View view) {
        Constant.CURRENT_USE_TYPE = Constant.WALLPAPER;
    }

    public void Save(View view) {
        mRenderer.mSavePic = true;
        mGLSurfaceView.requestRender();
        Toast.makeText(MainActivity.this, "保存到Pictures/OpenGLDemo目录下", Toast.LENGTH_SHORT).show();
    }

    public void Undo(View view) {
        int StepLeft = FBOArrayUtil.CheckLeft();//看看还有没有得回退
        if (StepLeft != 1) {
            mRenderer.mDrawLast = true;
            mGLSurfaceView.requestRender();
        } else {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("恢复原图？")
                    .setPositiveButton("确定",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    mRenderer.mDrawLast = true;
                                    mGLSurfaceView.requestRender();
                                }
                            }).setNegativeButton("取消", null).create()
                    .show();
        }
    }

    public void reUndo(View view) {
        mRenderer.mDrawNext = true;
        mGLSurfaceView.requestRender();

    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            int StepLeft = FBOArrayUtil.CheckLeft();
            if (StepLeft != 0) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("放弃该图片？")
                        .setPositiveButton("确定",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        finish();
                                    }
                                }).setNegativeButton("取消", null).create()
                        .show();
            } else {
                finish();
            }
        }
        return false;
    }

    /**
     * 计算相关参数
     */
    private void calculateSurfaceSize() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        Constant.ScreenWidth = dm.widthPixels;
        Constant.ScreenHeight = dm.heightPixels;
        Constant.mSurfaceViewWidth = dm.widthPixels;
        Constant.mSurfaceViewHeight = (int) ((dm.heightPixels - getStatusHeight()) * 0.9f);
    }

    /**
     * 获取通知栏高度
     */
    private int getStatusHeight() {
        int statusHeight = -1;
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height")
                    .get(object).toString());
            statusHeight = getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusHeight;
    }

    //=====================回弹start===========================

    /**
     * 判断是否回弹
     */
    private void checkRebound() {
        boolean canRun = false;
        mRebound = new Rebound();
        if (mRebound.isRunning) return;

        //是否在缩放状态下需要回弹
        if (PictureUtil.projectionMatrix0 < 1.0) {//小于原图
            mRebound.withScale(1);
            mRebound.withTranslate();
            canRun = true;
        } else if (PictureUtil.projectionMatrix0 > 2.5) {//放大倍数大于2.5倍
            mRebound.withScale(2);
            mRebound.withTranslate();
            canRun = true;
        } else if (PictureUtil.projectionMatrix0 > 1) { //是否需要在越过边界状态下回弹，这种只有放大
            if (isNeedTranslate()) {
                mRebound.withTranslate();
                canRun = true;
            }
        }

        if (canRun) {
            mRebound.startRebound();
        }
    }

    /**
     * 判断是否在放大状态时需要回弹
     *
     * @return 是否需要
     */
    private boolean isNeedTranslate() {
        float Scale = PictureUtil.projectionMatrix0;
        float XTranslate = Math.abs(PictureUtil.projectionMatrix12);
        float YTranslate = Math.abs(PictureUtil.projectionMatrix13);
        float Boundary = Math.abs(Scale - 1);//XY能偏移最大而不需要回弹的边界

        if (XTranslate - Boundary > 0) {
            return true;
        }

        if (YTranslate - Boundary > 0) {
            return true;
        }

        return false;
    }

    /**
     * 回弹线程
     */
    private class Rebound extends Thread {
        boolean isRunning;
        boolean isScaleFinished;
        boolean isTranslateFinished;

        @Override
        public void run() {
//            for (; ; ) {
//                if (isScaleFinished && isTranslateFinished) {
//                    stopRebound();
//                    break;
//                }
//            }
        }

        void withScale(final int type) {
            for (; ; ) {
                isScaleFinished = mRenderer.both(type);
                mGLSurfaceView.requestRender();


                if (isScaleFinished) {
                    break;
                }

            }
        }

        void withTranslate() {
            for (; ; ) {
                isTranslateFinished = mRenderer.reboundWithTranslate();
                mGLSurfaceView.requestRender();

                if (isTranslateFinished) {
                    break;
                }
            }
        }

        void startRebound() {
            isRunning = true;
            this.start();
        }

        void stopRebound() {
            isRunning = false;
            this.interrupt();
        }
    }
    //=====================回弹end===========================

}
