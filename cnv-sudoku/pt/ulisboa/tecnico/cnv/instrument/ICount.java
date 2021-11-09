	/* ICount.java
 * Sample program using BIT -- counts the number of instructions executed.
 *
 * Copyright (c) 1997, The Regents of the University of Colorado. All
 * Rights Reserved.
 * 
 * Permission to use and copy this software and its documentation for
 * NON-COMMERCIAL purposes and without fee is hereby granted provided
 * that this copyright notice appears in all copies. If you wish to use
 * or wish to have others use BIT for commercial purposes please contact,
 * Stephen V. O'Neil, Director, Office of Technology Transfer at the
 * University of Colorado at Boulder (303) 492-5647.
 */
package pt.ulisboa.tecnico.cnv.instrument;

import BIT.highBIT.*;
import java.io.*;
import java.util.*;

public class ICount {
    private static PrintStream out = null;
    private static int i_count = 0, b_count = 0, m_count = 0;
    private static HashMap<Long, Metric> metrics = new HashMap<Long, Metric>();
    
    /* main reads in all the files class files present in the input directory,
     * instruments them, and outputs them to the specified output directory.
     */
    public static void main(String argv[]) {
        File file_in = new File(argv[0]);
        String infilenames[] = file_in.list();

        System.out.println(infilenames);

        for (int i = 0; i < infilenames.length; i++) {
            String infilename = infilenames[i];
            if (infilename.endsWith(".class")) {
				// create class info object
				ClassInfo ci = new ClassInfo(argv[0] + System.getProperty("file.separator") + infilename);
                // loop through all the routines
                // see java.util.Enumeration for more information on Enumeration class
                for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
                    Routine routine = (Routine) e.nextElement();
                    // // System.out.println(ci.getClassName());
                    if (ci.getClassName().equals("pt/ulisboa/tecnico/cnv/solver/Solver") && routine.getMethodName().equals("solveSudoku")){
                        // routine.addAfter("ICount", "mcount", new Integer(1));
                        System.out.println(ci.getClassName() + ": " + routine.getMethodName());
                        routine.addBefore("pt/ulisboa/tecnico/cnv/instrument/ICount", "init_thread", ci.getClassName());
                        routine.addAfter("pt/ulisboa/tecnico/cnv/instrument/ICount", "log_results", ci.getClassName());
                        // System.out.println(b.nextElement());
                        ci.write(argv[1] + System.getProperty("file.separator") + infilename);    
                    }
                    if (ci.getClassName().equals("pt/ulisboa/tecnico/cnv/solver/SudokuSolverBFS") && !routine.getMethodName().equals("<init>")){
                        System.out.println(ci.getClassName() + ": " + routine.getMethodName());
                        
                        for (Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements(); ) {
                            BasicBlock bb = (BasicBlock) b.nextElement();
                            bb.addBefore("pt/ulisboa/tecnico/cnv/instrument/ICount", "count", new Integer(bb.size()));
                        }
                        ci.write(argv[1] + System.getProperty("file.separator") + infilename);
                    }
                    if (ci.getClassName().equals("pt/ulisboa/tecnico/cnv/solver/SudokuSolverCP") && !routine.getMethodName().equals("<init>")){

                        System.out.println(ci.getClassName() + ": " + routine.getMethodName());
                        
                        for (Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements(); ) {
                            BasicBlock bb = (BasicBlock) b.nextElement();
                            bb.addBefore("pt/ulisboa/tecnico/cnv/instrument/ICount", "count", new Integer(bb.size()));
                        }
                        ci.write(argv[1] + System.getProperty("file.separator") + infilename);
                    }
                    if (ci.getClassName().equals("pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver") && !routine.getMethodName().equals("<init>")){

                        System.out.println(ci.getClassName() + ": " + routine.getMethodName());
                        for (Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements(); ) {
                            BasicBlock bb = (BasicBlock) b.nextElement();
                            bb.addBefore("pt/ulisboa/tecnico/cnv/instrument/ICount", "count", new Integer(bb.size()));
                        }
                        ci.write(argv[1] + System.getProperty("file.separator") + infilename);
                    }
                    if (ci.getClassName().equals("pt/ulisboa/tecnico/cnv/solver/SolverMain") && routine.getMethodName().equals("main")){
                        System.out.println(ci.getClassName() + ": " + routine.getMethodName());
                        // ci.addAfter("pt.ulisboa.tecnico.cnv.instrument.ICount", "printICount", ci.getClassName());
                        ci.write(argv[1] + System.getProperty("file.separator") + infilename);
                    }
                }
            }
        }
    }
    
    public static synchronized void init_thread(String foo){
        System.out.println("Initiating thread " + Thread.currentThread().getId());
        metrics.put(Thread.currentThread().getId(), new Counter("Nr of instructions"));
    }

    public static synchronized void log_results(String foo) {
        // System.out.println(Thread.currentThread().getId());
        // for (Metric m : metrics.get(Thread.currentThread().getId())){
        //     m.log(System.out);
        // }

        metrics.get(Thread.currentThread().getId()).log(System.out);
        metrics.remove(Thread.currentThread().getId());
    }
    

    public static void count(int incr) {
        // for (Metric m : metrics.get(Thread.currentThread().getId())){
            // if (m instanceof Counter){
        try{
            metrics.get(Thread.currentThread().getId()).update((long)incr);

        } catch (Exception e){
            e.printStackTrace();
        }
    }
}

