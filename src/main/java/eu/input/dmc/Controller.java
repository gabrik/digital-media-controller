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

    private static final int PORT = 50051;

    private static DMCUpnpDiscoveryManager mDiscoveryManager;
    
    public static void main(String[] args) {

        try {
            //DiscoveryService.initialize();
            //DiscoveryService.browse();
            /*Thread.sleep(7000);
            DiscoveryService.getInstance().stopUpnpService();*/
            //System.out.printf("Devices: %s\n", DiscoveryService.getJsonListOfDevices());
            //System.out.printf("Contents: %s\n", DiscoveryService.getJsonListOfContents());
            
            
            DMCUpnpDiscoveryManager.initInstance();
            mDiscoveryManager=DMCUpnpDiscoveryManager.getInstance();
            
            mDiscoveryManager.init();
            mDiscoveryManager.discover();
            
            
            System.out.printf("Devices: %s\n", mDiscoveryManager.getJsonListOfDevices());
            System.out.printf("Contents: %s\n", mDiscoveryManager.getJsonListOfContents());
            

            //to catch Ctrl-C
            Runtime.getRuntime().addShutdownHook(new Thread() { @Override public void run() { mDiscoveryManager.stop(); }});
            
            //mDiscoveryManager.startOn("898f9738-d930-4db4-a3cf-dc4a3ea8caff", "6f9e7063-e588-1b8f-6290-5dcbfe0cf1d0", "http://192.168.34.27:49152/web/2.mp4");
            
            String request;
            String response;
            SocketController socket;
            socket = new SocketController(PORT);

            //TODO generare lista json così
            //[{"uuid":"898f9738-d930-4db4-a3cf-dc4a3ea8caff","content":[{"name":"Family","url":"http://172.16.0.39:49152/web/2.mp4"},{"name":"sony_eye_candy","url":"http://172.16.0.39:49152/web/3.mpg"}]}]
            while (true) {
                socket.runServer();
                request = socket.getRequest();

                System.out.println("Richiesta: " + request);
                JSONObject jsonRequest = new JSONObject(request);
                JSONObject jsonResponse = new JSONObject();
                //"{'operation':'device list'}"
                //"{'operation':'media list'}"
                //"{'operation':'start','server_uuid':'ddd','client_uuid':'ccc','url':'url video'}"
                //"{'operation':'scan'}
                //"{'operation':'play','client_uuid':'ccc'}"
                //"{'operation':'stop','client_uuid':'ccc'}"
                //"{'operation':'pause','client_uuid':'ccc'}"
                //"{'operation':'vol','client_uuid':'ccc','volume':volume}"
                
                 //"{'operation':'mute','client_uuid':'ccc'}"
                switch (jsonRequest.getString("operation")) {

                    case "scan":
                        //DiscoveryService.initialize();
                        //mDiscoveryManager.stop();
                        //DMCUpnpDiscoveryManager.initInstance();
                        //mDiscoveryManager=DMCUpnpDiscoveryManager.getInstance();
                        //mDiscoveryManager.init();
                        mDiscoveryManager.discover();
                        //DiscoveryService.browse();
                        System.out.printf("Devices: %s\n", mDiscoveryManager.getJsonListOfDevices());
                        System.out.printf("Contents: %s\n", mDiscoveryManager.getJsonListOfContents());
                        jsonResponse.put("status", "success");
                        socket.sendResponse(jsonResponse.toString());
                        socket.close();
                        break;

                    case "device list":
                        System.out.println("Sending UPNP/DLNA Devices");
                        socket.sendResponse(mDiscoveryManager.getJsonListOfDevices());
                        socket.close();
                        break;
                    case "media list":
                        System.out.println("Sending Contents on Media Servers");
                        socket.sendResponse(mDiscoveryManager.getJsonListOfContents());
                        socket.close();
                        break;

                    case "start":
                        System.out.println("Starting a Content");
                        mDiscoveryManager.startOn(jsonRequest.getString("server_uuid"), jsonRequest.getString("client_uuid"), jsonRequest.getString("url"));

                        jsonResponse.put("status", "success");
                        socket.sendResponse(jsonResponse.toString());
                        socket.close();
                        break;
                    case "play":
                        System.out.println("Playing a Content");
                        mDiscoveryManager.playOn(jsonRequest.getString("client_uuid"));
                        jsonResponse.put("status", "success");
                        socket.sendResponse(jsonResponse.toString());
                        socket.close();
                        break;
                    case "stop":
                        System.out.println("Stopping a Content");
                        mDiscoveryManager.stopOn(jsonRequest.getString("client_uuid"));
                        jsonResponse.put("status", "success");
                        socket.sendResponse(jsonResponse.toString());
                        socket.close();
                        break;
                    case "pause":
                        System.out.println("Pause a Content");
                        mDiscoveryManager.pauseOn(jsonRequest.getString("client_uuid"));
                        jsonResponse.put("status", "success");
                        socket.sendResponse(jsonResponse.toString());
                        socket.close();
                        break;
                    case "vol":
                        System.out.println("Playing a Content");
                        mDiscoveryManager.changeVolume(jsonRequest.getString("client_uuid"),0,Integer.parseInt(jsonRequest.getString("volume")),false);
                        jsonResponse.put("status", "success");
                        socket.sendResponse(jsonResponse.toString());
                        socket.close();
                        break;
                    case "mute":
                        System.out.println("Playing a Content");
                        mDiscoveryManager.changeVolume(jsonRequest.getString("client_uuid"),1,0,jsonRequest.getBoolean("value"));
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
            mDiscoveryManager.stop();
            System.exit(-1);
        } catch (ExecutionException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
