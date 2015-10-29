package org.pcj.tests.app;

import java.util.Locale;
import java.util.Random;
import org.pcj.FutureObject;
import org.pcj.PCJ;
import org.pcj.Shared;
import org.pcj.StartPoint;
import org.pcj.Storage;

public class PiMC extends Storage implements StartPoint {

    @Shared
    long circleCount;

    @Override
    public void main() {
        Random random = new Random();
        long nAll = 512_000_000;
        long n = nAll / PCJ.threadCount();

        circleCount = 0;
        long time = System.nanoTime();

        // Calculate  
        for (long i = 0; i < n; ++i) {
            double x = 2.0 * random.nextDouble() - 1.0;
            double y = 2.0 * random.nextDouble() - 1.0;
            if ((x * x + y * y) < 1.0) {
                circleCount++;
            }
        }
        PCJ.barrier();

        // Gather results 
        long c = 0;
        FutureObject cL[] = new FutureObject[PCJ.threadCount()];

        if (PCJ.myId() == 0) {
            for (int p = 0; p < PCJ.threadCount(); p++) {
                cL[p] = PCJ.getFutureObject(p, "circleCount");
            }
            for (int p = 0; p < PCJ.threadCount(); p++) {
                c = c + (long) cL[p].get();
            }
        }

        // Calculate pi 
        double pi = 4.0 * (double) c / (double) nAll;

        time = System.nanoTime() - time;
        double dtime = time * 1e-9;

        // Print results         
        if (PCJ.myId() == 0) {
            validate(pi);

            System.out.format(Locale.FRANCE, "%5d\ttime %12.7f%n",
                    PCJ.threadCount(), dtime);
        }
    }

    private void validate(double pi) {
        double refval = Math.PI;
        double dev = Math.abs(pi - refval);

        if (dev > 1.0e-4) {
            System.err.println("Validation failed");
            System.err.println("Value = " + pi + "  " + dev);
        }
    }
}
