package org.pcj.tests.micro;

import org.pcj.PCJ;
import org.pcj.Shared;

public class Barrier implements org.pcj.StartPoint {

    public enum EmptyStorage implements Shared {
        ;

        @Override
        public Class<?> type() {
            return null;
        }
    }

    @Override
    public void main() {
        int number_of_tests = 10;
        int ntimes = 10000;

        PCJ.barrier();

        double tmin = Double.MAX_VALUE;
        for (int k = 0; k < number_of_tests; k++) {
            long time = System.nanoTime();

            for (int i = 0; i < ntimes; i++) {
                PCJ.barrier();
            }

            time = System.nanoTime() - time;
            double dtime = (time / (double) ntimes) * 1e-9;

            if (tmin > dtime) {
                tmin = dtime;
            }

            PCJ.barrier();
        }

        if (PCJ.myId() == 0) {
            System.out.format("Barrier\t%5d\ttime %12.7f%n",
                    PCJ.threadCount(), tmin);
        }
    }
}
