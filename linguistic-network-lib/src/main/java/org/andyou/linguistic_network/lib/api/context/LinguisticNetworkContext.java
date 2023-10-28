package org.andyou.linguistic_network.lib.api.context;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LinguisticNetworkContext {

    private MainContext mainContext;
    private LinguisticMetricsContext linguisticMetricsContext;

    public LinguisticNetworkContext() {
        mainContext = new MainContext();
        linguisticMetricsContext = new LinguisticMetricsContext();
    }

}
