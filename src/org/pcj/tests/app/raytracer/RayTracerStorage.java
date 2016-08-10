/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pcj.tests.app.raytracer;

import org.pcj.Shared;

/**
 *
 * @author faramir
 */
public enum RayTracerStorage implements Shared {

    r_checksum(long[].class),
    r_p_row(int[][].class);
    private final Class<?> type;

    private RayTracerStorage(Class<?> type) {
        this.type = type;
    }

    @Override
    public Class<?> type() {
        return type;
    }
}
