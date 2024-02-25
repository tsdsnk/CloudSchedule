package org.cloudbus.cloudsim.examples.schedule;

import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.examples.DAG.DAGNode;

import java.util.List;
import java.util.Map;

public class PSODAGBroker extends AbstractDAGBroker{

    public PSODAGBroker(String name) throws Exception{
        super(name);
    }

    @Override
    protected Map<Vm, List<DAGNode>> scheduleTask() {

        
        return null;
    }

}
