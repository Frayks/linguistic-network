package org.andyou.linguistic_network.lib.api.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CMNode {

    private ElementNode elementNode;
    private double eccentricity;
    private double normalizedEccentricity;
    private double closeness;
    private double normalizedCloseness;
    private double averageCloseness;
    private double normalizedAverageCloseness;

    public CMNode(ElementNode elementNode, double eccentricity, double closeness, double averageCloseness) {
        this.elementNode = elementNode;
        this.eccentricity = eccentricity;
        this.closeness = closeness;
        this.averageCloseness = averageCloseness;
    }

}
