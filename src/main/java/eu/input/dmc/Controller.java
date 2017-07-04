package eu.input.dmc;


import java.util.concurrent.ConcurrentHashMap;
import org.json.JSONObject;
/**
 *
 * @author mauriziofloridia
 */
public class Controller {

    private static final int PORT = 50050;
    
    public static void main(String[] args) throws Exception {


        
        DiscoveryDevice.initialize();
        System.out.println(DiscoveryDevice.getJsonListOfDevices()); 

        String request;
        String response;
        SocketController socket;
        socket = new SocketController(PORT);
        
      

        
        
        for(String n : DiscoveryDevice.getmVideoMap().keySet()){
            ConcurrentHashMap<String,String> dir = DiscoveryDevice.getmVideoMap().get(n);
            System.out.println("Chiave: " + n);
            System.out.println("Value:");
            for(String k2 : dir.keySet()){
                System.out.println("Chiave 2: " + k2 +  " value: "+ dir.get(k2));
            }
           
        }
        
        
        /*
        while (true) {
            socket.runServer();
            request = socket.getRequest();

            System.out.println("Richiesta: " + request);
            JSONObject jsonRequest=new JSONObject(request);
            //"{'operation':'device list'}"
            
            switch (jsonRequest.getString("operation")) {

                case "device list":
                    System.out.println("Avvio la ricerca...");
                    socket.sendResponse(DiscoveryDevice.getJsonListOfDevices());
                    socket.close();
                    break;

                case "play":
                    System.out.println("play");
                    socket.sendResponse("in play");
                    socket.close();
                    break;
                default:
                    System.out.println("Default");
                    break;
            }
        }*/  
    }
}
