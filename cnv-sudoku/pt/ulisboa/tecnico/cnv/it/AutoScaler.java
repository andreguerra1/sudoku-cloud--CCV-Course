package pt.ulisboa.tecnico.cnv.it;

import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;


import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URL;
import java.net.URI;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.Map;
import java.util.LinkedList;
import java.io.*;

public class AutoScaler {

    // temporary value. Maybe recieve in argv
    private static String LB_ADDRESS = "localhost";
    private static int LB_PORT = 8010;
    // =========================================
    static AmazonEC2 ec2;
    static Estimative estimative_model;
    static ArrayList<Instance> activeInstances = new ArrayList<Instance>();
    static ArrayList<Instance> pendingInstances = new ArrayList<Instance>();
    static ArrayList<String> activeInstancesDNS = new ArrayList<String>();
    static HashMap<Instance, Long> serverLoad = new HashMap<Instance, Long>();
    static HashMap<String,HashMap<Long,Puzzle>> activePuzzles;
    static final int  MAX_AVAILABLE = 1;
    static final long INSTRUCTIONS_P_UNIT = 10746552;
    static final long GRACE_PERIOD = 80000;
    static final long MIN_THRESHOLD = (GRACE_PERIOD/1000)*INSTRUCTIONS_P_UNIT;
    static final long MAX_THRESHOLD = 60*INSTRUCTIONS_P_UNIT;
    static Runnable activate;
    

    private static void init() throws Exception {

        AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "location (~/.aws/credentials), and is in valid format.",
                    e);
        }
        ec2 = AmazonEC2ClientBuilder.standard().withRegion("us-east-1").withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
        
        final HttpServer server = HttpServer.create(new InetSocketAddress(8020), 0);
        server.createContext("/activeServers", new ActiveServers());
        server.createContext("/register", new RegisterLB());

        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println(server.getAddress().toString());

        activate = new Runnable() {
			public synchronized void run() {
                try{
                    Thread.sleep(GRACE_PERIOD);
                    activeInstances.addAll(pendingInstances);
                    // percorrer instancias no pending 
                    for (Instance i : pendingInstances){
                        activeInstancesDNS.add(i.getPrivateDnsName());
                    }
                    for(Instance instance : pendingInstances)
                        informLB("add",instance.getPrivateDnsName());
                    pendingInstances.clear();
                } catch(Exception e){
                    e.printStackTrace();
                }
			}
        };
    }

    public static boolean healthCheck(String server) {

            HttpResponse<InputStream> response = null;
            try{

                URI uri = new URI("http://" + server + ":8000/status");

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .timeout(Duration.ofSeconds(5))
                .build();
                /* Enviar para o servidor */
                response = client.send(request,HttpResponse.BodyHandlers.ofInputStream());

            }
            catch(Exception e){
                System.out.println("HEALTHCHECK FAIL");
                // e.printStackTrace();
                return false;
            }

            if(response.statusCode()==200)
                return true;
            
            return false;
    }

    public static void launchInstance(){

        System.out.println("Starting a new instance.");

        Instance instance = null;
        try{
        RunInstancesRequest runInstancesRequest = new RunInstancesRequest();
        runInstancesRequest.withImageId("ami-029258c7214469951")
        .withInstanceType("t2.micro")
        .withMinCount(1)
        .withMaxCount(1)
        .withKeyName("CNV-TP")
        .withSecurityGroups("CNV-ssh+http");

        RunInstancesResult runInstancesResult = ec2.runInstances(runInstancesRequest);
        instance = runInstancesResult.getReservation().getInstances().get(0);
        }
        catch (AmazonServiceException ase) {
            System.out.println("Caught Exception: " + ase.getMessage());
            System.out.println("Reponse Status Code: " + ase.getStatusCode());
            System.out.println("Error Code: " + ase.getErrorCode());
            System.out.println("Request ID: " + ase.getRequestId());
        }

        pendingInstances.add(instance);
        
    }

    public static void terminateInstance(Instance instance){

        System.out.println("Terminating an instance.");

        try{
        TerminateInstancesRequest termInstanceReq = new TerminateInstancesRequest();
        termInstanceReq.withInstanceIds(instance.getInstanceId());
        ec2.terminateInstances(termInstanceReq);
        }
        catch (AmazonServiceException ase) {
            System.out.println("Caught Exception: " + ase.getMessage());
            System.out.println("Reponse Status Code: " + ase.getStatusCode());
            System.out.println("Error Code: " + ase.getErrorCode());
            System.out.println("Request ID: " + ase.getRequestId());
        }

        // activeInstances.remove(instance);      

    }

    public static String getInstancePublicDnsName(String instanceId) {

        DescribeInstancesResult describeInstancesRequest = ec2.describeInstances();
        List<Reservation> reservations = describeInstancesRequest.getReservations();
        Set<Instance> allInstances = new HashSet<Instance>();
        for (Reservation reservation : reservations) {
        for (Instance instance : reservation.getInstances()) {
            if (instance.getInstanceId().equals(instanceId))
            return instance.getPublicDnsName();
        }
        }
        return null;
    }

    public static void getMetrics(){


        ZonedDateTime curr_timestamp = ZonedDateTime.now().minusSeconds(5);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");
        String ts = formatter.format(curr_timestamp);
        System.out.println(ts);

        activePuzzles = MSS.getRunningPuzzles();

        //System.out.println(activePuzzles);
        
    }

    public static void analyzeServers(){
        
        serverLoad = new HashMap<Instance,Long>();
        int running_servers = activeInstances.size();
        int loaded_servers = 0;
        LinkedList<Instance> empty_servers = new LinkedList<Instance>();
        ArrayList<Instance> toRemove = new ArrayList<Instance>();

        if(activeInstances.size() == 0 && pendingInstances.size() == 0){
            launchInstance();
            new Thread(activate).start();
            return;
        }


        for(Instance server: activeInstances){
            if(!healthCheck(server.getPrivateDnsName()) && !healthCheck(server.getPrivateDnsName())){ 
                toRemove.add(server);
                continue;
            }
            HashMap<Long,Puzzle> puzzles = activePuzzles.get(server.getPrivateDnsName());

            if(puzzles!=null)
                serverLoad.put(server, Long.valueOf(calculateServerLoad(puzzles)));
            else{
                empty_servers.add(server);
            }       
        }
        
        for(Instance server : toRemove){
            activeInstances.remove(server);
            activeInstancesDNS.remove(server.getPrivateDnsName());
        }

        // now we have the server loads
        for( Map.Entry<Instance,Long> srv : serverLoad.entrySet() ){
            if ( srv.getValue() > MAX_THRESHOLD )
                loaded_servers++;
        }
        running_servers = activeInstances.size();

        System.out.println("running: " + running_servers);
        System.out.println("loaded: " + loaded_servers);
        if(running_servers > 0 && loaded_servers == running_servers){
            // launch instance
            if(pendingInstances.size() == 0){
                launchInstance();
                new Thread(activate).start();
            }
        } else if(running_servers > 0 && (running_servers-loaded_servers) > MAX_AVAILABLE){
            System.out.println("termination test");
            int ctr = running_servers-loaded_servers;
            while(ctr > MAX_AVAILABLE && empty_servers.size() > 0){
                informLB("rm",empty_servers.peek().getPrivateDnsName());
                terminateInstance(empty_servers.peek());
                activeInstances.removeIf(x -> x.getInstanceId().equals(empty_servers.peek().getInstanceId()));
                ctr--;
                empty_servers.remove();
            }

        }
    }

    public static long calculateServerLoad(HashMap<Long,Puzzle> puzzles){
        
        long load = 0;

        for (Map.Entry<Long, Puzzle> puzz : puzzles.entrySet()){
            // 1. get instructions (through Puzzle.getInstr())
            long instr = puzz.getValue().getInstr();
            // 2. get prediction using estimative_model
            long pred = estimative_model.predict(puzz.getValue().getStrat(), Integer.parseInt(puzz.getValue().getN()), (int)puzz.getValue().getUn());
            // 3. add to total (if above threshold???)
            long remaining = pred - instr;
            if ( remaining > MIN_THRESHOLD ){
                load += remaining;
            }
        }

        return load;
    }

    public static void getEstimativeModel() throws Exception {
        URI uri = new URI("http://" + LB_ADDRESS + ":" + LB_PORT + "/estimative");

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
        .uri(uri)
        .GET()
        .build();
        /* Enviar para o servidor */
        HttpResponse<InputStream> response = null;

        response = client.send(request,HttpResponse.BodyHandlers.ofInputStream());

        InputStream in = response.body();
        ObjectInputStream objIn = new ObjectInputStream(in);
        Object obj = objIn.readObject();
        objIn.close();
        in.close();
        estimative_model = (Estimative) obj;

        // if (activeServers.size() != 0)
        //     System.out.println(activeServers.get(0));
    }

    public static void informLB(String action, String server) {

        System.out.println("Action : "+ action + "  Server : " + server + "\n");

        try{
        URI uri = new URI("http://" + LB_ADDRESS + ":" + LB_PORT + "/instanceUpdate?" + "act=" + action + "&srv=" + server);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
        .uri(uri)
        .GET()
        .build();
        /* Enviar para o servidor */
        HttpResponse<InputStream> response = null;
            response = client.send(request,HttpResponse.BodyHandlers.ofInputStream());

        if(response.statusCode() != 200) 
            System.err.println("Error updating servers");

        }
        catch(Exception e){e.printStackTrace();}

    }

    public static void main(String[] args) throws Exception {

        System.out.println("AutoScaler");
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                System.out.println("Shutting down ...");
                //some cleaning up code...
                for(Instance i : activeInstances){
                    terminateInstance(i);
                }

                for(Instance i : pendingInstances){
                    terminateInstance(i);
                }
            }
        });


        init();

        launchInstance();
        System.out.println("launched!");

        new Thread(activate).start();

        /////// TEST ///////
        MSS.init();

        while(true){
        //Timeout para avaliar scale up/down

        Thread.sleep(10000);

            getMetrics();
            try{
                getEstimativeModel();
            } catch(Exception e){
                System.err.println("LB NOT RESPONDING");
                e.printStackTrace();
            }
            analyzeServers();
        }


        // /* Nao e possivel saber logo o dns nem o ip ao iniciar a instancia mas acho que e pa usar o private dns/ip entre instancias*/
        // String dns_name = getInstancePublicDnsName(activeInstances.get(0).getInstanceId());
        // System.out.println("Instance Public DNS : " + dns_name);


        // //Terminar Instancia
        // terminateInstance(activeInstances.get(0));

    }


    static class ActiveServers implements HttpHandler {
    @Override
    public void handle(final HttpExchange t) throws IOException {

        System.out.println("A Enviar lista de servers");

        final Headers hdrs = t.getResponseHeaders();
        
        hdrs.add("Content-Type", "application/json");
        hdrs.add("Access-Control-Allow-Origin", "*");
        hdrs.add("Access-Control-Allow-Credentials", "true");
        hdrs.add("Access-Control-Allow-Methods", "POST, GET, HEAD, OPTIONS");
        hdrs.add("Access-Control-Allow-Headers", "Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers");

        t.sendResponseHeaders(200, 0);


        final OutputStream os = t.getResponseBody();
        ObjectOutputStream objOut = new ObjectOutputStream(os);
        objOut.writeObject(activeInstancesDNS);
        objOut.close();

        //os.close();
    }
    }

    static class RegisterLB implements HttpHandler {
    @Override
    public void handle(final HttpExchange t) throws IOException {

        System.out.println("Load Balancer register Handler");

        final String query = t.getRequestURI().getQuery();

        System.out.println(query);
        final String[] params = query.split("&");


        final ArrayList<String> newArgs = new ArrayList<>();
        for (final String p : params) {
            final String[] splitParam = p.split("=");
            newArgs.add("-" + splitParam[0]);
            newArgs.add(splitParam[1]);
        }

        // Store from ArrayList into regular String[].
        final String[] args = new String[newArgs.size()];
        int i = 0;
        for(String arg: newArgs) {
            args[i] = arg;
            i++;
        }

        final ITArgumentParser ap = new ITArgumentParser(args);
        AutoScaler.LB_ADDRESS = ap.getInputReg();

        System.out.println("A Enviar lista de servers");

        final Headers hdrs = t.getResponseHeaders();
        
        hdrs.add("Content-Type", "application/json");
        hdrs.add("Access-Control-Allow-Origin", "*");
        hdrs.add("Access-Control-Allow-Credentials", "true");
        hdrs.add("Access-Control-Allow-Methods", "POST, GET, HEAD, OPTIONS");
        hdrs.add("Access-Control-Allow-Headers", "Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers");

        t.sendResponseHeaders(200, 0);


        final OutputStream os = t.getResponseBody();
        ObjectOutputStream objOut = new ObjectOutputStream(os);
        objOut.writeObject(activeInstancesDNS);
        objOut.close();

        

        //os.close();
    }
    }
}




