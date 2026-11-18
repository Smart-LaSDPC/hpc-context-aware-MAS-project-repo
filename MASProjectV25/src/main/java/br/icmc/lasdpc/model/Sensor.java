package br.icmc.lasdpc.model;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;

public class Sensor extends Device {
    
    @SerializedName("sensed_value")
    private String sensedValue;

    @SerializedName("presence")
    private Presence presence;

    public static class Presence {
        @SerializedName("id_user")
        private String idUser;

        @SerializedName("rol")
        private String rol;

        public Presence() {}

        public Presence(String idUser, String rol) {
            this.idUser = idUser;
            this.rol = rol;
        }

        public String getIdUser() {
            return idUser;
        }

        public void setIdUser(String idUser) {
            this.idUser = idUser;
        }

        public String getRol() {
            return rol;
        }

        public void setRol(String rol) {
            this.rol = rol;
        }

        @Override
        public String toString() {
            return "Presence{" +
                    "idUser='" + idUser + '\'' +
                    ", rol='" + rol + '\'' +
                    '}';
        }
    }

    public Sensor(String type, String id, String mqttClientId, String sensedValue, String dateTime) {
        super(type, id, mqttClientId, dateTime);
        this.sensedValue = sensedValue;
    }

    public Sensor(String type, String id, String mqttClientId, String sensedValue, String dateTime, Presence presence) {
        super(type, id, mqttClientId, dateTime);
        this.sensedValue = sensedValue;
        this.presence = presence;
    }

    public String getSensedValue() {
        return sensedValue; //I consider string because exist different type of sensor eg int, double and boolean values.
    }

    public Presence getPresence() {
        return presence;
    }

    public void setPresence(Presence presence) {
        this.presence = presence;
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
                (presence != null ? ", presence=" + presence : "") +
                '}';
    }
}
