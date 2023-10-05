package physics.body;

import math.Vector2;
import math.Vector3;
import physics.quadtree.AABB;

import java.util.Random;

public class CircleBody extends RigidBody {

    private float radius;

    public CircleBody(Vector2 position, float rotation, float mass, float radius, int rbID) {
        super(position, rotation, mass, rbID);
        this.radius = radius;
        this.shape = BodyShape.CIRCLE;
        this.setColor(new Vector3(1.0f,0.9f,0.0f));
        updateI();
        updateExtents();
    }

    public CircleBody(Vector2 position, float rotation, float mass, float radius) {
        super(position, rotation, mass);
        this.radius = radius;
        this.shape = BodyShape.CIRCLE;
        this.setColor(new Vector3(1.0f,0.9f,0.0f));
        updateI();
        this.bounds = new AABB(new Vector2(position), new Vector2(radius + 0.1f, radius + 0.1f));
    }


    public void setRadius(float radius) {
        this.radius = radius;
        updateI();
        updateExtents();
    }

    public float getRadius() {
        return radius;
    }


    @Override
    void updateI() {
        // I = PI * radius^4 / 4
        this.setI((float) ((Math.PI * this.radius * this.radius * this.radius * this.radius) / 4f));
    }

    @Override
    void updateExtents() {
        this.bounds = new AABB(new Vector2(this.getPosition()), new Vector2(radius + 0.1f, radius + 0.1f));
    }

    @Override
    public CircleBody clone() {
        Random r = new Random();
        CircleBody clone = new CircleBody(new Vector2(getPosition()).add(r.nextFloat()/100, radius * 2), getRotation(), getMass(), getRadius());
        clone.setColor(getColor());
        return clone;
    }
}
