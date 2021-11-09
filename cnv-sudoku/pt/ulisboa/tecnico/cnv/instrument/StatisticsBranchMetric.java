package pt.ulisboa.tecnico.cnv.instrument;

import java.io.*;
import java.util.*;

public class StatisticsBranchMetric implements Metric<Long> {
    private static StatisticsBranch[] branch_info;
	private static int branch_number;
	private static int branch_pc;
	private static String branch_class_name;
	private static String branch_method_name;

    public StatisticsBranchMetric(){
        // _count = new Long(0);
    }

    // public StatisticsBranchMetric(String name){
    //     _count = new Long(0);
    //     _name = name;
    // }

    @Override
    public void update(Long amount) {
        return;
    }

    @Override
    public void log(PrintStream out) {
        // out.printf("%s: %d\n", _name, _count);
        out.println("Branch summary:");
        out.println("CLASS NAME" + '\t' + "METHOD" + '\t' + "PC" + '\t' + "TAKEN" + '\t' + "NOT_TAKEN");
        
        if(branch_info != null){
            for (int i = 0; i < branch_info.length; i++) {
                if (branch_info[i] != null) {
                    branch_info[i].print(out);
                }
            }
        }
    }

    @Override 
    public Metric.Label getLabel() {
        return Metric.Label.ADD;
    }

    public void setBranchClassName(String name)
    {
        branch_class_name = name;
    }

	public void setBranchMethodName(String name) 
    {
        branch_method_name = name;
    }
	
	public void setBranchPC(int pc)
    {
        branch_pc = pc;
    }
	
	public void branchInit(int n) 
    {
        if (branch_info == null) {
            branch_info = new StatisticsBranch[n];
        }
    }

	public void updateBranchNumber(int n)
    {
        branch_number = n;
        if (branch_info[branch_number] == null) {
            branch_info[branch_number] = new StatisticsBranch(branch_class_name, branch_method_name, branch_pc);
        }
    }

	public void updateBranchOutcome(int br_outcome)
    {
        if (br_outcome == 0) {
            branch_info[branch_number].incrNotTaken();
        }
        else {
            branch_info[branch_number].incrTaken();
        }
    }
}
