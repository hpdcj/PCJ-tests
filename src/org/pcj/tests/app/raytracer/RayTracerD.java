/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pcj.tests.app.raytracer;

import org.pcj.StartPoint;
import org.pcj.tests.app.raytracer.RayTracerBench;

/**
 *
 * @author faramir
 */
public class RayTracerD implements StartPoint {

    @Override
    public void main() throws Throwable {
        new RayTracerBench().run(3);
    }
}
