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
package org.pcj.tests;

import java.util.concurrent.atomic.AtomicInteger;
import org.pcj.Group;
import org.pcj.PCJ;
import org.pcj.StartPoint;
import org.pcj.Storage;
import org.pcj.tests.EasyTest.Shared;
import org.pcj.RegisterStorage;

/**
 *
 * @author faramir
 */
@RegisterStorage(Shared.class)
public class EasyTest implements StartPoint {

    private static final AtomicInteger minThreadIdOnNode = new AtomicInteger(Integer.MAX_VALUE);

    /*
     * Shared fields
     */
    @Storage(EasyTest.class)
    public enum Shared {
        A;
    }
    int A;

    @SuppressWarnings("method")
    @Override
    public void main() {
        {
            int curr;
            int min;
            do {
                curr = minThreadIdOnNode.get();
                min = Math.min(curr, PCJ.myId());
            } while (minThreadIdOnNode.compareAndSet(curr, min) == false);
        }
        System.out.println("EasyTest.main()");
        PCJ.barrier();
        System.out.println("myThread:   " + PCJ.myId());
        System.out.println("numThreads:   " + PCJ.threadCount());
        System.out.println("minThread:   " + minThreadIdOnNode.get());
        System.out.println("myNode:   " + PCJ.getNodeId());
        System.out.println("numNodes:   " + PCJ.getNodeCount());

        Group g = PCJ.splitGroup((PCJ.myId() % 2),0);
        PCJ.barrier();
        System.out.println("In group '" + g + "': " + g.threadCount() + " [groupId:" + g.myId() + "/globalId:" + PCJ.myId() + "]");
        if (PCJ.myId() == 0) {
            System.out.println("broadcasting...");
            PCJ.broadcast(0b010101, Shared.A);
            System.out.println("syncWith(1)");
            PCJ.barrier(1);
            System.out.println("synced(1)");
        }
        if (PCJ.myId() == 1) {
            System.out.println("syncWith(0)");
            PCJ.barrier(0);
            System.out.println("synced(0)");
        }
        for (int j = 0; j < 1_000; ++j) {
            //System.err.println(PCJ.myNode()+"> round "+j);
            if (PCJ.myId() == 0) {
                for (int i = 1; i < PCJ.threadCount(); ++i) {
                    //System.err.println("0: sync "+i);
                    PCJ.barrier(i);
                }
            } else {
                //System.err.println(PCJ.myNode()+": sync 0");
                PCJ.barrier(0);
            }
        }
        System.out.printf("[%d] sync ok\n", PCJ.myId());

        PCJ.barrier();
        if (PCJ.threadCount() >= 3 && PCJ.myId() <= 2) {
            for (int j = 0; j <= 333; ++j) {
                //System.err.println(PCJ.myNode()+"> round "+j);
                if (PCJ.myId() == 0) {
                    //for (int i = 1; i < PCJ.numNodes(); ++i) {
                    //System.err.println("["+j+"]"+"0: sync "+(j%2+1));
                    PCJ.barrier(j % 2 + 1);
                    PCJ.waitFor(Shared.A);
                    //System.err.println("["+j+"]"+PCJ.myNode()+": waitFor(A)");

                    //System.err.println("["+j+"]"+"0: synced "+(j%2+1));
                    //}
                } else if (PCJ.myId() == j % 2 + 1) {
                    //System.err.println("["+j+"]"+PCJ.myNode()+": sync 0");
                    PCJ.barrier(0);
                    PCJ.put(j, 0, Shared.A);
                    //System.err.println("["+j+"]"+PCJ.myNode()+": localPut(A)");

                    //System.err.println("["+j+"]"+PCJ.myNode()+": synced 0");
                }
            }
        }
        System.out.printf("[%d] wait ok\n", PCJ.myId());
        PCJ.barrier();

        System.out.printf("[%d] bef sync\n", PCJ.myId());
        PCJ.barrier();
        System.out.printf("[%d] A=%d\n", PCJ.myId(), A);

        if (PCJ.threadCount() >= 2 && PCJ.myId() == 0) {
            System.out.println("Put 0x10 by 0 to node 1 to variable 'A'");
            PCJ.put(0x10, 1, Shared.A);
        }

        PCJ.barrier();
        System.out.printf("[%d] A=%d\n", PCJ.myId(), A);

        if (PCJ.threadCount() >= 2 && PCJ.myId() == 0) {
            System.out.println("Get 'A' by 0 from 1");
            A = PCJ.<Integer>asyncGet(1, Shared.A).get();
        }

        PCJ.barrier();
        System.out.printf("[%d] A=%d\n", PCJ.myId(), A);
    }
}
