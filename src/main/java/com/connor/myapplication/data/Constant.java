package com.connor.myapplication.data;

/**
 * Created by meitu on 2016/7/6.
 */
public class Constant {
    public static final int BYTES_PER_FLOAT = 0x0004;//字节大小

    //==================start==========================
    public static final int ERASER = 0x0005;//橡皮擦状态
    public static final int PAINT = 0x0006;//画笔状态
    public static final int WALLPAPER = 0x0007;//使用图片作为画笔
    public static final int MOSAIC = 0x0008;//使用马赛克
    public static final int FIREWORKS = 0x0009;//使用烟花笔
    public static int CURRENT_USE_TYPE = PAINT;//当前使用
    //===================end=========================
    //===================start=========================
    public static final int GESTURE_MODE_NORMAL = 0x000A;//正常画
    public static final int GESTURE_MODE_DRAG = 0x000B;//拖拽缩放
    public static final int GESTURE_MODE_GONE = 0x000C;//手势失效
    public static int CURRENT_GESTURE_MODE = GESTURE_MODE_NORMAL;//默认正常
    //===================end=========================
    /**
     * 记录当前otherProgram序号,删除时使用
     */
    public static int CURRENT_OTHER_PROGRAM_INDEX ;
    /**
     * 记录当前fireworkprogram需要，删除时使用
     */
    public static int CURRENT_FIREWORKPROGARM_INDEX;
    /**
     * surfaceView宽度
     */
    public static int mSurfaceViewWidth ;
    /**
     * surfaceView高度
     */
    public static int mSurfaceViewHeight ;
    /**
     * 点击区域宽度
     */
    public static float AreaWidth;
    /**
     * 点击区域高度
     */
    public static float AreaHeight;
    /**
     * 纹理大小，设置FBO用
     */
    public static int TextureWidth;
    /**
     * 纹理大小，设置FBO用
     */
    public static int TextureHeight;
    /**
     * 屏幕宽度
     */
    public static int ScreenWidth;
    /**
     * 屏幕高度
     */
    public static int ScreenHeight;
}
