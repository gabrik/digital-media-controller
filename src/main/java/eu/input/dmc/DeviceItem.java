/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.input.dmc;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import java.awt.List;
import java.net.URL;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.types.DeviceType;
import org.json.JSONObject;

/**
 *
 * @author mauriziofloridia
 */
public class DeviceItem {
   
    private UDN udn;
    private DeviceDetails details;
   
    @Expose(serialize = false, deserialize = false)
    private Device device;
    private URL url;
    private DeviceType type;
    
    private DeviceIdentity identity;

    
    public DeviceItem(RemoteDevice device) {
        
        this.udn = device.getIdentity().getUdn();
        this.device = device;
        this.details = device.getDetails();
        this.url = device.getIdentity().getDescriptorURL();
        this.type = device.getType();
    }
    
   

  
    
    public DeviceItem() {}



    public String getUdn() {
        return udn.toString();
    }

    public String getType(){
        return type.getType();
    }
    
    public String getIdenity(){
        return identity.toString();
    }
    
    public String getURLHost(){
        return url.getHost();
    }
    
    public int getURLport(){
        return url.getPort();
    }
    
    public String getFriendlyName(){
        return details.getFriendlyName();
    }
    
    public String getManufacturerDetails(){
        return details.getManufacturerDetails().getManufacturer();
    }
    
    public String getModelName(){
        return details.getModelDetails().getModelName();
    }
    
    public Device getDevice() {
        return device;
    }
   

    
    public JSONObject toJSON(){
        JSONObject thisDevice = new JSONObject();
        thisDevice.put("name",this.getFriendlyName() );
        thisDevice.put("uuid", this.getUdn().split("uuid:")[1]);
        thisDevice.put("type", this.getType());
        thisDevice.put("host", this.getURLHost());
        thisDevice.put("port", this.getURLport());
        
        //System.out.println(thisDevice);
        return thisDevice;
        
    }
    

    

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        DeviceItem that = (DeviceItem) o;

        if (!udn.equals(that.udn))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return udn.hashCode();
    }
    
    public String getJSONDescription(){
        Gson g = new Gson();
        return  g.toJson(device);
    }
    
    @Override
    public String toString() {
        String display;

        if (device.getDetails().getFriendlyName() != null)
            display = device.getDetails().getFriendlyName();
        else
            display = device.getDisplayString();

        // Display a little star while the device is being loaded (see
        // performance optimization earlier)
        return device.isFullyHydrated() ? display : display + " *";
    }
}
