package org.pcj.tests.app.moldyn;

import org.pcj.StartPoint;

public class MolDynA implements StartPoint {

    @Override
    public void main() throws Throwable {
        new MolDynBench().JGFrun(0);
    }
}
