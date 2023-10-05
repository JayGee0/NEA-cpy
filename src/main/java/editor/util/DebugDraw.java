package editor.util;

import math.Vector2;
import math.Vector3;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class DebugDraw {
    public static int MAX_LINES = 50000;

    private static final List<Line> lines = new ArrayList<>();
    private static final float[] vertices = new float[5 * 2 * MAX_LINES];
    private static Shader shader;

    private static int vao;
    private static int vbo;


    public static void init() {
        shader = ResourceManager.getShader("debug");

        // Generate the vao
        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        // Create the vbo and buffer some memory
        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, (long) vertices.length * Float.BYTES, GL_DYNAMIC_DRAW);

        // Enable the vertex array attributes
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 5 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 5 * Float.BYTES, 2 * Float.BYTES);
        glEnableVertexAttribArray(1);

        glLineWidth(4.0f);
    }

    public static void beginFrame() {
        // Remove dead lines
        for (int i=0; i < lines.size(); i++) {
            if (lines.get(i).beginFrame() < 0) {
                lines.remove(i);
                i--;
            }
        }
    }


    public static void draw() {
        if (lines.size() <= 0) return;

        int index = 0;
        for (Line line : lines) {
            for (int i=0; i < 2; i++) {
                Vector2 position = i == 0 ? line.getFrom() : line.getTo();
                Vector3 color = line.getColor();

                // Load position
                vertices[index] = position.getX();
                vertices[index + 1] = position.getY();

                // Load the color
                vertices[index + 2] = color.getX();
                vertices[index + 3] = color.getY();
                vertices[index + 4] = color.getZ();
                index += 5;
            }
        }

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_DYNAMIC_DRAW);

        shader.use();

        // Bind the vao
        glBindVertexArray(vao);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        // Draw the batch
        glDrawArrays(GL_LINES, 0, lines.size());

        // Disable Location
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glBindVertexArray(0);

        shader.detach();
    }


    public static void addLine(Vector2 from, Vector2 to) {
        addLine(from, to, new Vector3(1, 0, 0), 1);
    }

    public static void addLine(Vector2 from, Vector2 to, Vector3 color) {
        addLine(from, to, color, 1);
    }

    public static void addLine(Vector2 from, Vector2 to, Vector3 color, int lifetime) {
        if (lines.size() >= MAX_LINES) return;
        DebugDraw.lines.add(new Line(from, to, color, lifetime));
    }


    public static void addBox(Vector2 center, Vector2 dimensions, Vector3 color) {
        addBox(center, dimensions, color, 1);
    }

    public static void addBox(Vector2 center, Vector2 dimensions, Vector3 color, int lifetime) {
        Vector2 min = new Vector2(center).sub(new Vector2(dimensions));
        Vector2 max = new Vector2(center).add(new Vector2(dimensions));

        Vector2[] vertices = {
                new Vector2(min.getX(), min.getY()), new Vector2(min.getX(), max.getY()),
                new Vector2(max.getX(), max.getY()), new Vector2(max.getX(), min.getY())
        };


        addLine(vertices[0], vertices[1], color, lifetime);
        addLine(vertices[0], vertices[3], color, lifetime);
        addLine(vertices[1], vertices[2], color, lifetime);
        addLine(vertices[2], vertices[3], color, lifetime);
    }

    public static void addPoint(Vector2 point, Vector3 color) {
        addPoint(point, color, 1);
    }

    public static void addPoint(Vector2 point, Vector3 color, int lifetime) {
        addBox(point, new Vector2(0.05f,0.05f), color, lifetime);
    }


}