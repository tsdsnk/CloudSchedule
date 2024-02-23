package org.cloudbus.cloudsim.examples.schedule;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Vm;

import java.util.Collections;
import java.util.Comparator;

public class TimeAwareBrroker extends DatacenterBroker {

    public TimeAwareBrroker(String name) throws Exception{
        super(name);
    }

    public void bindCloudletsToVmsTimeAwared(){
        int cloudletNum = cloudletList.size();
        int vmNum = vmList.size();
        double[][] time = new double[cloudletNum][vmNum];

        Collections.sort(cloudletList, new CloudletComparator());

        System.out.println("ce shi 1");
        for(int i=0; i<cloudletNum; i++){
            System.out.println("cloudlet_ID:" + cloudletList.get(i).getCloudletId()
                                + "--length:" + cloudletList.get(i).getCloudletLength() + " ");
        }

        for(int j=0; j<vmNum; j++){
            System.out.println("vm_ID:"+vmList.get(j).getId()+"--mips:"+vmList.get(j).getMips() + " ");
        }
        Collections.sort(vmList, new VmListComparator());

        for(int i=0; i<cloudletNum; i++){
            for(int j=0; j<vmNum; j++){
                time[i][j] = (double) cloudletList.get(i).getCloudletLength() / vmList.get(j).getMips();
                System.out.println("(cloudlet:"+cloudletList.get(i).getCloudletId()+", vm:"+vmList.get(j).getId()+"):"+cloudletList.get(i).getCloudletLength()+ "/" + vmList.get(j).getMips() +"=" + time[i][j]);
            }
            System.out.println();
        }

        double[] vmload = new double[vmNum];
        int[] vmTasks = new int[vmNum];
        double minLoad = 0;
        int idx = 0;
        vmload[vmNum-1] = time[0][vmNum-1];
        cloudletList.get(0).setVmId(vmList.get(vmNum-1).getId());
        System.out.println("cloudLet ID:"+cloudletList.get(0).getCloudletId()+"--Length:"+cloudletList.get(0).getCloudletLength()+ "--VM ID:"+cloudletList.get(0).getVmId());

        for(int i=1; i<cloudletNum; i++){
            minLoad = vmload[vmNum-1] + time[i][vmNum-1];
            idx = vmNum-1;
            for(int j=vmNum-2; j>=0; j--){
                if(vmload[j] + time[i][j] < minLoad){
                    minLoad = vmload[j]+time[i][j];
                    idx = j;
                }else if(vmload[j] + time[i][j] < minLoad  && vmTasks[j] < vmTasks[idx]){
                    idx = j;
                }
            }
            vmload[idx] += time[i][idx];
            vmTasks[idx]++;
            cloudletList.get(i).setVmId(vmList.get(idx).getId());
        }
    }




    private class CloudletComparator implements Comparator<Cloudlet>{
        public int compare(Cloudlet cl1, Cloudlet cl2){
            return (int)(cl2.getCloudletLength() - cl1.getCloudletLength());
        }
    }

    private class VmListComparator implements Comparator<Vm>{
        public int compare(Vm vm1, Vm vm2){
            return (int)(vm1.getMips() - vm2.getMips());
        }
    }


}
