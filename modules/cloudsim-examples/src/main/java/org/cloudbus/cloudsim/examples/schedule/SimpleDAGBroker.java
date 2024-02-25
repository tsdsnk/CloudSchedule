package org.cloudbus.cloudsim.examples.schedule;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.examples.DAG.DAGNode;
import org.cloudbus.cloudsim.lists.VmList;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SimpleDAGBroker extends AbstractDAGBroker{




    public SimpleDAGBroker(String name) throws Exception {
        super(name);
    }

    @Override
    protected Map<Vm, List<DAGNode>> scheduleTask() {
        int index = 0;


        List<GraphNode> available;
        while(true){
            available = getAvailableGraphNodeList();
            if(available.isEmpty()){
                break;
            }
            for(GraphNode node : available){
                node.bind(vmList.get(index).getId());
                index = (index + 1) % vmList.size();
            }
        }

        return getCurrentSchedule();
    }



}
