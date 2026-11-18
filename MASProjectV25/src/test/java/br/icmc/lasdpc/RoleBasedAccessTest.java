package br.icmc.lasdpc;

import br.icmc.lasdpc.agents.ReasonerAgent;
import br.icmc.lasdpc.model.Asset;
import br.icmc.lasdpc.model.DeviceData;
import br.icmc.lasdpc.model.Sensor;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Statement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class RoleBasedAccessTest {

    @Test
    @DisplayName("Test Sensor deserialization with presence metadata")
    public void testSensorPresenceDeserialization() {
        String payload = "{\n" +
                "      \"type\": \"SensorPresence\",\n" +
                "      \"id\": \"sensor_presence1\",\n" +
                "      \"mqtt_client_id\": \"SensorPresence-sensor-123\",\n" +
                "      \"agent_monitor_id\": \"AGENTX\",\n" +
                "      \"sensed_value\": 1,\n" +
                "      \"date_time\": \"11/15/2026 18:10:00\",\n" +
                "      \"presence\": {\n" +
                "        \"id_user\": \"user_professor1\",\n" +
                "        \"rol\": \"Professor\"\n" +
                "      }\n" +
                " }";

        Sensor sensor = Sensor.fromJson(payload);
        assertNotNull(sensor);
        assertEquals("SensorPresence", sensor.getType());
        assertEquals("sensor_presence1", sensor.getId());
        assertEquals("1", sensor.getSensedValue());
        assertNotNull(sensor.getPresence());
        assertEquals("user_professor1", sensor.getPresence().getIdUser());
        assertEquals("Professor", sensor.getPresence().getRol());

        // Test DeviceData serialization and deserialization roundtrip
        Map<String, Sensor> sensors = new HashMap<>();
        sensors.put(sensor.getId(), sensor);
        Map<String, Asset> assets = new HashMap<>();
        DeviceData data = new DeviceData(assets, sensors);

        String json = DeviceData.toJson(data);
        assertTrue(json.contains("\"presence\""));
        assertTrue(json.contains("\"user_professor1\""));
        assertTrue(json.contains("\"Professor\""));

        DeviceData deserialized = DeviceData.fromJson(json);
        assertNotNull(deserialized);
        Sensor s2 = deserialized.getSensors().get("sensor_presence1");
        assertNotNull(s2);
        assertNotNull(s2.getPresence());
        assertEquals("user_professor1", s2.getPresence().getIdUser());
        assertEquals("Professor", s2.getPresence().getRol());
    }

    @Test
    @DisplayName("Test ReasonerAgent execution for all 4 roles across both doors")
    public void testReasonerAgentRoleAccess() {
        ReasonerAgent reasonerAgent = new ReasonerAgent();

        String demoxNs = "http://www.icmc.usp.br/smartlasdpc#";

        // 1. Professor at sensor_presence1 (Lab door) -> Door Lab should open (state 1)
        {
            Map<String, Sensor> sensors = new HashMap<>();
            Sensor s1 = new Sensor("SensorPresence", "sensor_presence1", "client1", "1", "2026-11-15 18:10:00",
                    new Sensor.Presence("user_professor1", "Professor"));
            sensors.put(s1.getId(), s1);
            DeviceData dd = new DeviceData(new HashMap<>(), sensors);
            reasonerAgent.readOntologyFromFile(dd);

            OntModel model = org.apache.jena.rdf.model.ModelFactory.createOntologyModel();
            model.read("ontologies/lasdpc_v23.rdf_inference.rdf");
            Individual doorLab = model.getIndividual(demoxNs + "asset_door_lab1006");
            assertNotNull(doorLab);
            Property hasState = model.getProperty(demoxNs + "has_state");
            Statement stmt = doorLab.getProperty(hasState);
            assertNotNull(stmt, "Lab door has_state must exist");
            assertEquals("1", stmt.getObject().asLiteral().getLexicalForm(), "Professor must have access to Lab door");
        }

        // 2. Professor at sensor_presence2 (Datacenter door) -> Door Datacenter should open (state 1)
        {
            Map<String, Sensor> sensors = new HashMap<>();
            Sensor s2 = new Sensor("SensorPresence", "sensor_presence2", "client2", "1", "2026-11-15 18:10:00",
                    new Sensor.Presence("user_professor1", "professor"));
            sensors.put(s2.getId(), s2);
            DeviceData dd = new DeviceData(new HashMap<>(), sensors);
            reasonerAgent.readOntologyFromFile(dd);

            OntModel model = org.apache.jena.rdf.model.ModelFactory.createOntologyModel();
            model.read("ontologies/lasdpc_v23.rdf_inference.rdf");
            Individual doorDC = model.getIndividual(demoxNs + "asset_door_datacenter");
            assertNotNull(doorDC);
            Property hasState = model.getProperty(demoxNs + "has_state");
            Statement stmt = doorDC.getProperty(hasState);
            assertNotNull(stmt, "Datacenter door has_state must exist");
            assertEquals("1", stmt.getObject().asLiteral().getLexicalForm(), "Professor must have access to Datacenter door");
        }

        // 3. Student Postgraduate at sensor_presence1 (Lab door) -> Door Lab should open (state 1)
        {
            Map<String, Sensor> sensors = new HashMap<>();
            Sensor s1 = new Sensor("SensorPresence", "sensor_presence1", "client1", "1", "2026-11-15 18:10:00",
                    new Sensor.Presence("user_postgraduate1", "student_postgraduate"));
            sensors.put(s1.getId(), s1);
            DeviceData dd = new DeviceData(new HashMap<>(), sensors);
            reasonerAgent.readOntologyFromFile(dd);

            OntModel model = org.apache.jena.rdf.model.ModelFactory.createOntologyModel();
            model.read("ontologies/lasdpc_v23.rdf_inference.rdf");
            Individual doorLab = model.getIndividual(demoxNs + "asset_door_lab1006");
            assertNotNull(doorLab);
            Property hasState = model.getProperty(demoxNs + "has_state");
            Statement stmt = doorLab.getProperty(hasState);
            assertNotNull(stmt, "Lab door has_state must exist");
            assertEquals("1", stmt.getObject().asLiteral().getLexicalForm(), "Postgraduate student must have access to Lab door");
        }

        // 4. Student Postgraduate at sensor_presence2 (Datacenter door) -> Door Datacenter should be closed (state 0)
        {
            Map<String, Sensor> sensors = new HashMap<>();
            Sensor s2 = new Sensor("SensorPresence", "sensor_presence2", "client2", "1", "2026-11-15 18:10:00",
                    new Sensor.Presence("user_postgraduate1", "student_postgraduate"));
            sensors.put(s2.getId(), s2);
            DeviceData dd = new DeviceData(new HashMap<>(), sensors);
            reasonerAgent.readOntologyFromFile(dd);

            OntModel model = org.apache.jena.rdf.model.ModelFactory.createOntologyModel();
            model.read("ontologies/lasdpc_v23.rdf_inference.rdf");
            Individual doorDC = model.getIndividual(demoxNs + "asset_door_datacenter");
            assertNotNull(doorDC);
            Property hasState = model.getProperty(demoxNs + "has_state");
            Statement stmt = doorDC.getProperty(hasState);
            assertNotNull(stmt, "Datacenter door has_state must exist");
            assertEquals("0", stmt.getObject().asLiteral().getLexicalForm(), "Postgraduate student must NOT have access to Datacenter door");
        }

        // 5. Student Underground at sensor_presence1 (Lab door) -> Door Lab should open (state 1)
        {
            Map<String, Sensor> sensors = new HashMap<>();
            Sensor s1 = new Sensor("SensorPresence", "sensor_presence1", "client1", "1", "2026-11-15 18:10:00",
                    new Sensor.Presence("user_underground1", "student_underground"));
            sensors.put(s1.getId(), s1);
            DeviceData dd = new DeviceData(new HashMap<>(), sensors);
            reasonerAgent.readOntologyFromFile(dd);

            OntModel model = org.apache.jena.rdf.model.ModelFactory.createOntologyModel();
            model.read("ontologies/lasdpc_v23.rdf_inference.rdf");
            Individual doorLab = model.getIndividual(demoxNs + "asset_door_lab1006");
            assertNotNull(doorLab);
            Property hasState = model.getProperty(demoxNs + "has_state");
            Statement stmt = doorLab.getProperty(hasState);
            assertNotNull(stmt, "Lab door has_state must exist");
            assertEquals("1", stmt.getObject().asLiteral().getLexicalForm(), "Underground student must have access to Lab door");
        }

        // 6. Student Underground at sensor_presence2 (Datacenter door) -> Door Datacenter should be closed (state 0)
        {
            Map<String, Sensor> sensors = new HashMap<>();
            Sensor s2 = new Sensor("SensorPresence", "sensor_presence2", "client2", "1", "2026-11-15 18:10:00",
                    new Sensor.Presence("user_underground1", "student_underground"));
            sensors.put(s2.getId(), s2);
            DeviceData dd = new DeviceData(new HashMap<>(), sensors);
            reasonerAgent.readOntologyFromFile(dd);

            OntModel model = org.apache.jena.rdf.model.ModelFactory.createOntologyModel();
            model.read("ontologies/lasdpc_v23.rdf_inference.rdf");
            Individual doorDC = model.getIndividual(demoxNs + "asset_door_datacenter");
            assertNotNull(doorDC);
            Property hasState = model.getProperty(demoxNs + "has_state");
            Statement stmt = doorDC.getProperty(hasState);
            assertNotNull(stmt, "Datacenter door has_state must exist");
            assertEquals("0", stmt.getObject().asLiteral().getLexicalForm(), "Underground student must NOT have access to Datacenter door");
        }

        // 7. Unknowing at sensor_presence1 (Lab door) -> Door Lab should be closed (state 0)
        {
            Map<String, Sensor> sensors = new HashMap<>();
            Sensor s1 = new Sensor("SensorPresence", "sensor_presence1", "client1", "1", "2026-11-15 18:10:00",
                    new Sensor.Presence("user_unknowing1", "unknowing"));
            sensors.put(s1.getId(), s1);
            DeviceData dd = new DeviceData(new HashMap<>(), sensors);
            reasonerAgent.readOntologyFromFile(dd);

            OntModel model = org.apache.jena.rdf.model.ModelFactory.createOntologyModel();
            model.read("ontologies/lasdpc_v23.rdf_inference.rdf");
            Individual doorLab = model.getIndividual(demoxNs + "asset_door_lab1006");
            assertNotNull(doorLab);
            Property hasState = model.getProperty(demoxNs + "has_state");
            Statement stmt = doorLab.getProperty(hasState);
            assertNotNull(stmt, "Lab door has_state must exist");
            assertEquals("0", stmt.getObject().asLiteral().getLexicalForm(), "Unknowing user must NOT have access to Lab door");
        }

        // 8. Unknowing at sensor_presence2 (Datacenter door) -> Door Datacenter should be closed (state 0)
        {
            Map<String, Sensor> sensors = new HashMap<>();
            Sensor s2 = new Sensor("SensorPresence", "sensor_presence2", "client2", "1", "2026-11-15 18:10:00",
                    new Sensor.Presence("user_unknowing1", "unknowing"));
            sensors.put(s2.getId(), s2);
            DeviceData dd = new DeviceData(new HashMap<>(), sensors);
            reasonerAgent.readOntologyFromFile(dd);

            OntModel model = org.apache.jena.rdf.model.ModelFactory.createOntologyModel();
            model.read("ontologies/lasdpc_v23.rdf_inference.rdf");
            Individual doorDC = model.getIndividual(demoxNs + "asset_door_datacenter");
            assertNotNull(doorDC);
            Property hasState = model.getProperty(demoxNs + "has_state");
            Statement stmt = doorDC.getProperty(hasState);
            assertNotNull(stmt, "Datacenter door has_state must exist");
            assertEquals("0", stmt.getObject().asLiteral().getLexicalForm(), "Unknowing user must NOT have access to Datacenter door");
        }

        // 9. No presence at sensor_presence1 (value 0) -> Door Lab closes (state 0)
        {
            Map<String, Sensor> sensors = new HashMap<>();
            Sensor s1 = new Sensor("SensorPresence", "sensor_presence1", "client1", "0", "2026-11-15 18:10:00");
            sensors.put(s1.getId(), s1);
            DeviceData dd = new DeviceData(new HashMap<>(), sensors);
            reasonerAgent.readOntologyFromFile(dd);

            OntModel model = org.apache.jena.rdf.model.ModelFactory.createOntologyModel();
            model.read("ontologies/lasdpc_v23.rdf_inference.rdf");
            Individual doorLab = model.getIndividual(demoxNs + "asset_door_lab1006");
            assertNotNull(doorLab);
            Property hasState = model.getProperty(demoxNs + "has_state");
            Statement stmt = doorLab.getProperty(hasState);
            assertNotNull(stmt, "Lab door has_state must exist");
            assertEquals("0", stmt.getObject().asLiteral().getLexicalForm(), "No presence must close Lab door");
        }

        // 10. No presence at sensor_presence2 (value 0) -> Door Datacenter closes (state 0)
        {
            Map<String, Sensor> sensors = new HashMap<>();
            Sensor s2 = new Sensor("SensorPresence", "sensor_presence2", "client2", "0", "2026-11-15 18:10:00");
            sensors.put(s2.getId(), s2);
            DeviceData dd = new DeviceData(new HashMap<>(), sensors);
            reasonerAgent.readOntologyFromFile(dd);

            OntModel model = org.apache.jena.rdf.model.ModelFactory.createOntologyModel();
            model.read("ontologies/lasdpc_v23.rdf_inference.rdf");
            Individual doorDC = model.getIndividual(demoxNs + "asset_door_datacenter");
            assertNotNull(doorDC);
            Property hasState = model.getProperty(demoxNs + "has_state");
            Statement stmt = doorDC.getProperty(hasState);
            assertNotNull(stmt, "Datacenter door has_state must exist");
            assertEquals("0", stmt.getObject().asLiteral().getLexicalForm(), "No presence must close Datacenter door");
        }
    }
}
