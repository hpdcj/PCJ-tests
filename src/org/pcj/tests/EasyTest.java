/*
 * Test app
 */
package org.pcj.tests;

import java.util.concurrent.atomic.AtomicInteger;
import org.pcj.Group;
import org.pcj.PCJ;
import org.pcj.Shared;
import org.pcj.StartPoint;

/**
 *
 * @author faramir
 */
public class EasyTest implements StartPoint {

    private static final AtomicInteger minThreadIdOnNode = new AtomicInteger(Integer.MAX_VALUE);

    /*
     * Shared fields
     */
    public enum SharedEnum implements Shared {
        A(int.class);
        private final Class<?> type;

        private SharedEnum(Class<?> type) {
            this.type = type;
        }

        @Override
        public Class<?> type() {
            return type;
        }
    }

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

        Group g = PCJ.join("group" + (PCJ.myId() % 2));
        PCJ.barrier();
        System.out.println("In group '" + g.getGroupName() + "': " + g.threadCount() + " [groupId:" + g.myId() + "/globalId:" + PCJ.myId() + "]");
        if (PCJ.myId() == 0) {
            System.out.println("broadcasting...");
            PCJ.broadcast(SharedEnum.A, 0b010101);
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
                    PCJ.waitFor(SharedEnum.A);
                    //System.err.println("["+j+"]"+PCJ.myNode()+": waitFor(A)");

                    //System.err.println("["+j+"]"+"0: synced "+(j%2+1));
                    //}
                } else if (PCJ.myId() == j % 2 + 1) {
                    //System.err.println("["+j+"]"+PCJ.myNode()+": sync 0");
                    PCJ.barrier(0);
                    PCJ.put(0, SharedEnum.A, j);
                    //System.err.println("["+j+"]"+PCJ.myNode()+": putLocal(A)");

                    //System.err.println("["+j+"]"+PCJ.myNode()+": synced 0");
                }
            }
        }
        System.out.printf("[%d] wait ok\n", PCJ.myId());
        PCJ.barrier();

        System.out.printf("[%d] bef sync\n", PCJ.myId());
        PCJ.barrier();
        System.out.printf("[%d] A=%d\n", PCJ.myId(), PCJ.getLocal(SharedEnum.A));

        if (PCJ.threadCount() >= 2 && PCJ.myId() == 0) {
            System.out.println("Put 0x10 by 0 to node 1 to variable 'A'");
            PCJ.put(1, SharedEnum.A, 0x10);
        }

        PCJ.barrier();
        System.out.printf("[%d] A=%d\n", PCJ.myId(), PCJ.getLocal(SharedEnum.A));

        if (PCJ.threadCount() >= 2 && PCJ.myId() == 0) {
            System.out.println("Get 'A' by 0 from 1");
            PCJ.putLocal(SharedEnum.A, PCJ.asyncGet(1, SharedEnum.A).get());
        }

        PCJ.barrier();
        System.out.printf("[%d] A=%d\n", PCJ.myId(), PCJ.getLocal(SharedEnum.A));
//        PCJ.putLocal((PCJ.myNode() + 1) % PCJ.numNodes(), SharedEnum.A, PCJ.myNode());
//        PCJ.sync();
//        System.out.printf("[%d] A=%d\n", PCJ.myNode(), PCJ.getLocal(SharedEnum.A));
//        PCJ.sync();
//        if (PCJ.myNode() == 0) {
//            System.out.println("---");
//        }
//        PCJ.sync();
//        System.out.printf("[%d] A=%d\n", PCJ.myNode(), PCJ.getLocal(SharedEnum.A));
//        PCJ.putLocal(SharedEnum.A, A * A+1);
//        PCJ.sync();
//        if (PCJ.myNode() == 0) {
//            System.out.println("---");
//        }
//        PCJ.sync();
//        System.out.printf("[%d] A=%d\n", PCJ.myNode(), PCJ.getLocal((PCJ.myNode() + 1) % PCJ.numNodes(), SharedEnum.A));
//        PCJ.sync();
//        if (PCJ.myNode() == 0) {
//            System.out.println("---");
//            PCJ.broadcast(SharedEnum.A, 0x10);
//        }
//        PCJ.sync();
//        System.out.printf("[%d] A=%d\n", PCJ.myNode(), PCJ.getLocal(SharedEnum.A));
//        PCJ.sync();
//        if (PCJ.myNode() == 0) {
//            System.out.println("---");
//        }
//        PCJ.sync();
//        System.out.printf("[%d] A=%d\n", PCJ.myNode(), PCJ.getLocal(SharedEnum.A));
    }
}
