package math;


public class Vector2 {

    private float x;
    private float y;

    public Vector2() {
        x = 0.0f;
        y = 0.0f;
    }


    public Vector2(float d) {
        this.x = d;
        this.y = d;
    }

    public Vector2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vector2(Vector2 v) {
        this.x = v.x;
        this.y = v.y;
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public Vector2 set(float d) {
        this.x = d;
        this.y = d;
        return this;
    }

    public Vector2 set(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public Vector2 set(Vector2 v) {
        this.x = v.x;
        this.y = v.y;
        return this;
    }

    public Vector2 add(Vector2 v) {
        this.x += v.x;
        this.y += v.y;
        return this;
    }

    public Vector2 add(float x, float y) {
        this.x += x;
        this.y += y;
        return this;
    }

    public Vector2 sub(Vector2 v) {
        this.x -= v.x;
        this.y -= v.y;
        return this;
    }

    public Vector2 mul(float scalar) {
        this.x = x * scalar;
        this.y = y * scalar;
        return this;
    }


    public float dot(Vector2 v) {
        return x * v.x + y * v.y;
    }

    public float cross(Vector2 v) {
        return x * v.y - y * v.x;
    }

    public Vector2 perpendicularClockwise() {
        return new Vector2(y, -x);
    }

    public Vector2 perpendicularCounterClockwise() {
        return new Vector2(-y, x);
    }

    public float lengthSquared() {
        return x * x + y * y;
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y);
    }

    public float distance(Vector2 v) {
        float dx = this.x - v.x;
        float dy = this.y - v.y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    public float distanceSquared(Vector2 v) {
        float dx = this.x - v.x;
        float dy = this.y - v.y;
        return dx * dx + dy * dy;
    }

    public Vector2 normalize() {
        float invLength = 1 / length();
        this.x = x * invLength;
        this.y = y * invLength;
        return this;
    }

    public Vector2 absolute() {
        this.x = Math.abs(this.x);
        this.y = Math.abs(this.y);
        return this;
    }

    public static Vector2 clamp(Vector2 value, Vector2 min, Vector2 max) {
        float clampedX = Math.max(min.getX(), Math.min(max.getX(), value.getX()));
        float clampedY = Math.max(min.getY(), Math.min(max.getY(), value.getY()));
        return new Vector2(clampedX, clampedY);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }




}
