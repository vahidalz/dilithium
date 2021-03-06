package org.dilithium.networking;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dilithium.networking.Commands.NetworkCommand;
import org.dilithium.networking.Commands.PingCommandHandler;
import org.dilithium.util.Log;


public class Peer2Peer {

	private int port;
    private ArrayList<Peer>  peers;
    private DataOutputStream outputStream;
    public 	Thread           serverThread;
    private boolean          runningServer;
    private HashMap<String, NetworkCommand> commands = new HashMap<>();
    private ServerSocket server;
    private Socket socket = null;

    //Node with access to blockchain
    public Peer2Peer(int port){
        this.port = port;
        peers = new ArrayList<>();
        serverThread = new Thread(new Runnable() {
            public void run() {
                try {
                    listen();
                    Log.log(Level.INFO, "Connection Ended");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });		
        initializeCommands();
    }
    
    
    private void initializeCommands() {
        this.commands.put("ping", new PingCommandHandler()); 
    }

    public void start(){
        if(serverThread.isAlive()){
            Log.log(Level.INFO, "Server is already running.");
            return;
        }
        runningServer = true;
        serverThread.start();
    }

    public void stop() throws IOException{
    		runningServer = false;
    		try {
        		serverThread.interrupt();
    			socket.close();
        } catch (NullPointerException n) {
        		n.printStackTrace();
        }
        Log.log(Level.INFO, "Server Stopped");
    }

    public void listen() throws IOException, SocketTimeoutException{
        Log.log(Level.INFO, "Server starting...");
        server = new ServerSocket(this.port);
        Log.log(Level.INFO, "Server started on port " + this.port);
        	
        Peer peer;
        server.setSoTimeout(10000);
        while(runningServer){
        		try{
        			socket = server.accept();
                Log.log(Level.INFO, "Passed Accept");
                peer = new Peer(socket);
                Log.log(Level.INFO, "Connection received from: " + peer.toString());
                peers.add(peer);
                Log.log(Level.INFO, "New peer: " + peer.toString());
        		} catch (SocketTimeoutException e) {
        			//e.printStackTrace();
        		}
            

        }
    }

    public void connect(Socket socket){
        try {
            outputStream = new DataOutputStream(socket.getOutputStream());
            Peer.send("ping", outputStream);		
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }
}