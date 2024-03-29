package org.andyou.linguistic_network.lib.api.data;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class ElementNode {

    private int index;
    private String element;
    private int frequency;
    private double clusteringCoefficient;
    private double averagePathLength;
    private double multiplicity;
    private Map<ElementNode, Integer> neighbors;

    public ElementNode(String element, int frequency) {
        this.element = element;
        this.frequency = frequency;
        this.neighbors = new HashMap<>();
    }

    public ElementNode(ElementNode elementNode) {
        this.index = elementNode.index;
        this.element = elementNode.element;
        this.frequency = elementNode.frequency;
        this.clusteringCoefficient = elementNode.clusteringCoefficient;
        this.averagePathLength = elementNode.averagePathLength;
        this.neighbors = new HashMap<>();
    }

    public int getNeighborCount() {
        return neighbors.size();
    }

}
