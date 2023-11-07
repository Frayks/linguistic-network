package org.andyou.linguistic_network.lib.util;

import org.andyou.linguistic_network.lib.ProgressBarProcessor;
import org.andyou.linguistic_network.lib.api.node.ElementNode;

import java.util.*;

public class ElementNodeGraphUtil {

    public static Set<ElementNode> createElementNodeGraph(String[][] elementGroups, boolean useRange, int rangeSize, ProgressBarProcessor progressBarProcessor) {
        Map<String, ElementNode> elementNodeMap = new HashMap<>();
        if (progressBarProcessor != null) {
            int stepsCount = Arrays.stream(elementGroups).mapToInt(elementGroup -> elementGroup.length).sum();
            progressBarProcessor.initNextBlock(stepsCount);
        }

        for (String[] elementGroup : elementGroups) {
            for (String element : elementGroup) {
                ElementNode elementNode = elementNodeMap.computeIfAbsent(element, key -> new ElementNode(key, 0));
                elementNode.setFrequency(elementNode.getFrequency() + 1);
            }
            for (int i = 0; i < elementGroup.length; i++) {
                String element = elementGroup[i];

                List<String> neighborElements;
                if (useRange) {
                    neighborElements = getNeighborElements(elementGroup, i, rangeSize);
                } else {
                    neighborElements = getNeighborElements(elementGroup, i);
                }

                ElementNode elementNode = elementNodeMap.get(element);
                for (String neighborElement : neighborElements) {
                    elementNode.getNeighbors().merge(elementNodeMap.get(neighborElement), 1, Integer::sum);
                }
                if (progressBarProcessor != null) {
                    progressBarProcessor.walk();
                }
            }
        }

        return new HashSet<>(elementNodeMap.values());
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

    private static List<String> getNeighborElements(String[] elementGroup, int index, int rangeSize) {
        List<String> neighborElements = new ArrayList<>();
        int startIndex = Math.max(0, index - rangeSize);
        int endIndex = Math.min(elementGroup.length - 1, index + rangeSize);

        String element = elementGroup[index];
        for (int i = startIndex; i <= endIndex; i++) {
            String currentElement = elementGroup[i];
            if (!element.equals(currentElement)) {
                neighborElements.add(currentElement);
            }
        }

        return neighborElements;
    }

    public static ElementNode getElementNodeByIndex(Set<ElementNode> elementNodeGraph, int index) {
        return elementNodeGraph.stream()
                .filter(elementNode -> elementNode.getIndex() == index)
                .findFirst().orElse(null);
    }

    public static void filterByFrequency(Set<ElementNode> elementNodeGraph, int frequency) {
        elementNodeGraph.removeIf(elementNode -> {
            if (elementNode.getFrequency() <= frequency) {
                removeFromNeighbors(elementNode);
                return true;
            }
            return false;
        });
    }

    public static List<ElementNode> sortAndSetIndex(Set<ElementNode> elementNodeGraph) {
        List<ElementNode> elementNodes = new ArrayList<>(elementNodeGraph);
        elementNodes.sort(Comparator.comparingInt(ElementNode::getFrequency)
                .thenComparing(ElementNode::getElement)
                .reversed());

        for (int i = 0; i < elementNodes.size(); i++) {
            ElementNode elementNode = elementNodes.get(i);
            elementNode.setIndex(i + 1);
        }

        return elementNodes;
    }

    // Erdos–Renyi G(n, M)
    // n - number of vertices
    // m - number of edges
    public static Set<ElementNode> generateRandomElementNodeGraph(int n, int m) {
        if (m > (n * (n - 1)) / 2) {
            throw new IllegalArgumentException(String.format("Graph G(n, M), i.e. G(%d, %d) is not possible!", n, m));
        }

        List<ElementNode> elementNodeGraph = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            elementNodeGraph.add(new ElementNode(String.valueOf(i), 0));
        }

        Random random = new Random();
        int i = 0;
        while (i < m) {
            ElementNode elementNode1 = elementNodeGraph.get(random.nextInt(elementNodeGraph.size()));
            ElementNode elementNode2 = elementNodeGraph.get(random.nextInt(elementNodeGraph.size()));

            if (!elementNode1.equals(elementNode2) && !elementNode1.getNeighbors().containsKey(elementNode2)) {
                elementNode1.getNeighbors().merge(elementNode2, 1, Integer::sum);
                elementNode2.getNeighbors().merge(elementNode1, 1, Integer::sum);
                i++;
            }
        }

        return new HashSet<>(elementNodeGraph);
    }

    public static Set<ElementNode> clone(Set<ElementNode> elementNodeGraph) {
        Map<ElementNode, ElementNode> elementNodeCloneMap = new HashMap<>();

        for (ElementNode elementNode : elementNodeGraph) {
            elementNodeCloneMap.put(elementNode, new ElementNode(elementNode));
        }
        for (ElementNode elementNode : elementNodeGraph) {
            ElementNode elementNodeClone = elementNodeCloneMap.get(elementNode);
            for (Map.Entry<ElementNode, Integer> neighbor : elementNode.getNeighbors().entrySet()) {
                elementNodeClone.getNeighbors().put(elementNodeCloneMap.get(neighbor.getKey()), neighbor.getValue());
            }
        }

        return new HashSet<>(elementNodeCloneMap.values());
    }

    public static void removeElementNode(Set<ElementNode> elementNodeGraph, ElementNode elementNode) {
        elementNodeGraph.remove(elementNode);
        removeFromNeighbors(elementNode);
    }

    private static void removeFromNeighbors(ElementNode elementNode) {
        for (ElementNode neighbor : elementNode.getNeighbors().keySet()) {
            neighbor.getNeighbors().remove(elementNode);
        }
    }

    public static void addElementNode(Set<ElementNode> elementNodeGraph, ElementNode elementNode) {
        elementNodeGraph.add(elementNode);
        addToNeighbors(elementNode);
    }

    private static void addToNeighbors(ElementNode elementNode) {
        for (Map.Entry<ElementNode, Integer> neighbor : elementNode.getNeighbors().entrySet()) {
            neighbor.getKey().getNeighbors().put(elementNode, neighbor.getValue());
        }
    }

}
