package editor.util;

import math.Vector2;
import math.Vector3;

public class Line {
    private final Vector2 from;
    private final Vector2 to;
    private final Vector3 color;
    private int lifetime;

    public Line(Vector2 from, Vector2 to, Vector3 color, int lifetime) {
        this.from = from;
        this.to = to;
        this.color = color;
        this.lifetime = lifetime;
    }

    public int beginFrame() {
        this.lifetime--;
        return this.lifetime;
    }

    public Vector2 getFrom() {
        return from;
    }

    public Vector2 getTo() {
        return to;
    }

    public Vector3 getColor() {
        return color;
    }

    @Override
    public String toString() {
        return "Line2D{" +
                "from=" + from +
                ", to=" + to +
                ", color=" + color +
                ", lifetime=" + lifetime +
                '}';
    }
}