package br.icmc.lasdpc.model;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;

public class Sensor extends Device {
    
    @SerializedName("sensed_value")
    private String sensedValue;

    public Sensor(String type, String id, String mqttClientId, String sensedValue, String dateTime) {
        super(type, id, mqttClientId, dateTime);
        this.sensedValue = sensedValue;
    }

    public String getSensedValue() {
        return sensedValue; //I consider string because exist different type of sensor eg int, double and boolean values.
    }

    public static Sensor fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, Sensor.class);
    }

    public static String toJson(Object obj) {
        Gson gson = new Gson();
        return gson.toJson(obj);
    }

    public static List<Sensor> fromJsonList(String json) {
        Gson gson = new Gson();
        Type sensorListType = new TypeToken<List<Sensor>>() {}.getType();
        return gson.fromJson(json, sensorListType);
    }

    @Override
    public String toString() {
        return "Sensor{" +
                "type='" + getType() + '\'' +
                ", id='" + getId() + '\'' +
                ", mqttClientId='" + getMqttClientId() + '\'' +
                ", sensedValue='" + sensedValue + '\'' +
                ", dateTime='" + getDateTime() + '\'' +
                '}';
    }
}
