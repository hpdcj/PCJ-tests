/* 
 * Copyright (c) 2016, faramir
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.pcj.tests.micro;

/*
 * @author Piotr
 */
import org.pcj.PCJ;
import org.pcj.Shared;
import org.pcj.StartPoint;

public class Broadcast implements StartPoint {

    public enum SharedEnum implements Shared {
        a(double[].class);
        private final Class<?> type;

        private SharedEnum(Class<?> type) {
            this.type = type;
        }

        @Override
        public Class<?> type() {
            return type;
        }
    }

    @Override
    public void main() {
        int[] transmit
                = {
                    1, 10, 100, 1024, 2048, 4096, 8192, 16384,
                    32768, 65536, 131072, 262144, 524288, 1048576, 2097152,
                    4194304, //8388608, //16777216,
                };

        for (int n : transmit) {
            PCJ.barrier();

            double[] b = new double[n];
            for (int i = 0; i < n; i++) {
                b[i] = (double) i + 1;
            }
            PCJ.monitor(SharedEnum.a);

            PCJ.barrier();

            int ntimes = 100;

            long time = System.nanoTime();

            for (int i = 0; i < ntimes; i++) {
                if (PCJ.myId() == 0) {
                    PCJ.broadcast(SharedEnum.a, b);
                }
            }

            time = System.nanoTime() - time;
            double dtime = (time / (double) ntimes) * 1e-9;
            PCJ.barrier();

            if (PCJ.myId() == 0) {
                System.out.format("Broadcast\t%5d\tsize %12.7f\ttime %12.7f%n",
                        PCJ.threadCount(), (double) n * 8 / 1024, dtime);
            }
        }
    }
}
