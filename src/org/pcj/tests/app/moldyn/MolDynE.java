package org.pcj.tests.app.moldyn;

import org.pcj.StartPoint;

public class MolDynE implements StartPoint {

    @Override
    public void main() throws Throwable {
        new MolDynBench().JGFrun(4);
    }
}