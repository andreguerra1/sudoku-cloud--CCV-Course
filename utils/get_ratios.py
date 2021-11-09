import json, sys
import matplotlib.pyplot as plt
import pandas as pd

instr = { 
    'BFS': {
    '9':  279576131.8,
    '16': 1682528702,
    '25': 3868758370
    },
    'DLX': { 
    '9':  552853420,
    '16': 1822687184,
    '25': 5315604197
    },
    'CP': {
    '9':  242926600,
    '16': 1542481296,
    '25': 4074283306
    }
}

me = { 
    'BFS': { '9':  0, '16': 0, '25': 0 },
    'DLX': { '9':  0, '16': 0, '25': 0 },
    'CP':  { '9':  0, '16': 0, '25': 0 }
}

fl = { 
    'BFS': { '9':  0, '16': 0, '25': 0 },
    'DLX': { '9':  0, '16': 0, '25': 0 },
    'CP':  { '9':  0, '16': 0, '25': 0 }
}

counters = {
    'BFS': {'9': 0, '16': 0, '25': 0},
    'DLX': {'9': 0, '16': 0, '25': 0},
    'CP':  {'9': 0, '16': 0, '25': 0}
}

ratio = {
    'BFS': {'fl': 0, 'me': 0, 'occ': 0 },
    'DLX': {'fl': 0, 'me': 0, 'occ': 0 },
    'CP':  {'fl': 0, 'me': 0, 'occ': 0 }
}

strategies = ['BFS', 'CP', 'DLX']
sizes =      ['9', '16', '25']

def main(path):
    with open(path, "r") as f:
        metrics = f.read()

    # dic = json.loads(metrics)
    dic = [ json.loads("%s}" % (l)) for l in metrics.split('}')[:-1]]
    
    # print(dic[-1]['N1'])
    max_only = [ f for f in filter(lambda x: int(x['Un']) == (int(x['N1'])**2), dic)]

    for e in max_only:
        me[e['strat']][e['N1']] += int(e['dyn_method_count'])
        fl[e['strat']][e['N1']] += int(e['fieldloadcount'])
        counters[e['strat']][e['N1']] += 1

    for strat in strategies:
        for sz in sizes:
            me[strat][sz] = (me[strat][sz]*1.0) / counters[strat][sz]
            fl[strat][sz] = (fl[strat][sz]*1.0) / counters[strat][sz]

    print("AVERAGES, ")
    print(json.dumps(fl, indent=2))
    print(json.dumps(me, indent=2))
    print(json.dumps(instr, indent=2))

    print("RATIOS, ")
    for strat in strategies:
        for sz in sizes:
            ratio[strat]['fl'] += ((instr[strat][sz]*1.0)/fl[strat][sz])*counters[strat][sz]
            ratio[strat]['me'] += ((instr[strat][sz]*1.0)/me[strat][sz])*counters[strat][sz]
            ratio[strat]['occ'] += counters[strat][sz]

        ratio[strat]['fl'] /= ratio[strat]['occ']
        ratio[strat]['me'] /= ratio[strat]['occ']

    print(json.dumps(ratio, indent=2))

    pred_fl = { 'BFS': [], 'DLX': [], 'CP': [] }
    pred_me = { 'BFS': [], 'DLX': [], 'CP': [] }
    real    = { 'BFS': [], 'DLX': [], 'CP': [] }
    avg     = { 'BFS': [], 'DLX': [], 'CP': [] }
    sum_    = { 'BFS': [], 'DLX': [], 'CP': [] }
    
    for strat in strategies:
        for sz in sizes:
            pred_fl[strat] += [ratio[strat]['fl'] * fl[strat][sz]]
            pred_me[strat] += [ratio[strat]['me'] * me[strat][sz]]
            avg[strat]     += [((pred_me[strat][-1] + pred_fl[strat][-1])*1.0)/2]
            sum_[strat]    += [pred_fl[strat][-1] + (pred_fl[strat][-1] - pred_me[strat][-1])]
            real[strat]    += [instr[strat][sz]]
           
    

    plt.title('Prediction for BFS')
    plt.ylabel('instr')
    plt.xlabel('size')

    df = pd.DataFrame({'size': sizes, 'mtd': pred_me['BFS'], 'fl': pred_fl['BFS'], 'avg': avg['BFS'], 'real': real['BFS'], 'sum': sum_['BFS']})
    plt.plot('size', 'real', data=df, marker='o', color='skyblue')
    plt.plot('size', 'fl',   data=df, marker='s', color='red')
    plt.plot('size', 'mtd',  data=df, marker='^', color='yellow')
    plt.plot('size', 'avg',  data=df, marker='x', color='green')
    plt.plot('size', 'sum',  data=df, marker='x', color='orange')
    
    plt.legend()
    plt.savefig("pred_bfs.png")
    plt.close()

    plt.title('Prediction for DLX')
    plt.ylabel('instr')
    plt.xlabel('size')
    df = pd.DataFrame({'size': sizes, 'mtd': pred_me['DLX'], 'fl': pred_fl['DLX'], 'avg': avg['DLX'], 'real': real['DLX'], 'sum': sum_['DLX']})
    plt.plot('size', 'real', data=df, marker='o', color='skyblue')
    plt.plot('size', 'fl',   data=df, marker='s', color='red')
    plt.plot('size', 'mtd',  data=df, marker='^', color='yellow')
    plt.plot('size', 'avg',  data=df, marker='x', color='green')
    plt.plot('size', 'sum',  data=df, marker='x', color='orange')

    plt.legend()
    plt.savefig("pred_dlx.png")
    plt.close()
    
    plt.title('Prediction for CP')
    plt.ylabel('instr')
    plt.xlabel('size')
    df = pd.DataFrame({'size': sizes, 'mtd': pred_me['CP'], 'fl': pred_fl['CP'], 'avg': avg['CP'], 'real': real['CP'], 'sum': sum_['CP']})
    plt.plot('size', 'real', data=df, marker='o', color='skyblue')
    plt.plot('size', 'fl',   data=df, marker='s', color='red')
    plt.plot('size', 'mtd',  data=df, marker='^', color='yellow')
    plt.plot('size', 'avg',  data=df, marker='x', color='green')
    plt.plot('size', 'sum',  data=df, marker='x', color='orange')

    plt.legend()
    plt.savefig("pred_cp.png")
    plt.close()



if __name__ == '__main__':
    main(sys.argv[1])