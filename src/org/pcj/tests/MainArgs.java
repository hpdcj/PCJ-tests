/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pcj.tests;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.pcj.StartPoint;
import org.pcj.Storage;

/**
 *
 * @author faramir
 */
public class MainArgs {

    private Class<? extends StartPoint> startPoint;
    private Class<? extends Storage> storage;
    private String[] nodes;

    private String[] readNodes(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        List<String> _nodes = new ArrayList<>();
        String node;
        while ((node = br.readLine()) != null) {
            if (node.trim().isEmpty() == false) {
                _nodes.add(node);
            }
        }
        return _nodes.toArray(new String[0]);
    }

    private void getPcjClasses(String param) {
        switch (param) {
            case "PingPong":
                startPoint = org.pcj.tests.micro.PingPong.class; // StartPoint
                storage = org.pcj.tests.micro.PingPong.class; // Storage
                break;
            case "Barrier":
                startPoint = org.pcj.tests.micro.Barrier.class; // StartPoint
                storage = org.pcj.tests.micro.Barrier.class; // Storage
                break;
            case "Broadcast":
                startPoint = org.pcj.tests.micro.Broadcast.class; // StartPoint
                storage = org.pcj.tests.micro.Broadcast.class; // Storage
                break;
            case "PiInt":
                startPoint = org.pcj.tests.app.PiInt.class; // StartPoint
                storage = org.pcj.tests.app.PiInt.class; // Storage
                break;
            case "PiMC":
                startPoint = org.pcj.tests.app.PiMC.class; // StartPoint
                storage = org.pcj.tests.app.PiMC.class; // Storage
                break;
            case "RayTracerA":
                startPoint = org.pcj.tests.app.raytracer.RayTracerA.class;
                storage = org.pcj.tests.app.raytracer.RayTracerStorage.class;
                break;
            case "RayTracerB":
                startPoint = org.pcj.tests.app.raytracer.RayTracerB.class;
                storage = org.pcj.tests.app.raytracer.RayTracerStorage.class;
                break;
            case "RayTracerC":
                startPoint = org.pcj.tests.app.raytracer.RayTracerC.class;
                storage = org.pcj.tests.app.raytracer.RayTracerStorage.class;
                break;
            case "RayTracerD":
                startPoint = org.pcj.tests.app.raytracer.RayTracerD.class;
                storage = org.pcj.tests.app.raytracer.RayTracerStorage.class;
                break;
            case "MolDynA":
                startPoint = org.pcj.tests.app.moldyn.MolDynA.class; // StartPoint
                storage = org.pcj.tests.app.moldyn.MolDynStorage.class; // Storage
                break;
            case "MolDynB":
                startPoint = org.pcj.tests.app.moldyn.MolDynB.class; // StartPoint
                storage = org.pcj.tests.app.moldyn.MolDynStorage.class; // Storage
                break;
            case "MolDynC":
                startPoint = org.pcj.tests.app.moldyn.MolDynC.class; // StartPoint
                storage = org.pcj.tests.app.moldyn.MolDynStorage.class; // Storage
                break;
            case "MolDynD":
                startPoint = org.pcj.tests.app.moldyn.MolDynD.class; // StartPoint
                storage = org.pcj.tests.app.moldyn.MolDynStorage.class; // Storage
                break;
            case "EasyTest":
            default:
                startPoint = org.pcj.tests.EasyTest.class;
                storage = org.pcj.tests.EasyTest.class;
                break;
        }
    }

    public MainArgs(String[] args) throws FileNotFoundException, IOException {
        if (args.length >= 2 && args[1].isEmpty() == false) {
            try (FileInputStream fis = new FileInputStream(args[1])) {
                nodes = readNodes(fis);
            }
        } else {
            try (InputStream is = MainArgs.class.getResourceAsStream("nodes.txt")) {
                nodes = readNodes(is);
            }
        }

        if (args.length >= 3) {
            int NPROC = Integer.parseInt(args[2]);
            String[] _nodes = new String[NPROC];
            System.arraycopy(nodes, 0, _nodes, 0, NPROC);
            nodes = _nodes;
        }

        getPcjClasses(args[0]);
    }

    /**
     * @return the startPoint
     */
    Class<? extends StartPoint> getStartPoint() {
        return startPoint;
    }

    /**
     * @return the storage
     */
    Class<? extends Storage> getStorage() {
        return storage;
    }

    /**
     * @return the nodes
     */
    String[] getNodes() {
        return nodes;
    }
}
