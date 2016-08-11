package com.connor.myapplication.home;

import android.content.Context;
import android.graphics.PointF;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;

import com.connor.myapplication.R;
import com.connor.myapplication.data.Constant;
import com.connor.myapplication.program.MosaicTextureShaderProgram;
import com.connor.myapplication.program.TextureHelper;
import com.connor.myapplication.program.TextureShaderProgram;
import com.connor.myapplication.program.TraceTextureShaderProgram;
import com.connor.myapplication.util.FBOArrayUtil;
import com.connor.myapplication.util.RendererUtil;
import com.connor.myapplication.util.SaveUtil;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_BLEND;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA;
import static android.opengl.GLES20.GL_SRC_ALPHA;
import static android.opengl.GLES20.glBlendFunc;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDeleteProgram;
import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glDisable;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glViewport;

public class OpenGLRenderer implements GLSurfaceView.Renderer, MainActivity.GestureHandleCallback {
    private TextureShaderProgram mTextureProgram;
    private TextureShaderProgram mPointProgram;
    private TextureShaderProgram mEraserProgram;
    private TraceTextureShaderProgram mTraceProgram;
    private MosaicTextureShaderProgram mEffectProgram;
    private BackGround mBackGround;
    private FBOBackGround mFBOBackGround;
    private Group mRoot;
    private Context mContext;
    private int mTexture;
    private int mPointTexture;
    //    private int mFireWorkTexture;
    private int mTargetTexture;
    private int mReturnTexture;

    private int mFramebuffer;
    private FBOArrayUtil mArrayUtil;
    private int mResourceId;

    public boolean mDrawLast;
    public boolean mDrawNext;
    public boolean mSavePic;
    //=======手势部分start======
    private PointF mLastTouchPoint = new PointF();
    private PointF mCurrentTouchPoint = new PointF();
    private PointF mMidPoint = new PointF();
    private float mNewDist = 0f, mOldDist = 0f;
    private float mZoom = 0f;
    private float mCurrentViewPortX;
    private float mCurrentViewPortY;
    private float mCurrentViewPortWidth;
    private float mCurrentViewPortHeight;
    //=======手势部分end======


    public OpenGLRenderer(Context mContext, int resourceId) {
        this.mContext = mContext;
        Group group = new Group();
        this.mRoot = group;
        this.mResourceId = resourceId;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        initProgram();
        initTexture();

        mBackGround = new BackGround();//屏幕渲染使用
        mFBOBackGround = new FBOBackGround();//离屏渲染使用
        //给撤销的用
        mArrayUtil = new FBOArrayUtil();
        //独立创建一个FBO给当前的用
        mTargetTexture = mArrayUtil.createTargetTexture(Constant.TextureWidth, Constant
                .TextureHeight);
        mFramebuffer = mArrayUtil.createFrameBuffer(mTargetTexture);

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0, 0, width, height);
        //设置视角，改变位置时用
        mCurrentViewPortX = 0;
        mCurrentViewPortY = 0;
        mCurrentViewPortWidth = Constant.mSurfaceViewWidth;
        mCurrentViewPortHeight = Constant.mSurfaceViewHeight;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);

        if (Constant.CURRENT_GESTURE_MODE == Constant.GESTURE_MODE_DRAG || Constant
                .CURRENT_GESTURE_MODE == Constant.GESTURE_MODE_ZOOM) {
            glViewport((int)mCurrentViewPortX, (int)mCurrentViewPortY, (int)mCurrentViewPortWidth,
                    (int)mCurrentViewPortHeight);
            drawOnscreen();
        } else {
            if (mSavePic) {
                drawOnscreen();
                SaveUtil.takeScreenShot(gl);
                mSavePic = false;
            } else {
                if (mDrawLast) {
                    mReturnTexture = mArrayUtil.getLastTexture();
                    if (mReturnTexture == -1) {
                        mDrawLast = false;
                    }
                }

                if (mDrawNext) {
                    mReturnTexture = mArrayUtil.getNextTexture();
                    if (mReturnTexture == -1) {
                        mDrawNext = false;
                    }
                }

                drawInFrameBuffer(mFramebuffer);
                //因为在FBO画的时候改变了视角，需要重新改变视角
                glViewport((int)mCurrentViewPortX, (int)mCurrentViewPortY, (int)mCurrentViewPortWidth,
                        (int)mCurrentViewPortHeight);
                drawOnscreen();
            }
        }
    }

    /**
     * 离屏渲染
     */
    private void drawOffscreen(int width, int height) {
        glViewport(0, 0, width, height);
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glClear(GL_COLOR_BUFFER_BIT);

        mTextureProgram.useProgram();
        if (mDrawLast || mDrawNext) {
            mTextureProgram.setUniforms(mReturnTexture);
            mDrawLast = false;
            mDrawNext = false;
        } else {
            mTextureProgram.setUniforms(mTargetTexture);
        }
        mFBOBackGround.bindData(mTextureProgram);
        mFBOBackGround.draw();

        glEnable(GL_BLEND);//反正下面全部都要开启的

        switch (Constant.CURRENT_USE_TYPE) {
            case Constant.PAINT:
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                mRoot.draw(mPointProgram, mPointTexture);
                glDisable(GL_BLEND);
                break;

            case Constant.FIREWORKS:
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

                mRoot.draw(RendererUtil.CreateFireWorkProgram(mContext), RendererUtil
                        .SelectFireWorkTexture());

                glDisable(GL_BLEND);
                break;

            case Constant.WALLPAPER:
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

                int[] mCurrentOtherTextureIndex = new int[1];
                mCurrentOtherTextureIndex[0] = RendererUtil.CreateChangeTexture(mContext);

                mRoot.draw(RendererUtil.CreateChangeProgram(mContext),
                        mCurrentOtherTextureIndex[0]);

                glDeleteProgram(Constant.CURRENT_OTHER_PROGRAM_INDEX);
                glDeleteTextures(1, mCurrentOtherTextureIndex, 0);

                glDisable(GL_BLEND);
                break;

            case Constant.MOSAIC:
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

                mEffectProgram = new MosaicTextureShaderProgram(mContext,
                        R.raw.mosaic_texture_shader_program);
                mRoot.drawMultiTexture(mEffectProgram, mTexture, mPointTexture);

                glDisable(GL_BLEND);
                break;

            case Constant.ERASER:
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

                mEraserProgram = new TextureShaderProgram(mContext, R.raw
                        .eraer_texture_shader_program);
                mRoot.draw(mEraserProgram, mPointTexture);

                glDisable(GL_BLEND);
                break;
            default:
                break;

        }
    }

    /**
     * 在屏幕上面绘制
     */
    private void drawOnscreen() {
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);

        //每次将相片跟笔画混合在一起
        mTraceProgram.useProgram();
        if (mDrawLast || mDrawNext) {
            mTraceProgram.setUniforms(mTexture, mReturnTexture);//跟重做的纹理混合
        } else {
            mTraceProgram.setUniforms(mTexture, mTargetTexture);//跟当前屏幕对应的FBO的纹理混合
        }
        mBackGround.bindData(mTraceProgram);
        mBackGround.draw();
    }

    /**
     * 创建所需要的program
     */
    private void initProgram() {
        mTextureProgram = new TextureShaderProgram(mContext, R.raw.texture_fragment_shader);
        mPointProgram = new TextureShaderProgram(mContext, R.raw.point_texture_fragment_shader);

        mTraceProgram = new TraceTextureShaderProgram(mContext, R.raw.trace_texture_shader_program);

    }

    /**
     * 创建所需要的纹理
     */
    private void initTexture() {
        mPointTexture = TextureHelper.loadTexture(mContext, R.drawable.cover);
        RendererUtil.CreateFireWorkTexture(mContext);//不应该放这里
        mTexture = TextureHelper.loadOriginalTexture(mContext, mResourceId);
    }


    /**
     * 点击事件up的时候对在FBO数组里的保存备份,
     * 主要作用是调用FBOUtil保存当前笔画
     */
    public void drawInBackupFBO() {
        drawInFrameBuffer(mFramebuffer);
        drawInFrameBuffer(mArrayUtil.getFrameBuffer());
        mRoot.clear();
    }

    /**
     * 释放记录缩放平移位置的PointF
     */
    public void freeGesturePointF() {
        //=======平移========
        mCurrentTouchPoint.x = 0;
        mCurrentTouchPoint.y = 0;
        mLastTouchPoint.x = 0;
        mLastTouchPoint.y = 0;
        //=======缩放======
        mZoom = 0;
        mNewDist = 0;
        mOldDist = 0;
    }


    /**
     * 添加给离屏渲染用的对象
     */
    public void addOppositeMesh(Mesh mesh) {
        mRoot.addOppositeObject(mesh);
    }

    /**
     * 在选定的FB上面绘制
     */
    private void drawInFrameBuffer(int FrameBuffer) {
        //调用FBO
        GLES20.glBindFramebuffer(
                GLES20.GL_FRAMEBUFFER, FrameBuffer);

        drawOffscreen(Constant.TextureWidth, Constant.TextureHeight);

        GLES20.glBindFramebuffer(
                GLES20.GL_FRAMEBUFFER, 0);
    }

    //===================手势部分start========================
    @Override
    public boolean handleDragGesture(MotionEvent event) {
        mLastTouchPoint.x = mCurrentTouchPoint.x;
        mLastTouchPoint.y = mCurrentTouchPoint.y;

        mCurrentTouchPoint.x = event.getX();
        mCurrentTouchPoint.y = event.getY();
        //要的是两次点之间的距离，然后去偏移
        if (mLastTouchPoint.x != 0 && mLastTouchPoint.y != 0) {
            //计算视角偏移,X,Y方向分开算。
            //只要算坐标原点就可以了，用这两个点改变就可以平移
            mCurrentViewPortX += mCurrentTouchPoint.x - mLastTouchPoint.x;
            mCurrentViewPortY -= mCurrentTouchPoint.y - mLastTouchPoint.y;
        }
        return true;
    }

    @Override
    public boolean handlePinchGesture(int distance) {

//        mOldDist = mNewDist;
//        mNewDist = distance;
//        mZoom = mNewDist / mOldDist;

 //       if (mZoom != Float.POSITIVE_INFINITY) {//第一次mOldDist = 0时，mZoom会为infinity
//            midPoint(mMidPoint, event);
//            float xIncrement = calculateXIncrement();
//            float yIncrement = calculateYIncrement();
            //        Log.d("TAG", "x    " + xIncrement + "   y   " + yIncrement);

//            mCurrentViewPortX += (int) (mMidPoint.x - xMiddle * mZoom);
//            mCurrentViewPortY += (int) (Constant.mSurfaceViewHeight - mMidPoint.y - yMiddle *
//                    mZoom);
//            mCurrentViewPortX += xIncrement;
//            mCurrentViewPortY += yIncrement;
 //           mZoom = ((float) Math.round(mZoom * 10)) / 10;

//            mCurrentViewPortWidth *= mZoom;
  //          mCurrentViewPortHeight *= mZoom;
 //       }

        return true;
    }

    /**
     * 求pointID0和1之间的距离
     */
    private float spacing(MotionEvent event) {
        float x = 0;
        float y = 0;
        try {
            x = event.getX(0) - event.getX(1);
            y = event.getY(0) - event.getY(1);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * 求中点，用于缩放用
     */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    /**
     * 返回X上缩放增量
     */
    private float calculateXIncrement() {
        return mCurrentViewPortWidth * (1 - mZoom);
    }

    /**
     * 返回Y上缩放增量
     */
    private float calculateYIncrement() {
        return mCurrentViewPortHeight * (1 - mZoom);
    }

    /**
     * 分配算出的X增量，用于后面计算VIEWPORT的X
     * 返回int是因为便于之后计算是用整形
     * 用SurfaceView的宽去计算因为是按比例缩放的，所以用这个和用当前的width也一样
     */
    private int distributeXIncrement(float XIncrement) {
        return 0;
    }

    /**
     * 分配算出的X增量，用于后面计算VIEWPORT的X
     * 返回int是因为便于之后计算是用整形
     * 用SurfaceView的高去计算因为是按比例缩放的，所以用这个和用当前的width也一样
     */
    private int distributeYIncrement(float YIncrement) {
        return 0;
    }


    //===================手势部分end========================

}
