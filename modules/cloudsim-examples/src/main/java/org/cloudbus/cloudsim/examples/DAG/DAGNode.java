package org.cloudbus.cloudsim.examples.DAG;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.UtilizationModel;

import java.util.LinkedList;
import java.util.List;

public class DAGNode extends Cloudlet {

    private static int AllocateId = 0;
    private DAGLet DAGLet;

    private List<DAGNode> preletlist = new LinkedList<>();
    private List<DAGNode> nextletlist = new LinkedList<>();


    public DAGNode (final long cloudletLength,
                    final int pesNumber,
                    final long cloudletFileSize,
                    final long cloudletOutputSize,
                    final UtilizationModel utilizationModelCpu,
                    final UtilizationModel utilizationModelRam,
                    final UtilizationModel utilizationModelBw
                    ){
        super(AllocateId, cloudletLength, pesNumber, cloudletFileSize, cloudletOutputSize, utilizationModelCpu, utilizationModelRam, utilizationModelBw);
        AllocateId++;
    }

    public DAGNode(DAGNode old){
        super(old.getCloudletId(), old.getCloudletLength(), old.getNumberOfPes(), old.getCloudletFileSize(),
                old.getCloudletOutputSize(), old.getUtilizationModelCpu(), old.getUtilizationModelRam(), old.getUtilizationModelBw());
        DAGLet = null;
        setUserId(old.getUserId());
    }


    public boolean isStart(){
        return preletlist.isEmpty();
    }

    public boolean isEnd(){
        return nextletlist.isEmpty();
    }

    public boolean isAncestor(DAGNode node){
        for (DAGNode n : preletlist){
            if(n.isAncestor(node)){
                return true;
            }
        }
        return false;
    }

    public boolean isFather(DAGNode node){
        return preletlist.contains(node);
    }

    public void linkPre(DAGNode prelet){
        this.preletlist.add(prelet);
        prelet.nextletlist.add(this);
    }
    public void linkPreAll(List<DAGNode> prelet){
        for(DAGNode node : prelet){
            linkPre(node);
        }
    }

    public void setPreletlist(List<DAGNode> list){
        preletlist = list;
    }

    public void setNextletlist(List<DAGNode> list){
        nextletlist = list;
    }


    public void linkAfter(DAGNode nextlet){
        this.nextletlist.add(nextlet);
        nextlet.preletlist.add(this);
    }

    public void linkAfterAll(List<DAGNode> nextlet){
        for(DAGNode node : nextlet){
            linkAfter(node);
        }
    }

    public List<DAGNode> getPreNode(){
        return preletlist;
    }

    public List<DAGNode> getNextNode(){
        return nextletlist;
    }


    public void setDAGLet(DAGLet DAGLet){
        this.DAGLet = DAGLet;
    }

    public DAGLet getDAGLet(){
        return DAGLet;
    }

    public void removePre(DAGNode pre){
        // 这里保留了从前面节点找其后继的能力
        preletlist.remove(pre);
    }

}
