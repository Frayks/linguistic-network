package org.andyou.linguistic_network.lib.api.constant;

import lombok.Getter;

@Getter
public enum NGramType {
    WORDS("Words"),
    LETTERS_AND_NUMBERS("Letters and numbers"),
    SYMBOLS("Symbols");

    private String name;

    NGramType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
