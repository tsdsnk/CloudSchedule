package org.cloudbus.cloudsim.examples.schedule;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.examples.DAG.DAGNode;

public class SimpleDAGBroker extends AbstractDAGBroker{
    public SimpleDAGBroker(String name) throws Exception {
        super(name);
    }

    @Override
    public void bindCloudletsToVms() {
        for(Cloudlet node : cloudletList){
            if(node.getCloudletId() == 1){
                node.setVmId(1);
                break;
            }
            node.setVmId(0);
        }
    }
}
