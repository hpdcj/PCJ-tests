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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.pcj.StartPoint;

/**
 *
 * @author faramir
 */
public class MainArgs {

    private final Class<? extends StartPoint> startPoint;
    private final String[] nodes;

    private String[] readNodes(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        return br.lines()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
    }

    private Class<? extends StartPoint> getPcjClasses(String param) {
        switch (param) {
            case "HelloWorld":
                return org.pcj.tests.HelloWorld.class;
            case "PingPong":
                return org.pcj.tests.micro.PingPong.class;
            case "Barrier":
                return org.pcj.tests.micro.Barrier.class;
            case "Broadcast":
                return org.pcj.tests.micro.Broadcast.class;
            case "PiInt":
                return org.pcj.tests.app.pi.PiInt.class;
            case "PiMC":
                return org.pcj.tests.app.pi.PiMC.class;
            case "PiEstimator":
                return org.pcj.tests.app.pi.PiEstimator.class;
            case "RayTracerA":
                return org.pcj.tests.app.raytracer.RayTracerA.class;
            case "RayTracerB":
                return org.pcj.tests.app.raytracer.RayTracerB.class;
            case "RayTracerC":
                return org.pcj.tests.app.raytracer.RayTracerC.class;
            case "RayTracerD":
                return org.pcj.tests.app.raytracer.RayTracerD.class;
            case "MolDynA":
                return org.pcj.tests.app.moldyn.MolDynA.class;
            case "MolDynB":
                return org.pcj.tests.app.moldyn.MolDynB.class;
            case "MolDynC":
                return org.pcj.tests.app.moldyn.MolDynC.class;
            case "MolDynD":
                return org.pcj.tests.app.moldyn.MolDynD.class;
            case "MolDynE":
                return org.pcj.tests.app.moldyn.MolDynE.class;
            case "PingPongRev":
                return org.pcj.tests.micro.PingPongRev.class;
            case "BroadcastRev":
                return org.pcj.tests.micro.BroadcastRev.class;
            case "EasyTest":
                return org.pcj.tests.EasyTest.class;
            default:
                return org.pcj.tests.HelloWorld.class;
        }
    }

    public MainArgs(String[] args) throws FileNotFoundException, IOException {
        String[] _nodes = null;
        if (args.length >= 2 && args[1].isEmpty() == false) {
            try (FileInputStream fis = new FileInputStream(args[1])) {
                _nodes = readNodes(fis);
            } catch (IOException ex) {
                System.err.println("Unable to load nodes file: " + args[1]);
            }
        }

        if (_nodes == null) {
            try (InputStream is = MainArgs.class.getResourceAsStream("nodes.txt")) {
                _nodes = readNodes(is);
            }
        }

        if (args.length >= 3) {
            int NPROC = Integer.parseInt(args[2]);
            nodes = new String[NPROC];
            System.arraycopy(_nodes, 0, nodes, 0, NPROC);
        } else {
            nodes = _nodes;
        }

        startPoint = getPcjClasses(args[0]);
    }

    /**
     * @return the startPoint
     */
    Class<? extends StartPoint> getStartPoint() {
        return startPoint;
    }

    /**
     * @return the nodes
     */
    String[] getNodes() {
        return nodes;
    }
}
