/**************************************************************************
 *                                                                         *
 *         Java Grande Forum Benchmark Suite - MPJ Version 1.0             *
 *                                                                         *
 *                            produced by                                  *
 *                                                                         *
 *                  Java Grande Benchmarking Project                       *
 *                                                                         *
 *                                at                                       *
 *                                                                         *
 *                Edinburgh Parallel Computing Centre                      *
 *                                                                         *
 *                email: epcc-javagrande@epcc.ed.ac.uk                     *
 *                                                                         *
 *                                                                         *
 *      This version copyright (c) The University of Edinburgh, 2001.      *
 *                         All rights reserved.                            *
 *                                                                         *
 **************************************************************************/
package jgfutil;

import java.util.*;
import org.pcj.PCJ;

public class JGFInstrumentor {

    private static HashMap<String, JGFTimer> timers;
    private static HashMap<String, Object> data;

    static {
        timers = new HashMap<>();
        data = new HashMap<>();
    }

    public static synchronized void addTimer(String name) {

        if (timers.containsKey(name)) {
            PCJ.log("JGFInstrumentor.addTimer: warning -  timer " + name
                    + " already exists");
        } else {
            timers.put(name, new JGFTimer(name));
        }
    }

    public static synchronized void addTimer(String name, String opname) {

        if (timers.containsKey(name)) {
            PCJ.log("JGFInstrumentor.addTimer: warning -  timer " + name
                    + " already exists");
        } else {
            timers.put(name, new JGFTimer(name, opname));
        }

    }

    public static synchronized void addTimer(String name, String opname, int size) {

        if (timers.containsKey(name)) {
            PCJ.log("JGFInstrumentor.addTimer: warning -  timer " + name
                    + " already exists");
        } else {
            timers.put(name, new JGFTimer(name, opname, size));
        }

    }

    public static synchronized void startTimer(String name) {
        if (timers.containsKey(name)) {
            ((JGFTimer) timers.get(name)).start();
        } else {
            PCJ.log("JGFInstrumentor.startTimer: failed -  timer " + name
                    + " does not exist");
        }

    }

    public static synchronized void stopTimer(String name) {
        if (timers.containsKey(name)) {
            ((JGFTimer) timers.get(name)).stop();
        } else {
            PCJ.log("JGFInstrumentor.stopTimer: failed -  timer " + name
                    + " does not exist");
        }
    }

    public static synchronized void addOpsToTimer(String name, double count) {
        if (timers.containsKey(name)) {
            ((JGFTimer) timers.get(name)).addops(count);
        } else {
            PCJ.log("JGFInstrumentor.addOpsToTimer: failed -  timer " + name
                    + " does not exist");
        }
    }

    public static synchronized void addTimeToTimer(String name, double added_time) {
        if (timers.containsKey(name)) {
            ((JGFTimer) timers.get(name)).addtime(added_time);
        } else {
            PCJ.log("JGFInstrumentor.addTimeToTimer: failed -  timer " + name
                    + " does not exist");
        }



    }

    public static synchronized double readTimer(String name) {
        double time;
        if (timers.containsKey(name)) {
            time = ((JGFTimer) timers.get(name)).time;
        } else {
            PCJ.log("JGFInstrumentor.readTimer: failed -  timer " + name
                    + " does not exist");
            time = 0.0;
        }
        return time;
    }

    public static synchronized void resetTimer(String name) {
        if (timers.containsKey(name)) {
            ((JGFTimer) timers.get(name)).reset();
        } else {
            PCJ.log("JGFInstrumentor.resetTimer: failed -  timer " + name
                    + " does not exist");
        }
    }

    public static synchronized void printTimer(String name) {
        if (timers.containsKey(name)) {
            ((JGFTimer) timers.get(name)).print();
        } else {
            PCJ.log("JGFInstrumentor.printTimer: failed -  timer " + name
                    + " does not exist");
        }
    }

    public static synchronized void printperfTimer(String name) {
        if (timers.containsKey(name)) {
            ((JGFTimer) timers.get(name)).printperf();
        } else {
            PCJ.log("JGFInstrumentor.printTimer: failed -  timer " + name
                    + " does not exist");
        }
    }

    public static synchronized void printperfTimer(String name, int arr_size) {
        if (timers.containsKey(name)) {
            ((JGFTimer) timers.get(name)).printperf(arr_size);
        } else {
            PCJ.log("JGFInstrumentor.printTimer: failed -  timer " + name
                    + " does not exist");
        }
    }

    public static synchronized void storeData(String name, Object obj) {
        data.put(name, obj);
    }

    public static synchronized void retrieveData(String name, Object obj) {
        obj = data.get(name);
    }

    public static synchronized void printHeader(int section, int size, int nprocess) {

        String header, base;

        header = "";
        base = "Java Grande Forum MPJ Benchmark Suite - Version 1.0 - Section ";

        switch (section) {
            case 1:
                header = base + "1";
                break;
            case 2:
                switch (size) {
                    case 0:
                        header = base + "2 - Size A";
                        break;
                    case 1:
                        header = base + "2 - Size B";
                        break;
                    case 2:
                        header = base + "2 - Size C";
                        break;
                }
                break;
            case 3:
                switch (size) {
                    case 0:
                        header = base + "3 - Size A";
                        break;
                    case 1:
                        header = base + "3 - Size B";
                        break;
                }
                break;
        }

        PCJ.log(header);

        if (nprocess == 1) {
            PCJ.log("Executing on " + nprocess + " process");
        } else {
            PCJ.log("Executing on " + nprocess + " processes");
        }

        PCJ.log("");

    }
}
