package org.pcj.tests.app;

import java.util.Locale;
import org.pcj.FutureObject;
import org.pcj.PCJ;
import org.pcj.Shared;
import org.pcj.StartPoint;
import org.pcj.Storage;

/**
 *
 * @author faramir
 */
public class PiInt extends Storage implements StartPoint {

    @Shared
    private double sum;

    @SuppressWarnings("method")
    private double f(double x) {
        return (4.0 / (1.0 + x * x));
    }

    @SuppressWarnings("method")
    @Override
    public void main() throws Throwable {
        int ntimes = 1_000;
        int points = 1_000_000;

        double pi = 0.0;

        long time = System.currentTimeMillis();

        for (int i = 1; i < ntimes; ++i) {
            PCJ.barrier();

            pi = calc(points);
        }

        time = System.currentTimeMillis() - time;
        double dtime = time * 1e-9;

        if (PCJ.myId() == 0) {
            validate(pi);

            System.out.format(Locale.FRANCE, "%5d\ttime %12.7f%n",
                    PCJ.threadCount(), dtime);
        }
    }

    @SuppressWarnings("method")
    private double calc(int N) {
        double w;

        w = 1.0 / (double) N;
        sum = 0.0;
        for (int i = PCJ.myId() + 1; i <= N; i += PCJ.threadCount()) {
            sum = sum + f(((double) i - 0.5) * w);
        }
        sum = sum * w;

        PCJ.barrier();
        if (PCJ.myId() == 0) {
            FutureObject[] data = new FutureObject[PCJ.threadCount()];
            for (int i = 1; i < PCJ.threadCount(); ++i) {
                data[i] = PCJ.getFutureObject(i, "sum");
            }
            for (int i = 1; i < PCJ.threadCount(); ++i) {
                sum = sum + (double) data[i].getObject();
            }

            return sum;
        } else {
            return Double.NaN;
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
