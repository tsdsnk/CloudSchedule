package org.cloudbus.cloudsim.examples;

import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.examples.DAG.DAGNode;

public class DAGNodeFactory {
    public DAGNodeFactory(){

    }

    public DAGNode generate(){
        long length = 40000;
        long fileSize = 300;
        long outputSize = 300;
        int pesNumber = 1;
        UtilizationModel utilizationModel = new UtilizationModelFull();
        return new DAGNode(length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
    }
}
