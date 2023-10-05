package physics.collision;

import math.Vector2;
import org.joml.Math;
import physics.body.RigidBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CollisionHandler {
    private final RigidBody bodyA;
    private final RigidBody bodyB;
    private final float restitution;
    private final float friction;
    private final List<ContactPoint> contactPoints;
    private final boolean isColliding;


    static class ContactPoint {
        private final Vector2 contactPosition;
        private final Vector2 collisionNormal;
        private final float penetrationDepth;
        private float massNormal; // Mass and I for normal impulse calculation
        private float massTangent; // Mass and I for tangent impulse calculation
        private float bias; // Allowing some penetration to avoid jitter

        public ContactPoint(Vector2 contactPosition, Vector2 collisionNormal, float penetrationDepth) {
            this.contactPosition = contactPosition;
            this.collisionNormal = collisionNormal;
            this.penetrationDepth = penetrationDepth;
        }

        public Vector2 getCollisionNormal() {
            return new Vector2(collisionNormal);
        }

        public void setCollisionNormal(Vector2 collisionNormal) {
            this.collisionNormal.set(collisionNormal);
        }

        public Vector2 getContactPosition() {
            return contactPosition;
        }

        public void setContactPosition(Vector2 contactPosition) {
            this.contactPosition.set(contactPosition);
        }
    }

    public CollisionHandler(RigidBody bodyA, RigidBody bodyB) {
        this.bodyA = bodyA;
        this.bodyB = bodyB;
        contactPoints = new ArrayList<>();

        this.isColliding = CollisionDetector.detectCollision(this.bodyA, this.bodyB, this);

        this.friction = Math.sqrt(bodyA.getFriction() * bodyB.getFriction());
        this.restitution = bodyA.getCOR() * bodyB.getCOR();
    }


    public void preStep(float invDT) {
        for(ContactPoint c : contactPoints) {
            Vector2 relativeA = new Vector2(c.contactPosition).sub(bodyA.getPosition());
            Vector2 relativeB = new Vector2(c.contactPosition).sub(bodyB.getPosition());

            float relativeNormalA = relativeA.dot(c.collisionNormal);
            float relativeNormalB = relativeB.dot(c.collisionNormal);
            float kNormal = bodyA.getInvMass() + bodyB.getInvMass();
            kNormal += bodyA.getInvI() * (relativeA.dot(relativeA) - relativeNormalA * relativeNormalA) +
                    bodyB.getInvI() * (relativeB.dot(relativeB) - relativeNormalB * relativeNormalB);
            c.massNormal = 1.0f / kNormal;

            Vector2 tangent = new Vector2(c.collisionNormal).perpendicularClockwise();
            float relativeTangentA = relativeA.dot(tangent);
            float relativeTangentB = relativeB.dot(tangent);
            float kTangent = bodyA.getInvMass() + bodyB.getInvMass();
            kTangent += bodyA.getInvI() * (relativeA.dot(relativeA) - relativeTangentA * relativeTangentA) +
                    bodyB.getInvI() * (relativeB.dot(relativeB) - relativeTangentB * relativeTangentB);
            c.massTangent = 1.0f / kTangent;
            c.bias = -0.2f * invDT * Math.min(0.0f, c.penetrationDepth);
        }

    }

    public void resolveCollision() {
        float totalInverseMass = bodyA.getInvMass() + bodyB.getInvMass();
        if(totalInverseMass == 0.0f) return;

        for(ContactPoint c : contactPoints) {
            Vector2 relativeA = new Vector2(c.contactPosition).sub(bodyA.getPosition());
            Vector2 relativeB = new Vector2(c.contactPosition).sub(bodyB.getPosition());

            Vector2 angularVelocityA = new Vector2(relativeA).perpendicularCounterClockwise().mul(bodyA.getAngularVelocity());
            Vector2 angularVelocityB = new Vector2(relativeB).perpendicularCounterClockwise().mul(bodyB.getAngularVelocity());
            Vector2 relativeVelocity = new Vector2(bodyB.getLinearVelocity()).add(angularVelocityB).sub(bodyA.getLinearVelocity()).sub(angularVelocityA);

            float vn = relativeVelocity.dot(c.collisionNormal);

            float normalImpulse = c.massNormal * (-(1.0f + this.restitution) *vn + c.bias);
            normalImpulse = Math.max(normalImpulse, 0.0f);

            Vector2 Pn = new Vector2(c.collisionNormal).mul(normalImpulse);
            bodyA.setLinearVelocity(new Vector2(bodyA.getLinearVelocity()).sub(new Vector2(Pn).mul(bodyA.getInvMass())));
            bodyA.setAngularVelocity(bodyA.getAngularVelocity() - bodyA.getInvI() * relativeA.cross(Pn));

            bodyB.setLinearVelocity(new Vector2(bodyB.getLinearVelocity()).add(new Vector2(Pn).mul(bodyB.getInvMass())));
            bodyB.setAngularVelocity(bodyB.getAngularVelocity() + bodyB.getInvI() * relativeB.cross(Pn));

            angularVelocityA = new Vector2(relativeA).perpendicularCounterClockwise().mul(bodyA.getAngularVelocity());
            angularVelocityB = new Vector2(relativeB).perpendicularCounterClockwise().mul(bodyB.getAngularVelocity());
            relativeVelocity = new Vector2(bodyB.getLinearVelocity()).add(angularVelocityB).sub(bodyA.getLinearVelocity()).sub(angularVelocityA);

            Vector2 tangent = new Vector2(c.collisionNormal).perpendicularClockwise();
            float vt = relativeVelocity.dot(tangent);
            float tangentImpulse = c.massTangent * (-(1.0f + this.restitution) * vt);


            float maxFrictionImpulse = this.friction * normalImpulse;
            tangentImpulse = Math.clamp(-maxFrictionImpulse, maxFrictionImpulse, tangentImpulse);

            Vector2 Pt = new Vector2(tangent).mul(tangentImpulse);
            bodyA.setLinearVelocity(new Vector2(bodyA.getLinearVelocity()).sub(new Vector2(Pt).mul(bodyA.getInvMass())));
            bodyA.setAngularVelocity(bodyA.getAngularVelocity() - bodyA.getInvI() * relativeA.cross(Pt));

            bodyB.setLinearVelocity(new Vector2(bodyB.getLinearVelocity()).add(new Vector2(Pt).mul(bodyB.getInvMass())));
            bodyB.setAngularVelocity(bodyB.getAngularVelocity() + bodyB.getInvI() * relativeB.cross(Pt));
        }
    }

    public boolean isColliding() {
        return isColliding;
    }


    public CollisionHandler addContactPoint(Vector2 contactPosition, Vector2 collisionNormal, float penetrationDepth) {
        contactPoints.add(new ContactPoint(contactPosition, collisionNormal, penetrationDepth));
        return this;
    }

    public List<ContactPoint> getContactPoints() {
        return contactPoints;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CollisionHandler that = (CollisionHandler) o;
        return Objects.equals(bodyA, that.bodyA) &&
                Objects.equals(bodyB, that.bodyB);
    }

}
