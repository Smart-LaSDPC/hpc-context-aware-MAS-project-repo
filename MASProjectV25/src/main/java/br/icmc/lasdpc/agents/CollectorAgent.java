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
                        if (!assetsgroup.isEmpty() || !sensors.isEmpty()) {
                            double sumServers = 0,
                                sumPeople = 0;
                            int countServers = 0,
                                countPeople = 0;

                            java.util.Set<String> serverSensors =
                                new java.util.HashSet<>(
                                    java.util.Arrays.asList(
                                        "sensor_temperature1",
                                        "sensor_temperature2",
                                        "sensor_temperature3"
                                    )
                                );

                            for (Sensor sensor : sensors.values()) {
                                if (
                                    sensor
                                        .getType()
                                        .contains("SensorTemperature")
                                ) {
                                    try {
                                        double val = Double.parseDouble(
                                            sensor.getSensedValue()
                                        );
                                        if (
                                            serverSensors.contains(
                                                sensor.getId()
                                            )
                                        ) {
                                            sumServers += val;
                                            countServers++;
                                        } else {
                                            sumPeople += val;
                                            countPeople++;
                                        }
                                    } catch (NumberFormatException e) {
                                        System.out.println(
                                            "Invalid value from sensor: " +
                                                sensor.getId()
                                        );
                                    }
                                }
                            }

                            if (countServers > 0) {
                                double avg = sumServers / countServers;
                                System.out.printf(
                                    "Average temperature for servers (%d sensors): %.2f°C%n",
                                    countServers,
                                    avg
                                );
                                sensors.put(
                                    "sensor_temperature_avg_servers",
                                    new Sensor(
                                        "SensorTemperature",
                                        "sensor_temperature_avg_servers",
                                        "collector-internal",
                                        String.format(
                                            java.util.Locale.US,
                                            "%.2f",
                                            avg
                                        ),
                                        ""
                                    )
                                );
                            }

                            if (countPeople > 0) {
                                double avg = sumPeople / countPeople;
                                System.out.printf(
                                    "Average temperature for people (%d sensors): %.2f°C%n",
                                    countPeople,
                                    avg
                                );
                                sensors.put(
                                    "sensor_temperature_avg_people",
                                    new Sensor(
                                        "SensorTemperature",
                                        "sensor_temperature_avg_people",
                                        "collector-internal",
                                        String.format(
                                            java.util.Locale.US,
                                            "%.2f",
                                            avg
                                        ),
                                        ""
                                    )
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
