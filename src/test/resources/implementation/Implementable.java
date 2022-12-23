package org.example.implementation;

interface Implementable {
    default String saySomething() {
        return "Hello!";
    }
}