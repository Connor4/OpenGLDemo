package com.connor.myapplication.home;

import android.support.v7.widget.SwitchCompat;

import com.connor.myapplication.data.Constant;
import com.connor.myapplication.data.PointBean;
import com.connor.myapplication.data.VertexArray;
import com.connor.myapplication.program.ShaderProgram;
import com.connor.myapplication.program.TextureShaderProgram;
import com.connor.myapplication.util.PictureUtil;

import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.glDrawArrays;
import static com.connor.myapplication.data.Constant.BYTES_PER_FLOAT;

/**
 * Created by meitu on 2016/8/2.
 */
public class FBOPoints extends Mesh {
    private static final int POSITION_COMPONENT_COUNT = 2;
    private static final int TEXTURE_COORDINATES_COMPONENT_COUNT = 2;
    private static final int STRIDE = (POSITION_COMPONENT_COUNT
            + TEXTURE_COORDINATES_COMPONENT_COUNT) * BYTES_PER_FLOAT;

    private float[] vertices;
    private final VertexArray vertexArray;

    public FBOPoints(PointBean pb) {
        switch (Constant.CURRENT_USE_TYPE) {
            case Constant.FIREWORKS:
                vertices = PictureUtil.calculateFireWorkPointArea(pb);
                break;
            default:
                vertices = PictureUtil.calculateOppositePointsArea(pb);
                break;
        }


        vertexArray = new VertexArray(vertices);
    }

    public void bindData(ShaderProgram textureProgram) {
        vertexArray.setVertexAttribPointer(
                0,
                textureProgram.getPositionAttributeLocation(),
                POSITION_COMPONENT_COUNT,
                STRIDE);

        vertexArray.setVertexAttribPointer(
                POSITION_COMPONENT_COUNT,
                textureProgram.getTextureCoordinatesAttributeLocation(),
                TEXTURE_COORDINATES_COMPONENT_COUNT,
                STRIDE);
    }

    public void draw() {
        glDrawArrays(GL_TRIANGLE_FAN, 0, 6);
    }
}
