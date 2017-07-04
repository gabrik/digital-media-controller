/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.input.dmc;

import com.google.gson.annotations.Expose;

/**
 *
 * @author mauriziofloridia
 */
public class JSONformat {
    private  String name;
    private  String udn;
    private String details;
    private String model;
    private String type;
    private String host;
    private int port;

      
    
    public JSONformat(){}
    
    public void setName(String name){
        this.name = name;
     }
    
    public void setUDN(String udn){
        this.udn = udn;
    }
    
    public void setDetails(String details){
        this.details = details;
    }
    
    public void setModel(String model){ 
        this.model = model;
    }
    
    public void setURLHost(String host){
        this.host = host;
    }
    public void setURLPort(int port){
        this.port = port;
    }
    public void setType(String type){ 
        this.type = type;
    }
    

}