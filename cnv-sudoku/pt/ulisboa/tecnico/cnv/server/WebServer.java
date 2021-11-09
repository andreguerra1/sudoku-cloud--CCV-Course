package pt.ulisboa.tecnico.cnv.server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONArray;
import org.json.*;

import pt.ulisboa.tecnico.cnv.it.MSS;
import pt.ulisboa.tecnico.cnv.solver.Solver;
import pt.ulisboa.tecnico.cnv.solver.SolverArgumentParser;
import pt.ulisboa.tecnico.cnv.solver.SolverFactory;
import pt.ulisboa.tecnico.cnv.instrument.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ConcurrentHashMap;
import java.net.InetAddress;
import java.net.UnknownHostException;
 
public class WebServer {
	public static PrintStream outstream;
	public static ConcurrentHashMap<Long, String[]> request_params = new ConcurrentHashMap<Long, String[]>();
	private static final int UPDATE_INTERVAL = 2000;
	private static String server_ip;

	public static void main(final String[] args) throws Exception {

        try {
            server_ip = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
			e.printStackTrace();
			return;
		}

		MSS.init();

		Runnable r = new Runnable() {
			public void run() {
				background_log();
			}
		};

		new Thread(r).start();

		final HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

		server.createContext("/sudoku", new MyHandler());
		server.createContext("/status", new StatusHandler());

		// be aware! infinite pool of threads!
		server.setExecutor(Executors.newCachedThreadPool());

		try {
			outstream = new PrintStream(new FileOutputStream("metrics_log.txt", true));
		} catch (Exception e) {
			e.printStackTrace();
		}

		server.start();

		System.out.println(server.getAddress().toString());
	}
	// log thread

	public static void background_log() {
		while (true) {
			try {
				Thread.sleep(UPDATE_INTERVAL);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			update_metric_process();
		}
	}

	public static synchronized void update_metric_process() {
		if(!request_params.isEmpty()){
			for (Map.Entry<Long, HashMap<String, Metric>> entry : Statistics.metrics.entrySet()) {
				Long tid = entry.getKey();
				String[] request = request_params.get(tid);
				String timeStamp = timeStamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(new Date());;

				StringBuilder key = new StringBuilder();
						
				key.append(request[9]);
				key.append("-");
				key.append(request[1]);
				key.append("-");
				key.append(server_ip);
				key.append("-");
				key.append(request[request.length-1]);
				
				MSS.update(MSS.UpdateMetricLogRequest(key.toString(), ((Counter)Statistics.metrics.get(tid).get("dyn_method_count"))._count,
							((Counter)Statistics.metrics.get(tid).get("fieldloadcount"))._count));
			}
		}
	}	

	public static synchronized void start_metric(long tid) {
		String[] request = request_params.get(tid);
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(new Date());;

		StringBuilder key = new StringBuilder();
				
		key.append(request[9]);
		key.append("-");
		key.append(request[1]);
		key.append("-");
		key.append(server_ip);
		key.append("-");
		key.append(request[request.length-1]);
		
		// (String key, long dyn_method_count, long fieldloadcount, 
        //                         String ts, String Un, String server, String label, 
        //                         String N1, Boolean running, String N2)
		MSS.putMetric(key.toString(), ((Counter)Statistics.metrics.get(tid).get("dyn_method_count"))._count,
						((Counter)Statistics.metrics.get(tid).get("fieldloadcount"))._count,
						timeStamp, Long.valueOf(request[3]), server_ip, request[9], request[5], true, request[7]);
		
	}	


	public static synchronized void close_metric(long tid) {
		String[] request = request_params.get(tid);
		String timeStamp = timeStamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(new Date());;

		StringBuilder key = new StringBuilder();
				
		key.append(request[9]);
		key.append("-");
		key.append(request[1]);
		key.append("-");
		key.append(server_ip);
		key.append("-");
		key.append(request[request.length-1]);
		
		MSS.update(MSS.UpdateMetricFinalRequest(key.toString(), ((Counter)Statistics.metrics.get(tid).get("dyn_method_count"))._count,
					((Counter)Statistics.metrics.get(tid).get("fieldloadcount"))._count, timeStamp));
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
	
	static class MyHandler implements HttpHandler {
		@Override
		public void handle(final HttpExchange t) throws IOException {
			String[] params_info;
			Long local_id = new Long(0);
			// Get the query.
			final String query = t.getRequestURI().getQuery();
			System.out.println("> Query:\t" + query);

			// Break it down into String[].
			final String[] params = query.split("&");

			// synchronized(request_counter){
			// 	request_counter++;
			// 	local_counter = new Long(request_counter);
			// }
			try{
		            local_id = Long.valueOf(params[params.length-1].split("=")[1]);
			} catch(Exception e){
				System.out.println("NO ID PROVIDED");
			}
			// Store as if it was a direct call to SolverMain.
			final ArrayList<String> newArgs = new ArrayList<>();
			for (final String p : params) {
				final String[] splitParam = p.split("=");
				if(splitParam[0].equals("id")){
					break;
				}
				newArgs.add("-" + splitParam[0]);
				newArgs.add(splitParam[1]);
			}
			newArgs.add("-b");
			newArgs.add(parseRequestBody(t.getRequestBody()));

			newArgs.add("-d");

			// Store from ArrayList into regular String[].
			final String[] args = new String[newArgs.size()];
			int i = 0;
			for(String arg: newArgs) {
				args[i] = arg;
				i++;
			}

			params_info = new String[args.length + 1];
			System.arraycopy(args, 0, params_info, 0, args.length);
			params_info[params_info.length-1] = local_id.toString();

			request_params.put(Thread.currentThread().getId(), params_info);

			Statistics.init_thread();
			start_metric(Thread.currentThread().getId());

			// Get user-provided flags.
			final SolverArgumentParser ap = new SolverArgumentParser(args);


			// Create solver instance from factory.
			final Solver s = SolverFactory.getInstance().makeSolver(ap);

			//Solve sudoku puzzle
			JSONArray solution = s.solveSudoku();

			close_metric(Thread.currentThread().getId());

			request_params.remove(Thread.currentThread().getId());
			Statistics.metrics.remove(Thread.currentThread().getId());

			// Send response to browser.
			final Headers hdrs = t.getResponseHeaders();

			//t.sendResponseHeaders(200, responseFile.length());


			///hdrs.add("Content-Type", "image/png");
			hdrs.add("Content-Type", "application/json");

			hdrs.add("Access-Control-Allow-Origin", "*");

			hdrs.add("Access-Control-Allow-Credentials", "true");
			hdrs.add("Access-Control-Allow-Methods", "POST, GET, HEAD, OPTIONS");
			hdrs.add("Access-Control-Allow-Headers", "Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers");

			t.sendResponseHeaders(200, solution.toString().length());


			final OutputStream os = t.getResponseBody();
			OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
			osw.write(solution.toString());
			osw.flush();
			osw.close();

			os.close();

			System.out.println("> Sent response to " + t.getRemoteAddress().toString());
		}
	}

	static class StatusHandler implements HttpHandler {
		@Override
        public void handle(HttpExchange t) throws IOException {
            String response = "OK";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
	}
}
