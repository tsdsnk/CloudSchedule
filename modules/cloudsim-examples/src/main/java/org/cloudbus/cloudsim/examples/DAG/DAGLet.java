package org.cloudbus.cloudsim.examples.DAG;

import java.util.*;

public class DAGLet {
    private static int allocatedId = 0;
    private int id;
    private DAGNode start;

    private List<DAGNode> all;

    private double expectedTime;


    public DAGLet(DAGNode start, List<DAGNode> all, double expectedTime){
        id = allocatedId;
        allocatedId++;
        this.start = start;
        this.all = all;
        for(DAGNode node: all){
            node.setDAGLet(this);
        }
        this.expectedTime = expectedTime;
    }




    public DAGNode getStart(){
        return start;
    }


    public List<DAGNode> getAll(){
        return all;
    }


    public void setUserId(int userId) {
        for(DAGNode node:all){
            node.setUserId(userId);
        }
    }

    public double getExpectedTime(){
        return expectedTime;
    }
    public int getId(){
        return id;
    }

//
//    @Override
//    public DAGLet clone(){
//
//        List<DAGNode> newAll = new ArrayList<>(all.size());
//        HashMap<Integer, Integer> idMap = new HashMap<>();
//        for(int i=0; i<all.size(); i++){
//            DAGNode newNode = new DAGNode(all.get(i));
//            newAll.add(newNode);
//            idMap.put(newNode.getCloudletId(), i);
//        }
//
//        for(int i=0; i<all.size(); i++){
//            DAGNode oldNode = all.get(i);
//            DAGNode newNode = newAll.get(i);
//            for(DAGNode node : oldNode.getPreNode()){
//                List<DAGNode> list = new LinkedList<>();
//                list.add(newAll.get(idMap.get(node.getCloudletId())));
//                newNode.setPreletlist(list);
//            }
//            for(DAGNode node : oldNode.getNextNode()){
//                List<DAGNode> list = new LinkedList<>();
//                list.add(newAll.get(idMap.get(node.getCloudletId())));
//                newNode.setNextletlist(list);
//            }
//        }
//
//        return new DAGLet(this, newAll.get(idMap.get(start.getCloudletId())), newAll);
//    }

//    private DAGLet(DAGLet oldlet, DAGNode newStart, List<DAGNode> newAll){
//        id = oldlet.getId();
//        start = newStart;
//        all = newAll;
//        this.expectedTime = oldlet.getExpectedTime();
//        for(DAGNode node: all){
//            node.setDAGLet(this);
//        }
//    }

}
