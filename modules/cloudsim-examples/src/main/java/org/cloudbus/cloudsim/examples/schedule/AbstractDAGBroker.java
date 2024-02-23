package org.cloudbus.cloudsim.examples.schedule;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.examples.DAG.DAGLet;
import org.cloudbus.cloudsim.examples.DAG.DAGNode;

import java.util.LinkedList;
import java.util.List;

public abstract class AbstractDAGBroker extends DatacenterBroker {

    private List<DAGLet> DAGletlist = new LinkedList<>();
    private List<DAGNode> availableList = new LinkedList<>();



    public AbstractDAGBroker(String name) throws Exception {
        super(name);
        for(Cloudlet cloudlet : cloudletList){
            if(!DAGletlist.contains(((DAGNode)cloudlet).getDAGLet())){
                DAGletlist.add(((DAGNode)cloudlet).getDAGLet());
            }
        }
        for(DAGLet let : DAGletlist){
            availableList.add(let.getStart());
        }
    }

    public boolean removeNode(DAGNode node){
        if(!availableList.contains(node)){
            return false;
        }
        availableList.remove(node);
        for(DAGNode next : node.getNextNode()){
            next.removePre(node);
            if(next.getPreNode().isEmpty()){
                availableList.add(next);
            }
        }
        return true;
    }

    public abstract void bindCloudletsToVms();

}
