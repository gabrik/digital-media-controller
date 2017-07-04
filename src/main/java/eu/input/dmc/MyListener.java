/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.input.dmc;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.fourthline.cling.DefaultUpnpServiceConfiguration;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.Namespace;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.ServiceId;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.model.types.UDAServiceId;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.support.avtransport.callback.SetAVTransportURI;
import org.fourthline.cling.support.contentdirectory.callback.Browse;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;
import org.json.JSONArray;
/**
 *
 * @author mauriziofloridia

 */
public class MyListener extends DefaultRegistryListener{
    
         UpnpService upnpService;
         ServiceId avTrasportService = new UDAServiceId("AVTransport");
         ServiceId connectionService = new UDAServiceId("ConnectionManager");
         ServiceId contentDirectory = new UDAServiceId("ContentDirectory");
         Namespace namespace = new Namespace("schemas-upnp-org");
         
         String deviceFoundJSON;
         //List<> listDevice = new ArrayList<>();
         
         JSONArray listDevice = new JSONArray();
         Collection<Device> collectionDevice;
         DeviceItem deviceFound;
         
         
         
         
         
         public MyListener(UpnpService upnpService) {
            this.upnpService = upnpService;
        }
         
        @Override
        public void remoteDeviceAdded(Registry registry, RemoteDevice device){
            
            if (device.getType().getType().equals("MediaServer")) {         
            for (RemoteService service : device.getServices()) {
                if (service.getServiceType().getType().equals("ContentDirectory")) {
                    final String serviceUUID=service.getReference().getUdn().toString().split("uuid:")[1];
                    DiscoveryDevice.getmServices().put(serviceUUID, service);
                    DiscoveryDevice.browseServer(service);
                }
            }
        }
        }
                    
       

    public JSONformat getJSON(DeviceItem deviceFound) {
        JSONformat deviceToSend = new JSONformat();
        
        deviceToSend.setName(deviceFound.getFriendlyName());
    
        deviceToSend.setUDN(deviceFound.getUdn());
        deviceToSend.setType(deviceFound.getType());
        deviceToSend.setURLHost(deviceFound.getURLHost());
        deviceToSend.setURLPort(deviceFound.getURLport());
        
        return deviceToSend;
    }
    
      @Override 
      public void remoteDeviceRemoved(Registry registry, RemoteDevice device){
        Service serviceRender;
        DeviceDetails details = device.getDetails();
        if((serviceRender = device.findService(avTrasportService))!= null){
            System.out.println("Servizio non più disponibile: " + serviceRender.toString());
        }
         
      }
    
    public String getListDevice(){
        return this.listDevice.toString();
    }
    
    
    //Prima di chiudere la ricerca, vengono memorizzati i dispositivi trovati
    @Override
    public void beforeShutdown(Registry registry) {
        
        for(String n : DiscoveryDevice.getmContentMap().keySet()){
            ConcurrentHashMap<String,String> dir = DiscoveryDevice.getmContentMap().get(n);
            
            for(String k2 : dir.keySet()){
                DiscoveryDevice.browseServerDirectory(DiscoveryDevice.getmServices().get(n),k2);
            }
           
        }
        
        
        
        Gson g = new Gson();
        this.collectionDevice = registry.getDevices();
        LinkedList<Device> devices = new LinkedList<>();
        devices.addAll(collectionDevice);
        
       
       for(int i=0;i<devices.size();i++){
           Device d = devices.get(i);
           System.out.println("Device Discoveder: " + d.getDetails().getFriendlyName() + " UUID: " +d.getIdentity().getUdn());
           System.out.println("Type: " + d.getType().getType() + "\n\n");
           listDevice.put(new DeviceItem((RemoteDevice)d).toJSON());
           
       } 
        
        
        /*
        
        this.collectionDevice.stream().map((myDevice) -> {
            this.deviceFound = new DeviceItem( (RemoteDevice) myDevice);
                 return myDevice;
             }).map((_item) -> {
                 System.out.println("Dispositivo trovato: " + deviceFound.getFriendlyName() + "Tipo: " + deviceFound.getType());
                 return _item;
             }).forEachOrdered((_item) -> {
                 this.listDevice.add(getJSON(deviceFound));
             });
        */
        
        
        
        this.deviceFoundJSON = g.toJson(this.listDevice);
                
        }
    
     @Override
     public void afterShutdown() {
         System.out.println("Ricerca Terminata");
        }

     
     
     ///// AGGIUNTE DA GABRIELE 
    @Override
    public void deviceAdded(Registry registry, Device device) {
        super.deviceAdded(registry, device); 
        
        /*System.out.println("test "+device.getIdentity().getUdn().toString().contains(deviceUuid));
 
        if (device.getIdentity().getUdn().toString().contains(deviceUuid)) {
 
            Service[] services = device.findServices();
 
            for (Service service : services) {                
 
                if ("AVTransport".equals(service.getServiceType().getType())) {
 
                    System.out.println("device.getDisplayString(): "
 
                            + device.getDisplayString());
 
                }
 
            }
 
        }*/
    }
     
     
      public void setContent(final Service service,final String videoURL,final String videoTitle) {
 
        System.out.println("setContent started");
 
        ActionCallback setAVTransportURIAction = new SetAVTransportURI(service,videoURL,"<DIDL-Lite xmlns='urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/' xmlns:dc='http://purl.org/dc/elements/1.1/' xmlns:upnp='urn:schemas-upnp-org:metadata-1-0/upnp/' xmlns:dlna='urn:schemas-dlna-org:metadata-1-0/'><item id='sample' parentID='0' restricted='0'><dc:title>"+videoTitle+"</dc:title><dc:creator>Mohit</dc:creator><upnp:genre>No Genre</upnp:genre><res protocolInfo='http-get:*:video/mpeg:DLNA.ORG_FLAGS=01700000000000000000000000000000;DLNA.ORG_CI=0;DLNA.ORG_OP=01'>"+videoURL+"</res><upnp:class>object.item.videoItem</upnp:class></item></DIDL-Lite>") {
 
            @Override
 
            public void failure(ActionInvocation invocation,
 
                    UpnpResponse operation, String defaultMsg) {
 
                System.out.println(" " + defaultMsg);
 
            }

          
        };
                }
      
      ////////////////
    
}
