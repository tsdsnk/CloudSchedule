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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractDAGBroker extends DatacenterBroker {

    private List<DAGLet> DAGletlist = new LinkedList<>();




    public AbstractDAGBroker(String name) throws Exception {
        super(name);
    }

    public List<DAGNode> removeNode(DAGNode node){
        List<DAGNode> list = new LinkedList<>();
        List<DAGNode> nextList = new LinkedList<>(node.getNextNode());
        for(DAGNode next : nextList){
            next.removePre(node);
            if(next.getPreNode().isEmpty()){
                list.add(next);
            }
        }
        return list;
    }

    protected boolean isAvailable(DAGNode node, Integer vmId){
        Vm vm = VmList.getById(vmList, vmId);
        return node.getNumberOfPes() <= vm.getNumberOfPes();
    }

    protected boolean isAvailable(DAGNode node, Vm vm){
        return node.getNumberOfPes() <= vm.getNumberOfPes();
    }

    protected List<Vm> getAvailableVm(DAGNode node){
        List<Vm> list = new LinkedList<>();
        for(Vm vm : vmList){
            if(isAvailable(node, vm)){
                list.add(vm);
            }
        }
        return list;
    }



    protected abstract void bindCloudletsToVms();


    @Override
    protected void submitCloudlets(){
        for(Cloudlet cloudlet : getCloudletList()){
            if(!DAGletlist.contains(((DAGNode)cloudlet).getDAGLet())){
                DAGletlist.add(((DAGNode)cloudlet).getDAGLet());
            }
        }
        bindCloudletsToVms();
        Vm vm;
        for(Cloudlet cloudlet : getCloudletList()){
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

        for(DAGLet let : DAGletlist){
            DAGNode node = let.getStart();
            vm = VmList.getById(getVmList(), node.getVmId());

            if (!Log.isDisabled()) {
                Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": Sending cloudlet ",
                        node.getCloudletId(), " to VM #", vm.getId());
            }

            sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.CLOUDLET_SUBMIT, node);
            cloudletsSubmitted++;
            getCloudletSubmittedList().add(node);
        }
    }

    @Override
    protected void processCloudletReturn(SimEvent ev){
        DAGNode cloudlet = (DAGNode) ev.getData();
        getCloudletReceivedList().add(cloudlet);
        Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": Cloudlet ", cloudlet.getCloudletId(),
                " received");
        cloudletsSubmitted--;
        List<DAGNode> availableList =  removeNode(cloudlet);
        for(DAGNode node : availableList){
            Vm vm = VmList.getById(getVmList(), node.getVmId());
            sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.CLOUDLET_SUBMIT, node);
            cloudletsSubmitted++;
            getCloudletSubmittedList().add(node);
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



}
