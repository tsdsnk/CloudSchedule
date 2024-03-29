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
    protected Map<Vm, List<DAGNode>> scheduleTask() {

        List<GraphNode> available;

        while(true){
            available = getAvailableGraphNodeList();
            if(available.isEmpty()){
                break;
            }
            // 最小任务
            GraphNode minTask = available.get(0);
            long minTaskLength = minTask.getTask().getCloudletLength(), length;

            for(GraphNode node : available){
                if((length = node.getTask().getCloudletLength()) < minTaskLength){
                    minTaskLength = length;
                    minTask = node;
                }
            }

            // 最小时间
            Vm minVm = vmList.get(0);
            double minTime = minTask.estimateFinishTime(minVm), time;
            for(Vm vm : vmList){
                if((time = minTask.estimateFinishTime(vm)) < minTime){
                    minTime = time;
                    minVm = vm;
                }
            }
            minTask.bind(minVm.getId());
        }
        return getCurrentSchedule();

    }




}



