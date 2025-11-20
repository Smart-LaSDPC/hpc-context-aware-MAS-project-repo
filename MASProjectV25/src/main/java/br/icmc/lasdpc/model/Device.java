package br.icmc.lasdpc.model;

import com.google.gson.annotations.SerializedName;

public class Device {
    
    @SerializedName("type")
    private String type;
    
    @SerializedName("id")
    private String id;
    
    @SerializedName("mqtt_client_id")
    private String mqttClientId;
    
    @SerializedName("date_time")
    private String dateTime;

    public Device(String type, String id, String mqttClientId, String dateTime) {
        this.type = type;
        this.id = id;
        this.mqttClientId = mqttClientId;
        this.dateTime = dateTime;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public String getMqttClientId() {
        return mqttClientId;
    }

    public String getDateTime() {
        return dateTime;
    }

    @Override
    public String toString() {
        return "Device{" +
                "type='" + type + '\'' +
                ", id='" + id + '\'' +
                ", mqttClientId='" + mqttClientId + '\'' +
                ", dateTime='" + dateTime + '\'' +
                '}';
    }
}
