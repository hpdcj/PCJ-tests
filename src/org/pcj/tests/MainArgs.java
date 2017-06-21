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

    private Class<? extends StartPoint> startPoint;
    private String[] nodes;

    private String[] readNodes(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        return br.lines()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
    }

    private void getPcjClasses(String param) {
        switch (param) {
            case "PingPong":
                startPoint = org.pcj.tests.micro.PingPong.class; 
                break;
            case "Barrier":
                startPoint = org.pcj.tests.micro.Barrier.class; 
                break;
            case "Broadcast":
                startPoint = org.pcj.tests.micro.Broadcast.class; 
                break;
            case "PiInt":
                startPoint = org.pcj.tests.app.pi.PiInt.class; 
                break;
            case "PiMC":
                startPoint = org.pcj.tests.app.pi.PiMC.class; 
                break;
            case "PiEstimator":
                startPoint = org.pcj.tests.app.pi.PiEstimator.class; 
                break;
            case "RayTracerA":
                startPoint = org.pcj.tests.app.raytracer.RayTracerA.class;
                break;
            case "RayTracerB":
                startPoint = org.pcj.tests.app.raytracer.RayTracerB.class;
                break;
            case "RayTracerC":
                startPoint = org.pcj.tests.app.raytracer.RayTracerC.class;
                break;
            case "RayTracerD":
                startPoint = org.pcj.tests.app.raytracer.RayTracerD.class;
                break;
            case "MolDynA":
                startPoint = org.pcj.tests.app.moldyn.MolDynA.class; 
                break;
            case "MolDynB":
                startPoint = org.pcj.tests.app.moldyn.MolDynB.class; 
                break;
            case "MolDynC":
                startPoint = org.pcj.tests.app.moldyn.MolDynC.class; 
                break;
            case "MolDynD":
                startPoint = org.pcj.tests.app.moldyn.MolDynD.class; 
                break;
            case "MolDynE":
                startPoint = org.pcj.tests.app.moldyn.MolDynE.class; 
                break;
            case "PingPongRev":
                startPoint = org.pcj.tests.micro.PingPongRev.class;
                break;
            case "BroadcastRev":
                startPoint = org.pcj.tests.micro.BroadcastRev.class;
                break;
            case "EasyTest":
            default:
                startPoint = org.pcj.tests.EasyTest.class;
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
     * @return the nodes
     */
    String[] getNodes() {
        return nodes;
    }
}
