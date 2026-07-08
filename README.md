# Multi-Agent System (MAS)

## Intelligent Multi-Agent Platform for Smart Buildings

Welcome to the **Multi-Agent System (MAS)** component of the Smart-Lab platform.

The MAS is a scalable and context-aware platform designed to autonomously manage IoT sensors and smart devices in intelligent environments. It combines **Multi-Agent Systems**, **Ontologies**, and the **MQTT communication protocol** to provide real-time monitoring, reasoning, and automatic control of laboratory resources. The architecture presented in this repository is based on our published research and was validated through experiments in the Laboratory of Distributed Systems and Concurrent Programming (LaSDPC), University of São Paulo. The proposed solution demonstrated low communication latency while maintaining scalability as the number of connected sensors increased.

The platform is composed of four cooperative agents:

- **Creator Agent** – Automatically detects new sensors, creates Monitoring Agents when necessary, and dynamically assigns sensors.
- **Monitor Agent** – Supervises a group of sensors and maintains a balanced distribution of monitoring responsibilities.
- **Collector Agent** – Receives MQTT messages from IoT devices and aggregates sensor readings before reasoning.
- **Rational Agent** – Manipulates the ontology, executes inference rules, and generates intelligent decisions for actuators.

The agents cooperate to create an autonomous environment capable of adapting to context without human intervention. Sensor information is continuously collected, transformed into ontology instances, evaluated through semantic inference rules, and translated into actions such as switching lights on or off, controlling air conditioners, opening blinds, or closing doors.

Unlike traditional monitoring systems, the MAS supports **dynamic sensor discovery**, **automatic agent creation**, **ontology-based reasoning**, and **distributed execution**, allowing the platform to grow without requiring manual reconfiguration. This makes the architecture suitable for Smart Buildings, Smart Campuses, Industrial IoT, and Edge/Fog Computing environments.

The implementation primarily uses **JADE** for the multi-agent infrastructure, **Python + Owlready2** for ontology manipulation, **MQTT (Mosquitto)** for asynchronous communication, and **Eclipse Kura** as the Smart Gateway.

Performance evaluations demonstrated that the architecture scales efficiently with up to **100 simultaneously active sensors**, maintaining low message reception latency and stable CPU and memory consumption. These results indicate that the platform can operate continuously in real-world smart environments while preserving responsiveness and efficient resource utilization.

This repository contains the implementation of the MAS, including agent behaviors, MQTT communication, ontology integration, graphical monitoring tools, sensor simulators, and examples that can be extended to new intelligent environments.

---

## Main Features

- Context-aware autonomous decision making
- Dynamic creation and management of monitoring agents
- Ontology-based reasoning and inference rules
- MQTT asynchronous communication
- Automatic IoT sensor discovery
- Distributed multi-agent architecture
- Smart Building resource automation
- Scalable architecture validated with up to 100 sensors
- Graphical visualization of agents and monitored devices
- Easy integration with Digital Twin, Edge/Fog, and Cloud platforms

---

## Reference

If you use this repository in your research, please cite:

> Paucar Curasma, H., Pan, C. F., & Estrella, J. C. (2024). *Agents for automatic control of sensors using Multi-Agent Systems and Ontologies: A scalable IoT architecture*. Procedia Computer Science, 238, 404–411.
