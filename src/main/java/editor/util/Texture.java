package editor.util;

import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.stb.STBImage.*;

public class Texture {

    private int texID;

    public Texture(int width, int height) {
        // Generate a texture and bind it to GL_TEXTURE_2D
        texID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texID);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width, height, 0, GL_RGB,GL_UNSIGNED_BYTE,0);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

    }


    public Texture(String filepath) {
        // Generate a texture and bind it to GL_TEXTURE_2D
        texID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texID);

        // Set texture parameters
        // Repeat image in both directions
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

        // When stretching the image, pixelate
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

        // When shrinking image, pixelate
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        IntBuffer width = BufferUtils.createIntBuffer(1);
        IntBuffer height = BufferUtils.createIntBuffer(1);
        IntBuffer channels = BufferUtils.createIntBuffer(1);
        stbi_set_flip_vertically_on_load(true);
        ByteBuffer image = stbi_load(filepath, width, height, channels, 0);

        if(image != null) {
            if(channels.get(0) == 3) {
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width.get(0), height.get(0), 0, GL_RGB, GL_UNSIGNED_BYTE, image);
                glGenerateMipmap(GL_TEXTURE_2D);
            }
            else if(channels.get(0) == 4) {
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width.get(0), height.get(0), 0, GL_RGBA, GL_UNSIGNED_BYTE, image);
                glGenerateMipmap(GL_TEXTURE_2D);
            }

        } else {
            assert false : "Image not loaded right";
        }

        stbi_image_free(image);
    }
    public void bind() {
        glBindTexture(GL_TEXTURE_2D, texID);
    }

    public int getTexID() {
        return texID;
    }
}
