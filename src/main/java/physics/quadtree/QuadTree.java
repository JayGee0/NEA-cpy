package physics.quadtree;

import java.util.ArrayList;
import java.util.List;

public class QuadTree<T extends QuadObject> {
    private final QuadNode<T> rootNode;
    private final List<QuadNode<T>> allNodes;
    private final List<QuadNode<T>> leafNodes;

    public QuadTree(AABB bounds, int maxDepth, int nodeCapacity, List<T> objects) {
        allNodes = new ArrayList<>();
        leafNodes = new ArrayList<>();

        rootNode = new QuadNode<>(bounds, maxDepth, nodeCapacity, objects);
    }

    // Keep a store of all the nodes, so you don't have to redo the algorithm each time
    public List<QuadNode<T>> getAllNodes() {
        if(allNodes.isEmpty()) getAllNodes(rootNode, allNodes);
        return allNodes;
    }

    // Depth first search getting all the nodes
    private void getAllNodes(QuadNode<T> node, List<QuadNode<T>> allNodes) {
        allNodes.add(node);
        if(node.isLeaf()) {
            return;
        }
        for(QuadNode<T> child : node.getChildrenNodes()) {
            getAllLeaves(child, allNodes);
        }
    }


    // Keep a store of the leaves, so you don't have to redo the algorithm each time
    public List<QuadNode<T>> getAllLeaves() {
        if(leafNodes.isEmpty()) getAllLeaves(rootNode, leafNodes);
        return leafNodes;
    }

    // Depth first search getting all the leaves
    private void getAllLeaves(QuadNode<T> node, List<QuadNode<T>> leaves) {
        if(node.isLeaf()) {
            leaves.add(node);
            return;
        }
        for(QuadNode<T> child : node.getChildrenNodes()) {
            getAllLeaves(child, leaves);
        }
    }

}
