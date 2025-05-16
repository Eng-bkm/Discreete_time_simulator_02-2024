# Discreete_time_simulator_02-2024


# Job Scheduling Simulator

## Overview
A Java-based discrete-event simulator for modeling job scheduling in multi-server systems with multiple job categories.

## Features
- Supports multiple servers and job categories
- Two scheduling policies:
  - Policy 0: Category-based server assignment
  - Policy 1: Least-work-remaining assignment
- Exponential inter-arrival and service times
- Detailed statistics collection
- Both event traces and aggregated results

## Requirements
- Java 8 or higher

## Usage

### Input File Format
Create a parameter file (e.g., params.txt) with:
K,H,N,R,P
λ_arrival1,λ_service1,seed_arrival1,seed_service1
λ_arrival2,λ_service2,seed_arrival2,seed_service2
...
λ_arrivalH,λ_serviceH,seed_arrivalH,seed_serviceH

Where:
- K: Number of servers
- H: Number of job categories
- N: Total jobs to simulate
- R: Simulation repetitions
- P: Scheduling policy (0 or 1)
- λ_arrival: Arrival rate for category
- λ_service: Service rate for category
- seed_*: Random seeds

### Running the Simulation
1. Compile: javac Simulator.java
2. Run: java Simulator params.txt

## Output
For simple cases (R=1, N≤10, P=0):
- Detailed event log
- System end time
- Average queueing time

For other cases:
- Aggregated statistics
- Average system metrics
- Per-category statistics

## Example Input
params.txt:
2,3,100,5,0
0.5,1.0,12345,54321
0.3,0.8,67890,9876
0.2,1.2,13579,24680

## Implementation
- Priority queues for event management
- Exponential distribution generation
- Server state tracking
- Reproducible via random seeds

## License
MIT License