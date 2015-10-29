/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pcj.tests.app.raytracer;

import org.pcj.Shared;
import org.pcj.Storage;

/**
 *
 * @author faramir
 */
public class RayTracerStorage extends Storage {

    @Shared
    private long[] r_checksum;
    @Shared
    private int[][] r_p_row;
}
