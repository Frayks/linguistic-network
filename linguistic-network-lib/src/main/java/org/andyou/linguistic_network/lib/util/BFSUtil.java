package org.andyou.linguistic_network.lib.util;

import org.andyou.linguistic_network.lib.api.node.ElementNode;

import java.util.*;

public class BFSUtil {

    public static List<Integer> calcPathLengths(ElementNode targetElementNode) {
        Map<ElementNode, ElementNode> connectionMap = createConnectionMap(targetElementNode);

        List<Integer> pathLengths = new ArrayList<>();

        for (ElementNode currentElementNode : connectionMap.keySet()) {
            int pathLength = 0;
            while (!targetElementNode.equals(currentElementNode)) {
                pathLength += 1;
                currentElementNode = connectionMap.get(currentElementNode);
            }
            pathLengths.add(pathLength);
        }

        return pathLengths;
    }

    private static Map<ElementNode, ElementNode> createConnectionMap(ElementNode targetElementNode) {
        Set<ElementNode> visited = new HashSet<>();
        Queue<ElementNode> queue = new ArrayDeque<>();
        Map<ElementNode, ElementNode> connectionMap = new HashMap<>();

        visited.add(targetElementNode);
        queue.add(targetElementNode);
        while (!queue.isEmpty()) {
            ElementNode currentElementNode = queue.poll();
            for (ElementNode relatedElementNode : currentElementNode.getNeighbors()) {
                if (!visited.contains(relatedElementNode)) {
                    visited.add(relatedElementNode);
                    queue.add(relatedElementNode);
                    connectionMap.put(relatedElementNode, currentElementNode);
                }
            }
        }

        return connectionMap;
    }

}
