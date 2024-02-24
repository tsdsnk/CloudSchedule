package org.cloudbus.cloudsim.examples.schedule;

import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.examples.DAG.DAGNode;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MinMinDAGBroker extends AbstractDAGBroker{



    public MinMinDAGBroker(String name) throws Exception{
        super(name);
    }

    @Override
    protected Map<Vm, List<DAGNode>> bindCloudletsToVms() {

        return new HashMap<>();

    }








}



