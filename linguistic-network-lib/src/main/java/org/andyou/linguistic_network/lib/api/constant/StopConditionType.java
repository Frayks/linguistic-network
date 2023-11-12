package org.andyou.linguistic_network.lib.api.constant;

import lombok.Getter;

@Getter
public enum StopConditionType {

    ACCURACY("Accuracy"),
    ITERATION_COUNT("Iteration count");

    private final String name;

    StopConditionType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

}
