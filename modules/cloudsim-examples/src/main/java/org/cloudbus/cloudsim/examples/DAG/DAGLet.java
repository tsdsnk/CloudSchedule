package org.cloudbus.cloudsim.examples.DAG;

import java.util.*;

public class DAGLet {
    private static int allocatedId = 0;
    private int id;
    private DAGNode start;

    private List<DAGNode> all;

    private long expectedTime;


    public DAGLet(DAGNode start, List<DAGNode> all, long expectedTime){
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

    public long getExpectedTime(){
        return expectedTime;
    }
    public int getId(){
        return id;
    }
}
