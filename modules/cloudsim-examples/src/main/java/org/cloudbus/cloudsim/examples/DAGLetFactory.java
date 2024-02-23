package org.cloudbus.cloudsim.examples;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.examples.DAG.DAGNode;
import org.cloudbus.cloudsim.examples.DAG.DAGLet;

import java.util.*;

public class DAGLetFactory {

    private static Random random;
    private DAGNodeFactory factory;

    private int maxnum;
    private int maxfork;
    private int maxmerge;
    private double[] p;

    public DAGLetFactory(int maxnum, int maxfork, int maxmerge, double[] p, DAGNodeFactory factory){
        this.maxnum = maxnum;
        this.maxfork = maxfork;
        this.maxmerge = maxmerge;
        this.p = reset(maxmerge, p);
        this.factory = factory;
    }

    public DAGLet generate(){
        DAGNode start = factory.generate();
        List<DAGNode> finish = new LinkedList<>();

        DAGNode pre, next;

        if (maxnum > 1){
            ArrayList<DAGNode> nodelist = new ArrayList<>(maxnum);
            nodelist.add(start);
            pre = start;
            for(int i=1; i<maxnum/3; i++){
                next = factory.generate();
                pre.linkAfter(next);
                nodelist.add(next);
            }
            for(int i=maxnum/3; i<maxnum; i++){
                next = factory.generate();
                int merge = rand(maxmerge, p);
                for(int j=0; j<merge; j++){
                    int id = (int)(random.nextDouble() * nodelist.size());
                    pre = nodelist.get(id);
                    if(!next.isFather(pre)){
                        pre.linkAfter(next);
                        if(pre.getNextNode().size() >= maxfork){
                            nodelist.remove(id);
                        }
                    }
                }
                nodelist.add(next);
            }
        }

//        Log.printLine();
//        Log.printLine("==============  new DAGlet  =====================");
        Stack<DAGNode> stack = new Stack<>();
        List<DAGNode> all = new LinkedList<>();
        List<DAGNode> list = new LinkedList<>();
        stack.push(start);
        while(!stack.isEmpty()){
            DAGNode node = stack.pop();
            all.add(node);
            if(node.isEnd()){
                finish.add(node);
            }else{
                for(DAGNode n : node.getNextNode()){
//                    Log.printLine(node.getCloudletId()+ " -> " + n.getCloudletId());
                    if(n.getPreNode().size() > 1) {
                        if (list.contains(n)) {
                            continue;
                        }
                        list.add(n);
                    }
                    stack.push(n);
                }
            }
        }
//        Log.printLine("start:" + start.getCloudletId());
//        Log.print("finish: ");
//        for(DAGNode n : finish){
//            Log.print(n.getCloudletId() + " ");
//        }
//        Log.printLine("\n=====================================================");
        return new DAGLet(start, finish, all);

    }

    private double[] reset(int i, double[] p){
        double total = 0;
        for(int j=0; j<i; j++){
            total += p[j];
            p[j] = total;
        }
        return p;
    }

    private int rand(int max, double[] p){
        for(int i=0; i<max; i++){
            if(random.nextDouble() < p[i]){
                return ++i;
            }
        }
        return 1;   // 入度最小为1
    }
    public static void setRandom(Random random){
        DAGLetFactory.random = random;
    }
}
