package org.andyou.linguistic_network.util;

import org.andyou.linguistic_network.api.node.SWNode;

import java.util.*;

public class SWNodeGraphUtil {

    public static Set<SWNode> createSWNodeGraph(String[][] elementGroups, Integer range) {
        Map<String, SWNode> swNodeMap = new HashMap<>();

        for (String[] elementGroup : elementGroups) {
            for (String element : elementGroup) {
                SWNode swNode = swNodeMap.computeIfAbsent(element, key -> new SWNode(key, 0));
                swNode.setFrequency(swNode.getFrequency() + 1);
            }
            for (int i = 0; i < elementGroup.length; i++) {
                String element = elementGroup[i];

                List<String> neighborElements;
                if (range == null) {
                    neighborElements = getNeighborElements(elementGroup, i);
                } else {
                    neighborElements = getNeighborElements(elementGroup, i, range);
                }

                SWNode swNode = swNodeMap.get(element);
                for (String neighborElement : neighborElements) {
                    swNode.getNeighbors().add(swNodeMap.get(neighborElement));
                }
            }
        }

        return new HashSet<>(swNodeMap.values());
    }

    private static List<String> getNeighborElements(String[] elementGroup, int index) {
        List<String> neighborElements = new ArrayList<>();

        String element = elementGroup[index];
        for (String currentElement : elementGroup) {
            if (!element.equals(currentElement)) {
                neighborElements.add(currentElement);
            }
        }

        return neighborElements;
    }

    private static List<String> getNeighborElements(String[] elementGroup, int index, int range) {
        List<String> neighborElements = new ArrayList<>();
        int startIndex = Math.max(0, index - range);
        int endIndex = Math.min(elementGroup.length - 1, index + range);

        String element = elementGroup[index];
        for (int i = startIndex; i <= endIndex; i++) {
            String currentElement = elementGroup[i];
            if (!element.equals(currentElement)) {
                neighborElements.add(currentElement);
            }
        }

        return neighborElements;
    }

    public static void filterByFrequency(Set<SWNode> swNodeGraph, int frequency) {
        swNodeGraph.removeIf(swNode -> {
            if (swNode.getFrequency() <= frequency) {
                removeFromNeighbors(swNode);
                return true;
            }
            return false;
        });
    }

    // Erdos–Renyi G(n, M)
    // n - number of vertices
    // m - number of edges
    public static Set<SWNode> generateRandomSWNodeGraph(int n, int m) {
        if (m > (n * (n - 1)) / 2) {
            throw new IllegalArgumentException(String.format("Graph G(n, M), i.e. G(%d, %d) is not possible!", n, m));
        }

        List<SWNode> swNodeGraph = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            swNodeGraph.add(new SWNode(String.valueOf(i), 0));
        }

        Random random = new Random();
        int i = 0;
        while (i < m) {
            SWNode swNode1 = swNodeGraph.get(random.nextInt(swNodeGraph.size()));
            SWNode swNode2 = swNodeGraph.get(random.nextInt(swNodeGraph.size()));

            if (!swNode1.equals(swNode2) && !swNode1.getNeighbors().contains(swNode2)) {
                swNode1.getNeighbors().add(swNode2);
                swNode2.getNeighbors().add(swNode1);
                i++;
            }
        }

        return new HashSet<>(swNodeGraph);
    }

    public static Set<SWNode> clone(Set<SWNode> swNodeGraph) {
        Map<String, SWNode> swNodeMap = new HashMap<>();

        for (SWNode swNode : swNodeGraph) {
            swNodeMap.put(swNode.getElement(), new SWNode(swNode));
        }
        for (SWNode swNode : swNodeGraph) {
            SWNode swNodeCopy = swNodeMap.get(swNode.getElement());
            for (SWNode neighbor : swNode.getNeighbors()) {
                swNodeCopy.getNeighbors().add(swNodeMap.get(neighbor.getElement()));
            }
        }

        return new HashSet<>(swNodeMap.values());
    }

    public static void removeSWNode(Set<SWNode> swNodeGraph, SWNode swNode) {
        swNodeGraph.remove(swNode);
        removeFromNeighbors(swNode);
    }

    private static void removeFromNeighbors(SWNode swNode) {
        for (SWNode neighbor : swNode.getNeighbors()) {
            neighbor.getNeighbors().remove(swNode);
        }
    }

    public static void addSWNode(Set<SWNode> swNodeGraph, SWNode swNode) {
        swNodeGraph.add(swNode);
        addToNeighbors(swNode);
    }

    private static void addToNeighbors(SWNode swNode) {
        for (SWNode neighbor : swNode.getNeighbors()) {
            neighbor.getNeighbors().add(swNode);
        }
    }

}