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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.fourthline.cling.support.avtransport.callback.Pause;
import org.fourthline.cling.support.avtransport.callback.Play;
import org.fourthline.cling.support.avtransport.callback.SetAVTransportURI;
import org.fourthline.cling.support.avtransport.callback.Stop;
import org.fourthline.cling.support.contentdirectory.callback.Browse;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;
import org.fourthline.cling.support.renderingcontrol.callback.GetMute;
import org.fourthline.cling.support.renderingcontrol.callback.GetVolume;
import org.fourthline.cling.support.renderingcontrol.callback.SetMute;
import org.fourthline.cling.support.renderingcontrol.callback.SetVolume;
import org.json.JSONArray;

/**
 *
 * @author mauriziofloridia
 *
 */
public class DMCUpnpListener extends DefaultRegistryListener {

    UpnpService upnpService;
    ServiceId avTrasportService = new UDAServiceId("AVTransport");
    ServiceId connectionService = new UDAServiceId("ConnectionManager");
    ServiceId contentDirectory = new UDAServiceId("ContentDirectory");
    Namespace namespace = new Namespace("schemas-upnp-org");

    String deviceFoundJSON;

    JSONArray listDevice = new JSONArray();
    Collection<Device> collectionDevice;
    DeviceItem deviceFound;
    
    public static int volume=-1;
    public static boolean mute;

    public DMCUpnpListener(UpnpService upnpService) {
        this.upnpService = upnpService;
    }

    @Override
    public void remoteDeviceAdded(Registry registry, RemoteDevice device) {

        if (device.getType().getType().equals("MediaServer")) {
            for (RemoteService service : device.getServices()) {
                if (service.getServiceType().getType().equals("ContentDirectory")) {
                    final String serviceUUID = service.getReference().getUdn().toString().split("uuid:")[1];
                    DiscoveryService.getmServerServices().put(serviceUUID, service);
                    try {

                        DiscoveryService.browseServer(service);

                    } catch (InterruptedException | ExecutionException ex) {
                        Logger.getLogger(DMCUpnpListener.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        if (device.getType().getType().equals("MediaRenderer")) {
            for (RemoteService service : device.getServices()) {
                if (service.getServiceType().getType().equals("AVTransport")) {
                    final String serviceUUID = service.getReference().getUdn().toString().split("uuid:")[1];
                    DiscoveryService.getmRendererServices().put(serviceUUID, service);
                }
                 if (service.getServiceType().getType().equals("RenderingControl")) {
                    final String serviceUUID = service.getReference().getUdn().toString().split("uuid:")[1];
                    DiscoveryService.getmControlServices().put(serviceUUID, service);

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
    public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
        Service serviceRender;
        DeviceDetails details = device.getDetails();
        if ((serviceRender = device.findService(avTrasportService)) != null) {
            System.out.println("Servizio non più disponibile: " + serviceRender.toString());
        }

    }

    public String getListDevice() {
        return this.listDevice.toString();
    }

    //Prima di chiudere la ricerca, vengono memorizzati i dispositivi trovati
    @Override
    public void beforeShutdown(Registry registry) {

        /*for (String n : DiscoveryService.getmContentMap().keySet()) {
            ConcurrentHashMap<String, String> dir = DiscoveryService.getmContentMap().get(n);

            for (String k2 : dir.keySet()) {
                DiscoveryService.browseServerDirectory(DiscoveryService.getmServerServices().get(n), k2);
            }

        }*/
        Gson g = new Gson();
        this.collectionDevice = registry.getDevices();
        LinkedList<Device> devices = new LinkedList<>();
        devices.addAll(collectionDevice);

        for (int i = 0; i < devices.size(); i++) {
            Device d = devices.get(i);
            System.out.println("Device Discoveder: " + d.getDetails().getFriendlyName() + " UUID: " + d.getIdentity().getUdn());
            System.out.println("Type: " + d.getType().getType() + "\n\n");
            listDevice.put(new DeviceItem((RemoteDevice) d).toJSON());

        }

    }

    @Override
    public void afterShutdown() {
        System.out.println("Ricerca Terminata");
    }

    ///// AGGIUNTE DA GABRIELE 
    /*
    @Override
    public void deviceAdded(Registry registry, Device device) {
        super.deviceAdded(registry, device);

        System.out.println("test "+device.getIdentity().getUdn().toString().contains(deviceUuid));
 
        if (device.getIdentity().getUdn().toString().contains(deviceUuid)) {
 
            Service[] services = device.findServices();
 
            for (Service service : services) {                
 
                if ("AVTransport".equals(service.getServiceType().getType())) {
 
                    System.out.println("device.getDisplayString(): "
 
                            + device.getDisplayString());
 
                }
 
            }
 
        }
    }*/
    public void playContent(final Service service) throws InterruptedException, ExecutionException {

        UpnpService ser = new UpnpServiceImpl();

        Future wait_to_finish;

        ActionCallback setPlayAVAction = new Play(service) {

            @Override

            public void failure(ActionInvocation invocation,
                    UpnpResponse operation, String defaultMsg) {

                System.out.println("AV Action Error " + defaultMsg);

            }

            @Override
            public void success(ActionInvocation invocation) {
                super.success(invocation);
                System.out.println("AV Action Success " + invocation.toString());
            }

        };

        setPlayAVAction.setControlPoint(ser.getControlPoint());
        wait_to_finish = ser.getControlPoint().execute(setPlayAVAction);

        wait_to_finish.get();

        ser.shutdown();

    }

    public void pauseContent(final Service service) throws InterruptedException, ExecutionException {

        UpnpService ser = new UpnpServiceImpl();

        Future wait_to_finish;

        ActionCallback setPauseAVAction = new Pause(service) {

            @Override

            public void failure(ActionInvocation invocation,
                    UpnpResponse operation, String defaultMsg) {

                System.out.println("AV Action Error " + defaultMsg);

            }

            @Override
            public void success(ActionInvocation invocation) {
                super.success(invocation);
                System.out.println("AV Action Success " + invocation.toString());
            }

        };

        setPauseAVAction.setControlPoint(ser.getControlPoint());
        wait_to_finish = ser.getControlPoint().execute(setPauseAVAction);

        wait_to_finish.get();

        ser.shutdown();

    }

    public void stopContent(final Service service) throws InterruptedException, ExecutionException {

        UpnpService ser = new UpnpServiceImpl();

        Future wait_to_finish;

        ActionCallback setStopAVAction = new Stop(service) {

            @Override

            public void failure(ActionInvocation invocation,
                    UpnpResponse operation, String defaultMsg) {

                System.out.println("AV Action Error " + defaultMsg);

            }

            @Override
            public void success(ActionInvocation invocation) {
                super.success(invocation);
                System.out.println("AV Action Success " + invocation.toString());
            }

        };

        setStopAVAction.setControlPoint(ser.getControlPoint());
        wait_to_finish = ser.getControlPoint().execute(setStopAVAction);

        wait_to_finish.get();

        ser.shutdown();

    }

    public void setContent(final Service service, final String videoURL, final String videoTitle) throws InterruptedException, ExecutionException {

        System.out.println("setContent started");

        UpnpService ser = new UpnpServiceImpl();

        Future wait_to_finish;

        ActionCallback setStopAction = new Stop(service) {

            @Override

            public void failure(ActionInvocation invocation,
                    UpnpResponse operation, String defaultMsg) {

                System.out.println("AV Action Error " + defaultMsg);

            }

            @Override
            public void success(ActionInvocation invocation) {
                super.success(invocation);
                System.out.println("AV Action Success " + invocation.toString());
            }

        };

        setStopAction.setControlPoint(ser.getControlPoint());
        wait_to_finish = ser.getControlPoint().execute(setStopAction);

        wait_to_finish.get();

        Thread.sleep(5000);

        //id=\""+item_id+"\" parentID=\""+parent_id+"\" DA METTERE AL TAG ITEM SE SERVONO
        String METADATA = "<DIDL-Lite xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\" xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\"><item restricted=\"0\" dlna:dlnaManaged=\"4\"><dc:title>" + videoTitle + "</dc:title><upnp:class>object.item.videoItem</upnp:class><dc:date>2011-07-26T16:52:00</dc:date><res protocolInfo=\"http-get:*:video/mp4:*\" size=\"67220492\" dlna:resumeUpload=\"0\">" + videoURL + "</res></item></DIDL-Lite>";

        System.out.println("Metadata: " + METADATA);

        ActionCallback setAVTransportURIAction = new SetAVTransportURI(service, videoURL, METADATA) {

            @Override

            public void failure(ActionInvocation invocation,
                    UpnpResponse operation, String defaultMsg) {

                System.out.println("AV Action Error " + defaultMsg);

            }

            @Override
            public void success(ActionInvocation invocation) {
                super.success(invocation);
                System.out.println("AV Action Success " + invocation.toString());
            }

        };

        setAVTransportURIAction.setControlPoint(ser.getControlPoint());

        wait_to_finish = ser.getControlPoint().execute(setAVTransportURIAction);

        //Thread.sleep(1000);
        wait_to_finish.get();

        ActionCallback setPlayAVAction = new Play(service) {

            @Override

            public void failure(ActionInvocation invocation,
                    UpnpResponse operation, String defaultMsg) {

                System.out.println("AV Action Error " + defaultMsg);

            }

            @Override
            public void success(ActionInvocation invocation) {
                super.success(invocation);
                System.out.println("AV Action Success " + invocation.toString());
            }

        };

        setPlayAVAction.setControlPoint(ser.getControlPoint());
        wait_to_finish = ser.getControlPoint().execute(setPlayAVAction);

        wait_to_finish.get();

        ser.shutdown();
    }

    public int getVolume(final Service service, final UpnpService ser) throws InterruptedException, ExecutionException {

        //UpnpService ser = new UpnpServiceImpl();
        Future wait_to_finish;
    

        ActionCallback getVolumeAVAction = new GetVolume(service) {

            @Override

            public void failure(ActionInvocation invocation,
                    UpnpResponse operation, String defaultMsg) {

                System.out.println("AV Action Error " + defaultMsg);

            }

            @Override
            public void success(ActionInvocation invocation) {
                super.success(invocation);
                System.out.println("AV Action Success " + invocation.toString());
            }

            @Override
            public void received(ActionInvocation ai, int i) {
                System.out.println("AV Action Success " + ai.toString() + " Volume " + i);
                volume=i;
            }

        };

        getVolumeAVAction.setControlPoint(ser.getControlPoint());
        wait_to_finish = ser.getControlPoint().execute(getVolumeAVAction);

      
        return volume;
        //ser.shutdown();

    }
    
    
    public boolean getMute(final Service service, final UpnpService ser) throws InterruptedException, ExecutionException {

        //UpnpService ser = new UpnpServiceImpl();
        Future wait_to_finish;

        ActionCallback getVolumeAVAction = new GetMute(service) {

            @Override

            public void failure(ActionInvocation invocation,
                    UpnpResponse operation, String defaultMsg) {

                System.out.println("AV Action Error " + defaultMsg);

            }

            @Override
            public void success(ActionInvocation invocation) {
                super.success(invocation);
                System.out.println("AV Action Success " + invocation.toString());
            }

            @Override
            public void received(ActionInvocation ai, boolean bln) {
               System.out.println("AV Action Success " + ai.toString() + " mute is " + bln);
               mute=bln;
            }

          

        };

        getVolumeAVAction.setControlPoint(ser.getControlPoint());
        wait_to_finish = ser.getControlPoint().execute(getVolumeAVAction);

        return mute;
        //ser.shutdown();

    }
    

    public void setVolume(final Service service, int flag,int volume,boolean mute) throws InterruptedException, ExecutionException {

        UpnpService ser = new UpnpServiceImpl();
        Future wait_to_finish;
        ActionCallback setVolumeAction = null;
       
        switch (flag) {
            case 0: //volup

                setVolumeAction = new SetVolume(service, volume) {

                    @Override

                    public void failure(ActionInvocation invocation,
                            UpnpResponse operation, String defaultMsg) {

                        System.out.println("AV Action Error " + defaultMsg);

                    }

                    @Override
                    public void success(ActionInvocation invocation) {
                        super.success(invocation);
                        System.out.println("AV Action Success " + invocation.toString());
                    }

                };
                break;
          
            case 1: //mute

                 
                setVolumeAction = new  SetMute(service, mute) {

                    @Override

                    public void failure(ActionInvocation invocation,
                            UpnpResponse operation, String defaultMsg) {

                        System.out.println("AV Action Error " + defaultMsg);

                    }

                    @Override
                    public void success(ActionInvocation invocation) {
                        super.success(invocation);
                        System.out.println("AV Action Success " + invocation.toString());
                    }

                };
                break;
            default:
                break;
                
        }

        setVolumeAction.setControlPoint(ser.getControlPoint());
        wait_to_finish = ser.getControlPoint().execute(setVolumeAction);

        wait_to_finish.get();

        ser.shutdown();

    }

   

    ////////////////
}
