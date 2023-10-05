package physics.quadtree;

import math.Vector2;

// Axis Aligned Bounding Boxes
public class AABB {
    private final Vector2 position;
    private final Vector2 halfSize;

    public AABB(Vector2 position, Vector2 halfSize) {
        this.position = new Vector2(position);
        this.halfSize = new Vector2(halfSize);
    }

    public Vector2 getPosition() {
        return position;
    }

    public Vector2 getHalfSize() {
        return halfSize;
    }

    public boolean isIntersecting(AABB other) {
        Vector2 deltaPos = new Vector2(this.position).sub(other.getPosition()).absolute();
        Vector2 totalSize = new Vector2(this.halfSize).add(other.getHalfSize());
        return deltaPos.getX() < totalSize.getX() &&
                deltaPos.getY() < totalSize.getY();
    }

}
