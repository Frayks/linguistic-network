package org.andyou.linguistic_network.lib.api.context;

import lombok.Getter;
import lombok.Setter;
import org.andyou.linguistic_network.lib.api.constant.StopConditionType;
import org.andyou.linguistic_network.lib.api.data.TRNode;

import java.util.List;

@Getter
@Setter
public class KeywordExtractionTextRankContext {

    private List<TRNode> trNodes;
    private StopConditionType stopConditionType;
    private double accuracy;
    private int iterationCount;
    private double dampingFactor;
    private Double calculationError;
    private int iterationsCompleted;
    private long spentTime;

}
