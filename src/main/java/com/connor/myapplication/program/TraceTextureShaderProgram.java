package com.connor.myapplication.program;

import android.content.Context;

import com.connor.myapplication.R;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE1;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;

/**
 * Created by meitu on 2016/7/25.
 */
public class TraceTextureShaderProgram extends TextureShaderProgram {
    private static final String U_TRACE_TEXTURE_UNIT = "u_TraceTextureUnit";
    private int uTextureUnitLocation;
    private int uTraceTextureUnitLocation;
    private int aPositionLocation;
    private int aTextureCoordinatesLocation;

    public TraceTextureShaderProgram(Context context, int glsl) {
        super(context, glsl);

        aPositionLocation = glGetAttribLocation(program, A_POSITION);
        uTextureUnitLocation = glGetUniformLocation(program, U_TEXTURE_UNIT);
        uTraceTextureUnitLocation = glGetUniformLocation(program, U_TRACE_TEXTURE_UNIT);

        aTextureCoordinatesLocation =
                glGetAttribLocation(program, A_TEXTURE_COORDINATES);
    }

    public void setUniforms(int textureId1, int textureId2) {
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureId1);
        glUniform1i(uTextureUnitLocation, 0);

        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, textureId2);
        glUniform1i(uTraceTextureUnitLocation, 1);

    }

    public int getPositionAttributeLocation() {
        return aPositionLocation;
    }

    public int getTextureCoordinatesAttributeLocation() {
        return aTextureCoordinatesLocation;
    }
}
