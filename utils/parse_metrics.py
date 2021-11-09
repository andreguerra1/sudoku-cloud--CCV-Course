# import sys, time, os
# import networkx as nx
# import matplotlib.pyplot as plt
# import pandas as pd
# import math
from collections import Counter


# ITERATIONS = 5
# OUTPUT_FILE = "out"
# EPSILON = 0.01
# DELTA = 0.1
# C = 0.5

# def main(main_dir, graph_dir):

    #plt.title('Average error by epsilon')
    #plt.ylabel('Error')
    #plt.xlabel('Epsilon')

    #df=pd.DataFrame({'Epsilon': sizes, 'Avg': average_error,'Avg + Stddev':average_stddev_error,'Max Error':average_max_error })
    #plt.scatter( 'Epsilon', 'Avg', data=df, marker='o', color='skyblue')
    #plt.scatter( 'Epsilon', 'Avg + Stddev', data=df, marker='s', color='red')
    #plt.scatter( 'Epsilon', 'Max Error', data=df, marker='^', color='gray')
    #plt.legend()

    #plt.savefig('average_error_epsilon.png')
    #plt.close()

    # plt.title('Running Time by epsilon')
    # plt.ylabel('Running Time')
    # plt.xlabel('Epsilon')
    # df=pd.DataFrame({'Epsilon': sizes, 'VC': timesVC,'BP': timesBP })
    # plt.plot( 'Epsilon', 'VC', data=df, marker='o', markerfacecolor='blue', markersize=12, color='skyblue', linewidth=4)
    # plt.plot( 'Epsilon', 'BP', data=df, marker='s', markerfacecolor='red', markersize=12, color='red', linewidth=4,linestyle='dashed')
    # plt.hlines(time_exact, sizes[0], sizes[-1], colors='b', linestyles='solid', label='Exact')
    # plt.legend()
    # plt.savefig('running_time_epsilon.png')
    # plt.close()

    # plt.title('Running Time by epsilon')
    # plt.ylabel('Running Time')
    # plt.xlabel('Epsilon')
    # df=pd.DataFrame({'Epsilon': sizes, 'VC': timesVC,'BP': timesBP })
    # plt.plot( 'Epsilon', 'VC', data=df, marker='o', markerfacecolor='blue', markersize=12, color='skyblue', linewidth=4)
    # plt.plot( 'Epsilon', 'BP', data=df, marker='s', markerfacecolor='red', markersize=12, color='red', linewidth=4,linestyle='dashed')
    # plt.legend()
    # plt.savefig('running_time_epsilon_wo_exact.png')


def title(strat, size):
    print("{}{}".format(strat, size))

def unique():
    functions = {}

    # bfs = ["pt/ulisboa/tecnico/cnv/solver/Solver|run"]
    bfs = ['pt/ulisboa/tecnico/cnv/solver/SolverArgumentParser$SolverParameters|toString', 'pt/ulisboa/tecnico/cnv/solver/SudokuSolverBFS|colCheck', 'pt/ulisboa/tecnico/cnv/solver/SudokuSolverBFS|setNum', 'pt/ulisboa/tecnico/cnv/solver/Solver|run', 'pt/ulisboa/tecnico/cnv/solver/AbstractSudokuSolver|printFixedWidth', 'pt/ulisboa/tecnico/cnv/solver/SudokuSolverBFS|rowCheck', 'pt/ulisboa/tecnico/cnv/solver/SudokuSolverBFS|boxCheck', 'pt/ulisboa/tecnico/cnv/solver/SudokuSolverBFS|runSolver', 'pt/ulisboa/tecnico/cnv/solver/SolverArgumentParser|getN2', 'pt/ulisboa/tecnico/cnv/solver/SolverArgumentParser|getN1', 'pt/ulisboa/tecnico/cnv/solver/SolverFactory$SolverType|values']

    # bfs = ['pt/ulisboa/tecnico/cnv/solver/Solver|run', 'pt/ulisboa/tecnico/cnv/solver/SolverFactory|getInstance', 'pt/ulisboa/tecnico/cnv/solver/SudokuSolverBFS|rowCheck', 'pt/ulisboa/tecnico/cnv/solver/SudokuSolverBFS|setNum', 'pt/ulisboa/tecnico/cnv/solver/SudokuSolverBFS|boxCheck', 'pt/ulisboa/tecnico/cnv/solver/SudokuSolverBFS|colCheck', 'pt/ulisboa/tecnico/cnv/solver/SudokuSolverBFS|runSolver', 'pt/ulisboa/tecnico/cnv/solver/SolverArgumentParser|getN1', 'pt/ulisboa/tecnico/cnv/solver/SolverArgumentParser|getN2', 'pt/ulisboa/tecnico/cnv/solver/AbstractSudokuSolver|printFixedWidth', 'pt/ulisboa/tecnico/cnv/solver/AbstractSudokuSolver|deepCloneArray', 'pt/ulisboa/tecnico/cnv/solver/AbstractSudokuSolver|print', 'pt/ulisboa/tecnico/cnv/solver/SolverFactory$SolverType|values', 'pt/ulisboa/tecnico/cnv/solver/SolverFactory$SolverType|valueOf', 'pt/ulisboa/tecnico/cnv/solver/SolverArgumentParser$SolverParameters|toString']
    # cp = ["pt/ulisboa/tecnico/cnv/solver/Solver|run"]
    # cp = ['pt/ulisboa/tecnico/cnv/solver/Solver|run', 'pt/ulisboa/tecnico/cnv/solver/SudokuSolverCP|followsConstraints', 'pt/ulisboa/tecnico/cnv/solver/SudokuSolverCP|runSolver', 'pt/ulisboa/tecnico/cnv/solver/SolverArgumentParser|getN1', 'pt/ulisboa/tecnico/cnv/solver/SolverArgumentParser|getN2', 'pt/ulisboa/tecnico/cnv/solver/AbstractSudokuSolver|printFixedWidth', 'pt/ulisboa/tecnico/cnv/solver/AbstractSudokuSolver|deepCloneArray', 'pt/ulisboa/tecnico/cnv/solver/AbstractSudokuSolver|print', 'pt/ulisboa/tecnico/cnv/solver/SolverArgumentParser$SolverParameters|toString']
    cp = ['pt/ulisboa/tecnico/cnv/solver/SudokuSolverCP|runSolver', 'pt/ulisboa/tecnico/cnv/solver/SolverArgumentParser$SolverParameters|toString', 'pt/ulisboa/tecnico/cnv/solver/Solver|run', 'pt/ulisboa/tecnico/cnv/solver/AbstractSudokuSolver|printFixedWidth', 'pt/ulisboa/tecnico/cnv/solver/SudokuSolverCP|followsConstraints', 'pt/ulisboa/tecnico/cnv/solver/SolverArgumentParser|getN2', 'pt/ulisboa/tecnico/cnv/solver/SolverArgumentParser|getN1']
    # dlx = ["pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver|filled", "pt/ulisboa/tecnico/cnv/solver/Solver|run"]
    # dlx = ['pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver$ColumnNode|<init>', 'pt/ulisboa/tecnico/cnv/solver/Solver|run', 'pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver$Node|<init>', 'pt/ulisboa/tecnico/cnv/solver/SolverArgumentParser|getN1', 'pt/ulisboa/tecnico/cnv/solver/SolverArgumentParser|getN2', 'pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver$ColumnID|<init>', 'pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver|cover', 'pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver|search', 'pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver|uncover', 'pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver|mapSolvedToGrid', 'pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver|choose', 'pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver|filled', 'pt/ulisboa/tecnico/cnv/solver/AbstractSudokuSolver|printFixedWidth', 'pt/ulisboa/tecnico/cnv/solver/AbstractSudokuSolver|deepCloneArray', 'pt/ulisboa/tecnico/cnv/solver/AbstractSudokuSolver|print', 'pt/ulisboa/tecnico/cnv/solver/SolverArgumentParser$SolverParameters|toString']
    dlx = ['pt/ulisboa/tecnico/cnv/solver/SolverArgumentParser$SolverParameters|toString', 'pt/ulisboa/tecnico/cnv/solver/Solver|run', 'pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver$ColumnNode|<init>', 'pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver$ColumnID|<init>', 'pt/ulisboa/tecnico/cnv/solver/AbstractSudokuSolver|printFixedWidth', 'pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver$Node|<init>', 'pt/ulisboa/tecnico/cnv/solver/SolverArgumentParser|getN2', 'pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver|search', 'pt/ulisboa/tecnico/cnv/solver/SolverArgumentParser|getN1', 'pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver|cover', 'pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver|choose', 'pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver|uncover', 'pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver|filled']

    for f in bfs:
        functions[f] = 0
    for f in cp:
        functions[f] = 0
    for f in dlx:
        functions[f] = 0

    l = list(functions.keys())
    print(len(l), l)


def percentages(strat, size, data):
    title(strat, size)
    methods_bb = {}
    methods_i = {}
    percentages_bb = {}
    percentages_i = {}
    
    for lg in data:
        lg = lg.split("\n")
        total_s = 0
        total_i = 0
        
        if strat not in lg[0] or size not in lg[0]:
            continue

        for entry in lg[1:]:
            if not entry:
                continue
            e = entry.split()
            if (e[0]+"|"+e[1]) not in methods_i:
                methods_i[e[0]+"|"+e[1]] = []
                methods_bb[e[0]+"|"+e[1]] = []
                percentages_i[e[0]+"|"+e[1]] = []
                percentages_bb[e[0]+"|"+e[1]] = []

            total_s += int(e[2])
            total_i += int(e[3])
            methods_bb[e[0]+"|"+e[1]] = int(e[2])
            methods_i[e[0]+"|"+e[1]] = int(e[3])
        
        for entry in lg[1:]:
            if not entry:
                continue
            e = entry.split()
            percentages_bb[e[0]+"|"+e[1]].append(((methods_bb[e[0]+"|"+e[1]]*1.0)/total_s)*100)
            percentages_i[e[0]+"|"+e[1]].append(((methods_i[e[0]+"|"+e[1]]*1.0)/total_i)*100)

    # for i in methods_bb.keys():
    #     count_bb = dict(Counter(methods_bb[i]))
    #     count_i = dict(Counter(methods_i[i]))
        # if len(count_bb.keys()) != 1:
        #     print("bb: ", i, len(count_bb.keys()))
        
        # if len(count_i.keys()) != 1:
        #     print("i: ", i, count_i.keys())

    # print "Total", total_i
    return percentages_i, percentages_bb

def count_distinct(strat, size, data):
    title(strat, size)
    methods_bb = {}
    methods_i = {}
    
    for lg in data:
        lg = lg.split("\n")
        total_s = 0
        total_i = 0
        
        if strat not in lg[0] or size not in lg[0]:
            continue

        for entry in lg[1:]:
            if not entry:
                continue
            e = entry.split()
            if (e[0]+"|"+e[1]) not in methods_i:
                methods_i[e[0]+"|"+e[1]] = []
                methods_bb[e[0]+"|"+e[1]] = []

            methods_bb[e[0]+"|"+e[1]] += [int(e[2])]
            methods_i[e[0]+"|"+e[1]] += [int(e[3])]
        
    # for i in methods_bb.keys():
    #     count_bb = dict(Counter(methods_bb[i]))
    #     count_i = dict(Counter(methods_i[i]))
        # if len(count_bb.keys()) != 1:
        #     print("bb: ", i, len(count_bb.keys()))
        
        # if len(count_i.keys()) != 1:
        #     print("i: ", i, count_i.keys())

    return methods_i

if __name__ == '__main__':
    with open("metrics_log.txt", "r") as f:
        data = f.read()

    data = data.split("@\n")
    
    # a1 = count_distinct("BFS", "-9-",data)
    # a2 = count_distinct("BFS", "-16-",data)
    # a3 = count_distinct("BFS", "-25-",data)
    # a1 = count_distinct("CP", "-9-",data)
    # a2 = count_distinct("CP", "-16-",data)
    # a3 = count_distinct("CP", "-25-",data)
    # a1 = count_distinct("DLX", "-9-",data)
    # a2 = count_distinct("DLX", "-16-",data)
    # a3 = count_distinct("DLX", "-25-",data)
    # a1, b1 = percentages("BFS", "-9-",data)
    # a2, b2 = percentages("BFS", "-16-",data)
    # a3, b3 = percentages("BFS", "-25-",data)
    # a1, b1 = percentages("CP", "-9-",data)
    # a2, b2 = percentages("CP", "-16-",data)
    # a3, b3 = percentages("CP", "-25-",data)
    a1, b1 = percentages("DLX", "-9-",data)
    a2, b2 = percentages("DLX", "-16-",data)
    a3, b3 = percentages("DLX", "-25-",data)
    # b1 = {}
    # b2 = {}
    # b3 = {}

    print(a1)
    print(a2)
    print(a3)
    # PARSE PERCENTAGES
    for i in a1.keys():
        # count_ = dict(Counter(a1[i]))
        for j in range(len(a1[i])):
            if a1[i][j] > 1 or b1[i][j] > 1:
                print "BFS", "-9-", i#, a1[i][j], b1[i][j]
                break
    
    for i in a2.keys():
        # count_ = dict(Counter(a1[i]))
        for j in range(len(a2[i])):
            if a2[i][j] > 1 or b2[i][j] > 1:
                print "BFS", "-16-", i#, a2[i][j], b2[i][j]
                break
    
    for i in a3.keys():
        # count_ = dict(Counter(a1[i]))
        for j in range(len(a3[i])):
            if a3[i][j] > 1 or b3[i][j] > 1:
                print "BFS", "-25-", i#, a3[i][j], b3[i][j]
                break


    # # PARSE UNIQUE VALUES
    # for i in a1.keys():            
    #     count_ = dict(Counter(a2[i]))
    #     # if len(count_.keys()):
    #     b1[i] = count_.keys()

    # for i in a2.keys():
    #     count_ = dict(Counter(a2[i]))
        
    #     # if len(count_.keys()) != 1:
    #     b2[i] = count_.keys()

    # for i in a3.keys():
    #     count_ = dict(Counter(a3[i]))
        
    #     # if len(count_.keys()) != 1:
    #     b3[i] = count_.keys()

    # # print(b1)
    # # print(b2)
    # # print(b3)

    # dynamic = []
    # constant = []

    # for i in b1.keys():
    #     if i in b1 and i in b2 and i in b3:
    #         # print(i)
    #         # print(b1[i])
    #         # print(b2[i])
    #         # print(b3[i])
    #         if b1[i] == b2[i] and b1[i] == b3[i]:
    #             # print("AAAAAA")
    #             constant.append(i)
    #         else: 
    #             dynamic.append(i)


    # print("============================================")
    # print(constant)
    # print(dynamic)

    



# ["pt/ulisboa/tecnico/cnv/solver/AbstractSudokuSolver|printFixedWidth","pt/ulisboa/tecnico/cnv/solver/SolverArgumentParser$SolverParameters|toString","pt/ulisboa/tecnico/cnv/solver/SolverArgumentParser|getN2","pt/ulisboa/tecnico/cnv/solver/SudokuSolverBFS|runSolver","pt/ulisboa/tecnico/cnv/solver/SudokuSolverBFS|rowCheck","pt/ulisboa/tecnico/cnv/solver/SudokuSolverBFS|colCheck","pt/ulisboa/tecnico/cnv/solver/SudokuSolverBFS|setNum","pt/ulisboa/tecnico/cnv/solver/Solver|run","pt/ulisboa/tecnico/cnv/solver/SudokuSolverBFS|boxCheck","pt/ulisboa/tecnico/cnv/solver/AbstractSudokuSolver|printFixedWidth","pt/ulisboa/tecnico/cnv/solver/SudokuSolverCP|runSolver","pt/ulisboa/tecnico/cnv/solver/SudokuSolverCP|followsConstraints","pt/ulisboa/tecnico/cnv/solver/SolverArgumentParser|getN2","pt/ulisboa/tecnico/cnv/solver/SolverArgumentParser$SolverParameters|toString","pt/ulisboa/tecnico/cnv/solver/SolverArgumentParser|getN1","pt/ulisboa/tecnico/cnv/solver/SolverFactory$SolverType|values","pt/ulisboa/tecnico/cnv/solver/Solver|run","pt/ulisboa/tecnico/cnv/solver/AbstractSudokuSolver|printFixedWidth","pt/ulisboa/tecnico/cnv/solver/SolverArgumentParser$SolverParameters|toString","pt/ulisboa/tecnico/cnv/solver/SolverArgumentParser|getN2","pt/ulisboa/tecnico/cnv/solver/SolverArgumentParser|getN1","pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver|cover","pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver$Node|<init>","pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver|filled","pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver|search","pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver$ColumnID|<init>","pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver|choose","pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver|uncover","pt/ulisboa/tecnico/cnv/solver/SolverFactory$SolverType|values","pt/ulisboa/tecnico/cnv/solver/Solver|run","pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver$ColumnNode|<init>"]


