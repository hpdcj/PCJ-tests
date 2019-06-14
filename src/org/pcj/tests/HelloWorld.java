/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pcj.tests;

import org.pcj.PCJ;
import org.pcj.StartPoint;

/**
 *
 * @author faramir
 */
public class HelloWorld implements StartPoint {

    @Override
    public void main() throws Throwable {
        for (int i = 0; i < PCJ.threadCount(); ++i) {
            if (PCJ.myId() == i) {
                System.out.println("Hello World from PCJ Thread " + PCJ.myId());
            }
            PCJ.barrier();
        }
    }
}
