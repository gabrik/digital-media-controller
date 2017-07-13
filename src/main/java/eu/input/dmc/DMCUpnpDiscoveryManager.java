package eu.input.dmc;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.message.header.STAllHeader;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.contentdirectory.callback.Browse;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;
import org.json.JSONArray;
import org.json.JSONObject;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author gabriele
 */
public class DMCUpnpDiscoveryManager {
    
    private UpnpService upnpService;
    private DMCUpnpListener listener;

    
    
    
    private  ConcurrentHashMap<String, ConcurrentHashMap<String, String>> mContentMap;
    private  ConcurrentHashMap<String, RemoteService> mServerServices;
    private  ConcurrentHashMap<String, RemoteService> mRendererServices;
    private  ConcurrentHashMap<String, RemoteService> mControlServices;
    private  ConcurrentHashMap<String, ConcurrentHashMap<String, String>> mVideoMap;
    private  ConcurrentHashMap<String,List<String>> mDirMap;
    

    private  String jsonListOfDevices;
    private  String jsonListOfContents;
    
    
    private static DMCUpnpDiscoveryManager mInstance;
    
    private DMCUpnpDiscoveryManager(){
        mVideoMap = new ConcurrentHashMap<>();
        mServerServices = new ConcurrentHashMap<>();
        mRendererServices = new ConcurrentHashMap<>();
        mContentMap = new ConcurrentHashMap<>();
        mControlServices = new ConcurrentHashMap<>();
        mDirMap = new ConcurrentHashMap<>();
        upnpService = new UpnpServiceImpl();
    }
    
    public static void initInstance(){
        if(mInstance==null)
            mInstance=new DMCUpnpDiscoveryManager();
        
    }
    
    public static DMCUpnpDiscoveryManager getInstance(){
        return mInstance;
    }
    
    
    
    public void init(){
        listener = new DMCUpnpListener(upnpService,mInstance);
        upnpService.getRegistry().addListener(listener);
        
    }
    
    
    public void stop(){
        upnpService.shutdown();
    }
    
    
    public void discover() throws InterruptedException, ExecutionException{
        
        /*mVideoMap.clear();
        mServerServices.clear();
        mRendererServices.clear();
        mContentMap.clear();
        mControlServices.clear();
        mDirMap.clear();*/
        
        upnpService.getControlPoint().search(new STAllHeader());
        
        Thread.sleep(7000);
        System.out.println("Cerco nella cartella");

        
        //browse();
        
        jsonListOfDevices = listener.getListDevice();
        
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

                for(String k : mDirMap.keySet()){
                    List<String> media = mDirMap.get(k);
                    if(media.contains(valueObj.getString("url")))
                        valueObj.put("directory",mContentMap.get(k1).get(k));
                }

                insideValue.put(valueObj);

                //System.out.println("Chiave 2: " + k2 +  " value: "+ dir.get(k2));
            }

            insideObj.put("uuid", k1);
            insideObj.put("content", insideValue);
            arrayOfContents.put(insideObj);
        }

        
        
        jsonListOfContents = arrayOfContents.toString();
        
        
    }
    
    public void browse() throws InterruptedException, ExecutionException{
        
        
        for (String n : getmContentMap().keySet()) {
            ConcurrentHashMap<String, String> dir = getmContentMap().get(n);

            for (String k2 : dir.keySet()) {
                browseServerDirectory(getmServerServices().get(n), k2,dir.get(k2));
            }
        }
               
    }
    
    

    public void browseServerDirectory(RemoteService service, String containerID,String containerName) throws InterruptedException, ExecutionException {
        final String serviceUUID = service.getReference().getUdn().toString().split("uuid:")[1];
        System.out.printf("Discovering directory %s %s on %s\n", containerID,containerName, serviceUUID);
        
        
        upnpService.getControlPoint().execute(new Browse(service, containerID, BrowseFlag.DIRECT_CHILDREN) {

            @Override
            public void received(ActionInvocation arg0, DIDLContent didl) {

                List<Item> mItemList = didl.getItems();
                if (mItemList.size() > 0) {
                    System.out.printf("Item List size %d\n", mItemList.size());
                    
                    if(!mVideoMap.containsKey(serviceUUID))
                        mVideoMap.put(serviceUUID, new ConcurrentHashMap<>());
                    if(!mDirMap.containsKey(containerID))
                        mDirMap.put(containerID, new LinkedList<>());
                    for (Item i : didl.getItems()) {
                        String title = i.getTitle();
                        String id = i.getFirstResource().getValue();
                        System.out.printf("ITEM: title %s id %s url %s\n", title, i.getId(), id);
                        if(!mVideoMap.get(serviceUUID).containsKey(id))
                           mVideoMap.get(serviceUUID).put(id, title);
                        //if(!mDirMap.get(serviceUUID).contains(id))
                           mDirMap.get(containerID).add(id);
                                
                        
                    
                        
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
    
    
    public void browseServer(RemoteService service) throws InterruptedException, ExecutionException {
        final String serviceUUID = service.getReference().getUdn().toString().split("uuid:")[1];
        System.out.println(serviceUUID);
        System.out.printf("Discovering on %s\n", serviceUUID);
        upnpService.getControlPoint().execute(
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
                        if(!mContentMap.get(serviceUUID).containsKey(id))
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
    
    
    public void startOn(String uuid_server,String uuid_client,String url) throws InterruptedException, ExecutionException{
        Service mClient = mRendererServices.get(uuid_client);
        ConcurrentHashMap<String,String> mMedia = mVideoMap.get(uuid_server);
        String name = mMedia.get(url);
        
        mInstance.listener.setContent(mClient, url, name);
    }
    
    public void playOn(String uuid_client) throws InterruptedException, ExecutionException{
        Service mClient = mRendererServices.get(uuid_client);
        mInstance.listener.playContent(mClient);
    }
    
    
    public void pauseOn(String uuid_client) throws InterruptedException, ExecutionException{
        Service mClient = mRendererServices.get(uuid_client);
        mInstance.listener.pauseContent(mClient);
    }
    
    
    public void changeVolume(String uuid_client,int flag,int volume,boolean mute) throws InterruptedException, ExecutionException{
        Service mClient = mControlServices.get(uuid_client);
        mInstance.listener.setVolume(mClient, flag,volume,mute);
    }
    
    public void stopOn(String uuid_client) throws InterruptedException, ExecutionException{
        Service mClient = mRendererServices.get(uuid_client);
        mInstance.listener.stopContent(mClient);
    }
    
    
    
    public String getJsonListOfDevices() {
        return jsonListOfDevices;
    }


    public ConcurrentHashMap<String, ConcurrentHashMap<String, String>> getmContentMap() {
        return mContentMap;
    }

 

  
    public ConcurrentHashMap<String, ConcurrentHashMap<String, String>> getmVideoMap() {
        return mVideoMap;
    }



    public String getJsonListOfContents() {
        return jsonListOfContents;
    }

    public ConcurrentHashMap<String, RemoteService> getmServerServices() {
        return mServerServices;
    }

    public ConcurrentHashMap<String, RemoteService> getmRendererServices() {
        return mRendererServices;
    }

    public ConcurrentHashMap<String, RemoteService> getmControlServices() {
        return mControlServices;
    }

    public ConcurrentHashMap<String, List<String>> getmDirMap() {
        return mDirMap;
    }
    
    
    
    
    
    
}
