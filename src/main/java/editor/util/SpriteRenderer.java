package editor.util;

import org.joml.Matrix4f;
import math.Vector2;
import math.Vector3;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15C.glBindBuffer;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class SpriteRenderer {
    private final Shader shader;
    private int quadVAO;

    public SpriteRenderer(Shader shader) {
        this.shader = shader;
        initRenderData();
    }

    public void drawSprite(Texture texture, Vector2 position, Vector2 size, float rotate, Vector3 color) {
        this.shader.use();
        Matrix4f model = new Matrix4f().identity();
        model.translate(new Vector3f(position.getX(), position.getY(), 0.0f));


        model.translate(new Vector3f(0.5f * size.getX(), 0.5f * size.getY(), 0.0f));
        model.rotate((float) Math.toRadians(rotate), new Vector3f(0.0f, 0.0f, 1.0f));
        model.translate(new Vector3f(-0.5f * size.getX(), -0.5f * size.getY(), 0.0f));

        model.scale(new Vector3f(size.getX(), size.getY(), 1.0f));
        this.shader.uploadMat4("model", model);
        this.shader.uploadVec3("spriteColor", color);

        glActiveTexture(GL_TEXTURE0);
        texture.bind();

        glBindVertexArray(quadVAO);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        glBindVertexArray(0);

        this.shader.detach();
    }


    private void initRenderData() {
        int vboID;
        float[] vertices = {
                0.0f, 1.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 1.0f, 0.0f,
                0.0f, 0.0f, 0.0f, 0.0f,

                0.0f, 1.0f, 0.0f, 1.0f,
                1.0f, 1.0f, 1.0f, 1.0f,
                1.0f, 0.0f, 1.0f, 0.0f,
        };

        quadVAO = glGenVertexArrays();
        vboID = glGenBuffers();

        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        glBindVertexArray(quadVAO);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 4, GL_FLOAT, false, 4 * Float.BYTES, 0 );
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

}
