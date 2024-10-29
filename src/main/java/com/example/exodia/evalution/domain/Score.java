package com.example.exodia.evalution.domain;

/*
ex)
A : 5점
B : 4점
C : 3점
D : 2점
E : 1점
*/

public enum Score {
    A(5), B(4), C(3), D(2), E(1);

    private final int value;

    Score(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
