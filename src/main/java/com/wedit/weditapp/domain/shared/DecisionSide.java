package com.wedit.weditapp.domain.shared;

import lombok.Getter;

@Getter
public enum DecisionSide {
    GROOM("신랑측"),
    BRIDE("신부측");

    private final String side;

    DecisionSide(String side){
        this.side = side;
    }
}
