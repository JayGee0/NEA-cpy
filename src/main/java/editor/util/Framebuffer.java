package editor.util;

import static org.lwjgl.opengl.GL30.*;

public class Framebuffer {
    private final int fboID;
    private final int rboID;
    private final Texture texture;

    public Framebuffer(int width, int height) {
        fboID = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, fboID);

        texture = new Texture(width, height);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture.getTexID(), 0);

        rboID = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, rboID);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT32, width, height);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, rboID);

        if(glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            assert false: "Framebuffer not complete";
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public void bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, fboID);
    }

    public void unbind() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public int getTexture() {
        return this.texture.getTexID();
    }
}
