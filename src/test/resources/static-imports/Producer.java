package org.example;

public class Producer {
    public static final int DEFAULT_VALUE = 42;
    public static int getAGreaterNumber(int number) {
        return number + 1;
    }

    public static int getANumber() {
        return DEFAULT_VALUE;
    }
}