package org.andyou.linguistic_network.lib.api.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SWNode {

    private ElementNode elementNode;
    private double contribution1;
    private double adjustedContribution1;
    private double normalizedContribution1;
    private double contribution2;
    private double normalizedContribution2;
    private double contribution3;
    private double adjustedContribution3;
    private double normalizedContribution3;

    public SWNode(ElementNode elementNode, double contribution1, double contribution2, double contribution3) {
        this.elementNode = elementNode;
        this.contribution1 = contribution1;
        this.contribution2 = contribution2;
        this.contribution3 = contribution3;
    }
}
