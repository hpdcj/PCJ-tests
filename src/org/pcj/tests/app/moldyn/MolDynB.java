package org.pcj.tests.app.moldyn;

import org.pcj.StartPoint;

public class MolDynB implements StartPoint {

    @Override
    public void main() throws Throwable {
        new MolDynBench().JGFrun(1);
    }
}