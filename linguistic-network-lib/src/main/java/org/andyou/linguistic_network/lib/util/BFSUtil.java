package org.andyou.linguistic_network.lib.util;

import org.andyou.linguistic_network.lib.api.node.SWNode;

import java.util.*;

public class BFSUtil {

    public static List<Integer> calcPathLengths(SWNode targetSWNode) {
        Map<SWNode, SWNode> connectionMap = createConnectionMap(targetSWNode);

        List<Integer> pathLengths = new ArrayList<>();

        for (SWNode currentSWNode : connectionMap.keySet()) {
            int pathLength = 0;
            while (!targetSWNode.equals(currentSWNode)) {
                pathLength += 1;
                currentSWNode = connectionMap.get(currentSWNode);
            }
            pathLengths.add(pathLength);
        }

        return pathLengths;
    }

    private static Map<SWNode, SWNode> createConnectionMap(SWNode targetSWNode) {
        Set<SWNode> visited = new HashSet<>();
        Queue<SWNode> queue = new ArrayDeque<>();
        Map<SWNode, SWNode> connectionMap = new HashMap<>();

        visited.add(targetSWNode);
        queue.add(targetSWNode);
        while (!queue.isEmpty()) {
            SWNode currentSWNode = queue.poll();
            for (SWNode relatedSWNode : currentSWNode.getNeighbors()) {
                if (!visited.contains(relatedSWNode)) {
                    visited.add(relatedSWNode);
                    queue.add(relatedSWNode);
                    connectionMap.put(relatedSWNode, currentSWNode);
                }
            }
        }

        return connectionMap;
    }

}
