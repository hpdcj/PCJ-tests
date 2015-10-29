/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pcj.tests.app.moldyn;

import java.util.Locale;
import org.pcj.PCJ;

/**
 *
 * @author faramir
 */
public class MolDynBench extends MolDyn {

    public void JGFsetsize(int size) {
        this.size = size;
    }

    public void JGFinitialise() {
        if (PCJ.myId() == 0) {
            PCJ.putLocal("r_xforce", new double[PCJ.threadCount()][0]);
            PCJ.putLocal("r_yforce", new double[PCJ.threadCount()][0]);
            PCJ.putLocal("r_zforce", new double[PCJ.threadCount()][0]);
            PCJ.putLocal("r_epot", new double[PCJ.threadCount()]);
            PCJ.putLocal("r_vir", new double[PCJ.threadCount()]);
            PCJ.putLocal("r_interactions", new long[PCJ.threadCount()]);

            PCJ.monitor("r_xforce");
            PCJ.monitor("r_yforce");
            PCJ.monitor("r_zforce");
            PCJ.monitor("r_epot");
            PCJ.monitor("r_vir");
            PCJ.monitor("r_interactions");

            PCJ.monitor("tmp_xforce");
            PCJ.monitor("tmp_yforce");
            PCJ.monitor("tmp_zforce");
            PCJ.monitor("tmp_epot");
            PCJ.monitor("tmp_vir");
            PCJ.monitor("tmp_interactions");
        }

        initialise();
    }

    public void JGFapplication() {
        runiters(PCJ.myId(), PCJ.threadCount());
    }

    public void JGFvalidate() {
        double refval[] = {1731.4306625334357, 7397.392307839352,
            13774.625810229074, 46548.77475182352};
        double dev = Math.abs(ek - refval[size]);
        if (dev > 1.0e-8) {
            System.err.println("Validation failed");
            System.err.println("Kinetic Energy = " + ek + "  " + dev + "  " + size);
        }
    }

    public void JGFtidyup() {
        one = null;
        System.gc();
    }

    public void JGFrun(int size) {
        JGFsetsize(size);

        JGFinitialise();

        PCJ.barrier();
        long time = System.nanoTime();

        JGFapplication();
        PCJ.barrier();

        time = System.nanoTime() - time;
        double dtime = time * 1e-9;

        if (PCJ.myId() == 0) {
            JGFvalidate();

            System.out.format(Locale.FRANCE, "%5d\ttime %12.7f%n",
                    PCJ.threadCount(), dtime);
        }
        JGFtidyup();
    }
}
