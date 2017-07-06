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
import java.util.concurrent.ExecutionException;
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
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author mauriziofloridia
 */
@Configuration
public class DiscoveryService {

    private static UpnpService upnpService;
    private DMCUpnpListener listener;

    private static DiscoveryService mInstance;
    
    
    private static ConcurrentHashMap<String, ConcurrentHashMap<String, String>> mContentMap;
    private static ConcurrentHashMap<String, RemoteService> mServerServices;
    private static ConcurrentHashMap<String, RemoteService> mRendererServices;
    private static ConcurrentHashMap<String, RemoteService> mControlServices;
    private static ConcurrentHashMap<String, ConcurrentHashMap<String, String>> mVideoMap;
    

    private static String jsonListOfDevices;
    private static String jsonListOfContents;

    /*public static synchronized void initInstance() throws InterruptedException {
        if (mInstance == null) {
            mInstance = new DiscoveryService();
            mInstance.initialize();
        }
    }*

    public static synchronized DiscoveryService getInstance() {
        return mInstance;
    }*/

    public static void initialize() throws InterruptedException, ExecutionException {

        mInstance = new DiscoveryService();
        jsonListOfDevices = mInstance.getListener().getListDevice();

        JSONArray arrayOfContents = new JSONArray();
        for (String k1 : mVideoMap.keySet()) {
            ConcurrentHashMap<String, String> dir = mVideoMap.get(k1);
            //System.out.println("Chiave: " + k1);
            JSONObject insideObj = new JSONObject();
            JSONArray insideValue = new JSONArray();

            //System.out.println("Value:");
            for (String k2 : dir.keySet()) {
                JSONObject valueObj = new JSONObject();

                valueObj.put("url", k2);
                valueObj.put("name", dir.get(k2));

                insideValue.put(valueObj);

                //System.out.println("Chiave 2: " + k2 +  " value: "+ dir.get(k2));
            }

            insideObj.put("uuid", k1);
            insideObj.put("content", insideValue);
            arrayOfContents.put(insideObj);
        }

        jsonListOfContents = arrayOfContents.toString();

    }

    private DiscoveryService() throws InterruptedException, ExecutionException {
        mVideoMap = new ConcurrentHashMap<>();
        mServerServices = new ConcurrentHashMap<>();
        mRendererServices = new ConcurrentHashMap<>();
        mContentMap = new ConcurrentHashMap<>();
        mControlServices = new ConcurrentHashMap<>();
        
        upnpService = new UpnpServiceImpl();
        listener = new DMCUpnpListener(upnpService);
        upnpService.getRegistry().addListener(listener);
        upnpService.getControlPoint().search(new STAllHeader());
        Thread.sleep(7000);
        DiscoveryService.browse();
        
        upnpService.shutdown();
    }

    public DMCUpnpListener getListener() {
        return this.listener;
    }
    
    public void startUpnpService(){
        
    }
    
    public void stopUpnpService(){
        upnpService.shutdown();
    }
    
    
    
    public static void browse() throws InterruptedException, ExecutionException{
        
        //DiscoveryService.upnpService=new UpnpServiceImpl();
        
        for (String n : DiscoveryService.getmContentMap().keySet()) {
            ConcurrentHashMap<String, String> dir = DiscoveryService.getmContentMap().get(n);

            for (String k2 : dir.keySet()) {
                DiscoveryService.browseServerDirectory(DiscoveryService.getmServerServices().get(n), k2);
            }
        }
        
        //DiscoveryService.upnpService.shutdown();
        
    }
    
    

    public static void browseServerDirectory(RemoteService service, String containerID) throws InterruptedException, ExecutionException {
        final String serviceUUID = service.getReference().getUdn().toString().split("uuid:")[1];
        System.out.printf("Discovering directory %s on %s\n", containerID, serviceUUID);
        
        
        DiscoveryService.upnpService.getControlPoint().execute(new Browse(service, containerID, BrowseFlag.DIRECT_CHILDREN) {

            @Override
            public void received(ActionInvocation arg0, DIDLContent didl) {

                List<Item> mItemList = didl.getItems();
                if (mItemList.size() > 0) {
                    System.out.printf("Item List size %d\n", mItemList.size());
                    
                    if(!mVideoMap.containsKey(serviceUUID))
                        mVideoMap.put(serviceUUID, new ConcurrentHashMap<>());
                    for (Item i : didl.getItems()) {
                        String title = i.getTitle();
                        String id = i.getFirstResource().getValue();
                        System.out.printf("ITEM: title %s id %s url %s\n", title, i.getId(), id);
                        mVideoMap.get(serviceUUID).put(id, title);
                    }
                }
            }

            @Override
            public void updateStatus(Browse.Status arg0) {
                System.out.println("UpdateStatus!!! discovery content " + arg0.toString());
            }

            ;

                        @Override
            public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
                System.out.println("Failure discovery content: " + arg0.getFailure().getMessage());
            }
        ;

    }

    ).get();
        
    }
    
    
    public static void browseServer(RemoteService service) throws InterruptedException, ExecutionException {
        final String serviceUUID = service.getReference().getUdn().toString().split("uuid:")[1];
        System.out.println(serviceUUID);
        System.out.printf("Discovering on %s\n", serviceUUID);
        DiscoveryService.upnpService.getControlPoint().execute(
                new Browse(service, "0", BrowseFlag.DIRECT_CHILDREN) {

            @Override
            public void received(ActionInvocation arg0, DIDLContent didl) {

                List<Container> mContainerList = didl.getContainers();
                if (mContainerList.size() > 0) {
                    System.out.printf("number of dirs: %d\n", mContainerList.size());
                    if(!mContentMap.containsKey(serviceUUID))
                        mContentMap.put(serviceUUID, new ConcurrentHashMap<>());
                    for (Container c : didl.getContainers()) {
                        String title = c.getTitle();
                        String id = c.getId();
                        System.out.printf("DIR: name %s id %s\n", title, id);
                        mContentMap.get(serviceUUID).put(id, title);

                    }
                }
            }

            @Override
            public void updateStatus(Browse.Status arg0) {
                System.out.println("UpdateStatus!!! Discovery dir");
            }

            ;

                        @Override
            public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
                System.out.println("Failure Discovery dir: " + arg0.getFailure().getMessage());
            }
        ;

    }

    ).get();
        
        
    }
    
    
    public static void startOn(String uuid_server,String uuid_client,String url) throws InterruptedException, ExecutionException{
        Service mClient = mRendererServices.get(uuid_client);
        ConcurrentHashMap<String,String> mMedia = mVideoMap.get(uuid_server);
        String name = mMedia.get(url);
        
        mInstance.listener.setContent(mClient, url, name);
    }
    
    public static void playOn(String uuid_client) throws InterruptedException, ExecutionException{
        Service mClient = mRendererServices.get(uuid_client);
        mInstance.listener.playContent(mClient);
    }
    
    
    public static void pauseOn(String uuid_client) throws InterruptedException, ExecutionException{
        Service mClient = mRendererServices.get(uuid_client);
        mInstance.listener.pauseContent(mClient);
    }
    
    
    public static void changeVolume(String uuid_client,int flag,int volume,boolean mute) throws InterruptedException, ExecutionException{
        Service mClient = mControlServices.get(uuid_client);
        mInstance.listener.setVolume(mClient, flag,volume,mute);
    }
    
    public static void stopOn(String uuid_client) throws InterruptedException, ExecutionException{
        Service mClient = mRendererServices.get(uuid_client);
        mInstance.listener.stopContent(mClient);
    }
    
    
    
    public static String getJsonListOfDevices() {
        return jsonListOfDevices;
    }


    public static ConcurrentHashMap<String, ConcurrentHashMap<String, String>> getmContentMap() {
        return mContentMap;
    }

 

  
    public static ConcurrentHashMap<String, ConcurrentHashMap<String, String>> getmVideoMap() {
        return mVideoMap;
    }



    public static String getJsonListOfContents() {
        return jsonListOfContents;
    }

    public static ConcurrentHashMap<String, RemoteService> getmServerServices() {
        return mServerServices;
    }

    public static ConcurrentHashMap<String, RemoteService> getmRendererServices() {
        return mRendererServices;
    }

    public static ConcurrentHashMap<String, RemoteService> getmControlServices() {
        return mControlServices;
    }

    


}
