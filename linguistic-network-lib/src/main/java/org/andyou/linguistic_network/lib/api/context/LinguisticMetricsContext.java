package org.andyou.linguistic_network.lib.api.context;

import lombok.Getter;
import lombok.Setter;
import org.andyou.linguistic_network.lib.api.node.CDFNode;

import java.util.List;

@Getter
@Setter
public class LinguisticMetricsContext {

    private List<CDFNode> cdfNodes;
    private long spentTime;

}
