package org.andyou.linguistic_network.lib.api.context;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LinguisticNetworkContext {

    private MainContext mainContext;
    private SmallWorldContext smallWorldContext;

    public LinguisticNetworkContext() {
        mainContext = new MainContext();
        smallWorldContext = new SmallWorldContext();
    }

}
