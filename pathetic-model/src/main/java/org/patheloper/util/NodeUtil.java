package org.patheloper.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.patheloper.api.pathing.result.Path;
import org.patheloper.api.pathing.strategy.PathfinderStrategy;
import org.patheloper.api.snapshot.SnapshotManager;
import org.patheloper.api.wrapper.PathBlock;
import org.patheloper.api.wrapper.PathPosition;
import org.patheloper.api.wrapper.PathVector;
import org.patheloper.model.pathing.Node;
import org.patheloper.model.pathing.Offset;
import org.patheloper.model.pathing.result.PathImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This is a utility class that provides various helper methods for working with {@link Node} objects.
 * These methods do not fit into any other class and are provided here for convenience.
 */
@UtilityClass
public class NodeUtil {

    /**
     * Evaluates new nodes and adds them to the given node queue if they are valid.
     *
     * @param nodeQueue         the node queue to add new nodes to
     * @param examinedPositions a set of examined positions
     * @param currentNode       the current node
     * @param offset            the offset to apply to the current node's position to find neighbours
     * @param strategy          the pathfinder strategy to use for validating nodes
     * @param snapshotManager   the snapshot manager to use for validating nodes
     */
    public static void evaluateNewNodes(Collection<Node> nodeQueue, Set<PathPosition> examinedPositions, Node currentNode, Offset offset, PathfinderStrategy strategy, SnapshotManager snapshotManager) {
        for (Node neighbourNode : fetchNeighbours(currentNode, offset.getVectors()))
            if (isNodeValid(neighbourNode, nodeQueue, snapshotManager, examinedPositions, strategy))
                nodeQueue.add(neighbourNode);
    }

    /**
     * Returns the progress made so far along the path represented by the given node.
     *
     * @param node the node to calculate progress for
     * @return the progress made so far along the path represented by the given node
     */
    public static int getProgress(Node node) {

        int length = 0;

        Node currentNode = node;
        while (currentNode != null) {
            length++;
            currentNode = currentNode.getParent();
        }

        return length;
    }

    /**
     * Bloating up like a bubble until a reachable block is found
     * The block itself might not be passable, but at least reachable from the outside
     *
     * @api.Note The reachable block is not guaranteed to be the closest reachable block
     */
    public static PathBlock bubbleSearchAlternative(PathPosition target, Offset offset, SnapshotManager snapshotManager) {

        Set<PathPosition> newPositions = new HashSet<>();
        newPositions.add(target);

        Set<PathPosition> examinedPositions = new HashSet<>();
        while (!newPositions.isEmpty()) {
            Set<PathPosition> nextPositions = new HashSet<>();
            for (PathPosition position : newPositions) {
                for (PathVector vector : offset.getVectors()) {

                    PathPosition offsetPosition = position.add(vector);
                    PathBlock pathBlock = snapshotManager.getBlock(offsetPosition);

                    if (pathBlock.isPassable() && !pathBlock.getPathPosition().isInSameBlock(target))
                        return pathBlock;

                    if (!examinedPositions.contains(offsetPosition))
                        nextPositions.add(offsetPosition);
                }
                examinedPositions.add(position);
            }
            newPositions = nextPositions;
        }

        return snapshotManager.getBlock(target);
    }

    /**
     * Determines whether the given node is valid and can be added to the node queue.
     *
     * @param node              the node to validate
     * @param nodeQueue         the node queue to check for duplicates
     * @param snapshotManager   the snapshot manager to use for validating nodes
     * @param examinedPositions a set of examined positions
     * @param strategy          the pathfinder strategy to use for validating nodes
     * @return {@code true} if the node is valid and can be added to the node queue, {@code false} otherwise
     */
    public static boolean isNodeValid(Node node, Collection<Node> nodeQueue, SnapshotManager snapshotManager, Set<PathPosition> examinedPositions, PathfinderStrategy strategy) {

        if (examinedPositions.contains(node.getPosition()))
            return false;

        if (nodeQueue.contains(node))
            return false;

        if (!isWithinWorldBounds(node.getPosition()))
            return false;

        if (!strategy.isValid(node.getPosition(), snapshotManager))
            return false;

        return examinedPositions.add(node.getPosition());
    }

    /**
     * Determines whether the given position is within the bounds of the world.
     *
     * @param position the position to check
     * @return {@code true} if the position is within the bounds of the world, {@code false} otherwise
     */
    public static boolean isWithinWorldBounds(PathPosition position) {
        return position.getPathEnvironment().getMinHeight() < position.getBlockY() && position.getBlockY() < position.getPathEnvironment().getMaxHeight();
    }

    /**
     * Fetches the neighbours of the given node.
     *
     * @param currentNode the node to fetch neighbours for
     * @param offsets     the offsets to apply to the current node's position to find neighbours
     * @return a collection of neighbour nodes
     */
    public static Collection<Node> fetchNeighbours(Node currentNode, PathVector[] offsets) {

        Set<Node> newNodes = new HashSet<>(offsets.length);

        for (PathVector offset : offsets) {

            Node newNode = new Node(currentNode.getPosition().add(offset), currentNode.getStart(), currentNode.getTarget(), currentNode.getDepth() + 1);
            newNode.setParent(currentNode);

            newNodes.add(newNode);
        }

        return newNodes;
    }
    
    public static Path fetchMergedPath(Node endNode1, Node endNode2) {
        List<PathPosition> path1 = new ArrayList<>();
        List<PathPosition> path2 = new ArrayList<>();
        
        // Trace path 1 from end to start
        Node currentNode = endNode1;
        while (currentNode != null) {
            path1.add(currentNode.getPosition());
            currentNode = currentNode.getParent();
        }
        
        // Trace path 2 from end to start
        currentNode = endNode2;
        while (currentNode != null) {
            path2.add(currentNode.getPosition());
            currentNode = currentNode.getParent();
        }
        
        // Combine the two paths in reverse order
        Collections.reverse(path1);
        List<PathPosition> mergedPath = new ArrayList<>(path1);
        mergedPath.addAll(path2);
        
        // Remove overlapping nodes
        for (int i = 0; i < mergedPath.size() - 1; i++) {
            if (mergedPath.get(i).equals(mergedPath.get(i + 1))) {
                mergedPath.remove(i + 1);
                i--;
            }
        }
        
        return new PathImpl(endNode1.getStart(), endNode1.getTarget(), mergedPath);
    }

    /**
     * Fetches the path represented by the given node by retracing the steps from the node's parent.
     *
     * @param node the node to fetch the path for
     * @return the path represented by the given node
     */
    public static Path fetchRetracedPath(@NonNull Node node) {

        if(node.getParent() == null)
            return new PathImpl(node.getStart(), node.getTarget(), Collections.singletonList(node.getPosition()));

        List<PathPosition> path = new ArrayList<>();

        Node currentNode = node;
        while (currentNode != null) {
            path.add(currentNode.getPosition());
            currentNode = currentNode.getParent();
        }

        path.add(node.getStart());
        Collections.reverse(path);

        return new PathImpl(node.getStart(), node.getTarget(), path);
    }
}