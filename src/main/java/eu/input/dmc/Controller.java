package eu.input.dmc;


import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;
/**
 *
 * @author mauriziofloridia
 */
public class Controller {

    private static final int PORT = 50050;
    
    public static void main(String[] args) {


        
        try {
            DiscoveryService.initialize();
            /*Thread.sleep(7000);
            DiscoveryService.getInstance().stopUpnpService();*/
            System.out.printf("Devices: %s\n",DiscoveryService.getJsonListOfDevices());
            
            String request;
            String response;
            SocketController socket;
            socket = new SocketController(PORT);
            
         
            
            System.out.printf("Contents: %s\n",DiscoveryService.getJsonListOfContents());
            
            
            
            //TODO generare lista json così
            //[{"uuid":"898f9738-d930-4db4-a3cf-dc4a3ea8caff","content":[{"name":"Family","url":"http://172.16.0.39:49152/web/2.mp4"},{"name":"sony_eye_candy","url":"http://172.16.0.39:49152/web/3.mpg"}]}]
            DiscoveryService.playOn("898f9738-d930-4db4-a3cf-dc4a3ea8caff", "13f91d82-4356-1a20-808d-784561139eda","http://172.16.0.39:49152/web/2.mp4" );
            
            while (true) {
                socket.runServer();
                request = socket.getRequest();
                
                System.out.println("Richiesta: " + request);
                JSONObject jsonRequest=new JSONObject(request);
                //"{'operation':'device list'}"
                //"{'operation':'media list'}"
                //"{'operation':'play','server_uuid':'ddd','client_uuid':'ccc','url':'url video'}"
                switch (jsonRequest.getString("operation")) {
                    
                    case "device list":
                        System.out.println("Sending UPNP/DLNA Devices");
                        socket.sendResponse(DiscoveryService.getJsonListOfDevices());
                        socket.close();
                        break;
                    case "media list":
                        System.out.println("Sending Contents on Media Servers");
                        socket.sendResponse(DiscoveryService.getJsonListOfContents());
                        socket.close();
                        break;
                        
                        
                    case "play":
                        System.out.println("Playing a Content");
                        DiscoveryService.playOn(jsonRequest.getString("server_uuid"), jsonRequest.getString("client_uuid"), jsonRequest.getString("url"));
                        JSONObject jsonResponse = new JSONObject();
                        jsonResponse.put("status", "success");
                        socket.sendResponse(jsonResponse.toString());
                        socket.close();
                        break;
                    default:
                        System.out.println("Default");
                        break;
                }
            }
        } catch (InterruptedException | IOException | ClassNotFoundException | NullPointerException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-1);
        } catch (ExecutionException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}
