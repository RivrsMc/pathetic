package org.patheloper.model.pathing;

import org.patheloper.api.pathing.result.PathState;
import org.patheloper.api.pathing.result.PathfinderResult;
import org.patheloper.api.pathing.rules.PathingRuleSet;
import org.patheloper.api.pathing.strategy.PathfinderStrategy;
import org.patheloper.api.wrapper.PathPosition;
import org.patheloper.model.pathing.result.PathImpl;
import org.patheloper.model.pathing.result.PathfinderResultImpl;
import org.patheloper.util.WatchdogUtil;

import java.util.Collections;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

public class AStarPathfinder extends AbstractPathfinder {

    public AStarPathfinder(PathingRuleSet pathingRuleSet) {
        super(pathingRuleSet);
    }

    @Override
    protected PathfinderResult findPath(PathPosition start, PathPosition target, PathfinderStrategy strategy) {

        // Create the initial node
        Node startNode = new Node(start.floor(), start.floor(), target.floor(), 0);

        // Create the open and closed sets
        PriorityQueue<Node> nodeQueue = new PriorityQueue<>(Collections.singleton(startNode));
        Set<PathPosition> examinedPositions = new HashSet<>();

        // This is the current depth of the search and the last node
        int depth = 1;
        Node lastEverFound = null;

        while (!nodeQueue.isEmpty() && depth <= pathingRuleSet.getMaxIterations()) {

            // Every 500 iterations, tick the watchdog so that a watchdog timeout doesn't occur
            if (depth % 500 == 0) WatchdogUtil.tickWatchdog();

            // Get the next node from the queue
            Node currentNode = nodeQueue.poll();
            if (currentNode == null)
                throw new IllegalStateException("Something just exploded");

            if(lastEverFound == null || currentNode.heuristic() < lastEverFound.heuristic())
                lastEverFound = currentNode;

            // Check to see if we have reached the length limit
            if (pathingRuleSet.getMaxLength() > 0 && PathingHelper.getProgress(lastEverFound) >= pathingRuleSet.getMaxLength())
                return PathingHelper.finishPathing(new PathfinderResultImpl(PathState.FOUND, PathingHelper.fetchRetracedPath(lastEverFound)));

            // This means that the current node is the target, so we can stop here
            if (currentNode.hasReachedEnd())
                return PathingHelper.finishPathing(new PathfinderResultImpl(PathState.FOUND, PathingHelper.fetchRetracedPath(lastEverFound)));

            PathingHelper.evaluateNewNodes(nodeQueue, examinedPositions, currentNode, offset, strategy, snapshotManager);
            depth++;
        }

        if (pathingRuleSet.isAllowingFallback() && lastEverFound != null)
            return PathingHelper.finishPathing(new PathfinderResultImpl(PathState.FALLBACK, PathingHelper.fetchRetracedPath(lastEverFound)));

        return PathingHelper.finishPathing(new PathfinderResultImpl(PathState.FAILED, new PathImpl(start, target, EMPTY_LINKED_HASHSET)));
    }

}