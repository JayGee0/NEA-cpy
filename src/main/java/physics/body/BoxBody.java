package physics.body;

import math.Matrix22;
import math.Vector2;
import physics.quadtree.AABB;

import java.util.Random;

/**
 * Box shaped rigid body object
 */
public class BoxBody extends RigidBody {
    private final Vector2 halfSize;

    public BoxBody(Vector2 position, float rotation, float mass, Vector2 halfSize, int rbID) {
        super(position, rotation, mass, rbID);
        this.halfSize = new Vector2(halfSize);
        this.shape = BodyShape.BOX;
        updateI();
        updateExtents();
    }

    public BoxBody(Vector2 position, float rotation, float mass, Vector2 halfSize) {
        super(position, rotation, mass);
        this.halfSize = new Vector2(halfSize);
        this.shape = BodyShape.BOX;
        updateI();
        updateExtents();
    }

    public void setHalfSize(Vector2 halfSize) {
        this.halfSize.set(halfSize);
        updateI();
        updateExtents();
    }

    public Vector2 getHalfSize() {
        return halfSize;
    }

    @Override
    void updateI() {
        // I = mass * (width^2 + height^2) / 12
        this.setI(((this.getMass() * (halfSize.getX() * halfSize.getX() + halfSize.getY() * halfSize.getY())) / 3.0f));
    }

    @Override
    void updateExtents() {
        if(getRotation() == 0.0f) {
            this.bounds = new AABB(new Vector2(getPosition()), new Vector2(halfSize).add(0.1f, 0.1f));
        } else {
            Vector2[] corners = new Vector2[4];
            Matrix22 rotation = new Matrix22().rotation((float)Math.toRadians(getRotation()));
            corners[0] = rotation.mul(new Vector2(halfSize));
            corners[1] = rotation.mul(new Vector2(halfSize.getX(), -halfSize.getY()));
            corners[2] = rotation.mul(new Vector2(halfSize).mul(-1f));
            corners[3] = rotation.mul(new Vector2(-halfSize.getX(), halfSize.getY()));

            float maxX = 0;
            float maxY = 0;
            for (Vector2 v : corners) {
                if(Math.abs(v.getX()) > maxX) maxX = Math.abs(v.getX());
                if(Math.abs(v.getY()) > maxY) maxY = Math.abs(v.getY());
            }
            this.bounds = new AABB(new Vector2(getPosition()), new Vector2(maxX, maxY).add(0.1f,0.1f));
        }
    }

    @Override
    public BoxBody clone() {
        Random r = new Random();
        BoxBody clone = new BoxBody(new Vector2(getPosition()).add(r.nextFloat()/100f,halfSize.getY() * 2), getRotation(), getMass(), halfSize);
        clone.setColor(getColor());
        return clone;
    }
}
