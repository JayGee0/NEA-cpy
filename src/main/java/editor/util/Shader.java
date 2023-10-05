package editor.util;

import org.joml.Matrix4f;
import math.Vector3;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL20.*;

public class Shader {

    public int shaderProgram;

    public Shader(String vertexSource, String fragmentSource) {
        try {
            vertexSource = new String(Files.readAllBytes(Paths.get(vertexSource)));
            fragmentSource = new String(Files.readAllBytes(Paths.get(fragmentSource)));
        } catch(IOException e) {
            e.printStackTrace();
        }


        // Create a vertex shader
        int vertexShader = glCreateShader(GL_VERTEX_SHADER);

        // Set the shader source code from vertexSource into the vertexShader
        // And then compile it
        glShaderSource(vertexShader, vertexSource);
        glCompileShader(vertexShader);

        // Check for errors glGetShaderinfo
        int success = glGetShaderi(vertexShader, GL_COMPILE_STATUS);

        if(success == GL_FALSE) {
            int length = glGetShaderi(vertexShader, GL_INFO_LOG_LENGTH);
            assert false: "Error in the vertex shader compilation:\n" + glGetShaderInfoLog(vertexShader, length);
        }

        // Create a fragment shader
        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);

        // Set the shader source code from vertexSource into the vertexShader
        // And then compile it
        glShaderSource(fragmentShader, fragmentSource);
        glCompileShader(fragmentShader);

        // Check for errors glGetShaderinfo
        success = glGetShaderi(fragmentShader, GL_COMPILE_STATUS);

        if(success == GL_FALSE) {
            int length = glGetShaderi(fragmentShader, GL_INFO_LOG_LENGTH);
            assert false: "Error in the vertex shader compilation:\n" + glGetShaderInfoLog(fragmentShader, length);
        }

        shaderProgram = glCreateProgram();
        glAttachShader(this.shaderProgram, vertexShader);
        glAttachShader(this.shaderProgram, fragmentShader);
        glLinkProgram(shaderProgram);

        // Check for errors glGetProgrami
        success = glGetProgrami(this.shaderProgram, GL_LINK_STATUS);

        if(success == GL_FALSE) {
            int length = glGetShaderi(this.shaderProgram, GL_INFO_LOG_LENGTH);
            assert false: "Error in the shader program linking:\n" + glGetProgramInfoLog(this.shaderProgram, length);
        }

    }

    public Shader use() {
        glUseProgram(this.shaderProgram);
        return this;
    }

    public Shader detach() {
        glUseProgram(0);
        return this;
    }

    public void uploadVec4(String name, Vector4f value) {
        int location = glGetUniformLocation(this.shaderProgram, name);
        glUseProgram(shaderProgram);
        glUniform4f(location, value.x, value.y, value.z, value.w);
    }

    public void uploadVec3(String name, Vector3 value) {
        int location = glGetUniformLocation(this.shaderProgram, name);
        glUseProgram(shaderProgram);
        glUniform3f(location, value.getX(), value.getY(), value.getZ());
    }

    public void uploadMat4(String name, Matrix4f value) {
        int location = glGetUniformLocation(this.shaderProgram, name);
        glUseProgram(shaderProgram);
        FloatBuffer mat4 = BufferUtils.createFloatBuffer(16);
        value.get(mat4);
        glUniformMatrix4fv(location, false, mat4);
        glUseProgram(0);
    }

    public void uploadInt(String name, int value) {
        int location = glGetUniformLocation(this.shaderProgram, name);
        glUseProgram(shaderProgram);
        glUniform1i(location, value);
    }

    public void uploadFloat(String name, float value) {
        int location = glGetUniformLocation(this.shaderProgram, name);
        glUseProgram(shaderProgram);
        glUniform1f(location, value);
    }

}
