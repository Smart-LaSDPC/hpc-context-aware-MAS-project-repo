package br.icmc.lasdpc.mqtt;

import org.eclipse.paho.client.mqttv3.*;

public class MQTTSetupClient {

    private static MQTTSetupClient instance;
    private MqttClient client;

    private MQTTSetupClient() throws MqttException {
    	
        String brokerUrl = getenv("MQTT_BROKER_URL", "tcp://127.0.0.1:1883");
        String clientId  = getenv("MQTT_CLIENT_ID", "agent-" + System.currentTimeMillis());
        String username  = getenv("MQTT_USERNAME", null);
        String password  = getenv("MQTT_PASSWORD", null);
    	
        /*
            MQTT_BROKER_URL: "andromeda.lasdpc.icmc.usp.br:6183"
      		MQTT_CLIENT_ID: "agent-masproject"
      		MQTT_USERNAME: "lasdpc"
      		MQTT_PASSWORD: "l@sdpC10"        
        */
        client = new MqttClient("tcp://andromeda.lasdpc.icmc.usp.br:6183", "AgentMQTT");
        //client = new MqttClient(brokerUrl, clientId);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);

        // Set username and password for the MQTT broker
        options.setUserName("lasdpc");
        options.setPassword("l@sdpC10".toCharArray());

        client.connect(options);
    }

    public static synchronized MQTTSetupClient getInstance() throws MqttException {
        if (instance == null) {
            instance = new MQTTSetupClient();
        }
        return instance;
    }

    public MqttClient getClient() {
        return client;
    }

    public void disconnect() throws MqttException {
        if (client != null && client.isConnected()) {
            client.disconnect();
        }
    }

    public void setCallback(MqttCallback callback) {
        client.setCallback(callback);
    }
    
    private static String getenv(String key, String def) {
        String v = System.getenv(key);
        return (v != null && !v.isBlank()) ? v : def;
    }
}

