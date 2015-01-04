package org.specs.antbuilder;

import org.gradle.Person2;


public class Person {
    private final String name;

    public Person(String name) {
        this.name = name;
        
        System.out.println(new Person2().getVersion());
    }

    public String getName() {
        return name;
    }
    
    public static void main(String[] args) {
    	new Person("joao");
    }
}
