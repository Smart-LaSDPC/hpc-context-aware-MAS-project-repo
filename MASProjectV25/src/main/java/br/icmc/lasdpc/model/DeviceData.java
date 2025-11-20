package br.icmc.lasdpc.model;

import com.google.gson.Gson;
import java.util.Map;

public class DeviceData {
    private Map<String, Asset> assets;
    private Map<String, Sensor> sensors;

    public DeviceData(Map<String, Asset> assets, Map<String, Sensor> sensors) {
        this.assets = assets;
        this.sensors = sensors;
    }

    public Map<String, Asset> getAssets() {
        return assets;
    }

    public Map<String, Sensor> getSensors() {
        return sensors;
    }

    public static String toJson(DeviceData deviceData) {
        Gson gson = new Gson();
        return gson.toJson(deviceData);
    }

    public static DeviceData fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, DeviceData.class);
    }
}
