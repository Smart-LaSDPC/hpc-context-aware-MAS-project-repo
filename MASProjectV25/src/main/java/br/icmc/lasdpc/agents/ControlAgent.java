package br.icmc.lasdpc.agents;

import br.icmc.lasdpc.mqtt.MQTTSetupClient;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.eclipse.paho.client.mqttv3.*;

import com.google.gson.Gson;

import java.io.StringReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class ControlAgent extends Agent {

    private MQTTSetupClient mqttSetupClient;

    protected void setup() {
        try {
            mqttSetupClient = MQTTSetupClient.getInstance();
        } catch (MqttException e) {
            e.printStackTrace();
        }

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();

                if (msg != null) {
                    System.out.println("Received message from ReasonerAgent: " + msg.getSender());

                    String[] models = msg.getContent().split("\n---\n");
                    if (models.length == 2) {
                        String modelString = models[0];
                        String infModelString = models[1];

                        Model model = stringToModel(modelString);
                        Model infModel = stringToInfModel(infModelString, model);

                        //saveModel(infModel, "ontologies/lasdpc_v16_inference.rdf");

                        printAndPublishChangedIndividuals(model, infModel);
                    }
                } else {
                    block();
                }
            }
        });
    }

    private Model stringToModel(String modelString) {
        Model model = ModelFactory.createDefaultModel();
        model.read(new StringReader(modelString), null, "RDF/XML");
        return model;
    }

    private Model stringToInfModel(String infModelString, Model model) {
        Model infModel = ModelFactory.createDefaultModel();
        infModel.read(new StringReader(infModelString), null, "RDF/XML");
        return infModel;
    }

    private void saveModel(Model model, String fileName) {
        try (OutputStream out = new FileOutputStream(fileName)) {
            model.write(out, "RDF/XML-ABBREV");
            System.out.println("Model saved as " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void printAndPublishChangedIndividuals(Model model, Model infModel) {
        StmtIterator stmts = infModel.listStatements();
        while (stmts.hasNext()) {
            Statement stmt = stmts.nextStatement();
            
            Resource subject = stmt.getSubject();
            Property predicate = stmt.getPredicate();
            RDFNode object = stmt.getObject();        
            
            if (!model.contains(subject, predicate, object)) {
               
            	String id = subject.getLocalName();
                
                if(id != null && (id.contains("asset") || id.contains("sensor"))) {
                    // Retrieve the rdf:type of the subject
                	String type = id.contains("asset") ? "asset" : "sensor";
                    
                	// Get the value of the object
                    String stateValue = object.asLiteral().getLexicalForm();                	
                    
                    // Print and publish
                    System.out.print("Updating:> " + type + ":> " + id + " " + predicate.getLocalName() + " " + stateValue + "\n");
                    
                    publishChangedIndividual(id, type, stateValue);
                    
                }                
            }            
        }
    }

    private void publishChangedIndividual(String id, String type, String stateValue) {
        String topic = type.equals("asset") ? "lab1006/control/asset" : "lab1006/control/sensor";
        Map<String, String> payload = new HashMap<>();
        payload.put("id", id);
        payload.put("type", type);
        payload.put("state_value", stateValue);

        Gson gson = new Gson();
        String jsonPayload = gson.toJson(payload);

        try {
            MqttMessage message = new MqttMessage(jsonPayload.getBytes());
            message.setQos(2);
            mqttSetupClient.getClient().publish(topic, message);
            System.out.println("Published topic: " + topic + ", payload: " + jsonPayload);
        } catch (MqttException e) {
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
