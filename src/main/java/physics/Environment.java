package physics;

import math.Vector2;
import physics.body.BodyShape;
import physics.body.BoxBody;
import physics.body.CircleBody;
import physics.body.RigidBody;
import physics.collision.CollisionHandler;
import physics.quadtree.AABB;
import physics.quadtree.QuadNode;
import physics.quadtree.QuadTree;

import java.util.*;

public class Environment {
    private final List<RigidBody> objects;
    private final Vector2 gravity;
    private int iterations;

    private final float width;
    private final float height;

    private QuadTree<RigidBody> quadTree;
    private int maxDepth;
    private int nodeCapacity;

    /**
     * Constructor for the environment
     * @param gravity gravity of the environment in ms^-2
     * @param width width of the environment in meters
     * @param height height of the environment in meters
     */
    public Environment(Vector2 gravity, float width, float height) {
        RigidBody.resetIDCounter();
        this.gravity = new Vector2(gravity);
        this.objects = new ArrayList<>();
        this.width = width;
        this.height = height;

        this.maxDepth = 4;
        this.nodeCapacity = 3;
        this.iterations = 15;

        this.quadTree = new QuadTree<>(new AABB(new Vector2(width/2f, height/2f), new Vector2(width/2f, height/2f)), maxDepth, nodeCapacity, objects);

        createDefaultScene();
    }

    /**
     * Load the default scene
     */
    public void createDefaultScene() {
        objects.clear();
        objects.add(new BoxBody(new Vector2(width/2, 0), 0, 0, new Vector2(width,0.1f)));
        objects.add(new BoxBody(new Vector2(0,height/2), 0, 0, new Vector2(0.1f,height)));
        objects.add(new BoxBody(new Vector2(width,height/2), 0, 0, new Vector2(0.1f,height)));
        objects.add(new BoxBody(new Vector2(width/2,height), 0, 0, new Vector2(width,0.1f)));

        objects.add(new CircleBody(new Vector2(2f, 2f), 0, 3.5f, 0.3f));
    }


    /**
     * Update the environment each frame
     * @param dt delta time in seconds
     */
    public void update(float dt) {
        // frequency of frames in Hz
        float invDT = dt > 0.0f ? 1.0f / dt : 0.0f;

        cleanList();
        Set<CollisionHandler> collidingObjects = broadPhase();

        // Integrate acceleration
        for(RigidBody b : objects) {
            if(b.getInvMass() != 0) {
                b.setLinearVelocity(b.getLinearVelocity().add(new Vector2(gravity).mul(dt)));
            }
            if(b.getInvI() != 0) {
                b.setAngularVelocity(b.getAngularVelocity());
            }
        }

        // Pre calculate all the variables for each handler
        for(CollisionHandler handler : collidingObjects) {
            handler.preStep(invDT);
        }

        // For a fixed amount of iterations...
        for (int i = 0; i < iterations; i++) {
            // Resolve any collisions
            for(CollisionHandler handler : collidingObjects) {
                handler.resolveCollision();
            }
        }

        // Integrate velocities
        for(RigidBody b : objects) {
            if(b.getInvMass() != 0) {
                b.setPosition(b.getPosition().add(new Vector2(b.getLinearVelocity()).mul(dt)   ));
            }
            if(b.getInvI() != 0) {
                b.setRotation(b.getRotation() + (float)Math.toDegrees(b.getAngularVelocity() * dt));
            }
        }
    }

    /**
     * Clean the list of objects, deleting every object not within the bounds of the scene
     */
    private void cleanList() {
        objects.removeIf(b -> b.getPosition().getX() < -1 || b.getPosition().getX() > width + 1);
        objects.removeIf(b -> b.getPosition().getY() < -1 || b.getPosition().getY() > height + 1);
    }

    private Set<CollisionHandler> broadPhase() {
        quadTree = new QuadTree<>(new AABB(new Vector2(width/2f, height/2f), new Vector2(width/2f, height/2f)), maxDepth, nodeCapacity, objects);

        Set<CollisionHandler> collidingObjects = new HashSet<>();
        for(QuadNode<RigidBody> leaf : quadTree.getAllLeaves()) {
            List<RigidBody> children = leaf.getChildrenObjects();
            for (int i = 0; i < children.size(); ++i) {
                for (int j = i+1; j < children.size(); ++j) {
                    if(children.get(i).getInvMass() == 0 && children.get(j).getInvMass() == 0) continue;
                    if(!children.get(i).getBounds().isIntersecting(children.get(j).getBounds())) continue;
                    CollisionHandler handler = new CollisionHandler(children.get(i), children.get(j));
                    if(handler.isColliding()) {
                        collidingObjects.add(handler);
                    }
                }
            }
        }
        return collidingObjects;
    }

    public List<RigidBody> getObjects() {
        return new ArrayList<>(objects);
    }

    public QuadTree<RigidBody> getQuadTree() {
        return quadTree;
    }


    public void addObject(RigidBody b) {
        if(b.getShape() == BodyShape.BOX) objects.add(0, b);
        else objects.add(b);
    }

    public void removeObject(RigidBody b) {
        objects.remove(b);
    }


    public float getGravity() {
        return this.gravity.getY();
    }

    public void setGravity(float g) {
        this.gravity.set(new Vector2(0, g));
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public int getNodeCapacity() {
        return nodeCapacity;
    }

    public void setNodeCapacity(int nodeCapacity) {
        this.nodeCapacity = nodeCapacity;
    }

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }
}
