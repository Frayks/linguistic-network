package org.andyou.linguistic_network.api.node;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CDFNode {

    private int k; // Unique number of neighbors
    private int n;
    private double pdf;
    private double cdf;

    public CDFNode(int k, int n) {
        this.k = k;
        this.n = n;
    }

}
