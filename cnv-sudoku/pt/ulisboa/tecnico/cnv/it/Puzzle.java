package pt.ulisboa.tecnico.cnv.it;


import java.util.HashMap;


public class Puzzle{

    private String label;
    //private String id;
    private long methodCount;
    private long fieldLoad;
    private long instructions;
    private String ts;
    private String strat;
    private long un;
    private String n;
    private long id;

    private final HashMap<String, HashMap<String, Double>> ratio;

    public Puzzle(String label, Long methodCount, Long fieldLoad, String strat, String n, Long un, String ts, Long id){
        
        this.ratio = new HashMap<String, HashMap<String, Double>>();
        this.instructions = 0;
        this.methodCount = methodCount.longValue();
        this.fieldLoad = fieldLoad.longValue();
        this.strat = strat;
        this.label = label;
        this.ts = ts;
        this.un = un;
        this.n = n;
        this.id = id;
        ratio.put("BFS", new HashMap<String, Double>());
        ratio.put("DLX", new HashMap<String, Double>());
        ratio.put("CP" , new HashMap<String, Double>());
        // ratio.get("BFS").put("fl", new Double(94264.63022841015));
        ratio.get("BFS").put("me", 372504.34643259406);
        // ratio.get("DLX").put("fl", 4767.501920144166);
        ratio.get("DLX").put("me", 149296.08805308148);
        ratio.get("CP") .put("fl", 67249.82028326987);
        ratio.get("CP") .put("me", 426229.3158645568);
    }


    public String getLabel(){
        return label;
    }
 
    public long getInstr(){
        if(this.instructions != 0){
            return this.instructions;
        } 
        if(this.strat.equals("CP")){
            this.instructions = (long)(ratio.get(this.strat).get("fl").doubleValue()*this.fieldLoad + ratio.get(this.strat).get("me").doubleValue()*this.methodCount);
        } else{
            this.instructions = (long)(ratio.get(this.strat).get("me").doubleValue()*this.methodCount);
        }

        return this.instructions;
    }

    public long getUn(){
        return un;
    }
    
    public String getN(){
        return n;
    }

    public String getStrat(){
        return strat;
    }

    public long getId(){
        return id;
    }

    public long getMethodCount(){
        return methodCount;
    }

    public long getFieldLoad(){
        return fieldLoad;
    }

    public String getTimestamp(){
        return ts;
    }

    // public 

    @Override
    public String toString(){

        String s = "Label : " + this.label + "\n";
        s += "ID : " + this.id + "\n";
        s += "Instr : " + this.getInstr() + "\n";
        s += "\n";

        return s;
    }
}