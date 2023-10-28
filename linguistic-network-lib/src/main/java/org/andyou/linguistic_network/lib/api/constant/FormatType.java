package org.andyou.linguistic_network.lib.api.constant;

import lombok.Getter;

@Getter
public enum FormatType {
    TEXT("Text"),
    EXCEL("Excel");

    private String name;

    FormatType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
