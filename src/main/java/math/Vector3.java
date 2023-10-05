package math;


public class Vector3 {

    private float x;
    private float y;
    private float z;

    public Vector3() {
        x = 0.0f;
        y = 0.0f;
        z = 0.0f;
    }


    public Vector3(float d) {
        this.x = d;
        this.y = d;
        this.z = d;
    }

    public Vector3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3(Vector3 v) {
        x = v.x;
        y = v.y;
        z = v.z;
    }


    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public float getZ() {
        return this.z;
    }

    public Vector3 set(float d) {
        this.x = d;
        this.y = d;
        this.z = d;
        return this;
    }

    public Vector3 set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Vector3 set(Vector3 v) {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
        return this;
    }

    public Vector3 add(Vector3 v) {
        this.x += v.x;
        this.y += v.y;
        this.z += v.z;
        return this;
    }

    public Vector3 add(float x, float y, float z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public Vector3 sub(Vector3 v) {
        this.x -= v.x;
        this.y -= v.y;
        this.z -= v.z;
        return this;
    }

    public Vector3 mul(float scalar) {
        this.x = x * scalar;
        this.y = y * scalar;
        this.z = z * scalar;
        return this;
    }


    public float dot(Vector3 v) {
        return x * v.x + y * v.y + z * v.z;
    }
    
    public Vector3 cross(Vector3 v) {
        return new Vector3((y * v.z - z * v.y), -(x*v.z - z * v.x), (x * v.y - y * v.x));
    }

    public float lengthSquared() {
        return x * x + y * y + z * z;
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    public float distance(Vector3 v) {
        float dx = this.x - v.x;
        float dy = this.y - v.y;
        float dz = this.z - v.z;
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public float distanceSquared(Vector3 v) {
        float dx = this.x - v.x;
        float dy = this.y - v.y;
        float dz = this.z - v.z;
        return dx * dx + dy * dy + dz * dz;
    }

    public Vector3 normalize() {
        float invLength = 1 / length();
        this.x = x * invLength;
        this.y = y * invLength;
        this.z = z * invLength;
        return this;
    }

    public Vector3 absolute() {
        this.x = Math.abs(this.x);
        this.y = Math.abs(this.y);
        this.z = Math.abs(this.z);
        return this;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }


}
