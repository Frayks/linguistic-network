package org.andyou.linguistic_network.lib.api.data;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TextRankStatistics {

    private List<TRNode> trNodes;
    private double calculationError;
    private int iterationsCompleted;

}
