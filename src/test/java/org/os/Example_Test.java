package org.os;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class Example_Test {
    @Test
    void threePlusOneEqualFour(){
        Example example = new Example();

        int result = example.Add(3,1);

        assertEquals(4,result);

    }    @Test
    void twoPlustwoEqualFour(){
        Example example = new Example();

        int result = example.Add(2,2);

        assertEquals(4,result);

    }
}
