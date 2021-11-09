//
// StatisticsTool.java
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

import BIT.highBIT.*;
import BIT.lowBIT.*;
import pt.ulisboa.tecnico.cnv.server.WebServer2;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;
import java.util.Arrays;
import java.util.ArrayList;

public class StatisticsTool 
{	
	public static class InstrContextInfo{
		public String className;
		public String methodName;
		public InstrContextInfo(){className=""; methodName="";}
	}

	// public static ArrayList<String> methods_list = new ArrayList<String>(Arrays.asList("pt/ulisboa/tecnico/cnv/solver/Solver|run", "pt/ulisboa/tecnico/cnv/solver/SolverFactory|getInstance", "pt/ulisboa/tecnico/cnv/solver/SudokuSolverBFS|rowCheck", "pt/ulisboa/tecnico/cnv/solver/SudokuSolverBFS|setNum", "pt/ulisboa/tecnico/cnv/solver/SudokuSolverBFS|boxCheck", "pt/ulisboa/tecnico/cnv/solver/SudokuSolverBFS|colCheck", "pt/ulisboa/tecnico/cnv/solver/SudokuSolverBFS|runSolver", "pt/ulisboa/tecnico/cnv/solver/SolverArgumentParser|getN1", "pt/ulisboa/tecnico/cnv/solver/SolverArgumentParser|getN2", "pt/ulisboa/tecnico/cnv/solver/AbstractSudokuSolver|printFixedWidth", "pt/ulisboa/tecnico/cnv/solver/AbstractSudokuSolver|deepCloneArray", "pt/ulisboa/tecnico/cnv/solver/AbstractSudokuSolver|print", "pt/ulisboa/tecnico/cnv/solver/SolverFactory$SolverType|values", "pt/ulisboa/tecnico/cnv/solver/SolverFactory$SolverType|valueOf", "pt/ulisboa/tecnico/cnv/solver/SolverArgumentParser$SolverParameters|toString", "pt/ulisboa/tecnico/cnv/solver/SudokuSolverCP|followsConstraints", "pt/ulisboa/tecnico/cnv/solver/SudokuSolverCP|runSolver", "pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver$ColumnNode|<init>", "pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver$Node|<init>", "pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver$ColumnID|<init>", "pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver|cover", "pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver|search", "pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver|uncover", "pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver|mapSolvedToGrid", "pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver|choose", "pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver|filled"));
	
	public static ArrayList<String> methods_list = new ArrayList<String>(Arrays.asList("pt/ulisboa/tecnico/cnv/solver/AbstractSudokuSolver|printFixedWidth", "pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver|filled", "pt/ulisboa/tecnico/cnv/solver/SudokuSolverCP|runSolver", "pt/ulisboa/tecnico/cnv/solver/SolverArgumentParser|getN1", "pt/ulisboa/tecnico/cnv/solver/SolverArgumentParser$SolverParameters|toString", "pt/ulisboa/tecnico/cnv/solver/SolverArgumentParser|getN2", "pt/ulisboa/tecnico/cnv/solver/SudokuSolverBFS|runSolver", "pt/ulisboa/tecnico/cnv/solver/SudokuSolverBFS|rowCheck", "pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver$ColumnID|<init>", "pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver|cover", "pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver$Node|<init>", "pt/ulisboa/tecnico/cnv/solver/SolverFactory$SolverType|values", "pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver|search", "pt/ulisboa/tecnico/cnv/solver/SudokuSolverCP|followsConstraints", "pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver|choose", "pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver$ColumnNode|<init>", "pt/ulisboa/tecnico/cnv/solver/SudokuSolverBFS|colCheck", "pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver|uncover", "pt/ulisboa/tecnico/cnv/solver/SudokuSolverBFS|setNum", "pt/ulisboa/tecnico/cnv/solver/Solver|run", "pt/ulisboa/tecnico/cnv/solver/SudokuSolverBFS|boxCheck"));

	// public static ArrayList<String> methods_list = new ArrayList<String>(Arrays.asList("pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver|filled", "pt/ulisboa/tecnico/cnv/solver/Solver|run"));

	public static String method;
	public static HashMap<Long, HashMap<String, Metric>> metrics = new HashMap<Long, HashMap<String, Metric>>();
	public static HashMap<Long, HashMap<String, HashMap<String, StatisticsInstr>>> instructions = new HashMap<Long, HashMap<String, HashMap<String, StatisticsInstr>>>();
	public static HashMap<Long, InstrContextInfo> context_info = new HashMap<Long, InstrContextInfo>();

	public static void printUsage() 
		{
			System.out.println("Syntax: java StatisticsTool -stat_type in_path [out_path]");
			System.out.println("        where stat_type can be:");
			System.out.println("        static:     static properties");
			System.out.println("        dynamic:    dynamic properties");
			System.out.println("        alloc:      memory allocation instructions");
			System.out.println("        load_store: loads and stores (both field and regular)");
			System.out.println("        branch:     gathers branch outcome statistics");
			System.out.println();
			System.out.println("        in_path:  directory from which the class files are read");
			System.out.println("        out_path: directory to which the class files are written");
			System.out.println("        Both in_path and out_path are required unless stat_type is static");
			System.out.println("        in which case only in_path is required");
			System.exit(-1);
		}


	public static void doDynamic(File in_dir, File out_dir) 
		{
			String filelist[] = in_dir.list();
			
			
			for (int i = 0; i < filelist.length; i++) {
				String filename = filelist[i];
				if (filename.endsWith(".class")) {
					String in_filename = in_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
					String out_filename = out_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
					boolean changed = false;
					ClassInfo ci = new ClassInfo(in_filename);
					for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
						Routine routine = (Routine) e.nextElement();
						if(methods_list.contains(ci.getClassName()+"|"+routine.getMethodName())){
							System.out.println(ci.getClassName() + ": " + routine.getMethodName());
							changed = true;
							routine.addBefore("pt/ulisboa/tecnico/cnv/instrument/StatisticsTool", "setStatisticInstrClassName", ci.getClassName());
							routine.addBefore("pt/ulisboa/tecnico/cnv/instrument/StatisticsTool", "setStatisticInstrMethodName", routine.getMethodName());
							// count methods
							routine.addBefore("pt/ulisboa/tecnico/cnv/instrument/StatisticsTool", "dynInstrCount", new Integer(1));
							
							for (Enumeration instrs = (routine.getInstructionArray()).elements(); instrs.hasMoreElements(); ) {
								Instruction instr = (Instruction) instrs.nextElement();
								int opcode=instr.getOpcode();
								if (opcode == InstructionTable.getfield)
									instr.addBefore("pt/ulisboa/tecnico/cnv/instrument/StatisticsTool", "LSCount2", new Integer(0));
								// else if (opcode == InstructionTable.putfield)
							}
						}

							// for (Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements(); ) {
							// 	BasicBlock bb = (BasicBlock) b.nextElement();
							// 	if((ci.getClassName()+"|"+routine.getMethodName()).equals("pt/ulisboa/tecnico/cnv/solver/Solver|run")){
							// 		if (opcode == InstructionTable.getfield)
							// 			instr.addBefore("pt/ulisboa/tecnico/cnv/instrument/StatisticsTool", "LSFieldCount", new Integer(0));
							// 		// else if (opcode == InstructionTable.putfield)
							// 		if (opcode == InstructionTable.putfield)
							// 			instr.addBefore("pt/ulisboa/tecnico/cnv/instrument/StatisticsTool", "LSFieldCount", new Integer(1));
							// 		// if(bb.getOldStartAddress() == 22){
							// 		// 	// bb.addBefore("pt/ulisboa/tecnico/cnv/instrument/StatisticsTool", "dynInstrCount", new Integer(bb.size()));
							// 		// }
							// 	}

							// 	// run 22
							// 	// bb.addBefore("pt/ulisboa/tecnico/cnv/instrument/StatisticsTool", "setStatisticInstrMethodName", String.valueOf(bb.getOldStartAddress()));
							// 	// bb.addBefore("pt/ulisboa/tecnico/cnv/instrument/StatisticsTool", "dynInstrCount", new Integer(bb.size()));
							// }
							// InstructionArray instructions = routine.getInstructionArray();
		  
							// for (Enumeration instrs = instructions.elements(); instrs.hasMoreElements(); ) {
							// 	Instruction instr = (Instruction) instrs.nextElement();
							// 	int opcode=instr.getOpcode();
							// 	short instr_type = InstructionTable.InstructionTypeTable[opcode];
							// 	if (instr_type == InstructionTable.STORE_INSTRUCTION) {
							// 		instr.addBefore("pt/ulisboa/tecnico/cnv/instrument/StatisticsTool", "LSCount2", new Integer(1));
							// 	}
							// }
						// }
					}
					if(changed)
						ci.write(out_filename);
				}	
			}
		}
	
	public static synchronized void setStatisticInstrMethodName(String name) {
		try {
			context_info.get(Thread.currentThread().getId()).methodName = name;
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	public static synchronized void setStatisticInstrClassName(String name) {
		try {
			context_info.get(Thread.currentThread().getId()).className = name;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void dynInstrCount(int incr) {
		// try {
		// 	// ((Counter)metrics.get(Thread.currentThread().getId()).get("dyn_bb_count"))._count ++;
		// 	((Counter)metrics.get(Thread.currentThread().getId()).get("dyn_instr_count"))._count += incr;
		// } catch (Exception e) {
		// 	e.printStackTrace();
		// }
		try{
		
			HashMap<String, StatisticsInstr> classname = instructions.get(Thread.currentThread().getId()).get(context_info.get(Thread.currentThread().getId()).className);
			StatisticsInstr metric = (StatisticsInstr)classname.get(context_info.get(Thread.currentThread().getId()).methodName);

			metric.instr_count += incr;
			metric.bb_count = new Long(0);

		} catch(NullPointerException e){
			String classname = context_info.get(Thread.currentThread().getId()).className;
			String methodName = context_info.get(Thread.currentThread().getId()).methodName;
			if(!instructions.get(Thread.currentThread().getId()).containsKey(classname))
				instructions.get(Thread.currentThread().getId()).put(classname, new HashMap<String,StatisticsInstr>());
		
			instructions.get(Thread.currentThread().getId()).get(classname).put(methodName, new StatisticsInstr(classname, methodName, new Long(1), new Long(incr)));
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public static void dynMethodCount(int incr) {
		try{
			((Counter)metrics.get(Thread.currentThread().getId()).get("dyn_method_count"))._count++;

		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public static void doAlloc(File in_dir, File out_dir) 
		{
			String filelist[] = in_dir.list();
			
			for (int i = 0; i < filelist.length; i++) {
				String filename = filelist[i];
				if (filename.endsWith(".class")) {
					String in_filename = in_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
					String out_filename = out_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
					ClassInfo ci = new ClassInfo(in_filename);

					
					for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
						Routine routine = (Routine) e.nextElement();
						InstructionArray instructions = routine.getInstructionArray();
		  
						for (Enumeration instrs = instructions.elements(); instrs.hasMoreElements(); ) {
							Instruction instr = (Instruction) instrs.nextElement();
							int opcode=instr.getOpcode();
							if ((opcode==InstructionTable.NEW) ||
								(opcode==InstructionTable.newarray) ||
								(opcode==InstructionTable.anewarray) ||
								(opcode==InstructionTable.multianewarray)) {
								instr.addBefore("pt/ulisboa/tecnico/cnv/instrument/StatisticsTool", "allocCount", new Integer(opcode));
							}
						}
					}
					ci.write(out_filename);
				}
			}
		}

	public static synchronized void allocCount(int type)
		{
			try{
				switch(type) {
				case InstructionTable.NEW:
					((Counter)metrics.get(Thread.currentThread().getId()).get("newcount"))._count++;
					break;
				case InstructionTable.newarray:
					((Counter)metrics.get(Thread.currentThread().getId()).get("newarraycount"))._count++;
					break;
				case InstructionTable.anewarray:
					((Counter)metrics.get(Thread.currentThread().getId()).get("anewarraycount"))._count++;
					break;
				case InstructionTable.multianewarray:
					((Counter)metrics.get(Thread.currentThread().getId()).get("multianewarraycount"))._count++;
					break;
				}
			} catch (Exception e){
				e.printStackTrace();
			}
		}
	
	public static void doLoadStore(File in_dir, File out_dir) 
		{
			String filelist[] = in_dir.list();
			
			for (int i = 0; i < filelist.length; i++) {
				String filename = filelist[i];
				if (filename.endsWith(".class")) {
					String in_filename = in_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
					String out_filename = out_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
					ClassInfo ci = new ClassInfo(in_filename);

					for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
						Routine routine = (Routine) e.nextElement();
						
						for (Enumeration instrs = (routine.getInstructionArray()).elements(); instrs.hasMoreElements(); ) {
							Instruction instr = (Instruction) instrs.nextElement();
							int opcode=instr.getOpcode();
							if (opcode == InstructionTable.getfield)
								instr.addBefore("pt/ulisboa/tecnico/cnv/instrument/StatisticsTool", "LSFieldCount", new Integer(0));
							// else if (opcode == InstructionTable.putfield)
							if (opcode == InstructionTable.putfield)
								instr.addBefore("pt/ulisboa/tecnico/cnv/instrument/StatisticsTool", "LSFieldCount", new Integer(1));
							else {
								short instr_type = InstructionTable.InstructionTypeTable[opcode];
								if (instr_type == InstructionTable.LOAD_INSTRUCTION) {
									instr.addBefore("pt/ulisboa/tecnico/cnv/instrument/StatisticsTool", "LSCount", new Integer(0));
								}
								if (instr_type == InstructionTable.STORE_INSTRUCTION) {
									instr.addBefore("pt/ulisboa/tecnico/cnv/instrument/StatisticsTool", "LSCount", new Integer(1));
								}
							}
						}
					}
					ci.write(out_filename);
				}
			}	
		}	
		
	public static synchronized void LSFieldCount(int type) 
		{
			try{
				if (type == 0){
					((Counter)metrics.get(Thread.currentThread().getId()).get("fieldloadcount"))._count++;
				}
				else
					((Counter)metrics.get(Thread.currentThread().getId()).get("fieldstorecount"))._count++;
			} catch (Exception e){
				e.printStackTrace();
			}
		}

	public static synchronized void LSCount(int type) 
		{
			try{
				if (type == 0)
					metrics.get(Thread.currentThread().getId()).get("loadcount").update((long)1);
				else
					metrics.get(Thread.currentThread().getId()).get("storecount").update((long)1);
			} catch (Exception e){
				e.printStackTrace();
			}
		}

	public static synchronized void LSCount2(int type) 
		{
			try{
		
				HashMap<String, StatisticsInstr> classname = instructions.get(Thread.currentThread().getId()).get(context_info.get(Thread.currentThread().getId()).className);
				StatisticsInstr metric = (StatisticsInstr)classname.get(context_info.get(Thread.currentThread().getId()).methodName);
	
				// metric.instr_count += incr;
				metric.bb_count ++;
				// metric.bb_count ++;
	
			} catch(NullPointerException e){
				String classname = context_info.get(Thread.currentThread().getId()).className;
				String methodName = context_info.get(Thread.currentThread().getId()).methodName;
				if(!instructions.get(Thread.currentThread().getId()).containsKey(classname))
					instructions.get(Thread.currentThread().getId()).put(classname, new HashMap<String,StatisticsInstr>());
			
				instructions.get(Thread.currentThread().getId()).get(classname).put(methodName, new StatisticsInstr(classname, methodName, new Long(1), new Long(0)));
			}
			catch (Exception e){
				e.printStackTrace();
			}
		}
	
	public static void doBranch(File in_dir, File out_dir) 
		{
			String filelist[] = in_dir.list();
			int k = 0;
			int total = 0;
			
			for (int i = 0; i < filelist.length; i++) {
				String filename = filelist[i];
				if (filename.endsWith(".class")) {
					String in_filename = in_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
					ClassInfo ci = new ClassInfo(in_filename);

					for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
						Routine routine = (Routine) e.nextElement();
						InstructionArray instructions = routine.getInstructionArray();
						for (Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements(); ) {
							BasicBlock bb = (BasicBlock) b.nextElement();
							Instruction instr = (Instruction) instructions.elementAt(bb.getEndAddress());
							short instr_type = InstructionTable.InstructionTypeTable[instr.getOpcode()];
							if (instr_type == InstructionTable.CONDITIONAL_INSTRUCTION) {
								total++;
							}
						}
					}
				}
			}
			
			for (int i = 0; i < filelist.length; i++) {
				String filename = filelist[i];
				if (filename.endsWith(".class")) {
					String in_filename = in_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
					String out_filename = out_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
					ClassInfo ci = new ClassInfo(in_filename);
					
					for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
						Routine routine = (Routine) e.nextElement();
						routine.addBefore("pt/ulisboa/tecnico/cnv/instrument/StatisticsTool", "setBranchMethodName", routine.getMethodName());
						System.out.println(ci.getClassName() + ": " + routine.getMethodName());
						InstructionArray instructions = routine.getInstructionArray();
						for (Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements(); ) {
							BasicBlock bb = (BasicBlock) b.nextElement();
							Instruction instr = (Instruction) instructions.elementAt(bb.getEndAddress());
							short instr_type = InstructionTable.InstructionTypeTable[instr.getOpcode()];
							if (instr_type == InstructionTable.CONDITIONAL_INSTRUCTION) {
								instr.addBefore("pt/ulisboa/tecnico/cnv/instrument/StatisticsTool", "setBranchPC", new Integer(instr.getOffset()));
								instr.addBefore("pt/ulisboa/tecnico/cnv/instrument/StatisticsTool", "updateBranchNumber", new Integer(k));
								instr.addBefore("pt/ulisboa/tecnico/cnv/instrument/StatisticsTool", "updateBranchOutcome", "BranchOutcome");
								k++;
							}
						}
					}
					ci.addBefore("pt/ulisboa/tecnico/cnv/instrument/StatisticsTool", "setBranchClassName", ci.getClassName());
					ci.addBefore("pt/ulisboa/tecnico/cnv/instrument/StatisticsTool", "branchInit", new Integer(total));
					ci.write(out_filename);
				}
			}	
		}

	public static synchronized void setBranchClassName(String name) {
			try {
				((StatisticsBranchMetric) (metrics.get(Thread.currentThread().getId()).get("branch_metrics")))
						.setBranchClassName(name);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	
	public static synchronized void setBranchMethodName(String name) {
			try {
				((StatisticsBranchMetric) (metrics.get(Thread.currentThread().getId()).get("branch_metrics")))
						.setBranchMethodName(name);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	
	public static synchronized void setBranchPC(int pc) {
		try {
			((StatisticsBranchMetric) (metrics.get(Thread.currentThread().getId()).get("branch_metrics")))
					.setBranchPC(pc);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static synchronized void branchInit(int n) {
		try {
			((StatisticsBranchMetric) (metrics.get(Thread.currentThread().getId()).get("branch_metrics")))
					.branchInit(n);

		} catch (Exception e) {
			e.printStackTrace();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	public static synchronized void updateBranchNumber(int n) {
		try {
			((StatisticsBranchMetric) (metrics.get(Thread.currentThread().getId()).get("branch_metrics")))
					.updateBranchNumber(n);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static synchronized void updateBranchOutcome(int br_outcome) {
		try {
			((StatisticsBranchMetric) (metrics.get(Thread.currentThread().getId()).get("branch_metrics")))
					.updateBranchOutcome(br_outcome);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String argv[]) 
		{
			// WebServer.outstream = System.out;
			try {
				WebServer2.outstream = new PrintStream(new FileOutputStream("samplefile.txt", true));
			} catch (Exception e) {
				//TODO: handle exception
			}


			if (argv.length < 2 || !argv[0].startsWith("-")) {
				printUsage();
			}

			else if (argv[0].equals("-dynamic")) {
				if (argv.length != 3) {
					printUsage();
				}
				
				try {
					File in_dir = new File(argv[1]);
					File out_dir = new File(argv[2]);
					method = "dynamic";
					if (in_dir.isDirectory() && out_dir.isDirectory()) {
						doDynamic(in_dir, out_dir);
					}
					else {
						printUsage();
					}
				}
				catch (NullPointerException e) {
					printUsage();
				}
			}
			else if (argv[0].equals("-alloc")) {
				if (argv.length != 3) {
					printUsage();
				}
				
				try {
					File in_dir = new File(argv[1]);
					File out_dir = new File(argv[2]);
					
					method = "alloc";
					if (in_dir.isDirectory() && out_dir.isDirectory()) {
						doAlloc(in_dir, out_dir);
					}
					else {
						printUsage();
					}
				}
				catch (NullPointerException e) {
					printUsage();
				}
			}
			else if (argv[0].equals("-load_store")) {
				if (argv.length != 3) {
					printUsage();
				}
				
				try {
					File in_dir = new File(argv[1]);
					File out_dir = new File(argv[2]);

					method = "load_store";
					if (in_dir.isDirectory() && out_dir.isDirectory()) {
						doLoadStore(in_dir, out_dir);
					}
					else {
						printUsage();
					}
				}
				catch (NullPointerException e) {
					printUsage();
				}
			}
			else if (argv[0].equals("-branch")) {
				if (argv.length != 3) {
					printUsage();
				}
				
				try {
					File in_dir = new File(argv[1]);
					File out_dir = new File(argv[2]);

					method = "branch";
					if (in_dir.isDirectory() && out_dir.isDirectory()) {
						doBranch(in_dir, out_dir);
					}
					else {
						printUsage();
					}
				}
				catch (NullPointerException e) {
					printUsage();
				}
			}
		}
		
		
	public static synchronized void init_thread(){
		// HashMap<String, Metric> varmap = new HashMap<String, Metric>();
		HashMap<String, HashMap<String, StatisticsInstr>> outside_varmap = new HashMap<String, HashMap<String, StatisticsInstr>>();
		// HashMap<String, Metric> varmap = new HashMap<String, Metric>();

		System.out.println("Initiating thread " + Thread.currentThread().getId());
		// varmap.put("dyn_instr_count", new Counter("dyn_instr_count"));
		// varmap.put("storecount", new Counter("storecount"));
		// varmap.put("dyn_bb_count", new Counter("dyn_bb_count"));

		// varmap.put("fieldloadcount", new Counter("fieldloadcount"));
		// varmap.put("storecount", new Counter("storecount"));

		// metrics.put(Thread.currentThread().getId(), varmap);

		// HashMap<String, HashMap<String, StatisticsInstr>> outside_varmap = new HashMap<String, HashMap<String, StatisticsInstr>>();
		// System.out.println("Initiating thread " + Thread.currentThread().getId());

		// // varmap.put("dyn_method_count", new Counter("dyn_method_count"));
		// // varmap.put("dyn_instr_count", new Counter("dyn_instr_count"));
		// // varmap.put("dyn_bb_count", new Counter("dyn_bb_count"));

		
		// // varmap.put("newcount", new Counter("newcount"));
		// // varmap.put("newarraycount", new Counter("newarraycount"));
		// // varmap.put("anewarraycount", new Counter("anewarraycount"));
		// // varmap.put("multianewarraycount", new Counter("multianewarraycount"));
		
		// // varmap.put("fieldloadcount", new Counter("fieldloadcount"));
		// // varmap.put("fieldstorecount", new Counter("fieldstorecount"));
		// // varmap.put("loadcount", new Counter("loadcount"));
		// // varmap.put("storecount", new Counter("storecount"));
		// // varmap.put("branch_metrics", new StatisticsBranchMetric());

		// varmap.put("dyn_instr_count", new Counter("dyn_instr_count"));
		// varmap.put("storecount", new Counter("storecount"));

		// metrics.put(Thread.currentThread().getId(), varmap);

		context_info.put(Thread.currentThread().getId(), new InstrContextInfo());
		// // System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
		// // System.out.println(context_info.get(Thread.currentThread().getId()));
		instructions.put(Thread.currentThread().getId(), outside_varmap);
		
	}

}
