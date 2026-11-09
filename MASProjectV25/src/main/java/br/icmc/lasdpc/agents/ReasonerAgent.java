package br.icmc.lasdpc.agents;

import br.icmc.lasdpc.model.*;
import br.icmc.lasdpc.utils.CustomFunctions2;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.PrintUtil;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.XSD;

public class ReasonerAgent extends Agent {

    String inputFileName = "ontologies/lasdpc_v23.rdf";

    protected void setup() {
        addBehaviour(
            new CyclicBehaviour() {
                @Override
                public void action() {
                    ACLMessage msg = receive();

                    if (msg != null) {
                        System.out.println(
                            "Received message from AgentSender: " +
                                msg.getSender()
                        );
                        try {
                            DeviceData deviceData = DeviceData.fromJson(
                                msg.getContent()
                            );

                            // generateOntology(deviceData);
                            readOntologyFromFile(deviceData);

                            //Sensor sensorDelete = deviceData.getSensors().get("presence1");
                            //System.out.println("Inside Reasoner >>>> "+ sensorDelete.toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        block();
                    }
                }
            }
        );
    }

    public void readOntologyFromFile(DeviceData deviceData) {
        //String inputFileName = "ontologies/lasdpc_v16.rdf";
        // Create an ontology model
        OntModel model = ModelFactory.createOntologyModel(
            OntModelSpec.OWL_DL_MEM
        );

        // Use the FileManager to find the input file
        InputStream in = FileManager.get().open(inputFileName);
        if (in == null) {
            throw new IllegalArgumentException(
                "File: " + inputFileName + " not found"
            );
        }

        // Read the RDF/XML file
        model.read(in, null);

        // Define the namespace URIs

        //String demoNs = "http://www.ex.com/demox/"; //This is only for Class
        String demoxNs = "http://www.icmc.usp.br/smartlasdpc#"; //This is to Data Properties

        PrintUtil.registerPrefix("eg", demoxNs);

        Property hasStateProperty = model.getProperty(demoxNs + "has_state");
        Property hasValueProperty = model.getProperty(demoxNs + "has_value");
        // Role of the user detected by a presence sensor (used by the data center access rules)
        Property detectedRoleProperty = model.createDatatypeProperty(demoxNs + "detected_role");

        // Get all individuals of type AssetLamp
        OntClass assetLampClass = model.getOntClass(demoxNs + "AssetLamp");
        for (
            ExtendedIterator<? extends OntResource> it =
                assetLampClass.listInstances();
            it.hasNext();

        ) {
            Individual ind = (Individual) it.next();
            if (ind.hasProperty(hasStateProperty)) {
                ind.removeAll(hasStateProperty);
                System.out.println("Removing AssetLamp: " + ind.getLocalName());
            }
        }

        // Get all individuals of type SensorPresence
        OntClass sensorPresenceClass = model.getOntClass(
            demoxNs + "SensorPresence"
        );
        for (
            ExtendedIterator<? extends OntResource> it =
                sensorPresenceClass.listInstances();
            it.hasNext();

        ) {
            Individual ind = (Individual) it.next();
            if (ind.hasProperty(hasValueProperty)) {
                ind.removeAll(hasValueProperty);
                System.out.println(
                    "Removing SensorPresence: " + ind.getLocalName()
                );
            }
            ind.removeAll(detectedRoleProperty);
        }

        //Get all individuals of type sensorTemperature
        OntClass sensorTemperatureClass = model.getOntClass(
            demoxNs + "SensorTemperature"
        );
        for (
            ExtendedIterator<? extends OntResource> it =
                sensorTemperatureClass.listInstances();
            it.hasNext();

        ) {
            Individual ind = (Individual) it.next();
            if (ind.hasProperty(hasValueProperty)) {
                ind.removeAll(hasValueProperty);
                System.out.println(
                    "Removing SensorTemperature: " + ind.getLocalName()
                );
            }
        }

        //Get all individuals of type AssetAirConditioning
        OntClass assetACClass = model.getOntClass(
            demoxNs + "AssetAirConditioning"
        );
        if (assetACClass != null) {
            for (
                ExtendedIterator<? extends OntResource> it =
                    assetACClass.listInstances();
                it.hasNext();

            ) {
                Individual ind = (Individual) it.next();
                if (ind.hasProperty(hasStateProperty)) {
                    ind.removeAll(hasStateProperty);
                    System.out.println(
                        "Removing AssetAirConditioning: " + ind.getLocalName()
                    );
                }
            }
        }

        OntClass assetHeatClass = model.getOntClass(demoxNs + "AssetHeat");
        if (assetHeatClass != null) {
            for (
                ExtendedIterator<? extends OntResource> it =
                    assetHeatClass.listInstances();
                it.hasNext();

            ) {
                Individual ind = (Individual) it.next();
                if (ind.hasProperty(hasStateProperty)) {
                    ind.removeAll(hasStateProperty);
                    System.out.println(
                        "Removing AssetHeat: " + ind.getLocalName()
                    );
                }
            }
        }

        //Fill the ontology with values getting from Control Assets
        for (Map.Entry<String, Asset> entry : deviceData
            .getAssets()
            .entrySet()) {
            Asset asset = entry.getValue();
            int state = "ON".equals(asset.getStateValue()) ? 1 : 0;
            Individual indvAsset = model.createIndividual(
                demoxNs + asset.getId(),
                model.getOntClass(demoxNs + asset.getType())
            );
            indvAsset.addProperty(
                hasStateProperty,
                model.createTypedLiteral(state, XSDDatatype.XSDinteger)
            );
        }

        //Fill the ontology with values getting from Sensors
        for (Map.Entry<String, Sensor> entry : deviceData
            .getSensors()
            .entrySet()) {
            Sensor sensor = entry.getValue();

            if (sensor.getType().contains("SensorPresence")) {
                Individual sPresence = model.createIndividual(
                    demoxNs + sensor.getId(),
                    model.getOntClass(demoxNs + sensor.getType())
                );
                sPresence.addProperty(
                    hasValueProperty,
                    model.createTypedLiteral(
                        Integer.parseInt(sensor.getSensedValue()),
                        XSDDatatype.XSDinteger
                    )
                );
                if (sensor.getPresence() != null && sensor.getPresence().getRol() != null) {
                    sPresence.addProperty(detectedRoleProperty, sensor.getPresence().getRol());
                    System.out.println(
                        "Presence " + sensor.getId() + " role: " + sensor.getPresence().getRol()
                    );
                }
            } else if (sensor.getType().contains("SensorTemperature")) {
                Individual sTemperature = model.createIndividual(
                    demoxNs + sensor.getId(),
                    model.getOntClass(demoxNs + sensor.getType())
                );
                sTemperature.addProperty(
                    hasValueProperty,
                    model.createTypedLiteral(
                        Float.parseFloat(sensor.getSensedValue()),
                        XSDDatatype.XSDfloat
                    )
                );
            }
        }

        // Register custom functions
        CustomFunctions2.registerCustomFunctions();
        CustomFunctions2.setModel(model);

        // Create rule
        String rules =
            // Rule to turn off the lamps when anybody stay on the laboratory.
            //"[r1: (?l rdf:type eg:AssetLamp), (?s rdf:type eg:SensorPresence), (?s eg:has_value 0) -> changeStatePropertyInteger(?l, eg:has_state, 0)]"+
            "[r1: (?l rdf:type eg:AssetLamp), (eg:sensor_presence1 eg:has_value 0), (eg:sensor_presence2 eg:has_value 0) -> changeStatePropertyInteger(?l, eg:has_state, 0)]" +
            // Rule to turn ON when recognise a user on front of the door.
            "[r2: (?l rdf:type eg:AssetLamp), (?l eg:has_state 0), (eg:sensor_presence1 eg:has_value 1) -> changeStatePropertyInteger(?l, eg:has_state, 1), changeStatePropertyInteger(eg:asset_door_lab1006, eg:has_state, 1)]" +
            //Rule to CLOSE the door when not recognise a user on front of the door.
            "[r3: (eg:asset_door_lab1006 eg:has_state 1), (eg:sensor_presence1 eg:has_value 0) -> changeStatePropertyInteger(eg:asset_door_lab1006, eg:has_state, 0)]" +
            // Additional rules
            // Rule to classify room as "unoccupied" based on sensor data
            "[r4: (?r rdf:type eg:Room), (eg:sensor_presence2 eg:has_value 0), (?s eg:installedIn ?r) -> changeStatePropertyString(?r, eg:has_occupancy_status, 'unoccupied')] " +
            // Rule to classify room as "occupied" based on sensor data
            "[r5: (?r rdf:type eg:Room), (eg:sensor_presence2 eg:has_value 1), (?s eg:installedIn ?r) -> changeStatePropertyString(?r, eg:has_occupancy_status, 'occupied')] " +
            // Server room temperature control 17°C
            "[r6: (eg:sensor_temperature_avg_servers eg:has_value ?v), greaterThan(?v, 17.0) -> changeStatePropertyInteger(eg:asset_aircond1, eg:has_state, 1), changeStatePropertyInteger(eg:asset_aircond2, eg:has_state, 1), changeStatePropertyInteger(eg:asset_aircond3, eg:has_state, 1)]" +
            "[r7: (eg:sensor_temperature_avg_servers eg:has_value ?v), lessThanOrEqual(?v, 17.0) -> changeStatePropertyInteger(eg:asset_aircond1, eg:has_state, 0), changeStatePropertyInteger(eg:asset_aircond2, eg:has_state, 0), changeStatePropertyInteger(eg:asset_aircond3, eg:has_state, 0)]" +
            // People room temperature control 22°C
            "[r8: (eg:sensor_temperature_avg_people eg:has_value ?v), greaterThan(?v, 22.0) -> changeStatePropertyInteger(eg:asset_aircond4, eg:has_state, 1), changeStatePropertyInteger(eg:asset_aircond5, eg:has_state, 1)]" +
            "[r9: (eg:sensor_temperature_avg_people eg:has_value ?v), lessThanOrEqual(?v, 22.0) -> changeStatePropertyInteger(eg:asset_aircond4, eg:has_state, 0), changeStatePropertyInteger(eg:asset_aircond5, eg:has_state, 0)]" +
            // Data center access: only a professor detected at the data center door opens it
            "[r10: (eg:sensor_presence3 eg:has_value 1), (eg:sensor_presence3 eg:detected_role 'Professor') -> changeStatePropertyInteger(eg:asset_door_datacenter, eg:has_state, 1)]" +
            // Any other role keeps the data center door closed
            "[r11: (eg:sensor_presence3 eg:has_value 1), (eg:sensor_presence3 eg:detected_role ?r), notEqual(?r, 'Professor') -> changeStatePropertyInteger(eg:asset_door_datacenter, eg:has_state, 0)]" +
            // Nobody at the data center door: close it
            "[r12: (eg:sensor_presence3 eg:has_value 0) -> changeStatePropertyInteger(eg:asset_door_datacenter, eg:has_state, 0)]";
        // Combine your original rules with the new ones
        //String combinedRules = rules + additionalRules;

        // Create a reasoner with the specified rules
        Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
        reasoner.setDerivationLogging(true);

        // Create an inference model
        InfModel inf = ModelFactory.createInfModel(reasoner, model);

        // Send both models to ControllerAgent
        sendModels(model, inf);

        String fileNameChanged = inputFileName + "_changed.rdf";
        saveModel1(model, fileNameChanged);
        String fileNameInference = inputFileName + "_inference.rdf";
        saveModel2(inf, fileNameInference);
    }

    private static void saveModel1(OntModel model, String fileName) {
        try (OutputStream out = new FileOutputStream(fileName)) {
            model.write(out, "RDF/XML-ABBREV");
            System.out.println("OntModel saved as " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveModel2(Model model, String fileName) {
        try (OutputStream out = new FileOutputStream(fileName)) {
            model.write(out, "RDF/XML-ABBREV");
            System.out.println("Model saved as " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendModels(OntModel model, InfModel infModel) {
        try {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(getAID("ControlAgent"));

            // Serialize the models to strings
            String modelString = modelToString(model);
            String infModelString = modelToString(infModel);

            // Set the models as message content
            msg.setContent(modelString + "\n---\n" + infModelString);
            send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String modelToString(Model model) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            model.write(out, "RDF/XML-ABBREV");
            return out.toString("UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void printInference(InfModel inf, Individual individual) {
        StmtIterator stmts = inf.listStatements(
            individual,
            (Property) null,
            (RDFNode) null
        );
        while (stmts.hasNext()) {
            Statement stmt = stmts.nextStatement();
            Resource subject = stmt.getSubject();
            Property predicate = stmt.getPredicate();
            RDFNode object = stmt.getObject();

            System.out.print(subject.getLocalName());
            System.out.print(" " + predicate.getLocalName() + " ");
            if (object instanceof Resource) {
                System.out.print(((Resource) object).getLocalName());
            } else {
                System.out.print(
                    " \"" + object.asLiteral().getLexicalForm() + "\""
                );
            }
            System.out.println(".");
        }
    }
}
