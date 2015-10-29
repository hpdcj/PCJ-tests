package org.pcj.tests.app.moldyn;

/**
 * ************************************************************************
 *                                                                         *
 * Java Grande Forum Benchmark Suite - MPJ Version 1.0 * * produced by * * Java
 * Grande Benchmarking Project * * at * * Edinburgh Parallel Computing Centre *
 * * email: epcc-javagrande@epcc.ed.ac.uk * * * This version copyright (c) The
 * University of Edinburgh, 2001. * All rights reserved. * *
 * ************************************************************************
 */
import org.pcj.PCJ;
import org.pcj.StartPoint;
import org.pcj.tests.app.moldyn.MolDynBench;

public class MolDynD implements StartPoint {

    @Override
    public void main() throws Throwable {
        new MolDynBench().JGFrun(3);
    }
}