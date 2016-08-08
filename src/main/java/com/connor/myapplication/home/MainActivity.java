package com.connor.myapplication.home;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.PointF;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
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
    private int mResourceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = this.getIntent().getExtras();
        mResourceId = bundle.getInt("id");

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
                    final float midX, midY, LastTouchX, LastTouchY;
                    float NewDist = 0.0f, OldDist = 0.0f, LastZoom = 0.0f;
                    boolean gestureFlag = false;

                    switch (action) {
                        case MotionEvent.ACTION_DOWN:
                            Constant.CURRENT_GESTURE_MODE = Constant.GESTURE_MODE_NORMAL;

                            ObjectUtil.setPointCoordinate(currentX, currentY);
                            //添加点，用于贝塞尔曲线
                            BezierUtil.addScreenPoint(currentX, currentY);
                            break;

                        case MotionEvent.ACTION_POINTER_DOWN:
                            OldDist = spacing(event);
                            gestureFlag = true;
                            break;

                        case MotionEvent.ACTION_MOVE:
                            if (gestureFlag) {
                                NewDist = spacing(event);
//                            //根据两指操作的距离判断是什么是什么操作
                                float distance = distance(OldDist, NewDist);
//                            Log.d("TAG", "distance " + distance);
//                            //用两次距离判断操作
//                            if (distance < 10) {
//                                Constant.CURRENT_GESTURE_MODE = Constant.GESTURE_MODE_DRAG;
//                            } else {
//                                Constant.CURRENT_GESTURE_MODE = Constant.GESTURE_MODE_ZOOM;
//                            }
                            }


                            if (Constant.CURRENT_GESTURE_MODE == Constant.GESTURE_MODE_NORMAL) {
                                //点下来了继续滑动就画线
                                BezierUtil.addScreenPoint(currentX, currentY);

                            } else if (Constant.CURRENT_GESTURE_MODE == Constant
                                    .GESTURE_MODE_DRAG) {


                            } else if (Constant.CURRENT_GESTURE_MODE == Constant
                                    .GESTURE_MODE_ZOOM) {


                            }

                            OldDist = NewDist;
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
                                BezierUtil.releasePoints();
                            }

                            Constant.CURRENT_GESTURE_MODE = Constant.GESTURE_MODE_GONE;

                        case MotionEvent.ACTION_POINTER_UP:
                            Constant.CURRENT_GESTURE_MODE = Constant.GESTURE_MODE_GONE;
                            break;

                        default:
                            break;
                    }
                }
                return true;
            }
        });

    }

    /**
     * 求pointID0和1之间的距离
     */
    private float spacing(MotionEvent event) {
        try {
            float x = event.getX(0) - event.getX(1);
            float y = event.getY(0) - event.getY(1);
            return (float) Math.sqrt(x * x + y * y);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 计算两次两指间距离，用来判断是什么操作
     * @param Old
     * @param New
     * @return
     */
    private float distance(float Old, float New) {
        return Math.abs(Old - New);
    }

    /**
     * 求中点，用于缩放用
     */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
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

}
