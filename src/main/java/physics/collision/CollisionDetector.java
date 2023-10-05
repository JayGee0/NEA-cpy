package physics.collision;

import math.Matrix22;
import math.Vector2;
import physics.body.BodyShape;
import physics.body.BoxBody;
import physics.body.CircleBody;
import physics.body.RigidBody;

public class CollisionDetector {

    /**
     * Detect a collision between two rigid body objects
     * @param a first rigid body
     * @param b second rigid body
     * @param handler collision handler to pass the collision data through
     * @return result of the collision
     */
    public static boolean detectCollision(RigidBody a, RigidBody b, CollisionHandler handler) {
        assert a.getShape() != null && b.getShape() != null : "Unknown shape of colliders";

        if(a.getShape() == BodyShape.CIRCLE &&
                b.getShape() == BodyShape.CIRCLE) {
            return CircleCircleCollision((CircleBody)a, (CircleBody)b, handler);
        }
        else if(a.getShape() == BodyShape.CIRCLE &&
                b.getShape() == BodyShape.BOX) {
            return CircleBoxCollision((CircleBody)a, (BoxBody)b, handler);
        }
        else if(a.getShape() == BodyShape.BOX &&
                b.getShape() == BodyShape.CIRCLE) {
            return CircleBoxCollision((CircleBody)b, (BoxBody)a, handler);
        }
        else if(a.getShape() == BodyShape.BOX &&
                b.getShape() == BodyShape.BOX) {
            return BoxBoxCollision((BoxBody)a, (BoxBody)b, handler);
        }

        return false;
    }

    /**
     * Detect collision between two circle objects
     * @param a first circle object
     * @param b second circle object
     * @param handler collision handler to pass data through
     * @return result of collision detection
     */
    private static boolean CircleCircleCollision(CircleBody a, CircleBody b, CollisionHandler handler) {
        float sumRadii = a.getRadius() + b.getRadius();
        float lengthSquared = a.getPosition().distanceSquared(b.getPosition());
        // Using squared to avoid square rooting to lessen resource use
        if(lengthSquared - (sumRadii * sumRadii) >= 0) return false;

        float length = (float) Math.sqrt(lengthSquared);
        float penetrationDepth = (length - sumRadii) * -0.5f;
        Vector2 collisionNormal;

        // If completely overlapping, set default collision normal to avoid error
        if(length == 0.0f) {
            collisionNormal = new Vector2(1f,0);
        }
        else {
            collisionNormal = new Vector2(b.getPosition()).sub(a.getPosition()).normalize();
        }

        Vector2 collisionPoint = new Vector2(collisionNormal).mul(a.getRadius()).add(a.getPosition());

        handler.addContactPoint(collisionPoint, collisionNormal, penetrationDepth);

        return true;
    }

    /**
     * Detect collision between a circle and an axis oriented box
     * @param a Circle object
     * @param b Box object with rotation 0
     * @param handler collision handler to pass data through
     * @return result of collision detection
     */
    private static boolean CircleAABBCollision(CircleBody a, BoxBody b, CollisionHandler handler) {
        if(b.getRotation() != 0) assert false: "Error: wrongful use of CircleAABB collision. Oriented box passed";
        Vector2 deltaPos = new Vector2(a.getPosition()).sub(b.getPosition());
        Vector2 closestPoint = Vector2.clamp(deltaPos, new Vector2(b.getHalfSize()).mul(-1f), new Vector2(b.getHalfSize()));
        Vector2 edgeToCentre = new Vector2(deltaPos).sub(closestPoint);
        float distanceSquared = edgeToCentre.lengthSquared();

        // Using squared to avoid square rooting to lessen resource use
        if(distanceSquared >= a.getRadius() * a.getRadius()) return false;

        Vector2 collisionNormal;

        // If completely overlapping, set default collision normal to avoid error
        if(edgeToCentre.getX() == 0.0f && edgeToCentre.getY() == 0.0f) {
            collisionNormal = new Vector2(1f,0.0f);
        }
        else {
            collisionNormal = new Vector2(edgeToCentre).normalize();
        }

        float penetrationDepth = (float) (a.getRadius() - Math.sqrt(distanceSquared));
        Vector2 contactPosition = new Vector2(a.getPosition()).add(new Vector2(collisionNormal).mul(-a.getRadius()));
        handler.addContactPoint(contactPosition, collisionNormal, penetrationDepth);
        return true;
    }

    /**
     * Detect collision between a circle and a box
     * @param a Circle object
     * @param b Box object
     * @param handler collision handler to pass data through
     * @return result of collision detection
     */
    private static boolean CircleBoxCollision(CircleBody a, BoxBody b, CollisionHandler handler) {
        Vector2 deltaPos = new Vector2(a.getPosition()).sub(b.getPosition());
        Matrix22 rotB = new Matrix22().rotation((float) Math.toRadians(b.getRotation()));
        Matrix22 invRotB = new Matrix22(rotB).transpose(); // Transpose of a 2d rotation matrix is it's inverse
        Vector2 relativeB = invRotB.mul(new Vector2(deltaPos));

        // Create two local shape objects in the reference frame of the box object
        CircleBody localCircle = new CircleBody(new Vector2(relativeB), a.getRotation(), a.getMass(), a.getRadius(), -1);
        BoxBody localBox = new BoxBody(new Vector2(0,0), 0.0f, b.getMass(), new Vector2(b.getHalfSize()), -1);

        // Pass through the local shapes into CircleAABB collision...
        if(!CircleAABBCollision(localCircle, localBox, handler)) return false;

        // Transform local collision data into world collision data using matrix multiplication
        Vector2 localNormal = handler.getContactPoints().get(0).getCollisionNormal();
        Vector2 worldNormal = rotB.mul(localNormal);
        handler.getContactPoints().get(0).setCollisionNormal(worldNormal);

        Vector2 localPosition = handler.getContactPoints().get(0).getContactPosition();
        Vector2 worldPosition = rotB.mul(localPosition).add(b.getPosition());
        handler.getContactPoints().get(0).setContactPosition(worldPosition);
        return true;
    }

    /**
     * Detect collision between two boxes
     * @param a First box object
     * @param b Second box object
     * @param handler collision handler to pass data through
     * @return result of collision detection
     */
    private static boolean BoxBoxCollision(BoxBody a, BoxBody b, CollisionHandler handler) {
        // Separating Axis theorem
        Vector2 halfSizeA = new Vector2(a.getHalfSize());
        Vector2 halfSizeB = new Vector2(b.getHalfSize());

        Vector2 positionA = new Vector2(a.getPosition());
        Vector2 positionB = new Vector2(b.getPosition());

        Matrix22 rotA = new Matrix22().rotation((float) Math.toRadians(a.getRotation()));
        Matrix22 rotB = new Matrix22().rotation((float) Math.toRadians(b.getRotation()));

        // Transpose of a 2d rotation matrix == it's inverse
        Matrix22 invRotA = new Matrix22(rotA).transpose();
        Matrix22 invRotB = new Matrix22(rotB).transpose();

        Vector2 deltaPos = new Vector2(positionB).sub(positionA);
        Vector2 relativeA = invRotA.mul(new Vector2(deltaPos));
        Vector2 relativeB = invRotB.mul(new Vector2(deltaPos));

        // Transform an object from B's frame of reference to A
        Matrix22 C = new Matrix22(invRotA).mul(rotB);
        Matrix22 absC = new Matrix22(C).absolute();
        Matrix22 invAbsC = new Matrix22(absC).transpose();

        Vector2 separationA = new Vector2(relativeA).absolute().sub(halfSizeA).sub(absC.mul(new Vector2(halfSizeB)));
        if(separationA.getX() > 0.0f || separationA.getY() > 0.0f) return false;

        Vector2 separationB = new Vector2(relativeB).absolute().sub(halfSizeB).sub(invAbsC.mul(new Vector2(halfSizeA)));
        if(separationB.getX() > 0.0f || separationB.getY() > 0.0f) return false;

        // Choosing the axis with the least separation as the reference frame
        // for clipping
        float minimumSeparation = separationA.getX();
        Vector2 normal = rotA.getColumn(0);
        Axis bestAxis = Axis.A_X;

        if(relativeA.getX() <= 0.0f) normal.mul(-1.0f);

        if(separationA.getY() > minimumSeparation) {
            minimumSeparation = separationA.getY();
            normal = rotA.getColumn(1);
            bestAxis = Axis.A_Y;
            if(relativeA.getY() <= 0.0f) normal.mul(-1.0f);
        }

        if(separationB.getX() > minimumSeparation ) {
            minimumSeparation = separationB.getX();
            normal = rotB.getColumn(0);
            bestAxis = Axis.B_X;
            if(relativeB.getX() <= 0.0f) normal.mul(-1.0f);
        }

        if(separationB.getY() > minimumSeparation) {
            normal = rotB.getColumn(1);
            bestAxis = Axis.B_Y;
            if (relativeB.getY() <= 0.0f) normal.mul(-1.0f);
        }


        // Sutherland Hodgeman Clipping
        Vector2 frontNormal; // Normal in the direction of the collision
        Vector2 sideNormal; // Normal perpendicular to the front normal
        Vector2[] incidentEdge = new Vector2[2]; // vertices on the incident edge of the incident box
        incidentEdge[0] = new Vector2();
        incidentEdge[1] = new Vector2();
        float frontFace, negSide, posSide; // Sides of where the collision happened and the edges

        // Setting up clipping data
        switch(bestAxis) {
            case A_X:
                frontNormal = new Vector2(normal);
                frontFace = positionA.dot(frontNormal) + halfSizeA.getX();
                sideNormal = rotA.getColumn(1);
                float side = positionA.dot(sideNormal);

                negSide = -side + halfSizeA.getY();
                posSide = side + halfSizeA.getY();

                selectIncidentEdge(incidentEdge, halfSizeB, positionB, rotB, frontNormal);
                break;

            case A_Y:
                frontNormal = new Vector2(normal);
                frontFace = positionA.dot(frontNormal) + halfSizeA.getY();
                sideNormal = rotA.getColumn(0);
                side = positionA.dot(sideNormal);

                negSide = -side + halfSizeA.getX();
                posSide = side + halfSizeA.getX();

                selectIncidentEdge(incidentEdge, halfSizeB, positionB, rotB, frontNormal);
                break;

            case B_X:
                frontNormal = new Vector2(normal).mul(-1f);
                frontFace = positionB.dot(frontNormal) + halfSizeB.getX();
                sideNormal = rotB.getColumn(1);
                side = positionB.dot(sideNormal);

                negSide = -side + halfSizeB.getY();
                posSide = side + halfSizeB.getY();

                selectIncidentEdge(incidentEdge, halfSizeA, positionA, rotA, frontNormal);
                break;

            case B_Y:
                frontNormal = new Vector2(normal).mul(-1f);
                frontFace = positionB.dot(frontNormal) + halfSizeB.getY();
                sideNormal = rotB.getColumn(0);
                side = positionB.dot(sideNormal);

                negSide = -side + halfSizeB.getX();
                posSide = side + halfSizeB.getX();

                selectIncidentEdge(incidentEdge, halfSizeA, positionA, rotA, frontNormal);
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + bestAxis);
        }

        // Clipping time
        Vector2[] clipPoints1 = new Vector2[2];
        clipPoints1[0] = new Vector2();
        clipPoints1[1] = new Vector2();
        Vector2[] clipPoints2 = new Vector2[2];
        clipPoints2[0] = new Vector2();
        clipPoints2[1] = new Vector2();
        int points; // Vertices left after clipping takes place

        points = clipSegmentToLine(clipPoints1, incidentEdge, new Vector2(sideNormal).mul(-1f), negSide);
        // If clipping has removed colliding points, they are not colliding
        if(points < 2) return false;

        points = clipSegmentToLine(clipPoints2, clipPoints1, sideNormal, posSide);
        // If clipping has removed colliding points, they are not colliding
        if(points < 2) return false;

        normal.normalize();
        for (int i = 0; i < 2; i++) {
            float separation = frontNormal.dot(clipPoints2[i]) - frontFace;
            // If clipped point is intersecting with other box...
            if(separation <= 0) {
                // Add the contact point
                Vector2 position = new Vector2(clipPoints2[i]).sub(new Vector2(frontNormal).mul(separation));
                handler.addContactPoint(position, normal, separation);
            }
        }
        return true;
    }

    /**
     * Select the two vertices located on the incident edge on the incident box
     * @param clipVertices Array to store the incident vertices
     * @param halfSize // Half size of the incident box
     * @param position // Position of the incident box
     * @param rotation // Rotation matrix of the incident box
     * @param normal // Collision normal
     */
    private static void selectIncidentEdge(Vector2[] clipVertices, Vector2 halfSize, Vector2 position, Matrix22 rotation, Vector2 normal) {
        Matrix22 invRotation = new Matrix22(rotation).transpose();
        Vector2 relativeNormal = new Vector2(invRotation.mul(new Vector2(normal))).mul(-1f);
        Vector2 absRelativeNormal = new Vector2(relativeNormal).absolute();

        // What direction are they colliding on...
        if(absRelativeNormal.getX() > absRelativeNormal.getY()) {
            // if it's colliding --> chose the right edge
            // else, chose the left edge
            if(relativeNormal.getX() >= 0.0f) {
                clipVertices[0].set(halfSize.getX(), -halfSize.getY());

                clipVertices[1].set(halfSize.getX(), halfSize.getY());
            } else {
                clipVertices[0].set(-halfSize.getX(), halfSize.getY());

                clipVertices[1].set(-halfSize.getX(), -halfSize.getY());
            }
        } else {
            // if it's colliding ^ chose the top edge
            // else chose the bottom edge
            if(relativeNormal.getY() >= 0.0f) {
                clipVertices[0].set(halfSize.getX(), halfSize.getY());

                clipVertices[1].set(-halfSize.getX(), halfSize.getY());
            } else {
                clipVertices[0].set(-halfSize.getX(), -halfSize.getY());

                clipVertices[1].set(halfSize.getX(), -halfSize.getY());
            }
        }

        clipVertices[0]= new Vector2(position).add(rotation.mul(new Vector2(clipVertices[0])));
        clipVertices[1]= new Vector2(position).add(rotation.mul(new Vector2(clipVertices[1])));
    }

    /**
     * Clip the vertices to the line
     * @param vOut // Output vertices that are clipped to the line specified
     * @param vIn // Input vertices to clip
     * @param normal // The normal of the line to clip on
     * @param offset // offset from the center of the box to clip
     *               __________________
     *               |         offset |  normal
     *               |     o----------|--->
     *               |________________|
     *
     * @return amount of points left after clipping
     */
    private static int clipSegmentToLine(Vector2[] vOut, Vector2[] vIn, Vector2 normal, float offset) {
        int outputs = 0; // number of output points

        float distance0 = normal.dot(vIn[0]) - offset;
        float distance1 = normal.dot(vIn[1]) - offset;

        // If points behind the incident plane
        if(distance0 <= 0.0f) vOut[outputs++] = vIn[0];
        if(distance1 <= 0.0f) vOut[outputs++] = vIn[1];

        // If points on different sides of plane
        if(distance0 * distance1 < 0.0f) {
            // get interpolation point of edge and plane
            float interpolation = distance0 / (distance0 - distance1);
            vOut[outputs] = new Vector2(vIn[0])
                    .add(new Vector2(new Vector2(vIn[1]).sub(vIn[0]))
                            .mul(interpolation));
            outputs++;
        }
        return outputs;
    }


    /**
     * Axis which the collision is happening on
     */
    enum Axis {
        A_X,
        A_Y,
        B_X,
        B_Y
    }

}