package org.cloudbus.cloudsim.examples.DAG;

import java.util.*;

public class DAGLet {
    private DAGNode start;
    private List<DAGNode> finish;

    private List<DAGNode> all;

    public DAGLet(DAGNode start, List<DAGNode> finish, List<DAGNode> all){
        this.start = start;
        this.finish = finish;
        this.all = all;
        for(DAGNode node: all){
            node.setDAGLet(this);
        }
    }


    public DAGNode getStart(){
        return start;
    }

    public List<DAGNode> getFinish(){
        return finish;
    }

    public List<DAGNode> getAll(){
        return all;
    }


    public void setUserId(int userId) {
        for(DAGNode node:all){
            node.setUserId(userId);
        }
    }
}
