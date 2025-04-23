# News Feed Analyzer

* [Mock News Analyzer](#mock-news-analyzer)
  * [Overview](#overview)
  * [Architecture and Components](#architecture-and-components)
    * [**Mock News Feed (Client)**](#mock-news-feed-client)
    * [**News Analyzer (Server)**](#news-analyzer-server)
    * [**Common Module**](#common-module)
  * [Tech Stack](#tech-stack)
  * [OS Compatibility](#os-compatibility)
  * [Configuration Properties](#configuration-properties)
  * [How to Build and Deploy the Project](#how-to-build-and-deploy-the-project)
    * [Local Deployment (Using Scripts)](#local-deployment-using-scripts)
      * [**Step 1: Configure Environment Variables**](#step-1-configure-environment-variables)
      * [**Step 2: Start the Services**](#step-2-start-the-services)
      * [**Step 3: Verify the Deployment**](#step-3-verify-the-deployment)
    * [Containerized Deployment (Docker & Docker Compose)](#containerized-deployment-docker--docker-compose)
      * [**Step 1: Ensure Docker & Docker-Compose V2 are Installed**](#step-1-ensure-docker--docker-compose-v2-are-installed)
      * [**Step 2: Configure Environment Variables**](#step-2-configure-environment-variables)
      * [**Step 3: Build Docker Images**](#step-3-build-docker-images)
      * [**Step 4: Start the Services**](#step-4-start-the-services)
      * [**Step 5: Verify Running Containers**](#step-5-verify-running-containers)
  * [Logs](#logs)
    * [**Manual Execution Logs**](#manual-execution-logs)
    * [**Docker Logs**](#docker-logs)
      * [**View Server Logs**](#view-server-logs)
      * [**View Client Logs**](#view-client-logs)
      * [**View Logs for All Services**](#view-logs-for-all-services)
  * [Stopping the Services](#stopping-the-services)
    * [**Manual Shutdown (Script-Based Deployment)**](#manual-shutdown-script-based-deployment)
    * [**Docker Shutdown**](#docker-shutdown)
  * [📖 Core Functionality Details](#-core-functionality-details)
  * [Future Improvements](#future-improvements)

---

## Overview

The **Mock News Analyzer** is a Java-based system designed to simulate a real-time news feed and analyze incoming news
items. It consists of two main components:

- **Mock News Feed**: Generates and sends randomized news headlines with assigned priority levels over a TCP connection.
- **News Analyzer**: Receives incoming news messages, filters relevant ones based on sentiment analysis, and
  periodically summarizes the most important news.

The project demonstrates **real-time data processing** using **TCP communication**, **multi-threading**, and
**data generation**. The system can handle multiple concurrent news feeds and provides periodic reports
on significant news trends.

Additionally, the project can be deployed using **Docker** and **Docker Compose** for simplified setup and scalability.

---

## Architecture and Components

The system consists of the following core components:

![](./docs/images/design.png)

### **Mock News Feed (Client)**

- **Purpose**: Generates and sends randomized news items to the News Analyzer.
- **Core Functionality**:
    - Generates random news headlines using predefined keywords.
    - Assigns priority levels to news items based on a given probability distribution.
    - Sends news items to the Server over a persistent TCP connection.
    - Supports automatic reconnection if the connection is lost.
    - Configurable message sending interval through Java properties.
    - Can be deployed as multiple instances using Docker Compose for scalability.

### **News Analyzer (Server)**

- **Purpose**: Receives and processes news messages from the Mock News Feed clients.
- **Core Functionality**:
    - Listens for incoming client connections over TCP.
    - Manages concurrent client connections efficiently using multi-threading.
    - Stores data into efficient sorted data structure.
    - Filters incoming news based on sentiment analysis.
    - Aggregates and summarizes positive news every 10 seconds.
    - Can be deployed as a containerized service using Docker.

### **Common Module**

- **Purpose**: Provides shared utilities, functionalities, and DTOs for both services.

---

## Tech Stack

| Component                       | Technology                 |
|---------------------------------|----------------------------|
| Language                        | Java 8                     |
| Build System                    | Maven                      |
| Logging                         | SLF4J + Logback            |
| Networking                      | TCP Sockets                |
| Build and Dependency Management | Maven                      |
| Testing Framework               | JUnit 5                    |
| Mocking Library                 | Mockito                    |
| Asynchronous Testing            | Awaitility                 |
| Containerization                | Docker & Docker Compose V2 |


## OS Compatibility

- Windows (Tested)
- Linux, MacOS

## Configuration Properties

The following environment variables are used for configuring both the **server** and **client** applications. These
configurations apply whether the project is deployed manually using scripts (`start.sh`/`start.bat`) or via **Docker
Compose**.

| Environment Variable                    | Description                                                | Default Value |
|-----------------------------------------|------------------------------------------------------------|---------------|
| **Server Configuration**                |                                                            |               |
| `SERVER_PORT`                           | Server listening port for incoming TCP connections.        | `8080`        |
| `SERVER_CONNECTIONS_POOL_SIZE`          | Thread pool size for handling incoming client connections. | `10`          |
| `NEWS_SUMMARY_REPORT_PERIOD_IN_SECONDS` | Interval (in seconds) for generating the summary report.   | `10`          |
| **Client Configuration**                |                                                            |               |
| `NEWS_ANALYZE_SERVER_HOST`              | Host of the News Analyzer Server.                          | `localhost`   |
| `NEWS_ANALYZE_SERVER_PORT`              | Port of the News Analyzer Server.                          | `8080`        |
| `SEND_MESSAGE_INTERVAL_IN_MS`           | Interval (in milliseconds) between sending news messages.  | `200`         |

These values can be modified in **deployment scripts (`start.sh`, `start.bat`)** or within the **Docker Compose
file (`docker-compose.yml`)**.

---

## How to Build and Deploy the Project

This section outlines two approaches for building and deploying the **Mock News Analyzer** system:

1. **Local Deployment** – Using manual scripts (`start.sh`/`stop.sh` for Linux/Mac, `start.bat`/`stop.bat` for Windows).
2. **Containerized Deployment** – Using **Docker & Docker Compose** for a fully containerized environment.

### Local Deployment (Using Scripts)

For development and testing on a local machine, follow these steps to build and run the project manually.

#### **Step 1: Configure Environment Variables**

Before running the services, ensure the **configuration properties** (
see [Configuration Properties](#configuration-properties)) are properly set in your system or within the deployment
scripts.

#### **Step 2: Start the Services**

> Ensure **Java 8** and **Maven** are installed.

Execute the provided scripts to start the services.

**For Linux/Mac:**

```sh
cd mock-news-analyer
chmod +x start.sh
./start.sh
```

**For Windows:**

```sh
cd mock-news-analyer
.\start.bat
```

#### **Step 3: Verify the Deployment**

Check if the **server** is running by connecting to the specified port:

```sh
netstat -an | grep 8080     # For Linux/Mac
netstat -an | findstr 8080  # For Windows
```

If the server is listening, it is running correctly.

### Containerized Deployment (Docker & Docker Compose)

For production-like environments, **Docker** and **Docker Compose** provide a streamlined approach to deploying the
system.

#### **Step 1: Ensure Docker & Docker-Compose V2 are Installed**

Verify Docker and Docker Compose v2 are installed by running:

```sh
docker -v
```

```sh
docker compose version
```

#### **Step 2: Configure Environment Variables**

The **Docker Compose file (`docker-compose.yml`)** includes the same environment variables listed
in [Configuration Properties](#configuration-properties). Ensure these values match your desired setup.

#### **Step 3: Build Docker Images**

Build the necessary Docker images before deployment:

```sh
docker compose build
```

#### **Step 4: Start the Services**

Launch the server and multiple client instances using:

```sh
docker compose up --scale client=5 -d
```

This will:

- Start **one News Analyzer Server**.
- Start **five Mock News Feed Client instances**.

#### **Step 5: Verify Running Containers**

List all running containers:

```sh
docker ps
```

To check logs for a specific service:

```sh
docker compose logs -f server  # Logs for the server
```

```sh
docker compose logs -f client  # Logs for all client instances
```

---

## Logs

Once the server and clients are started, you can monitor their logs.

### **Manual Execution Logs**

For local deployments, logs will be displayed directly in the terminal of each running app.

### **Docker Logs**

If running via Docker, use the following commands:

#### **View Server Logs**

```sh
docker compose logs -f server
```

#### **View Client Logs**

```sh
docker compose logs -f client
```

#### **View Logs for All Services**

```sh
docker compose logs -f
```


## Stopping the Services

### **Manual Shutdown (Script-Based Deployment)**

To stop manually executed services, run:

**For Linux/Mac:**

```sh
cd mock-news-analyer
chmod +x stop.sh
./stop.sh
```

**For Windows:**

```sh
cd mock-news-analyer
.\stop.bat
```

### **Docker Shutdown**

If deployed via Docker, shut down all running containers using:

```sh
docker compose down
```

---

## 📖 Core Functionality Details

For an in-depth explanation of some of the **major functionalities**:
👉 [View Core Functionality Documentation](./docs/core-functionality.md)

## Future Improvements

- Improve project configurability to control the resources up to what the host machine can handle.
- Write more integration (end-to-end) tests alongside the written unit tests.
- Improve deployment strategy using Docker and Docker Compose. ✅
- Improve logs streaming into files.
- Implement rate limiter for TCP connections.
- Improve data serialization/deserialization on transmission over the network.


