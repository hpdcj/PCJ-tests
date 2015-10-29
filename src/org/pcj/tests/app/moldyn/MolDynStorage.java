/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pcj.tests.app.moldyn;

import org.pcj.Shared;
import org.pcj.Storage;

/**
 *
 * @author faramir
 */
public class MolDynStorage extends Storage {

    @Shared
    private double[] tmp_xforce;
    @Shared
    private double[] tmp_yforce;
    @Shared
    private double[] tmp_zforce;
    @Shared
    private double tmp_epot;
    @Shared
    private double tmp_vir;
    @Shared
    private long tmp_interactions;
    /*reduce*/
    @Shared
    private double[][] r_xforce;
    @Shared
    private double[][] r_yforce;
    @Shared
    private double[][] r_zforce;
    @Shared
    private double[] r_epot;
    @Shared
    private double[] r_vir;
    @Shared
    private long[] r_interactions;
}
