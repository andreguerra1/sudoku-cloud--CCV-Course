# Sudoku@Cloud - Checkpoint

André Guerra
Pedro Custódio
Pedro Bernardo


## Resources needed
- BIT (http://grupos.ist.utl.pt/meic-cnv/labs/labs-bit/BIT.zip)
- path-to-BIT in the CLASSPATH 
- path to aws-java-sdk in the CLASSPATH
- path-to-cnv-sudoku in the CLASSPATH
- java11 for running the AutoScaler and LoadBalancer
- java11 for running the WebServer and Solver code

## Code structure and Server modificaitons
####  Project structure
```
.
├── cnv-sudoku
│   ├── pt
│   │   └── ulisboa
│   │       └── tecnico
│   │           └── cnv
│   │               ├── instrument  # instrumentation related code
│   │               │   ├── Counter.java                    # a type of metric that can be incremented
│   │               │   ├── ICount.java                     # primitive instrumentation class
│   │               │   ├── Metric.java                     # represents a metric
│   │               │   ├── StatisticsBranchMetric.java     # Metric subclass used for getting branch metrics
│   │               │   ├── StatisticsInstr.java            # class used to obtain metrics on number of instructions executed
│   │               │   ├── Statistics.java                 # Final instrumentation class
│   │               │   └── StatisticsTool.java             # class used to obtain metrics for analysis
│   │               ├── it  # infraestructure related code
│   │               │   ├── AutoScaler.java         # Auto Scaler code
│   │               │   ├── Estimative.java         # Class that can be trained and estimate puzzle cost
│   │               │   ├── Buckets.java            # used by pt.tecnico.ulisboa.cnv.it.Estimative
│   │               │   ├── ITArgumentParser.java   # Used for argument parsing in AutoScaler-LoadBalancer comms.
│   │               │   ├── LoadBalancer.java       # Load Balancer code
│   │               │   ├── MSS.java                # Wrapper class for interacting with the the Metrics Storage System
│   │               │   └── Puzzle.java             # Helper class to represent puzzles
│   │               ├── server
│   │               │   └── WebServer.java
│   │               ├── solver                                  # instrumented solver code
│   │               └── util
├── metrics                 # some of the collected metrics for analysis
└── utils
    ├── get_metrics.py      # python script that generates the puzzles and makes requests to a specific server
    ├── get_ratios.py       # python utility to calculate the metrics ratios based on collected metrics
    ├── parse_metrics.py    # python utility that parses locally collected metrics for analysis
    └── setup_cp.sh         # misc script to help setup the environment
```

### Instrumentation
All instrumentation related classes and structures belong to the package `pt.ulisboa.tecnico.cnv.instrumentation`.  

The main instrumentation class used is `pt.ulisboa.tecnico.cnv.instrumentation.Statistics`. It holds a structure of the following type: 

```java
public static HashMap<Long, HashMap<String, Metric>>
```

This structures maps each running thread (identified by their `thread id`) to another HashMap that keeps pairs <"metric name", Metric> so that each metric can be read or updated concurrently at every point in time.

## WebServer
To keep information about the request each thread is processing, so we keep a Map in the `pt.ulisboa.tecnico.cnv.instrumentation.WebServer` that keeps pairs corresponding to `<thread_id, request parameters>`.    
When a request arrives, a metric object is created in the MSS, with a unique key. A background thread on the webserver will update this entry in the MSS periodically and when the request finishes the main thread will update it one last time, before releasing the resources used by it on the WebServer.

## Metrics
The class `pt.ulisboa.tecnico.cnv.it.MSS` is a wrapper for the main actions regarding the MSS, such as updating, adding and deleting entries, as well as scanning for the necessary entries, like the running puzzles for example.

## LoadBalancer
The `pt.ulisboa.tecnico.cnv.it.LoadBalancer` class handles request distribution by using a mixture of metrics provided *a priori* with metrics collected in runtime from the `MSS` for making request cost predictions.   
It is also able to detect server failure and replay these requests. For this, we need to keep a collection of pending requests associated with each server, which we can then interrupt and restart on demand.

## AutoScaler
The `pt.ulisboa.tecnico.cnv.it.AutoScaler` keeps track of the running servers and their load and aims to keep a programmer specified number of "available instances" through launching and removing instances. It is also able to detect server failure and it relaunches such instances.

## System Configuration
Each component runs on its own EC2 instance. We chose to implement the AutoScaler and LoadBalancer in `Java 11` since we wanted to use some of its features, so we installed it in our AMI's.

The first class to be run is the `AutoScaler` which launches instances up to a programmer defined constant that represents the desired "available" instances at any moment.  

The `LoadBalancer` is the second to run, and it has no connection to the AutoScaler until we specify it. The `LoadBalancer` provides an endpoint `/register` that takes the `AutoScaler` address and port in its fields and registers the `LoadBalancer` in this specified address.


- LoadBalancer port: 8010
- AutoScaler port: 8020
- WebServer port: 8000