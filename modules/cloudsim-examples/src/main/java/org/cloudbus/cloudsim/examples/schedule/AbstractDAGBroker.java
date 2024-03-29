package org.cloudbus.cloudsim.examples.schedule;


import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.examples.DAG.DAGLet;
import org.cloudbus.cloudsim.examples.DAG.DAGNode;
import org.cloudbus.cloudsim.lists.VmList;

import java.util.*;

public abstract class AbstractDAGBroker extends DatacenterBroker {

    protected List<DAGLet> DAGletlist = new LinkedList<>();
    private List<GraphNode> graphNodeList = new LinkedList<>();

    private Map<DAGNode, GraphNode> letGraphNodeMap = new HashMap<>();

    private Map<Vm, List<DAGNode>> schedule;
    private List<DAGNode> availableList = new LinkedList<>();
    private Map<Vm, Boolean> vmWorking = new HashMap<>();

    private List<GraphNode> availableGraphNodeList = new LinkedList<>();
    private Map<Vm, List<GraphNode>> vmAllocated = new HashMap<>();


    public AbstractDAGBroker(String name) throws Exception {
        super(name);
    }


    // 用于调度算法


    protected List<GraphNode> getStartNode(){
        List<GraphNode> list = new LinkedList<>();
        for(DAGLet let : DAGletlist){
            list.add(letGraphNodeMap.get(let.getStart()));
        }
        return list;
    }

    protected List<GraphNode> getFinishNode(){
        List<GraphNode> list = new LinkedList<>();
        for(GraphNode node : graphNodeList){
            if(node.getTask().isEnd()){
                list.add(node);
            }
        }
        return list;
    }

    protected List<GraphNode> getAllNode(){
        return new LinkedList<>(graphNodeList);
    }


    protected Map<Vm, List<DAGNode>> getCurrentSchedule(){
        Map<Vm, List<DAGNode>> schedule = new HashMap<>();
        for(Vm vm : vmList){
            List<DAGNode> task = new LinkedList<>();
            for(GraphNode graphNode : vmAllocated.get(vm)){
                task.add(graphNode.getTask());
            }
            schedule.put(vm, task);
        }
        return schedule;
    }

//    protected void bindGraghNode(GraphNode node, int vmID){
//        node.bind(vmID);
//        availableGraphNodeList.remove(node);
//        for(GraphNode child : node.getChilds()){
//            if(child.isAvailable()){
//                availableGraphNodeList.add(child);
//            }
//        }
//    }

    protected void resetGraghNode(){
        for(GraphNode node : graphNodeList){
            node.reset();
        }
        for(Vm vm : vmList){
            vmAllocated.get(vm).clear();
        }
    }

    protected List<GraphNode> getAvailableGraphNodeList(){
        return new LinkedList<>(availableGraphNodeList);
    }

    protected double getVmReadyTime(Vm vm){
        List<GraphNode> taskList = vmAllocated.get(vm);
        return taskList.isEmpty() ? 0 : taskList.get(taskList.size() - 1).getEstimateFinishTime();
    }



    private void removeNode(DAGNode node){
        availableList.remove(node);
        List<DAGNode> nextList = new LinkedList<>(node.getNextNode());
        for(DAGNode next : nextList){
            next.removePre(node);
            if(next.getPreNode().isEmpty()){
                availableList.add(next);
            }
        }
    }



    protected abstract Map<Vm, List<DAGNode>> scheduleTask();






    // broker提交相关，与调度算法无关

    @Override
    protected void submitCloudlets(){

        // 生成供子类使用的DAG图结构
        processDAGStruct();

        // 获取调度结果
        schedule = scheduleTask();


        // 绑定虚拟机
        for(Vm vm : vmList){
            List<DAGNode> list = schedule.get(vm);
            for(DAGNode node : list){
                node.setVmId(vm.getId());
            }
            vmWorking.put(vm, false);
        }

        // 检查是否全部绑定，并打印信息
        for(Cloudlet cloudlet : getCloudletList()){
            Vm vm;
            if(cloudlet.getVmId() == -1){
                vm = getVmsCreatedList().get(0);
                Log.printLine("******************   unallocated cloudlet ****************");
            }else{
                vm = VmList.getById(getVmList(), cloudlet.getVmId());
                if(vm == null){
                    Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": Postponing execution of cloudlet ",
                            cloudlet.getCloudletId(), ": bount VM not available");
                    continue;
                }
            }

            cloudlet.setVmId(vm.getId());
        }


        // 将各个DAG图起始节点添加到可调度队列
        for(DAGLet let : DAGletlist){
            DAGNode node = let.getStart();
            availableList.add(node);
        }
        for(Vm vm : vmList){
            List<DAGNode> list = schedule.get(vm);
            if(!list.isEmpty()){
                DAGNode node = list.get(0);
                if(!availableList.contains(node)){
                    continue;
                }
                if (!Log.isDisabled()) {
                    Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": Sending cloudlet ",
                            node.getCloudletId(), " to VM #", vm.getId());
                }

                sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.CLOUDLET_SUBMIT, node);
                vmWorking.put(vm, true);
                cloudletsSubmitted++;
                getCloudletSubmittedList().add(node);
            }
        }
    }



    @Override
    protected void processCloudletReturn(SimEvent ev){
        DAGNode cloudlet = (DAGNode) ev.getData();
        getCloudletReceivedList().add(cloudlet);
        Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": Cloudlet ", cloudlet.getCloudletId(),
                " received");
        cloudletsSubmitted--;

//        removeGraphNode(cloudlet);

        removeNode(cloudlet);
        vmWorking.put(VmList.getById(vmList, cloudlet.getVmId()), false);
        schedule.get(VmList.getById(vmList, cloudlet.getVmId())).remove(cloudlet);

        for(Vm vm : vmList){
            if(!vmWorking.get(vm)){
                List<DAGNode> list = schedule.get(vm);
                if(!list.isEmpty()){
                    DAGNode node = list.get(0);
                    if(availableList.contains(node)){
                        sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.CLOUDLET_SUBMIT, node);
                        cloudletsSubmitted++;
                        getCloudletSubmittedList().add(node);
                        Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": Sending cloudlet ",
                                node.getCloudletId(), " to VM #", vm.getId());
                        vmWorking.put(vm, true);
                    }
                }
            }
        }

        if(availableList.size() == 0 && cloudletsSubmitted == 0){
            Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": All Cloudlets executed. Finishing...");
            clearDatacenters();
            finishExecution();
        }else if(availableList.size() > 0 && cloudletsSubmitted == 0){
            clearDatacenters();
            createVmsInDatacenter(0);
        }
    }



//    private void removeGraphNode(DAGNode task){
//        GraphNode node = letGraphNodeMap.get(task);
//        for(GraphNode child : node.getChilds()){
//            child.getFathers().remove(node);
//        }
//        letGraphNodeMap.remove(node);
//        graphNodeList.remove(node);
//    }


    private void processDAGStruct(){
        List<DAGNode> letList = getCloudletList();
        List<GraphNode> unhandleGraphNode = new LinkedList<>();
        List<DAGLet> unhandleDAGLet = new LinkedList<>();

        for(Vm vm : vmList){
            if(!vmAllocated.containsKey(vm)){
                vmAllocated.put(vm, new LinkedList<>());
            }
        }

        for(DAGNode cloudlet: letList){
            if(!DAGletlist.contains(cloudlet.getDAGLet())){
                DAGletlist.add(cloudlet.getDAGLet());
                unhandleDAGLet.add(cloudlet.getDAGLet());
            }
            if(!letGraphNodeMap.containsKey(cloudlet)){
                GraphNode graphNode = new GraphNode(cloudlet);
                letGraphNodeMap.put(cloudlet, graphNode);
                unhandleGraphNode.add(graphNode);
            }
        }
        graphNodeList.addAll(unhandleGraphNode);
        for(DAGLet let : unhandleDAGLet){
            availableGraphNodeList.add(letGraphNodeMap.get(let.getStart()));
        }
        for(GraphNode graphNode : unhandleGraphNode){
            List<DAGNode> preTaskList = graphNode.getTask().getPreNode();
            List<GraphNode> graphNodeList1 = new LinkedList<>();
            for(DAGNode task : preTaskList){
                graphNodeList1.add(letGraphNodeMap.get(task));
            }
            graphNode.setFathers(graphNodeList1);
            List<DAGNode> nextTaskList = graphNode.getTask().getNextNode();
            List<GraphNode> graphNodeList2 = new LinkedList<>();
            for (DAGNode task : nextTaskList){
                graphNodeList2.add(letGraphNodeMap.get(task));
            }
            graphNode.setChilds(graphNodeList2);
        }

    }

    // 用于DAG图

    enum Status{
        UNBIND, BIND, ESTIMATE
    }

    protected class GraphNode{

        private Status status = Status.UNBIND;

        private List<GraphNode> fathers = new LinkedList<>();
        private List<GraphNode> childs = new LinkedList<>();
        private DAGNode task;
        private int vmId = -1;
        private double estimateExecTime;
        private double estimateStartTime;
        private double estimateFinishTime;

        private Object info = null;



        public GraphNode(DAGNode node){
            task = node;
        }




        public void bind(int vmId){
            if(this.vmId == vmId){
                return;
            }else if(status == Status.BIND){
                unbind();
            }
            this.vmId = vmId;
            Vm vm = VmList.getById(vmList, vmId);
            status = Status.BIND;
            availableGraphNodeList.remove(this);
            for(GraphNode child : childs){
                if(child.isAvailable()){
                    availableGraphNodeList.add(child);
                }
            }

            // 更新执行时间
            estimateExecTime = estimateExecTime(vm);
            estimateStartTime = estimateStartTime(vm);
            estimateFinishTime = estimateStartTime + estimateExecTime + getUploadTime(vm);
            vmAllocated.get(vm).add(this);
        }

        public double getDownloadTime(Vm vm){
            return 0.0;
        }

        public double getUploadTime(Vm vm){
            return 0.0;
        }


        public double estimateExecTime(Vm vm){
            return task.getCloudletLength()/vm.getMips();
        }

        public double estimateStartTime(Vm vm){
            double maxTime = 0;
            for(GraphNode father : fathers){
                double fatherTime = father.getEstimateFinishTime();
                if(maxTime < fatherTime){
                    maxTime = fatherTime;
                }
            }
            double vmReadyTime = getVmReadyTime(vm);
            if(vmReadyTime >  maxTime){
                return vmReadyTime + getDownloadTime(vm);
            }else {
                return maxTime + getDownloadTime(vm);
            }
        }
        public double estimateFinishTime(Vm vm){
            return  estimateStartTime(vm) + estimateExecTime(vm) + getUploadTime(vm);
        }



        public List<GraphNode> getFathers(){
            return fathers;
        }

        public List<GraphNode> getChilds(){
            return childs;
        }

        public DAGNode getTask(){
            return task;
        }

        public void setFathers(List<GraphNode> fathers){
            this.fathers = fathers;
        }

        public void setChilds(List<GraphNode> childs){
            this.childs = childs;
        }

        public void unbind(){
            status = Status.UNBIND;
            Vm vm = VmList.getById(vmList, vmId);
            vmAllocated.get(vm).remove(this);
            vmId = -1;
            estimateExecTime = Double.MAX_VALUE;
            estimateStartTime = Double.MAX_VALUE;
            estimateFinishTime = Double.MAX_VALUE;
            for(GraphNode child : childs){
                availableGraphNodeList.remove(child);
            }
            availableGraphNodeList.add(this);

        }

        public boolean isAvailable(){
            boolean flag = true;
            for(GraphNode father : fathers){
                flag &= (father.status != Status.UNBIND);
            }
            return flag;
        }

        private void reset(){
            status = Status.UNBIND;
            vmId = -1;
            estimateExecTime = Double.MAX_VALUE;
            estimateStartTime = Double.MAX_VALUE;
            estimateFinishTime = Double.MAX_VALUE;
        }


        public double getEstimateStartTime(){
            return estimateStartTime;
        }

        public double getEstimateExecTime(){
            return estimateExecTime;
        }

        public double getEstimateFinishTime(){
            return estimateFinishTime;
        }

        public void mark(Object info){
            this.info = info;
        }
        public Object getMarks(){
            return info;
        }


    }



}

