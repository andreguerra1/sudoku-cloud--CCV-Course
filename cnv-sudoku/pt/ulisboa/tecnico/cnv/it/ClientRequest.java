package pt.ulisboa.tecnico.cnv.it;

import com.sun.net.httpserver.HttpExchange;


public class ClientRequest{

    private byte[] body;
    private HttpExchange t;

    public ClientRequest(byte[] body, HttpExchange t){
        this.body = body;
        this.t = t;
    }

    public byte[] getBody(){
        return this.body;
    }
    public HttpExchange getExchange(){
        return this.t;
    }

}