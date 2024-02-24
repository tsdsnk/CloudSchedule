package org.cloudbus.cloudsim.examples.schedule;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.examples.DAG.DAGNode;

import java.util.List;

public class SimpleDAGBroker extends AbstractDAGBroker{
    public SimpleDAGBroker(String name) throws Exception {
        super(name);
    }

    @Override
    protected void bindCloudletsToVms() {
        for(DAGNode node : (List<DAGNode>) cloudletList){
            node.setVmId(0);
        }
    }
}
