package com.wedit.weditapp.domain.shared;

import lombok.Getter;

@Getter
public enum Theme {
    TRADITIONAL("전통형"),
    BASIC("일반형");

    private final String theme;

    Theme(String theme) {
        this.theme = theme;
    }
}
