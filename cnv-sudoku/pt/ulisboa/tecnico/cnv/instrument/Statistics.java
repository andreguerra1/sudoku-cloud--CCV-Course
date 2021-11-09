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
import pt.ulisboa.tecnico.cnv.server.WebServer;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Arrays;

public class Statistics
{	
	public static HashMap<Long, HashMap<String, Metric>> metrics = new HashMap<Long, HashMap<String, Metric>>();
	public static ArrayList<String> methods_list = new ArrayList<String>(Arrays.asList("pt/ulisboa/tecnico/cnv/solver/Solver|run", "pt/ulisboa/tecnico/cnv/solver/SudokuSolverBFS|rowCheck", "pt/ulisboa/tecnico/cnv/solver/SudokuSolverBFS|setNum", "pt/ulisboa/tecnico/cnv/solver/SudokuSolverBFS|boxCheck", "pt/ulisboa/tecnico/cnv/solver/SudokuSolverBFS|colCheck", "pt/ulisboa/tecnico/cnv/solver/SudokuSolverBFS|runSolver", "pt/ulisboa/tecnico/cnv/solver/SolverArgumentParser|getN1", "pt/ulisboa/tecnico/cnv/solver/SolverArgumentParser|getN2", "pt/ulisboa/tecnico/cnv/solver/AbstractSudokuSolver|printFixedWidth", "pt/ulisboa/tecnico/cnv/solver/SolverFactory$SolverType|values", "pt/ulisboa/tecnico/cnv/solver/SolverArgumentParser$SolverParameters|toString", "pt/ulisboa/tecnico/cnv/solver/SudokuSolverCP|followsConstraints", "pt/ulisboa/tecnico/cnv/solver/SudokuSolverCP|runSolver", "pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver$ColumnNode|<init>", "pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver$Node|<init>", "pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver$ColumnID|<init>", "pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver|cover", "pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver|search", "pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver|uncover", "pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver|choose", "pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver|filled", "pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver|mapSolvedToGrid"));

	public static void printUsage() 
		{
			System.out.println("Syntax: java StatisticsTool in_path [out_path]");
			System.out.println("        in_path:  directory from which the class files are read");
			System.out.println("        out_path: directory to which the class files are written");
			System.out.println("        Both in_path and out_path are required unless stat_type is static");
			System.out.println("        in which case only in_path is required");
			System.exit(-1);
		}

	public static void doInstrumentation(File in_dir, File out_dir) 
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
							changed = true;
							System.out.println(ci.getClassName() + ": " + routine.getMethodName());
							routine.addBefore("pt/ulisboa/tecnico/cnv/instrument/Statistics", "incMetric", "dyn_method_count");

							for (Enumeration instrs = (routine.getInstructionArray()).elements(); instrs.hasMoreElements(); ) {
								Instruction instr = (Instruction) instrs.nextElement();
								int opcode=instr.getOpcode();
								short instr_type = InstructionTable.InstructionTypeTable[opcode];
								if (opcode == InstructionTable.getfield)
									instr.addBefore("pt/ulisboa/tecnico/cnv/instrument/Statistics", "incMetric", "fieldloadcount");
								// else if (instr_type == InstructionTable.STORE_INSTRUCTION) 
								// 	instr.addBefore("pt/ulisboa/tecnico/cnv/instrument/Statistics", "incMetric", "storecount");
								
							}
						}
					}
					if(changed)
						ci.write(out_filename);
				}
			}	
		}	
		
	public static synchronized void incMetric(String metric) 
		{
			try{
				((Counter)metrics.get(Thread.currentThread().getId()).get(metric))._count++;
			} catch (Exception e){
				e.printStackTrace();
			}
		}

	public static void main(String argv[]) 
		{
			// WebServer.outstream = System.out;
			try {
				WebServer.outstream = new PrintStream(new FileOutputStream("samplefile.txt", true));
			} catch (Exception e) {
				//TODO: handle exception
			}

			if (argv.length > 2) {
				printUsage();
			}

			try {
				File in_dir = new File(argv[0]);
				File out_dir = new File(argv[1]);
				if (in_dir.isDirectory() && out_dir.isDirectory()) {
					doInstrumentation(in_dir, out_dir);
				}
				else {
					printUsage();
				}
			}
			catch (NullPointerException e) {
				printUsage();
			}
		}
		
		
	public static synchronized void init_thread(){
		HashMap<String, Metric> varmap = new HashMap<String, Metric>();
		System.out.println("Initiating thread " + Thread.currentThread().getId());
		varmap.put("dyn_method_count", new Counter("dyn_method_count"));

		varmap.put("fieldloadcount", new Counter("fieldloadcount"));
		// varmap.put("storecount", new Counter("storecount"));

		metrics.put(Thread.currentThread().getId(), varmap);
	}	
}
