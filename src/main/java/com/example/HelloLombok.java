package com.example;

import lombok.Getter;

@Getter
public class HelloLombok {
	private final String hello; 
    private final int lombok; 

    public HelloLombok(String hello, int lombok) { 
        this.hello = hello; 
        this.lombok = lombok; 
    } 
}
