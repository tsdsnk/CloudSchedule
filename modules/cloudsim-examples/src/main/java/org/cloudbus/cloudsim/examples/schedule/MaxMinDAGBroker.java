package org.cloudbus.cloudsim.examples.schedule;

import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.examples.DAG.DAGNode;

import java.util.List;
import java.util.Map;

public class MaxMinDAGBroker extends AbstractDAGBroker{

    public MaxMinDAGBroker (String name)throws Exception{
        super(name);
    }

    @Override
    protected Map<Vm, List<DAGNode>> scheduleTask() {

        List<GraphNode> available;

        while(true){
            available = getAvailableGraphNodeList();
            if(available.isEmpty()){
                break;
            }
            // 最大任务
            GraphNode maxTask = available.get(0);
            long maxTaskLength = maxTask.getTask().getCloudletLength(), length;

            for(GraphNode node : available){
                if((length = node.getTask().getCloudletLength()) > maxTaskLength){
                    maxTaskLength = length;
                    maxTask = node;
                }
            }

            // 最小时间
            Vm minVm = vmList.get(0);
            double minTime = maxTask.estimateFinishTime(minVm), time;
            for(Vm vm : vmList){
                if((time = maxTask.estimateFinishTime(vm)) < minTime){
                    minTime = time;
                    minVm = vm;
                }
            }
            maxTask.bind(minVm.getId());
        }
        return getCurrentSchedule();

    }
}
