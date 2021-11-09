package pt.ulisboa.tecnico.cnv.it;


import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Comparator;
import java.io.Serializable;


public class Estimative implements Serializable {

                        //BFS            n  bucket
    private final HashMap<String,TreeMap<Integer,Buckets>> info = new HashMap<String,TreeMap<Integer,Buckets>>();

    public void train(String type, int n, int un, long instr){
        TreeMap<Integer,Buckets> strat = info.get(type);

        if(strat == null){
            strat = new TreeMap<Integer,Buckets>(
                (Comparator<Integer> & Serializable) (o1, o2) -> {
                    return o1.compareTo(o2);
                 }
            );
            info.put(type, strat);
        }
        Buckets buckets = strat.get(n);

        if(buckets == null){
            buckets = new Buckets();
            strat.put(n,buckets);
        }
        buckets.train(un,instr);
            
    }
    
    public long predict(String type, int n, int un){
        TreeMap<Integer,Buckets> strat = info.get(type);

        if(strat == null) //no other puzzle was log for this strategy
            return 0;
        
        Buckets buckets = strat.get(n);
        if(buckets == null)
            return getAdjacentPredict(strat,n,un); //FIXME: does this makes sense?

        long res = buckets.predict(un);
        if(res == 0) //no value found
             return getAdjacentPredict(strat,n,un);
        return res;
    }

    private long getAdjacentPredict(TreeMap<Integer,Buckets> strat, int n, int un){

        Map.Entry<Integer, Buckets> before_entry = strat.higherEntry(n);

        Map.Entry<Integer, Buckets> next_entry = strat.floorEntry(n);
        long res = 0;

        if(before_entry !=null)
            res += before_entry.getValue().predict(un);
        if(next_entry !=null)
            res += next_entry.getValue().predict(un);

        if(before_entry !=null && next_entry !=null)
            res = res/2;
        return res;
    }
}