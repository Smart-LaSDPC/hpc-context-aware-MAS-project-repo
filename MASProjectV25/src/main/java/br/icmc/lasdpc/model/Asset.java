package br.icmc.lasdpc.model;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;

public class Asset extends Device {
    
    @SerializedName("state_value")
    private String stateValue;

    public Asset(String type, String id, String mqttClientId, String stateValue, String dateTime) {
        super(type, id, mqttClientId, dateTime);
        this.stateValue = stateValue;
    }

    public String getStateValue() {
        return stateValue;
    }

    public static Asset fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, Asset.class);
    }

    public static String toJson(Object obj) {
        Gson gson = new Gson();
        return gson.toJson(obj);
    }

    public static List<Asset> fromJsonList(String json) {
        Gson gson = new Gson();
        Type assetListType = new TypeToken<List<Asset>>() {}.getType();
        return gson.fromJson(json, assetListType);
    }

    @Override
    public String toString() {
        return "Asset{" +
                "type='" + getType() + '\'' +
                ", id='" + getId() + '\'' +
                ", mqttClientId='" + getMqttClientId() + '\'' +
                ", stateValue='" + stateValue + '\'' +
                ", dateTime='" + getDateTime() + '\'' +
                '}';
    }
}
