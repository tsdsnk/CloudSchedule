package org.cloudbus.cloudsim.examples.schedule;

import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.examples.DAG.DAGNode;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CPOPDAGBroker extends AbstractDAGBroker{

    public CPOPDAGBroker(String name) throws Exception{
        super(name);
    }

    @Override
    protected Map<Vm, List<DAGNode>> scheduleTask(){
        for(GraphNode node : getAllNode()){
            calAvgInfo(node);
        }
        processUpward();
        processDownward();

        // 选择关键路径
        List<GraphNode> cp = new LinkedList<>();
        GraphNode node = getMaxPriority(getStartNode());
        cp.add(node);
        while(!node.getTask().isEnd()){
            node = getMaxPriority(node.getChilds());
            cp.add(node);
        }

        // 寻找处理能力最强的Vm
        double maxmips = vmList.get(0).getMips();
        Vm maxvm = vmList.get(0);
        for(Vm vm : vmList){
            double mips = vm.getMips();
            if(mips > maxmips){
                maxmips = mips;
                maxvm = vm;
            }
        }

        List<GraphNode> available;
        while (!(available = getAvailableGraphNodeList()).isEmpty()){
            node = getMaxPriority(available);
            // 使关键路径上节点exec时间最小
            if(cp.contains(node)){
                node.bind(maxvm.getId());
                continue;
            }

            // 其他节点选择综合时间最小
            Vm minvm = vmList.get(0);
            double minFinishTime = node.estimateFinishTime(minvm);
            for(Vm vm : vmList){
                double finishTime = node.estimateFinishTime(vm);
                if(finishTime < minFinishTime){
                    minFinishTime = finishTime;
                    minvm = vm;
                }
            }
            node.bind(minvm.getId());

            estimateInfo info = (estimateInfo) node.getMarks();
            info.status = Status.Allocate;
            info.execTime = node.estimateExecTime(minvm);
            info.uploadTime = node.getUploadTime(minvm);
            info.downloadTime = node.getDownloadTime(minvm);
            calBackwardRank(node);

            processDownward(node);

        }
        return getCurrentSchedule();

    }

    private GraphNode getMaxPriority(List<GraphNode> list){
        GraphNode maxnode = list.get(0);
        estimateInfo info = (estimateInfo)maxnode.getMarks();
        double maxpriorty = info.upwardRank + info.downwardRank;
        for(GraphNode node : list){
            info = (estimateInfo) node.getMarks();
            double priorty = info.upwardRank + info.downwardRank;
            if(priorty > maxpriorty){
                maxnode = node;
                maxpriorty = priorty;
            }
        }
        return maxnode;
    }


    private void calAvgInfo(GraphNode node){
        node.mark(new estimateInfo(calAvgExecTime(node), calAvgUploadTime(node), calAvgDownloadTime(node)));
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
    private void processDownward(){
        List<GraphNode> available = getStartNode();
        while(!available.isEmpty()){
            List<GraphNode> newList = new LinkedList<>();
            for(GraphNode node : available){
                calBackwardRank(node);
                for(GraphNode child : node.getChilds()){
                    if(!newList.contains(child) && isReachableWhenDownward(child)){
                        newList.add(child);
                    }
                }
            }
            available = newList;
        }

    }

    private void processDownward(GraphNode root){
        List<GraphNode> available = new LinkedList<>();
        List<GraphNode> visit = new LinkedList<>();
        available.add(root);
        visit.add(root);
        while(!available.isEmpty()){
            List<GraphNode> list = new LinkedList<>();
            for(GraphNode node : available){
                if(!visit.contains(node)){
                    visit.add(node);
                    list.add(node);
                    ((estimateInfo)node.getMarks()).status = Status.Default;
                }
            }
            available = list;
        }
        available.add(root);
        while(!available.isEmpty()){
            List<GraphNode> newList = new LinkedList<>();
            for(GraphNode node : available){
                calBackwardRank(node);
                for(GraphNode child : node.getChilds()){
                    if(!newList.contains(child) && isReachableWhenDownward(child)){
                        newList.add(child);
                    }
                }
            }
            available = newList;
        }
    }


    private boolean isReachableWhenUpward(GraphNode node){
        boolean flag = true;
        for(GraphNode child : node.getChilds()){
            Status status = ((estimateInfo)child.getMarks()).status;
            flag &= (status == Status.Upward || status == Status.Allocate);
        }
        return flag;
    }


    private boolean isReachableWhenDownward(GraphNode node){
        boolean flag = true;
        for(GraphNode father : node.getFathers()){
            Status status = ((estimateInfo)father.getMarks()).status;
            flag &= (status == Status.DownWard || status == Status.Allocate);
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
        info = (estimateInfo) node.getMarks();
        maxRank = maxRank + info.uploadTime + info.execTime;
        info.setUpwardRank(maxRank);
    }

    private void calBackwardRank(GraphNode node){
        estimateInfo info;
        double maxRank = 0;
        for(GraphNode father : node.getFathers()){
            info = (estimateInfo) father.getMarks();
            if(maxRank < info.downwardRank + info.uploadTime + info.execTime){
                maxRank = info.downwardRank + info.uploadTime + info.execTime;
            }
         }
        info = (estimateInfo) node.getMarks();
        maxRank = maxRank + info.downwardRank;
        info.downwardRank = maxRank;
        if(info.status != Status.Allocate){
            info.status = Status.DownWard;
        }
    }


    enum Status {
        Upward, DownWard, Default, Allocate
    }
    class estimateInfo{
        private double upwardRank;
        private double downwardRank;
        private double execTime;
        private double uploadTime;
        private double downloadTime;
        private Status status = Status.Default;


        private estimateInfo(double execTime, double uploadTime, double downloadTime){
            this.execTime = execTime;
            this.uploadTime = uploadTime;
            this.downloadTime = downloadTime;
        }
        private void setUpwardRank(double upwardRank){
            this.upwardRank = upwardRank;
            status = Status.Upward;
        }
        private void setDownwardRank(double downwardRank){
            this.downwardRank = downwardRank;
            status = Status.DownWard;
        }

    }


}
