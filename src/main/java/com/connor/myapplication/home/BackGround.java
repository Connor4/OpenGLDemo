package com.connor.myapplication.home;

import com.connor.myapplication.data.VertexArray;
import com.connor.myapplication.program.TextureShaderProgram;
import com.connor.myapplication.util.PictureUtil;
import com.connor.myapplication.program.TraceTextureShaderProgram;

import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.glDrawArrays;
import static com.connor.myapplication.data.Constant.BYTES_PER_FLOAT;

public class BackGround {
    private static final int POSITION_COMPONENT_COUNT = 2;
    private static final int TEXTURE_COORDINATES_COMPONENT_COUNT = 2;
    private static final int STRIDE = (POSITION_COMPONENT_COUNT
            + TEXTURE_COORDINATES_COMPONENT_COUNT) * BYTES_PER_FLOAT;

    private float[] vertices;

    private final VertexArray vertexArray;

    public BackGround() {
        vertices = PictureUtil.calculateVertices();
        vertexArray = new VertexArray(vertices);
    }

    public void bindData(TraceTextureShaderProgram textureProgram) {
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
