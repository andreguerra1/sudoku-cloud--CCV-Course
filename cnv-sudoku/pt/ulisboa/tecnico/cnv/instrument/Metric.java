package pt.ulisboa.tecnico.cnv.instrument;

import java.io.*;
import java.util.*;

public interface Metric<T> {
   enum Label {
      INCR,
      ADD
   }
   public void update(T object);
   public void log(PrintStream out);
   public Metric.Label getLabel();
}
