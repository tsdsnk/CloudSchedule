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
    protected Map<Vm, List<DAGNode>> bindCloudletsToVms() {
        HashMap<Vm, List<DAGNode>> schedule = new HashMap<>();
        for(int i=0; i<vmList.size(); i++){
            schedule.put(vmList.get(i), new LinkedList<>());
        }

        List<GraphNode> available;
        while(true){
            available = new LinkedList<>(getAvailableGraphNodeList());
            if(available.isEmpty()){
                break;
            }
            for(GraphNode node : available){
                bindGraghNode(node, 0);
                schedule.get(VmList.getById(vmList, 0)).add(node.getTask());
            }
        }

        return schedule;
    }



}
