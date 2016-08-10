/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pcj.tests.app.moldyn;

import org.pcj.Shared;

/**
 *
 * @author faramir
 */
public enum MolDynStorage implements Shared {

    tmp_xforce(double[].class),
    tmp_yforce(double[].class),
    tmp_zforce(double[].class),
    tmp_epot(double.class),
    tmp_vir(double.class),
    tmp_interactions(long.class),
    /*reduce*/
    r_xforce(double[][].class),
    r_yforce(double[][].class),
    r_zforce(double[][].class),
    r_epot(double[].class),
    r_vir(double[].class),
    r_interactions(long[].class);
    private final Class<?> type;

    private MolDynStorage(Class<?> type) {
        this.type = type;
    }

    @Override
    public Class<?> type() {
        return type;
    }
}
