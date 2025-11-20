package br.icmc.lasdpc.mqtt;

import org.eclipse.paho.client.mqttv3.*;

public class MQTTSetupClient {

    private static MQTTSetupClient instance;
    private MqttClient client;

    private MQTTSetupClient() throws MqttException {
        //client = new MqttClient("tcp://host.docker.internal:1883", "AgentMQTT");
        client = new MqttClient("tcp://127.0.0.1:1883", "");
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);

        // Set username and password for the MQTT broker
        options.setUserName("lasdpc");
        options.setPassword("lasdpc".toCharArray());

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
}

