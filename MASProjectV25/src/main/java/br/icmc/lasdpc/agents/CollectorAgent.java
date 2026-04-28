package br.icmc.lasdpc.agents;

import br.icmc.lasdpc.model.*;
import br.icmc.lasdpc.mqtt.MQTTSetupClient;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.paho.client.mqttv3.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class CollectorAgent extends Agent {

    private MQTTSetupClient mqttSetupClient;
    private Map<String, Sensor> sensors = new HashMap<>();
    private Map<String, Asset> assetsgroup = new HashMap<>();

    protected void setup() {
        try {
            mqttSetupClient = MQTTSetupClient.getInstance();
            MqttClient client = mqttSetupClient.getClient();

            String[] topics = {
                "lab1006/sensor/#",
                "lab1006/control/assets/#",
            };

            for (String topic : topics) {
                client.subscribe(topic);
            }

            client.setCallback(
                new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable cause) {
                        System.out.println("Connection lost");
                    }

                    @Override
                    public void messageArrived(
                        String topic,
                        MqttMessage message
                    ) {
                        String payload = new String(message.getPayload());
                        if (topic.startsWith("lab1006/sensor/")) {
                            Sensor sensor = Sensor.fromJson(payload);
                            if (sensor != null) {
                                sensors.put(sensor.getId(), sensor);
                                System.out.println(
                                    "Received Sensor message: " + sensor
                                );
                            }
                        } else if (topic.startsWith("lab1006/control/assets")) {
                            handleControlAssetsMessage(payload);
                        }
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {}
                }
            );

            addBehaviour(
                new TickerBehaviour(this, 20000) {
                    @Override
                    protected void onTick() {
                        System.out.println(
                            ">>>>>>> onTick ejecutado. Sensores: " +
                                sensors.size() +
                                ", Assets: " +
                                assetsgroup.size()
                        );
                        if (!assetsgroup.isEmpty() || !sensors.isEmpty()) {
                            double sum = 0;
                            int count = 0;
                            for (Sensor sensor : sensors.values()) {
                                if (
                                    sensor
                                        .getType()
                                        .contains("SensorTemperature")
                                ) {
                                    try {
                                        sum += Double.parseDouble(
                                            sensor.getSensedValue()
                                        );
                                        count++;
                                    } catch (NumberFormatException e) {
                                        System.out.println(
                                            "Valor inválido en sensor: " +
                                                sensor.getId()
                                        );
                                    }
                                }
                            }

                            if (count > 0) {
                                double average = sum / count;
                                System.out.printf(
                                    "Temperatura promedio (%d sensores): %.2f°C%n",
                                    count,
                                    average
                                );
                            } else {
                                System.out.println(
                                    "No hay sensores de temperatura disponibles en este ciclo."
                                );
                            }

                            DeviceData deviceData = new DeviceData(
                                assetsgroup,
                                sensors
                            );
                            String jsonData = DeviceData.toJson(deviceData);

                            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                            msg.addReceiver(getAID("ReasonerAgent"));
                            msg.setContent(jsonData);
                            send(msg);

                            assetsgroup.clear();
                            sensors.clear();
                        }
                    }
                }
            );
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void handleControlAssetsMessage(String payload) {
        try {
            JSONObject json = new JSONObject(payload);
            JSONArray assetsArray = json.getJSONArray("assets");

            System.out.println("\nGetting Asset information >>>>>>>>>");
            for (int i = 0; i < assetsArray.length(); i++) {
                JSONObject assetJson = assetsArray.getJSONObject(i);
                String id = assetJson.getString("id");
                String status = assetJson.getString("status");
                String type = assetJson.getString("type");

                Asset asset = new Asset(
                    type,
                    id,
                    json.getString("id"),
                    status,
                    json.getString("datetime")
                );
                assetsgroup.put(id, asset);

                System.out.println(":> " + assetJson.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void takeDown() {
        try {
            mqttSetupClient.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
