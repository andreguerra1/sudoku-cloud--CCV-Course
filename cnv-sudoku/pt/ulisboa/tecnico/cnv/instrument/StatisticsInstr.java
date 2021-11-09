//
// StatisticsInstr.java
//
// This program measures and instruments to obtain different statistics
// about Java programs.
//
// Copyright (c) 1998 by Han B. Lee (hanlee@cs.colorado.edu).
// ALL RIGHTS RESERVED.
//
// Permission to use, copy, modify, and distribute this software and its
// documentation for non-commercial purposes is hereby granted provided 
// that this copyright notice appears in all copies.
// 
// This software is provided "as is".  The licensor makes no warrenties, either
// expressed or implied, about its correctness or performance.  The licensor
// shall not be liable for any damages suffered as a result of using
// and modifying this software.


package pt.ulisboa.tecnico.cnv.instrument;
import java.io.*;
import java.util.*;

public class StatisticsInstr implements Metric<Long>
{	
	public String class_name_;
	public String method_name_;
	public Long bb_count;
	public Long instr_count;
	public StatisticsInstr(String class_name, String method_name, Long b, Long i) 
	{
		class_name_ = class_name;
		method_name_ = method_name;
		bb_count = b;
		instr_count = i;
	}

	public void print(PrintStream out) 
	{
		out.println(class_name_ + ' ' + method_name_ + ' ' + bb_count + ' ' + instr_count);
	}

	@Override
	public void update(Long amount) {
		if(amount > 0){
			instr_count += amount;
			bb_count++;
		}
	}


    @Override
    public void log(PrintStream out) {
		// out.printf("%s %s: %d\n", class_name_, method_name_, counter);
		return;
    }

    @Override 
    public Metric.Label getLabel() {
        return Metric.Label.ADD;
    }
}

