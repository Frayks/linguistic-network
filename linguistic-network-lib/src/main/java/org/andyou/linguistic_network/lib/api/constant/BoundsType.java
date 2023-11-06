package org.andyou.linguistic_network.lib.api.constant;

import lombok.Getter;

@Getter
public enum BoundsType {
    ABSENT("Absent"),
    WORD("Word"),
    SENTENCE("Sentence");

    private final String name;

    BoundsType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
