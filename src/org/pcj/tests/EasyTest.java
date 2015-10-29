/*
 * Test app
 */
package org.pcj.tests;

import org.pcj.Group;
import org.pcj.PCJ;
import org.pcj.Shared;
import org.pcj.StartPoint;
import org.pcj.Storage;

/**
 *
 * @author faramir
 */
public class EasyTest extends Storage implements StartPoint {

    /*
     * Shared fields
     */
    @Shared
    private int A;

    @SuppressWarnings("method")
    @Override
    public void main() {
        PCJ.log("EasyTest.main()");
        PCJ.barrier();
        PCJ.log("myNode:   " + PCJ.myId());
        PCJ.log("numNodes:   " + PCJ.threadCount());

        Group g = PCJ.join("group" + (PCJ.myId() % 2));
        PCJ.barrier();
        PCJ.log("In group '" + g.getGroupName() + "': " + g.threadCount() + " [groupId:" + g.myId() + "/globalId:" + PCJ.myId() + "]");
        if (PCJ.myId() == 0) {
            PCJ.log("broadcasting...");
            PCJ.broadcast("A", 0b010101);
            PCJ.log("syncWith(1)");
            PCJ.barrier(1);
            PCJ.log("synced(1)");
        }
        if (PCJ.myId() == 1) {
            PCJ.log("syncWith(0)");
            PCJ.barrier(0);
            PCJ.log("synced(0)");
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
                    PCJ.waitFor("A");
                    //System.err.println("["+j+"]"+PCJ.myNode()+": waitFor(A)");

                    //System.err.println("["+j+"]"+"0: synced "+(j%2+1));
                    //}
                } else {
                    if (PCJ.myId() == j % 2 + 1) {
                        //System.err.println("["+j+"]"+PCJ.myNode()+": sync 0");
                        PCJ.barrier(0);
                        PCJ.put(0, "A", j);
                        //System.err.println("["+j+"]"+PCJ.myNode()+": putLocal(A)");

                        //System.err.println("["+j+"]"+PCJ.myNode()+": synced 0");
                    }
                }
            }
        }
        System.out.printf("[%d] wait ok\n", PCJ.myId());
        PCJ.barrier();

        System.out.printf("[%d] bef sync\n", PCJ.myId());
        PCJ.barrier();
        System.out.printf("[%d] A=%d\n", PCJ.myId(), PCJ.getLocal("A"));

        if (PCJ.threadCount() >= 2 && PCJ.myId() == 0) {
            PCJ.log("Put 0x10 by 0 to node 1 to variable 'A'");
            PCJ.put(1, "A", 0x10);
        }

        PCJ.barrier();
        System.out.printf("[%d] A=%d\n", PCJ.myId(), PCJ.getLocal("A"));

        if (PCJ.threadCount() >= 2 && PCJ.myId() == 0) {
            PCJ.log("Get 'A' by 0 from 1");
            A = PCJ.getFutureObject(1, "A").getObject();
        }

        PCJ.barrier();
        System.out.printf("[%d] A=%d\n", PCJ.myId(), PCJ.getLocal("A"));
//        PCJ.putLocal((PCJ.myNode() + 1) % PCJ.numNodes(), "A", PCJ.myNode());
//        PCJ.sync();
//        System.out.printf("[%d] A=%d\n", PCJ.myNode(), PCJ.getLocal("A"));
//        PCJ.sync();
//        if (PCJ.myNode() == 0) {
//            System.out.println("---");
//        }
//        PCJ.sync();
//        System.out.printf("[%d] A=%d\n", PCJ.myNode(), PCJ.getLocal("A"));
//        PCJ.putLocal("A", A * A+1);
//        PCJ.sync();
//        if (PCJ.myNode() == 0) {
//            System.out.println("---");
//        }
//        PCJ.sync();
//        System.out.printf("[%d] A=%d\n", PCJ.myNode(), PCJ.getLocal((PCJ.myNode() + 1) % PCJ.numNodes(), "A"));
//        PCJ.sync();
//        if (PCJ.myNode() == 0) {
//            System.out.println("---");
//            PCJ.broadcast("A", 0x10);
//        }
//        PCJ.sync();
//        System.out.printf("[%d] A=%d\n", PCJ.myNode(), PCJ.getLocal("A"));
//        PCJ.sync();
//        if (PCJ.myNode() == 0) {
//            System.out.println("---");
//        }
//        PCJ.sync();
//        System.out.printf("[%d] A=%d\n", PCJ.myNode(), PCJ.getLocal("A"));
    }
}
