package org.cloudbus.cloudsim.examples;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.examples.DAG.DAGLet;
import org.cloudbus.cloudsim.examples.DAG.DAGNode;
import org.cloudbus.cloudsim.examples.schedule.AbstractDAGBroker;
import org.cloudbus.cloudsim.examples.schedule.SimpleDAGBroker;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import java.text.DecimalFormat;
import java.util.*;

public class CloudSimExample {

    /** 生成DAG任务数目 */
    private static final int DAGLetNum = 10;

    /** 每个DAG图包含的节点个数，及其对应概率 */
    private static final int[] DAGLetNodeNum = {6, 10, 20};
    private static final double[] pDAGLetNodeNum = {0.3, 0.5, 0.2};

    /** DAG图中最大出度 */
    private static final int maxFork = 3;

    /** DAG图中入度及其对应概率 */
    private static final int[] merge = {1, 2};
    private static final double[] pmerge = {0.8, 0.2};

    /** DAG图中每个子任务的任务量(的基准值)，及其概率
     * 会在随机后的大/中/小任务上添加最多10%的波动
     * */
    private static final long[][] DAGNodeLength = {{10000, 100, 100}, {40000, 300, 300}, {80000, 600, 600}};

    private static double[] pDAGNodeLength = {0.4, 0.3, 0.3};

    /** 子任务的CPU数，这里目前仅考虑了为1的情况，修改可能导致任务提交到虚拟机不能运行(因为虚拟机只有1个CPU) */
    private static final int pesOfDAGNode = 1;

    /** 预期完成时间与任务总时间的系数关系，这里总时间仅仅是将各个任务总量相加除以虚拟机处理能力，没有考虑DAG图结构 */
    private static double coefficientOfTime = 5;







    public static Random random;

    static {
        random = new Random();
        random.setSeed(123456);
    }

    public static void main(String[] args){
        Log.printLine("========  Simulation Begin ==========");
        try{
            int num_user = 1;
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false;
            CloudSim.init(num_user, calendar, trace_flag);

            Datacenter datacenter = generateDatacenter();

            AbstractDAGBroker broker = new SimpleDAGBroker("simple");
            List<Vm> vmList = createVM(broker.getId(), 6);
            List<DAGLet> DAGLets = generateDAGletList(broker.getId(), userMips(vmList));



            List<Cloudlet> totallist = new LinkedList<>();
            for(DAGLet let : DAGLets){
                totallist.addAll(let.getAll());
            }
            broker.submitVmList(vmList);
            broker.submitCloudletList(totallist);

            CloudSim.startSimulation();

            List<Cloudlet> newList = broker.getCloudletReceivedList();
            CloudSim.stopSimulation();
            printCloudletList(newList);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static List<DAGLet> generateDAGletList(int userId, long mips){
        List<DAGLet> list = new LinkedList<>();
        for(int i=0; i<DAGLetNum; i++){
            list.add(generateDAGlet(userId, mips));
        }
        return list;
    }


    public static DAGLet generateDAGlet(int userId, long mips){
        // 生成DAG图
        DAGNode start = generateDAGNode();
        DAGNode pre, next;
        long length = start.getCloudletLength();
        int maxNum = DAGLetNodeNum[testRand(pDAGLetNodeNum)];

        ArrayList<DAGNode> nodelist = new ArrayList<>(maxNum);
        ArrayList<DAGNode> all = new ArrayList<>(maxNum);
        nodelist.add(start);
        pre = start;
        for(int i=1; i<maxNum/3; i++){
            next = generateDAGNode();
            pre.linkAfter(next);
            nodelist.add(next);
            length += next.getCloudletLength();
            pre = next;
        }
        for(int i=maxNum/3; i<maxNum; i++){
            next = generateDAGNode();
            int degree = merge[testRand(pmerge)];
            for(int j=0; j<degree; j++){
                int id = (int)(random.nextDouble() * nodelist.size());
                pre = nodelist.get(id);
                if(!next.isFather(pre)){
                    pre.linkAfter(next);
                    if(pre.getNextNode().size() >= maxFork){
                        all.add(nodelist.get(id));
                        nodelist.remove(id);
                    }
                }
            }
            nodelist.add(next);
            length += next.getCloudletLength();
        }
        all.addAll(nodelist);
        DAGLet let = new DAGLet(start, all,  coefficientOfTime * length/mips);
        let.setUserId(userId);
        return let;
    }

    private static long userMips(List<Vm> vmList){
        long mips = 0;
        for(Vm vm : vmList){
            mips += vm.getMips();
        }
        return mips;
    }

    public static DAGNode generateDAGNode(){
        long[] nodeBase = DAGNodeLength[testRand(pDAGNodeLength)];
        // 各项数据添加10%的波动
        long length = (long)(nodeBase[0] * 0.9 + nodeBase[0] * 0.2 * random.nextDouble());
        long fileSize = (long)(nodeBase[1] * 0.9 + nodeBase[1] * 0.2 * random.nextDouble());
        long outputSize = (long)(nodeBase[2] * 0.9 + nodeBase[2] * 0.2 * random.nextDouble());
        int pesNumber = pesOfDAGNode;
        UtilizationModel utilizationModel = new UtilizationModelFull();
        return new DAGNode(length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
    }


    public static Datacenter generateDatacenter(){
        List<Host> hostList = new ArrayList<>();

        List<Pe> peList1 = new ArrayList<Pe>();
        int mips = 1000;

        peList1.add(new Pe(0, new PeProvisionerSimple(mips)));
        peList1.add(new Pe(1, new PeProvisionerSimple(mips)));
        peList1.add(new Pe(2, new PeProvisionerSimple(mips)));
        peList1.add(new Pe(3, new PeProvisionerSimple(mips)));


        List<Pe> peList2 = new ArrayList<Pe>();
        peList2.add(new Pe(0, new PeProvisionerSimple(mips)));
        peList2.add(new Pe(1, new PeProvisionerSimple(mips)));
        int ram = 2048; //host memory (MB)
        long storage = 1000000; //host storage
        int bw = 10000;

        hostList.add(
                new Host(
                        0,
                        new RamProvisionerSimple(ram),
                        new BwProvisionerSimple(bw),
                        storage,
                        peList1,
                        new VmSchedulerTimeShared(peList1)
                )
        );


        hostList.add(
                new Host(
                        1,
                        new RamProvisionerSimple(ram),
                        new BwProvisionerSimple(bw),
                        storage,
                        peList2,
                        new VmSchedulerTimeShared(peList2)
                )
        );

        String arch = "x86";      // system architecture
        String os = "Linux";          // operating system
        String vmm = "Xen";
        double time_zone = 10.0;         // time zone this resource located
        double cost = 3.0;              // the cost of using processing in this resource
        double costPerMem = 0.05;		// the cost of using memory in this resource
        double costPerStorage = 0.1;	// the cost of using storage in this resource
        double costPerBw = 0.1;			// the cost of using bw in this resource
        LinkedList<Storage> storageList = new LinkedList<Storage>();	//we are not adding SAN devices by now

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);

        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter("Datacenter0", characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;

    }

    private static List<Vm> createVM(int userId, int vms) {

        //Creates a container to store VMs. This list is passed to the broker later
        LinkedList<Vm> list = new LinkedList<Vm>();

        //VM Parameters
        long size = 10000; //image size (MB)
        int ram = 512; //vm memory (MB)
        int mips = 1000;
        long bw = 1000;
        int pesNumber = 1; //number of cpus
        String vmm = "Xen"; //VMM name

        //create VMs
        Vm[] vm = new Vm[vms];

        for(int i=0;i<vms;i++){
            vm[i] = new Vm(i, userId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerSpaceShared());
            //for creating a VM with a space shared scheduling policy for cloudlets:
            //vm[i] = Vm(i, userId, mips, pesNumber, ram, bw, size, priority, vmm, new CloudletSchedulerSpaceShared());

            list.add(vm[i]);
        }

        return list;
    }

    private static void printCloudletList(List<Cloudlet> list) {
        List<DAGLet> letList = new ArrayList<>(DAGLetNum);
        for(Cloudlet cloudlet : list){
            DAGNode node = (DAGNode) cloudlet;
            if(!letList.contains(node.getDAGLet())){
                letList.add(node.getDAGLet());
            }
        }
        Log.printLine("===================  OUTPUT  ========================");
        for(int i=0; i<DAGLetNum; i++){
            printDAGLet(letList.get(i));
            Log.printLine();
        }

    }

    private static void printDAGLet(DAGLet let){
        List<DAGNode> visit = new LinkedList<>();
        List<DAGNode> finish = new LinkedList<>();
        Log.printLine("=============================  DAGLet " + let.getId() + " ================================================================================");
        Stack<DAGNode> stack = new Stack<>();
        stack.push(let.getStart());
        while(!stack.isEmpty()){
            DAGNode node = stack.pop();
            if(!visit.contains(node)){
                visit.add(node);
                Log.format("|nodeID %4d   length %6d  filesize %6d  output %6d   ||   ", node.getCloudletId(), node.getCloudletLength(), node.getCloudletFileSize(), node.getCloudletOutputSize());
                if(node.isEnd()){
                    finish.add(node);
                    Log.print("\n");
                    continue;
                }
                for(DAGNode next : node.getNextNode()){
                    if(!visit.contains(next)){
                        stack.push(next);
                    }
                    Log.print(node.getCloudletId() + "->" + next.getCloudletId() + " ");
                }
                Log.print("\n");

            }
        }
        Log.printLine("|");
        Log.print("|finish  ");
        double maxtime = 0.0;
        for(DAGNode node : finish){
            Log.print(node.getCloudletId() + " ");
            if(node.getFinishTime() > maxtime){
                maxtime = node.getFinishTime();
            }
        }
        Log.print("\n");
        Log.format("|start:%.2f  finish:%.2f  total:%.2f expected:%.2f\n", let.getStart().getExecStartTime(), maxtime, maxtime-let.getStart().getExecStartTime(), let.getExpectedTime());
        Log.format("|%16s  %16s  %16s  %10s  %16s  %16s  %16s|\n", "LetID-NodeId", "Status", "Datacenter", "VmId", "Time", "Start time", "Finish time");
        for(DAGNode node : let.getAll()){
            Log.format("|%16s  %16s  %16d  %10d  %16.2f  %16.2f  %16.2f|\n",
                     let.getId()+"-"+ node.getCloudletId(), node.getCloudletStatusString(), node.getResourceId(), node.getVmId(),
                    node.getActualCPUTime(), node.getExecStartTime(), node.getFinishTime());
        }
        Log.printLine("|=======================================================================================================================|");
    }

    private static int testRand(double[] p){
        double q = random.nextDouble();
        for(int i=0; i<p.length; i++){
            if(q < p[i]){
                return i;
            }
            q -= p[i];
        }
        return p.length - 1;
    }




}
