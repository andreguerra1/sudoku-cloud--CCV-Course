package pt.ulisboa.tecnico.cnv.it;

import java.util.TreeMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Arrays;
import java.util.Comparator;
import java.io.Serializable;

public class Buckets implements Serializable {

    TreeSet<Integer> bucket_intervals = new TreeSet<Integer>(Arrays.asList(625, 619, 617, 613, 607, 601, 599, 593, 587, 577, 575, 571, 569, 563, 557, 547, 541, 529, 523, 521, 509, 503, 499, 491, 487, 479, 467, 463, 461, 457, 449, 443, 439, 433, 439, 433, 431, 421, 419, 409, 401, 399, 397, 389, 383, 379, 373, 367, 361, 359, 353, 349, 347, 337, 331, 323, 317, 313, 311, 307, 293, 289, 283, 281, 277, 271, 269, 263, 257, 251, 241, 239, 233, 229, 227, 223, 211, 199, 197, 195, 193, 191, 181, 179, 173, 169, 167, 163, 157, 151, 149, 143, 139, 137, 131, 127, 121, 113, 109, 107, 103, 101, 97, 89, 83, 79, 73, 71, 67, 63, 61, 59, 53, 49, 47, 43, 41, 37, 35, 31, 29, 25, 23, 19, 17, 15, 13, 11, 9, 8, 7, 6, 5, 4, 3));

    private final TreeMap<Integer,Long> buckets = new TreeMap<Integer,Long>(
        (Comparator<Integer> & Serializable) (o1, o2) -> {
        return o1.compareTo(o2);
    }); //to save the average
    private final TreeMap<Integer,Integer> counter = new TreeMap<Integer,Integer>(
        (Comparator<Integer> & Serializable) (o1, o2) -> {
        return o1.compareTo(o2);
    }
    ); //to know how many

    public Buckets(){
    }

    public synchronized void AtomicIncrement(int key, long value){ //to keep the reading consistent
        Long bucket_value = buckets.get(key);
        int counter_value = counter.get(key);

        Long new_bucket_value = ( bucket_value * counter_value + value )/( counter_value + 1 );

        counter.put(key, counter_value + 1);
        buckets.put(key,new_bucket_value);
    }

    public void train(int un, long intr){
        int bucket_key = bucket_intervals.floor(un);

        if(buckets.isEmpty()){ //no race condition because there is only one thread writing to here
            counter.put(bucket_key, 1);
            buckets.put(bucket_key,intr);
        }else{
            AtomicIncrement(bucket_key,intr);
        }
    }

    public long predict(int un){

        if(buckets.size() == 0){
            return 0;
        }

        int bucket_key = bucket_intervals.floor(un);
        Long bucket_value = buckets.get(bucket_key);

        if(bucket_value != null){
            return bucket_value;
        }else{
            return getAdjacentPredict(bucket_key);
        }
    }

    public long getAdjacentPredict(int key){

        Map.Entry<Integer, Long> before_entry = buckets.higherEntry(key);

        Map.Entry<Integer, Long> next_entry = buckets.floorEntry(key);
        long res = 0;

        if(before_entry !=null)
            res += before_entry.getValue();
        if(next_entry !=null)
            res += next_entry.getValue();

        if(before_entry !=null && next_entry !=null)
            res = res/2;
        return res;
    }


}