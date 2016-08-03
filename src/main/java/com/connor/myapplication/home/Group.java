package com.connor.myapplication.home;

import com.connor.myapplication.program.MosaicTextureShaderProgram;
import com.connor.myapplication.program.TextureShaderProgram;

import java.util.LinkedList;

/**
 * 管理需要绘制的点
 */
public class Group extends Mesh {
    private final LinkedList<Mesh> mOppositeContainer = new LinkedList<>();//存放在FBO上操作的对象

    /**
     * 选择对象的容器去绘制
     *
     * @param type       在哪里画
     * @param program    片段着色器
     * @param resourceId 那张cover图片的纹理
     */
    public void draw(int type, TextureShaderProgram program, int resourceId) {
        int size;
        size = mOppositeContainer.size();
        for (int i = 0; i < size; i++) {
            if (mOppositeContainer.peekFirst() != null) {
                program.useProgram();
                program.setUniforms(resourceId);
                mOppositeContainer.peekFirst().bindData(program);
                mOppositeContainer.peekFirst().draw();
                mOppositeContainer.pollFirst();
            }
        }
    }

    public void drawMosaic(int type, MosaicTextureShaderProgram program, int resourceId, int pointId) {
        int size;

        size = mOppositeContainer.size();
        for (int i = 0; i < size; i++) {
            if (mOppositeContainer.peekFirst() != null) {
                program.useProgram();
                program.setUniforms(resourceId, pointId);
                mOppositeContainer.peekFirst().bindData2(program);
                mOppositeContainer.peekFirst().draw();
                mOppositeContainer.pollFirst();
            }
        }
    }

    /**
     * 添加对象进入FBO使用的对象容器
     */
    public void addOppositeObject(Mesh object) {
        mOppositeContainer.add(object);
    }

    /**
     * 清除容器
     */
    public void clear() {
        mOppositeContainer.clear();
    }

}