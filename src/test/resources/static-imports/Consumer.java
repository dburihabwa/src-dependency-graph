package org.example;


import static org.example.Producer.DEFAULT_VALUE;
import static org.example.Producer.getAGreaterNumber;
import static org.example.Producer.getANumber;

class Consumer {
    void foo() {
        System.out.println("Priting a number: " + getANumber());
        System.out.println("Priting a number: " + getAGreaterNumber(DEFAULT_VALUE));
    }
}