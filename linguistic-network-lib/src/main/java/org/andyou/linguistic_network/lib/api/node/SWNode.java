package org.andyou.linguistic_network.lib.api.node;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SWNode {

    private ElementNode elementNode;
    private Double contribution1;
    private Double adjustedContribution1;
    private Double normalizedContribution1;
    private Double contribution2;
    private Double normalizedContribution2;
    private Double contribution3;
    private Double adjustedContribution3;
    private Double normalizedContribution3;

    public SWNode(ElementNode elementNode, Double contribution1, Double contribution2, Double contribution3) {
        this.elementNode = elementNode;
        this.contribution1 = contribution1;
        this.contribution2 = contribution2;
        this.contribution3 = contribution3;
    }
}
