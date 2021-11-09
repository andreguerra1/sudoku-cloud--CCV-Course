//package pt.ulisboa.tecnico.cnv.server;
package pt.ulisboa.tecnico.cnv.it;

import pt.ulisboa.tecnico.cnv.it.ITArgumentParser;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URL;
import java.net.URI;
import org.json.JSONArray;
import pt.ulisboa.tecnico.cnv.it.MSS;
import pt.ulisboa.tecnico.cnv.solver.Solver;
import pt.ulisboa.tecnico.cnv.solver.SolverArgumentParser;
import pt.ulisboa.tecnico.cnv.solver.SolverFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Base64;


import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.net.InetAddress;

public class LoadBalancer {

    // static ArrayList<String> activeServers;
    static String AUTOSCALER_ADDR = "";
    static Integer AUTOSCALER_PORT = 0;
    static ArrayList<String> activeServers;
    static private HashMap<String,HashMap<Long,Puzzle>> activePuzzles;
    static private Estimative estimative_model;
    static private HashMap<String,Long> loadServers;
    static private HashMap<String,Long> threadsServers;
    static private HashMap<Long,Long> estimativeByRequest;
    static private HashMap<String,ArrayList<HttpExchange>> activeExchanges;
    static private HashMap<String,ArrayList<Thread>> activeThreads;
    static private ConcurrentHashMap<String,byte[]> puzzleBoards;
    static ZonedDateTime last_timestamp;
    static private int concludedPuzzles = 0;
    static int THRESHOLD = 78240092;
    static int TIMEOUT = 10000;
    static int HEALTHCHECK_PERIOD = 10000;


	public static void main(final String[] args) throws Exception {

        // maybe error check??


        activeServers = new ArrayList<String>();
        activePuzzles = new HashMap<String,HashMap<Long,Puzzle>>();
        loadServers = new HashMap<String, Long>();
        threadsServers = new HashMap<String, Long>();
        estimativeByRequest = new HashMap<Long,Long>();
        activeExchanges = new HashMap<String,ArrayList<HttpExchange>>();
        activeThreads = new HashMap<String,ArrayList<Thread>>();
        puzzleBoards = new ConcurrentHashMap<String,byte[]>();
        estimative_model = new Estimative();
        
        MSS.init();

        last_timestamp = ZonedDateTime.now();
        final HttpServer server = HttpServer.create(new InetSocketAddress(8010), 0);
        server.createContext("/sudoku", new MyHandler());
        server.createContext("/instanceUpdate", new InstanceUpdateHandler());
        server.createContext("/estimative", new EstimativeHandler());
        server.createContext("/register", new RegisterInASHandler());
        
        // be aware! infinite pool of threads!
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();

        System.out.println(server.getAddress().toString());

        /*  TRAIN THE MODEL WITH OUR DEFAULT DATA */
        estimative_model.train("BFS",9,81,Long.valueOf("279987735").longValue());
        estimative_model.train("BFS",16,256,Long.valueOf("1087062997").longValue());
        estimative_model.train("BFS",25,625,Long.valueOf("3868758370").longValue());
        estimative_model.train("DLX",9,81,Long.valueOf("552853420").longValue());
        estimative_model.train("DLX",16,256,Long.valueOf("1822689644").longValue());
        estimative_model.train("DLX",25,625,Long.valueOf("5315604197").longValue());
        estimative_model.train("CP",9,81,Long.valueOf("242926600").longValue());
        estimative_model.train("CP",16,256,Long.valueOf("1317578258").longValue());
        estimative_model.train("CP",25,625,Long.valueOf("4074283306").longValue());        
        /*  #############################  */


        

        Runnable update = new Runnable() {
			public void run() {
				updateEstimative();        // UPDATE ESTIMATIVE
			}
		};
        new Thread(update).start();


        Runnable health_check = new Runnable() {
			public void run() {
				checkServers();        // HEALTH CHECK ON SERVERS
			}
		};
        new Thread(health_check).start();



        //FETCH DATA IN MSS
        while(true){
            Thread.sleep(TIMEOUT);
            //UPDATE ACITVE PUZZLES
            updateData();
        }

	}

    public static void checkServers(){

        
        while(true) {
            try{
                boolean down = false;
                String srv = "";
                Thread.sleep(HEALTHCHECK_PERIOD);

                synchronized (activeServers){
                    for(String server : activeServers){
                        System.out.println("RUNNING");
                        boolean healthy = healthCheck(server);
                        boolean healthy2 = true;
                        System.out.println("healtcheck1");
                        
                        // confirm??
                        if(!healthy){
                            healthy2 = healthCheck(server);
                        }
                        System.out.println("healtcheck2");

                        if(activeServers.size() > 0 && !healthy && !healthy2){
                            srv = server;
                            down = true;
                            MSS.deleteServerPuzzles(server);

                            for(Thread thread : activeThreads.get(server))
                                thread.interrupt();

                            for(HttpExchange t : activeExchanges.get(server)){
                                Runnable handle_exchange = new Runnable() {
                                    public void run() {
                                        try{
                                            new MyHandler().handle(t);
                                        }
                                        catch(Exception e){e.printStackTrace();}
                                    }
                                };
                                new Thread(handle_exchange).start();
                                // new MyHandler().handle(t);
                            }
                        } 
                    }
                }
                if(down){
                    activeServers.remove(srv);
                    activeThreads.remove(srv);
                    loadServers.remove(srv);
                    threadsServers.remove(srv);
                    activePuzzles.remove(srv);
                }
                
            } catch(Exception e){e.printStackTrace();}

        }

        

    }

    public static boolean healthCheck(String server) {

            HttpResponse<InputStream> response = null;
            try{

                URI uri = new URI("http://" + server + ":8000/status");

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .timeout(Duration.ofSeconds(10))
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



    public static void updateEstimative() {

        try{
            while (true) {
                Thread.sleep(2000);

                if(concludedPuzzles==3){
                    System.out.println("training estimatives");
                    updateBuckets();
                    concludedPuzzles = 0;
                }   
            }
        }
        catch(InterruptedException e){e.printStackTrace();}
	}

    public static void register() throws Exception{
      
        URI uri = new URI("http://" + AUTOSCALER_ADDR + ":" + AUTOSCALER_PORT + "/register?reg=" + InetAddress.getLocalHost().getHostName());

        System.out.println("http://" + AUTOSCALER_ADDR + ":" + AUTOSCALER_PORT + "/register?reg=" + InetAddress.getLocalHost().getHostName());

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
        activeServers = (ArrayList<String>) obj;

        if (activeServers.size() != 0)
            System.out.println(activeServers.get(0));
        
        for(String server : activeServers){
            activeExchanges.put(server,new ArrayList<HttpExchange>());
            activeThreads.put(server,new ArrayList<Thread>());
            threadsServers.put(server, new Long(0));
            loadServers.put(server, new Long(0));

        }


    }

    public static void getActiveServers() throws Exception{
      
        URI uri = new URI("http://" + AUTOSCALER_ADDR + ":" + AUTOSCALER_PORT + "/activeServers");

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
        activeServers = (ArrayList<String>) obj;

        if (activeServers.size() != 0)
            System.out.println(activeServers.get(0));


    }

	public static String parseRequestBody(InputStream is) throws IOException {
        InputStreamReader isr =  new InputStreamReader(is,"utf-8");
        BufferedReader br = new BufferedReader(isr);

        // From now on, the right way of moving from bytes to utf-8 characters:

        int b;
        StringBuilder buf = new StringBuilder(512);
        while ((b = br.read()) != -1) {
            buf.append((char) b);

        }

        br.close();
        isr.close();

        return buf.toString();
    }
    
    public static String forwardRequest(URI server, byte[] puzzle) throws Exception {

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
            .uri(server)
            .POST(HttpRequest.BodyPublishers.ofByteArray(puzzle))
            .build();
            /* Enviar para o servidor */
            HttpResponse<String> response = null;

            response = client.send(request,HttpResponse.BodyHandlers.ofString());

            String solution = response.body();
            System.out.println(solution);

            return solution;
    }

    public static void printActivePuzzles(){

        for(String server: activeServers){
            String s = "###### SERVER : " + server + " #########\n";

            HashMap<Long,Puzzle> puzzles = activePuzzles.get(server);

            if(puzzles==null){ 
               s+= "--0 ACTIVE PUZZLES--\n";
               continue;
            }

            for(Map.Entry<Long, Puzzle> entry : puzzles.entrySet()){
                Puzzle puzzle = entry.getValue();
                s+= puzzle.toString();     
            }
            System.out.println(s);
        }

    }



    public static void sendToClient(HttpExchange t, String solution) throws IOException {

            final Headers hdrs = t.getResponseHeaders();
           
            hdrs.add("Content-Type", "application/json");
			hdrs.add("Access-Control-Allow-Origin", "*");
            hdrs.add("Access-Control-Allow-Credentials", "true");
			hdrs.add("Access-Control-Allow-Methods", "POST, GET, HEAD, OPTIONS");
			hdrs.add("Access-Control-Allow-Headers", "Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers");

            t.sendResponseHeaders(200, solution.length());


            final OutputStream os = t.getResponseBody();
            OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
            osw.write(solution);
            osw.flush();
            osw.close();

			os.close();

    }
    /**
    *   Will check from all the servers, which one has less load
    *   
    *   @param      query  defines the puzzle
    *   @return      String with the name of the chosen server
    */
    public static String DistributeToServer(String query) {

        Map.Entry<String, Long> min = null;
        long predict = Predict(query);
        System.out.println("Request Cost : " + predict);


        if( predict > THRESHOLD){ //same THRESHOLD ?

            // Se o cost for > Threshold vamos ver o load de problemas grandes em cada server
            for (Map.Entry<String, Long> entry : loadServers.entrySet()) {
                if (min == null || min.getValue() > entry.getValue()) {
                    min = entry;
                }
            }
        }else{
            // Se o cost for < Threshold vamos ver apenas o numero de threads em cada server
            for (Map.Entry<String, Long> entry : threadsServers.entrySet()) {
                if (min == null || min.getValue() > entry.getValue()) {
                    min = entry;
                }
            }
        }
        return min.getKey();
    }

    /**
    *   Uses the nr of unsignes, the size of the puzzle and the algorithm
    *   used and compares it to previously known information to predict how
    *   much time this one will take    
    *   
    *   @param      query  everything needed to identify the request
    *   @return      returns how many instructions has this server done
    */
    public static long Predict(String query) {

        String[] params = query.split("&");
        
        String strat = params[0].split("=")[1];
        int un = Integer.parseInt(params[1].split("=")[1]);
        int n = Integer.parseInt(params[2].split("=")[1]);
        
        return estimative_model.predict(strat,n,un);

    }
    
    /**
    *   Fetches the MSS for puzzles
    *   should be called by a background thread
    *   
    *   @return      Overrides private activePuzzles variable
    */
    public static void updateData() { //TODO: can also calculate what is the min server threadsServers

        System.out.println("Updating Data");

        HashMap<String,Long> load = new HashMap<String,Long>();
        HashMap<String,Long> threads =  new HashMap<String,Long>();
        activePuzzles = MSS.getRunningPuzzles();

        for(String server: activeServers){
            long loadS = 0;
            HashMap<Long,Puzzle> puzzles = activePuzzles.get(server);

            if(puzzles==null){ 
               load.put(server, new Long(0));
               continue;
            }

            for(Map.Entry<Long, Puzzle> entry : puzzles.entrySet()){
                Puzzle puzzle = entry.getValue();
                long estimative = estimative_model.predict(puzzle.getStrat(),Integer.valueOf(puzzle.getN()),(int)puzzle.getUn());
                estimativeByRequest.put(new Long(puzzle.getId()),new Long(estimative));
                long instrLeft = estimative - puzzle.getInstr(); 
                if(instrLeft>THRESHOLD)
                    loadS += instrLeft; //Podemos guardar a prediction inicial?
            }
            load.put(server,new Long(loadS));
        }

        loadServers = load;
        printActivePuzzles();
    }

    public static void updateBuckets(){

        System.out.println("UPDATING ESTIMATION MODEL");
        // ZonedDateTime curr_timestamp = ZonedDateTime.now().minusSeconds(60);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");
        String ts = formatter.format(last_timestamp);
        System.out.println(ts);
        ArrayList<Puzzle> recentlyConcludedPuzzles;

        recentlyConcludedPuzzles = MSS.getConcludedPuzzles(ts);

        last_timestamp = ZonedDateTime.now().minusSeconds(60);

        for(Puzzle puzzle: recentlyConcludedPuzzles)
            estimative_model.train(puzzle.getStrat(),Integer.valueOf(puzzle.getN()),(int)puzzle.getUn(), puzzle.getInstr());

    }

    public static Puzzle createPuzzle(String query,long id){

        String[] params = query.split("&");
        
        String strat = params[0].split("=")[1];
        int un = Integer.parseInt(params[1].split("=")[1]);
        String n = params[2].split("=")[1];
        String label = params[4].split("=")[1];
        ZonedDateTime curr_timestamp = ZonedDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");
        String ts = formatter.format(curr_timestamp);

        return new Puzzle(label,new Long(0),new Long(0),strat,n,new Long(un),ts,id);
        
    }


	static class MyHandler implements HttpHandler {
		@Override
		public void handle(final HttpExchange t) throws IOException {

            System.out.println("Load Balancer Handler");

	        try{

            String query = t.getRequestURI().getQuery();

            InputStream is = null;
            byte[] puzzle = null;
            if(puzzleBoards.get(query)==null){
                is = t.getRequestBody();
                puzzle = is.readAllBytes();
                System.out.println("A Guardar board");
                puzzleBoards.put(query,puzzle);
            }

            else{
                puzzle = puzzleBoards.get(query);
            }
            

            System.out.println(query);


		    String server = DistributeToServer(query);


            activeExchanges.get(server).add(t);
            activeThreads.get(server).add(Thread.currentThread());

		    if(activePuzzles.get(server)==null)
			activePuzzles.put(server,new HashMap<Long,Puzzle>());
		    
		    //ADD REQUEST TO ACTIVE PUZZLES
		    long id = Thread.currentThread().getId();
		    Puzzle request = createPuzzle(query,id);
		    activePuzzles.get(server).put(new Long(id),request);
		    //GET ESTIMATIVE AND UPDATE
		    long estimative = estimative_model.predict(request.getStrat(),Integer.valueOf(request.getN()),(int)request.getUn());
		    estimativeByRequest.put(new Long(id), new Long(estimative));
		    //UPDATE LOAD AND THREADS
		    long load = loadServers.get(server).longValue();
		    load += estimative;
		    loadServers.put(server,new Long(load));
		    long threads = threadsServers.get(server).longValue();
		    threads += 1;
		    threadsServers.put(server,new Long(threads));

		    // escolher sever na lista
		    // redirecionar o pedido
		    query += "&id=" + id;
		    URI uri = URI.create("http://" + server + ":8000/sudoku?"+ query);
            System.out.println(uri);           


             /*Responder ao cliente*/
            //Mandar para o servidor
            System.out.println(puzzle);
            String solution = forwardRequest(uri,puzzle);
            //Responder ao cliente
            sendToClient(t,solution);

        
            //DELETE REQUEST FROM ACTIVE PUZZLES AND UPDATE LOAD/THREADS STRUCTURE
            long instr = estimativeByRequest.get(id) - activePuzzles.get(server).get(id).getInstr();
            load = loadServers.get(server).longValue();
            load -= instr;
            loadServers.put(server,new Long(load));
            threads = threadsServers.get(server).longValue();
            threads -= 1;
            threadsServers.put(server,new Long(threads));

            activePuzzles.get(server).remove(id);
            activeExchanges.get(server).remove(t);
            activeThreads.get(server).remove(Thread.currentThread());
            puzzleBoards.remove(t.getRequestURI().getQuery());
            concludedPuzzles ++;
            printActivePuzzles();
			System.out.println("> Sent response to " + t.getRemoteAddress().toString());
        } catch(Exception e){ e.printStackTrace();}  
        }
    }



    static class InstanceUpdateHandler implements HttpHandler {
		@Override
		public synchronized void handle(final HttpExchange t) throws IOException {

            System.out.println("Load Balancer Handler");

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

            System.out.println("action " + ap.getInputAction());
            System.out.println("server " + ap.getInputServer());

            // remove instance from activeServers
            // remove from loadServers and threadsServers maps

            String act = ap.getInputAction();
            String srv = ap.getInputServer();

            if (act.equals("add")){
                // has to receive an instance... why tho? only needs the private DNS
                // activeServers.removeIf(x -> x.getPrivateDnsName().equals(srv));
                activeServers.add(srv);
                activeExchanges.put(srv,new ArrayList<HttpExchange>());
                activeThreads.put(srv,new ArrayList<Thread>());
                loadServers.put(srv, new Long(0));
                threadsServers.put(srv,new Long(0));
            } else {
                activeServers.removeIf(x -> x.equals(srv));
                threadsServers.remove(srv);
                loadServers.remove(srv);
            }

            String response = "OK";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    static class EstimativeHandler implements HttpHandler {
		@Override
		public synchronized void handle(final HttpExchange t) throws IOException {

            System.out.println("A Enviar estimativas");

            final Headers hdrs = t.getResponseHeaders();
            
            hdrs.add("Content-Type", "application/json");
            hdrs.add("Access-Control-Allow-Origin", "*");
            hdrs.add("Access-Control-Allow-Credentials", "true");
            hdrs.add("Access-Control-Allow-Methods", "POST, GET, HEAD, OPTIONS");
            hdrs.add("Access-Control-Allow-Headers", "Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers");

            t.sendResponseHeaders(200, 0);


            final OutputStream os = t.getResponseBody();
            ObjectOutputStream objOut = new ObjectOutputStream(os);
            objOut.writeObject(estimative_model);
            objOut.close();
        }
    }

    static class RegisterInASHandler implements HttpHandler {
		@Override
		public synchronized void handle(final HttpExchange t) throws IOException {

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
                System.out.println(arg);
                args[i] = arg;
                i++;
            }

            final ITArgumentParser ap = new ITArgumentParser(args);
            LoadBalancer.AUTOSCALER_ADDR = ap.getInputReg();
            LoadBalancer.AUTOSCALER_PORT = Integer.parseInt(ap.getInputPort());

            try{
                LoadBalancer.register();
            }
            catch(Exception e){ e.printStackTrace(); } 

            final Headers hdrs = t.getResponseHeaders();
            
            
            String response = "OK";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        

        }
    }
}

/*

puzzlesAtivos = getRunningProcess(



####### IMPLEMENTACAO 1 #############
Para cada Server em Puzzles Ativos:
    NumeroDePedidosGrandes = 0
    Para cada Puzzle em PuzzleAtivos[Server]:
        instrucoes = puzzle.InstrucoesExecutadas
        estimativaTotal = getEstimativa(puzzle.label)
        instrucoesqueFaltam = estimativatotal - instrucoes

        if(instrucoesQueFaltam > Threshold)
            NumeroDePedidosGrandes[Server] += 1;
        
Request Vai para o servidor com menos pedidos grandes?

Pedido Grande >=  100000instr left

Exemplo1
S1 -> 20000 (2threads)
S2 -> 10000 + 10000 (2threads)

Exemplo1
S1 -> 20000 (2threads)
S2 -> 10000 + 10000 (2threads)



####### IMPLEMENTACAO 2 #############
QUANDO RECEBE UM PEDIDO GRANDE:
SomaDeInstrucoesQueFaltam = InstrucoesQueFaltamPedidoGrande1 + InstrucoesQueFaltamPedidoGrande2 + ...
THRESHOLD = (SomaDeinstrucoesQueFaltamTotal*numeroDeRequestsNoServer)/NumeroTotalDaMaquina

TH1 = 200000*1/2 = 100000
TH2 = 200000*2/2 = 200000

QUANDO RECEBE PEDIDO < GRANDE :

SERVER = MIN_THREAD(SERVER_LIST)  MIN_THread retorn sempre so 1


//distribute
INPUT : PEDIDO
COST = ESTIMATIVA(PEDIDO)

IF(COST > THRESHOLD):
    FOR EACH SERVER:
        SomaDeInstrucoesQueFaltam = InstrucoesQueFaltam(SERVER)
        LOAD_NO_Server = (SomaDeinstrucoesQueFaltamTotal*numeroDeRequestsNoServer[SERVER])/NumeroTotalDaMaquina[SERVER]
        Load_list.append(Load_no_server)
    
    return Load_list.Min()
    

ELSE:
    return MIN_THREAD(SERVER_LIST)  MIN_THread retorn sempre so 1



 //InstrucoesQueFaltam(Server) -----> Puzzle.getInstr() nao sao as que ja correu?
    sum = 0

    puzzles = PuzzlesAtivos[Server]
    FOR EACH puzzle in puzzles
        if Puzzle.getInstr() > THRESHOLD:
            sum += Puzzle.getInstr()
    
    return sum


*/
