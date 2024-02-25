package org.cloudbus.cloudsim.examples.schedule;

import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.examples.DAG.DAGNode;

import java.util.*;

public class HEFTDAGBroker extends AbstractDAGBroker{

    public HEFTDAGBroker(String name) throws Exception{
        super(name);
    }





    @Override
    protected Map<Vm, List<DAGNode>> scheduleTask() {
        processUpward();
        List<GraphNode> available;
        while(!(available = getAvailableGraphNodeList()).isEmpty()){
            // 找到最大upwardRank
            GraphNode maxnode = available.get(0);
            double maxrank = ((estimateInfo) maxnode.getMarks()).upwardRank;
            for(GraphNode node : available){
                double rank = ((estimateInfo) node.getMarks()).upwardRank;
                if(rank > maxrank){
                    maxnode = node;
                    maxrank = rank;
                }
            }

            // 绑定最快完成的机器
            Vm minVm = vmList.get(0);
            double minTime = maxnode.estimateFinishTime(minVm);
            for(Vm vm : vmList){
                double time = maxnode.estimateFinishTime(vm);
                if(time < minTime){
                    minTime = time;
                    minVm = vm;
                }
            }
            maxnode.bind(minVm.getId());

        }
        return getCurrentSchedule();

    }


    private void processUpward(){
        List<GraphNode> available = getFinishNode();
        while(!available.isEmpty()){
            List<GraphNode> newList = new LinkedList<>();
            for(GraphNode node : available){
                calUpwardRank(node);
                for(GraphNode father : node.getFathers()){
                    if(!newList.contains(father) && isReachableWhenUpward(father)){
                        newList.add(father);
                    }
                }

            }
            available = newList;
        }
    }

    private boolean isReachableWhenUpward(GraphNode node){
        boolean flag = true;
        for(GraphNode child : node.getChilds()){
            flag &= (child.getMarks() != null);
        }
        return flag;
    }



    private double calAvgExecTime(GraphNode node){
        double time = 0;
        for(Vm vm : vmList){
            time += node.estimateExecTime(vm);
        }
        return time / vmList.size();
    }
    private double calAvgUploadTime(GraphNode node){
        double time = 0;
        for(Vm vm : vmList){
            time += node.getUploadTime(vm);
        }
        return time / vmList.size();
    }

    private double calAvgDownloadTime(GraphNode node){
        double time = 0;
        for(Vm vm : vmList){
            time += node.getDownloadTime(vm);
        }
        return time / vmList.size();
    }

    private void calUpwardRank(GraphNode node){
        estimateInfo info;
        double maxRank = 0;
        for(GraphNode child : node.getChilds()){
            info = (estimateInfo) child.getMarks();
            if(maxRank < info.upwardRank + info.downloadTime){
                maxRank = info.upwardRank + info.downloadTime;
            }
        }
        double downloadTime = calAvgDownloadTime(node);
        double uploadTime = calAvgUploadTime(node);
        double execTime = calAvgExecTime(node);
        maxRank = maxRank + uploadTime + execTime;
        node.mark(new estimateInfo(maxRank, execTime, uploadTime, downloadTime));
    }



    class estimateInfo{
        private double upwardRank;
        private double execTime;
        private double uploadTime;
        private double downloadTime;


        private estimateInfo(double upwardRank, double execTime, double uploadTime, double downloadTime){
            this.upwardRank = upwardRank;
            this.execTime = execTime;
            this.uploadTime = uploadTime;
            this.downloadTime = downloadTime;
        }
    }

}
