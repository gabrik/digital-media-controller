/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.input.dmc;

import java.rmi.Remote;
import java.rmi.server.RemoteServer;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import javax.print.attribute.HashAttributeSet;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.message.header.STAllHeader;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.avtransport.callback.SetAVTransportURI;
import org.fourthline.cling.support.contentdirectory.callback.Browse;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author mauriziofloridia
 */
@Configuration
public class DiscoveryDevice {

    
    private static UpnpService upnpService;
    private MyListener listener;
    
    private static DiscoveryDevice mDiscoveryService;
    private static ConcurrentHashMap<String,ConcurrentHashMap<String,String>> mContentMap; 
    private static ConcurrentHashMap<String,RemoteService> mServices;
    private static ConcurrentHashMap<String,ConcurrentHashMap<String,String>> mVideoMap; 
    
    private static String jsonListOfDevices;
          
     
     public static void initialize() throws InterruptedException{
         mVideoMap=new ConcurrentHashMap<>();
         mServices = new ConcurrentHashMap<>();
         mContentMap=new ConcurrentHashMap<>();
         mDiscoveryService=new DiscoveryDevice();
         jsonListOfDevices=mDiscoveryService.getListener().getListDevice();
         
           
         
     }
     
     
     
    
    
    private DiscoveryDevice() throws InterruptedException{
         
        this.upnpService = new UpnpServiceImpl();
        
        this.listener = new MyListener(upnpService);
        upnpService.getRegistry().addListener(this.listener);
        // eseguo la ricerca dei dispositivi senza vincoli. E' possibile definire
        //un header differente per restringere e discriminare la ricerca
        upnpService.getControlPoint().search(new STAllHeader());//.search(new STAllHeader());  
        Thread.sleep(7000);
        upnpService.shutdown();
    }
    
    public MyListener getListener(){
        return this.listener;
    }
    
    
    public static void browseServerDirectory(RemoteService service,String containerID){
        final String serviceUUID=service.getReference().getUdn().toString().split("uuid:")[1];
        System.out.printf("Discovering directory %s on %s\n",containerID,serviceUUID);
        DiscoveryDevice.upnpService.getControlPoint().execute(new Browse(service, containerID, BrowseFlag.DIRECT_CHILDREN) {
                                
                        @Override public void received(ActionInvocation arg0, DIDLContent didl) {
                            
                            List<Item> mItemList = didl.getItems();
                            if(mItemList.size()>0){
                                System.out.printf("Item List size %d\n",mItemList.size());
                                mVideoMap.put(serviceUUID, new ConcurrentHashMap<>());
                                for(Item i : didl.getItems()){
                                    String title = i.getTitle();
                                    String id = i.getFirstResource().getValue();
                                    System.out.printf("ITEM: title %s id %s url %s\n",title,i.getId(),id);
                                    mVideoMap.get(serviceUUID).put(id, title);
                                           
                                    
                                    
                                    //System.out.println(c.getFirstResource().getValue());
                                }
                            }
                            
                            /*
                            if(didl.getItems().size()>0){
                                System.out.printf("found %d items.\n", didl.getItems().size());
                                for(Item i : didl.getItems()){
                                System.out.println(i.getTitle());
                                System.out.println(i.getId());
                                System.out.println(i.getFirstResource().getValue());
                                }
                            }else{
                                
                            }*/
                              
                        }

                        @Override public void updateStatus(Browse.Status arg0) { 
                            System.out.println("UpdateStatus!!! discovery content");
                        };

                        @Override public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) { 
                            System.out.println("Failure discovery content: "+arg0.getFailure().getMessage());
                        };
                            
                       
                    });
        
    }
    
    
    public static void browseServer(RemoteService service){
        final String serviceUUID=service.getReference().getUdn().toString().split("uuid:")[1];
        System.out.println(serviceUUID);
        System.out.printf("Discovering on %s\n",serviceUUID);
        DiscoveryDevice.upnpService.getControlPoint().execute(
                            new Browse(service, "0", BrowseFlag.DIRECT_CHILDREN) {
                                
                        @Override public void received(ActionInvocation arg0, DIDLContent didl) {
                            
                            List<Container> mContainerList = didl.getContainers();
                            if(mContainerList.size()>0){
                                System.out.printf("number of dirs: %d\n",mContainerList.size());
                                mContentMap.put(serviceUUID, new ConcurrentHashMap<>());
                                for(Container c : didl.getContainers()){
                                    String title = c.getTitle();
                                    String id = c.getId();
                                    System.out.printf("DIR: name %s id %s\n",title,id);
                                    mContentMap.get(serviceUUID).put(id, title);
                                           
                                    
                                    
                                    //System.out.println(c.getFirstResource().getValue());
                                }
                            }
                            
                            /*
                            if(didl.getItems().size()>0){
                                System.out.printf("found %d items.\n", didl.getItems().size());
                                for(Item i : didl.getItems()){
                                System.out.println(i.getTitle());
                                System.out.println(i.getId());
                                System.out.println(i.getFirstResource().getValue());
                                }
                            }else{
                                
                            }*/
                              
                        }

                        @Override public void updateStatus(Browse.Status arg0) { 
                            System.out.println("UpdateStatus!!! Discovery dir");
                        };

                        @Override public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) { 
                            System.out.println("Failure Discovery dir: "+arg0.getFailure().getMessage());
                        };
                            
                       
                    });
        
        
    }
    
    
    public static DiscoveryDevice getmDiscoveryService() {
        return mDiscoveryService;
    }

    public static void setmDiscoveryService(DiscoveryDevice amDiscoveryService) {
        mDiscoveryService = amDiscoveryService;
    }

    @Bean
    public static String getJsonListOfDevices() {
        return jsonListOfDevices;
    }

    public static void setJsonListOfDevices(String jsonListOfDevices) {
        DiscoveryDevice.jsonListOfDevices = jsonListOfDevices;
    }

    public static ConcurrentHashMap<String, ConcurrentHashMap<String, String>> getmContentMap() {
        return mContentMap;
    }

    public static void setmContentMap(ConcurrentHashMap<String, ConcurrentHashMap<String, String>> mContentMap) {
        DiscoveryDevice.mContentMap = mContentMap;
    }

    public static ConcurrentHashMap<String, RemoteService> getmServices() {
        return mServices;
    }

    public static void setmServices(ConcurrentHashMap<String, RemoteService> mServices) {
        DiscoveryDevice.mServices = mServices;
    }

    public static ConcurrentHashMap<String, ConcurrentHashMap<String, String>> getmVideoMap() {
        return mVideoMap;
    }

    public static void setmVideoMap(ConcurrentHashMap<String, ConcurrentHashMap<String, String>> mVideoMap) {
        DiscoveryDevice.mVideoMap = mVideoMap;
    }
    
    
    
   
}
                
                

