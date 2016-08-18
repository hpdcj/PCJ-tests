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

import org.pcj.PCJ;

public class Barrier implements org.pcj.StartPoint {

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
