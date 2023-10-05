package physics.body;

import math.Vector2;
import math.Vector3;
import physics.quadtree.AABB;
import physics.quadtree.QuadObject;

public abstract class RigidBody implements Cloneable, QuadObject {
    private final Vector2 position;
    private float rotation; // rotation in degrees
    private float mass, invMass; // Mass in Kg
    private float I, invI; // I in Kg meters^2
    private final Vector2 linearVelocity; // Velocity meters per second
    private float angularVelocity; // Velocity in radians per second
    private float restitution; // Coefficient of restitution
    private float friction; // Coefficient of friction
    private final Vector3 color;
    protected BodyShape shape = null;
    protected AABB bounds;
    private final int rbID;
    private static int ID_COUNT = 0;

    public RigidBody(Vector2 position,
                     float rotation,
                     float mass, int rbID) {

        this.position = new Vector2(position);
        this.rotation = rotation;
        this.mass = mass;
        this.invMass = (this.mass == 0)? 0 : 1.0f/this.mass; // If the mass is 0, static object, set inverse mass to 0 else do 1/mass
        this.linearVelocity = new Vector2(0f,0f);
        this.angularVelocity = 0f;
        this.restitution = 0.5f;
        this.friction = 0.2f;
        this.color = new Vector3(0,1,0);
        this.rbID = rbID;
    }

    public RigidBody(Vector2 position,
                     float rotation,
                     float mass) {

        this.position = new Vector2(position);
        this.rotation = rotation;
        this.mass = mass;
        this.invMass = (this.mass == 0)? 0 : 1.0f/this.mass; // If the mass is 0, static object, set inverse mass to 0 else do 1/mass
        this.linearVelocity = new Vector2(0f,0f);
        this.angularVelocity = 0f;
        this.restitution = 0.5f;
        this.friction = 0.2f;
        this.color = new Vector3(0,1,0);
        this.rbID = ID_COUNT++;
    }

    public BodyShape getShape() {
        return shape;
    }

    public Vector2 getPosition() {
        return new Vector2(position);
    }

    public void setPosition(Vector2 position) {
        this.position.set(new Vector2(position));
        updateExtents();
    }


    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
        updateExtents();
    }

    public float getMass() {
        return mass;
    }

    public float getInvMass() {
        return this.invMass;
    }

    public void setMass(float mass) {
        this.mass = mass;
        this.invMass = (mass == 0)? 0 : 1.0f/mass;
        updateI();
    }

    public float getI() {
        return I;
    }

    public float getInvI() {
        return this.invI;
    }

    public void setI(float i) {
        I = i;
        this.invI = (I == 0)? 0 : 1.0f/I;
    }

    public Vector2 getLinearVelocity() {
        return new Vector2(linearVelocity);
    }

    public void setLinearVelocity(Vector2 linearVelocity) {
        this.linearVelocity.set(linearVelocity);
    }

    public float getAngularVelocity() {
        return angularVelocity;
    }

    public void setAngularVelocity(float angularVelocity) {
        this.angularVelocity = angularVelocity;
    }

    public float getCOR() {
        return restitution;
    }

    public void setCOR(float e) {
        this.restitution = e;
    }

    public float getFriction() {
        return friction;
    }

    public void setFriction(float friction) {
        this.friction = friction;
    }

    @Override
    public AABB getBounds() {
        return bounds;
    }

    public Vector3 getColor() {
        return this.color;
    }

    public void setColor(Vector3 col) {
        this.color.set(new Vector3(col));
    }

    public int getRbID() {
        return rbID;
    }

    public static void resetIDCounter() {
        ID_COUNT = 0;
    }

    @Override
    public String toString() {
        return shape.name() + " - " + rbID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RigidBody rigidbody = (RigidBody) o;

        return rbID == rigidbody.rbID;
    }

    @Override
    public int hashCode() {
        return rbID;
    }

    public abstract RigidBody clone();

    abstract void updateExtents();

    abstract void updateI();
}
