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

import org.pcj.NodesDescription;
import org.pcj.PCJ;

/**
 *
 * @author faramir
 */
public class MainWithDeploy {

    public static void main(String[] args) throws Throwable {
//            args = new String[]{"EasyTest"};
//            args = new String[]{"PiInt"};
//            args = new String[]{"PiMC"};
//            args = new String[]{"Barrier"};
//            args = new String[]{"Broadcast"};
//            args = new String[]{"PingPong"};
//            args = new String[]{"RayTracerA"};
//            args = new String[]{"RayTracerB"};
//            args = new String[]{"RayTracerC"};
//            args = new String[]{"RayTracerD"};
//            args = new String[]{"MolDynA"};
//            args = new String[]{"MolDynB"};
//            args = new String[]{"MolDynC"};
//            args = new String[]{"MolDynD"};
//            args = new String[]{"MolDynE"};

        if (args.length == 0) {
            System.out.println("<start point> [nodes file] [num nodes]");
            System.exit(1);
        }

        MainArgs mainArgs = new MainArgs(args);

        if (mainArgs.getStartPoint() == null || mainArgs.getStorage() == null) {
            System.err.println("Unknown task: " + args[0]);
            System.exit(2);
        }

        PCJ.deploy(mainArgs.getStartPoint(),
                new NodesDescription(mainArgs.getNodes()),
                mainArgs.getStorage());
    }
}
