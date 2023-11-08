package org.andyou.linguistic_network.lib.util;

import org.andyou.linguistic_network.lib.api.node.ElementNode;

import java.util.*;

public class BFSUtil {

    public static List<Integer> calcPathLengths(ElementNode targetNode) {
        Map<ElementNode, ElementNode> connectionMap = createConnectionMap(targetNode);

        List<Integer> pathLengths = new ArrayList<>();

        for (ElementNode currentNode : connectionMap.keySet()) {
            int pathLength = 0;
            while (!targetNode.equals(currentNode)) {
                pathLength += 1;
                currentNode = connectionMap.get(currentNode);
            }
            pathLengths.add(pathLength);
        }

        return pathLengths;
    }

    private static Map<ElementNode, ElementNode> createConnectionMap(ElementNode targetNode) {
        Set<ElementNode> visited = new HashSet<>();
        Queue<ElementNode> queue = new ArrayDeque<>();
        Map<ElementNode, ElementNode> connectionMap = new HashMap<>();

        visited.add(targetNode);
        queue.add(targetNode);
        while (!queue.isEmpty()) {
            ElementNode currentNode = queue.poll();
            for (ElementNode relatedNode : currentNode.getNeighbors().keySet()) {
                if (!visited.contains(relatedNode)) {
                    visited.add(relatedNode);
                    queue.add(relatedNode);
                    connectionMap.put(relatedNode, currentNode);
                }
            }
        }

        return connectionMap;
    }

}
