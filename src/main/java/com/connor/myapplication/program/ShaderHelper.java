package com.connor.myapplication.program;

import static android.opengl.GLES20.GL_COMPILE_STATUS;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_LINK_STATUS;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glAttachShader;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateProgram;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDeleteProgram;
import static android.opengl.GLES20.glDeleteShader;
import static android.opengl.GLES20.glGetProgramiv;
import static android.opengl.GLES20.glGetShaderiv;
import static android.opengl.GLES20.glLinkProgram;
import static android.opengl.GLES20.glShaderSource;

/**
 * Created by meitu on 2016/7/5.
 */
public class ShaderHelper {

    public static int compileVertexShader(String shaderCode) {
        return compileShader(GL_VERTEX_SHADER, shaderCode);
    }

    public static int compileFragmentShader(String shaderCode) {
        return compileShader(GL_FRAGMENT_SHADER, shaderCode);
    }

    private static int compileShader(int type, String shaderCode) {
        int shaderObjectId = glCreateShader(type);

        if (shaderObjectId == 0) {
            return 0;
        }

        glShaderSource(shaderObjectId, shaderCode);
        glCompileShader(shaderObjectId);

        int[] compileStatus = new int[1];
        glGetShaderiv(shaderObjectId, GL_COMPILE_STATUS, compileStatus, 0);
        if (compileStatus[0] == 0) {
            glDeleteShader(shaderObjectId);
            return 0;
        }
        return shaderObjectId;
    }

    public static int linkProgram(int vertexShaderId, int fragmentShaderId) {
        int programObjectId = glCreateProgram();
        if (programObjectId == 0) {
            return 0;
        }
        glAttachShader(programObjectId, vertexShaderId);
        glAttachShader(programObjectId, fragmentShaderId);

        glLinkProgram(programObjectId);
        int[] linkStatus = new int[1];
        glGetProgramiv(programObjectId, GL_LINK_STATUS, linkStatus, 0);

        if (linkStatus[0] == 0) {
            glDeleteProgram(programObjectId);
            return 0;
        }
        return programObjectId;
    }

    public static int buildProgram(String vertexShaderSource,
                                   String fragmentShaderSource) {
        int program;

        int vertexShader = compileVertexShader(vertexShaderSource);
        int fragmentShader = compileFragmentShader(fragmentShaderSource);

        program = linkProgram(vertexShader, fragmentShader);

        return program;
    }
}
