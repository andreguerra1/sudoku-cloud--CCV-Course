package pt.ulisboa.tecnico.cnv.instrument;

import java.io.*;
import java.util.*;

public class Counter implements Metric<Long> {
    public Long    _count;
    public String  _name;

    public Counter(){
        _count = new Long(0);
    }

    public Counter(String name){
        _count = new Long(0);
        _name = name;
    }

    @Override
    public void update(Long amount) {
        if(amount > 0)
            _count += amount;
    }

    @Override
    public void log(PrintStream out) {
        out.printf("%s: %d\n", _name, _count);
    }

    @Override 
    public Metric.Label getLabel() {
        return Metric.Label.ADD;
    }
}
