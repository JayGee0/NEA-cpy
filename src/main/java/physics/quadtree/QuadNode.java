package physics.quadtree;

import math.Vector2;

import java.util.ArrayList;
import java.util.List;

public class QuadNode<T extends QuadObject> implements QuadObject {
    private final AABB bounds;
    private final int maxDepth;
    private final int objectCapacity;
    private final List<QuadNode<T>> childrenNodes;
    private final List<T> childrenObjects;
    private int nodeCount;
    private boolean isLeaf;

    public QuadNode(AABB bounds, int maxDepth, int objectCapacity, List<T> objects) {
        this.bounds = bounds;
        this.maxDepth = maxDepth;
        this.objectCapacity = objectCapacity;
        this.childrenNodes = new ArrayList<>();
        this.childrenObjects = new ArrayList<>();
        isLeaf = true;
        init(objects);
    }

    /**
     * Initialise the Quad Node, adding all the objects within it's bounds
     * @param objects list of objects near node
     */
    private void init(List<T> objects) {
        // For every object in the list...
        for (T object : objects) {
            // If it's within it's bounds, then add it
            if (object.getBounds().isIntersecting(bounds)) childrenObjects.add(object);
        }

        // If the amount of children objects exceed the capacity..
        if (childrenObjects.size() > objectCapacity && maxDepth > 0) {
            // Then subdivide
            subdivide();
            isLeaf = false;
        }
    }

    private void subdivide() {
            Vector2 position = new Vector2(bounds.getPosition());
            Vector2 quartSize = new Vector2(bounds.getHalfSize()).mul(0.5f);
            int newDepth = this.maxDepth-1;

            // Creating 4 child nodes of equal size in each of the original node
            addChildNode(new QuadNode<>(new AABB(new Vector2(position).add(quartSize), quartSize), newDepth, objectCapacity, childrenObjects));
            addChildNode(new QuadNode<>(new AABB(new Vector2(position).add(quartSize.getX(), -quartSize.getY()), quartSize), newDepth, objectCapacity, childrenObjects));
            addChildNode(new QuadNode<>(new AABB(new Vector2(position).sub(quartSize), quartSize), newDepth, objectCapacity, childrenObjects));
            addChildNode(new QuadNode<>(new AABB(new Vector2(position).add(-quartSize.getX(), quartSize.getY()), quartSize), newDepth, objectCapacity, childrenObjects));
     }

    private void addChildNode(QuadNode<T> node) {
        if(nodeCount >= 4) return;
        this.childrenNodes.add(node);
        nodeCount++;
    }

    public boolean isLeaf() {
        return this.isLeaf;
    }

    public List<QuadNode<T>> getChildrenNodes() {
        return new ArrayList<>(this.childrenNodes);
    }

    public List<T> getChildrenObjects() {
        return new ArrayList<>(this.childrenObjects);
    }

    @Override
    public AABB getBounds() {
        return bounds;
    }
}
