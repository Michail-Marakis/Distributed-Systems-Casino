# Distributed Systems Casino

A distributed casino application built using the MapReduce Framework, designed to execute computations across multiple machines. The project demonstrates core distributed systems concepts such as task distribution, parallel processing, inter-node communication, and result aggregation. The system distributes workloads among multiple worker nodes, enabling parallel execution and improved scalability compared to a traditional centralized approach. This project was developed as part of a Distributed Systems course on 6th Semester.

## Key Features

- Distributed execution across multiple PCs in the same LAN
- MapReduce-based processing architecture
- Parallel task execution using worker nodes
- Client-Master-Worker communication model
- Task scheduling and workload distribution
- Result aggregation through a Reduce phase
- Multi-threaded processing
- Scalable architecture supporting multiple workers

## Architecture

```text
                +-------------+
                |   Client    |
                +------+------+
                       |
                       v
                +-------------+
                |   Master    |
                +------+------+
                       |
       +---------------+---------------+
       |               |               |
       v               v               v
+-------------+ +-------------+ +-------------+
|  Worker 1   | |  Worker 2   | |  Worker N   |
+-------------+ +-------------+ +-------------+
       |               |               |
       +------- Map Processing --------+
                       |
                       v
                +-------------+
                |   Reduce    |
                +-------------+
```

## Technologies Used

- Java
- Java Sockets
- Multithreading
- MapReduce Framework
- Simple XML-based User Interface

## System Workflow

1. The client submits a request to the system.
2. The Master node receives and partitions the workload.
3. Tasks are distributed among available Worker nodes.
4. Workers execute Map operations in parallel.
5. Intermediate results are returned to the Master.
6. The Reduce phase aggregates all partial results.
7. The final result is sent back to the client.

## Fault Tolerance & Active Replication

A key objective of this project was to provide high availability and fault tolerance within the distributed environment.

### Active Replication

Each worker has a backup replica capable of taking over execution if the primary worker becomes unavailable.

This approach minimizes downtime and allows the system to continue processing requests without requiring manual intervention.

### Heartbeat Monitoring

The Master node continuously monitors worker availability using a heartbeat mechanism.

At regular intervals:

1. Workers send heartbeat messages to the Master.
2. The Master verifies that workers are alive and responsive.
3. Missing heartbeats indicate a potential worker failure.
4. The Master automatically redirects tasks to the corresponding backup worker.

### Failure Recovery Workflow

```text
Worker A
    │
    │ Heartbeats
    ▼
 Master
    │
    │ Worker Failure Detected
    ▼
 Backup Worker A
    │
    ▼
 Continue Processing
```

### Benefits

- High Availability
- Fault Tolerance
- Automatic Failure Detection
- Reduced Service Downtime
- Continuous Task Execution
- Improved System Reliability

## Configuration

Before starting the system, ensure that all network configurations are properly set.

### Backend Configuration

Update the `config.json` file with the appropriate IP addresses and ports for your environment.

```json
{
    "Master": {"first": "127.0.0.1", "second": 3333},
    "Workers": [
        {"first": "127.0.0.1", "second": 4001},
        {"first": "127.0.0.1", "second": 4002},
        {"first": "127.0.0.1", "second": 4003}
    ],
    "RNG": {"first": "127.0.0.1", "second": 5000},
    "Reducer": {"first": "127.0.0.1", "second": 6000},
    "Secret": "your-secret-key",
    "bufferSize": 100000
}
```

#### Configuration Parameters

| Component | Description |
|------------|------------|
| Master | Master node IP and port |
| Workers | List of available worker nodes |
| RNG | Random Number Generator service |
| Reducer | Reducer service |
| Secret | Shared secret used for secure communication |
| bufferSize | Socket communication buffer size |

For distributed deployment across multiple machines, replace the localhost (`127.0.0.1`) addresses with the actual IP address of each host.

---

### Frontend Configuration

The frontend must be configured to communicate with the Master node.

Navigate to:

```text
src/main/java/com/example/taketwo_partb/network/InternalConfig.java
```

and update the backend IP address:

```java
public class InternalConfig {

    private static final String BackendIp = "192.168.X.X";
    private static final int port = 3333;

    public static String getBackendIp() {
        return BackendIp;
    }

    public static int getPort() {
        return port;
    }
}
```

#### Important

- `BackendIp` must point to the machine running the Master node.
- `port` must match the Master port defined in `config.json`.
- When running all components locally, `127.0.0.1` can be used.
- When deploying across multiple PCs, use the actual LAN or public IP address of the Master machine.

---

### Example Distributed Deployment

| Component | IP Address | Port |
|------------|------------|--------|
| Master | 192.168.1.10 | 3333 |
| Worker 1 | 192.168.1.11 | 4001 |
| Worker 2 | 192.168.1.12 | 4002 |
| Worker 3 | 192.168.1.13 | 4003 |
| RNG | 192.168.1.14 | 5000 |
| Reducer | 192.168.1.15 | 6000 |

The frontend should then use:

```java
private static final String BackendIp = "192.168.1.10";
```

to connect to the Master node.

---

## Running the System

The system consists of several distributed components that must be started in the following order:

### 1. Start the Random Number Generator (RNG)

```bash
java RNG
```

Provides the random values required by the casino games.

### 2. Start the Reducer

```bash
java Reducer
```

Responsible for aggregating intermediate results produced by worker nodes during the Reduce phase.

### 3. Start Worker Nodes

```bash
java Worker
```

Launch one or more worker instances. Workers execute Map tasks assigned by the Master node.

Example:

```bash
java Worker
java Worker
java Worker
```

Workers may run on separate machines to enable distributed execution.

### 4. Start the Master Node

```bash
java Master
```

The Master coordinates the system by:

- Receiving requests
- Partitioning workloads
- Assigning tasks to workers
- Monitoring worker health
- Handling fault recovery
- Aggregating results

### 5. Start the Manager Console

```bash
java ManagerConsole
```

The Manager Console allows administrators to monitor the state of the distributed system and manage casino operations.

### 6. Start the Player Application

```bash
java Player
```

Players connect to the distributed casino system and interact with the available games.

---

## Demonstration

### Task Distribution

> Master node assigning tasks to workers.

<img width="1820" height="858" alt="image" src="https://github.com/user-attachments/assets/48a7cadc-15c3-4e8b-9def-84cf1f677e6c" />


*Worker-2 was intentionally shut down during execution. The Master detected the failure and automatically redirected requests to the backup worker, demonstrating Active Replication and fault-tolerant execution.*

### Application Interface

> Casino application's user interface.

<img width="366" height="861" alt="image" src="https://github.com/user-attachments/assets/040daa54-1991-4c18-b76e-a6083db9d325" />

