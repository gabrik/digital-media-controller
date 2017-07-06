/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.input.dmc;
import com.google.gson.JsonElement;
import java.net.*;
import java.io.*;
import java.util.List;
/**
 *
 * @author mauriziofloridia
 */
public class SocketController {

private int port;
private ServerSocket server;
String request;
String response;
PrintWriter out = null;
InputStreamReader in = null;
Socket s = null;

 public SocketController (int port)
 {
  this.port = port;
  if(!startServer())
  System.err.println("Errore durante la creazione del Server");
 }
 
 
private boolean startServer(){
        try{
         server = new ServerSocket(port);   
        }
        catch (IOException ex)
        {
         ex.printStackTrace();
         return false;
        }
        System.out.println("Controller avviato!");
        return true;
 }
 
 public void runServer() throws ClassNotFoundException, InterruptedException{
  try{
            // Il server resta in attesa di una richiesta
            System.out.println("In attesa..");
            s = server.accept();
            System.out.println("Richiesta ricevuta dal client " + s);
            // Ricava lo stream associate al socket
            
           in = new InputStreamReader(s.getInputStream());
           out = new PrintWriter(s.getOutputStream());
           
           BufferedReader r = new BufferedReader(in);
            
           setRequest(r.readLine());
           
           
            
            }catch (IOException ex){
        ex.printStackTrace();
        }
    
 }

public synchronized void setRequest(String request){
    this.request = request;
    notify();
    System.out.println("Il client desidera: " + this.request);
}
 



public synchronized String getRequest() throws InterruptedException{
  
    while (this.request == null){
     wait();
      }  
   notify();
 return this.request;
}
    

public synchronized void sendResponse(String response) throws InterruptedException, IOException{
    System.out.println("Invio " + response);
    out.write(response+"\n");
    out.flush();
 }


public void close() throws IOException{
    System.out.println("Chiusa la connessione al client");
}

}
