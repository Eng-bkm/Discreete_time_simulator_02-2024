import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
//*************Important parts of the code will be commented with this indentation
public class Simulator {
    private static int K; // Number of servers
    private static int H; // Number of job categories
    private static int N; // Total number of jobs to be simulated
    private static int R; // Repetitions of the simulation
    private static int P; // Scheduling policy type (0 or 1)
    private static int running;
    private static int running2;
    private static double[] lambdaArrival;
    private static double[] lambdaService;
    private static long[] seedArrival;
    private static long[] seedService;

    private static Random[] arrivalGenerators;
    private static Random[] serviceGenerators;

    private static PriorityQueue<Event> eventQueue;
    private static PriorityQueue<Event> eventQueueEnd;
    private static Server[] servers;
	
	private static double lastend;
	private static double queAll;
	private static double endAll;
    private static double queueingTimeSumAll;
    private static double queueingTimeSumAll1;
    private static double[] queueingTimeSumCategory;
    private static double[] serviceTimeSumCategory;
    private static int[] arrivalCountCategory;
    private static int[] serviceCountCategory;

    public static void main(String[] args) {
    if (args.length != 1) {
        System.out.println("Usage: java Simulation <parameter_file_path>");
        System.exit(1);
    }

    String parameterFilePath = args[0];
    

    try {
		arrivalGenerators = new Random[H];
        serviceGenerators = new Random[H];
        readInput(parameterFilePath);
        initializeGenerators();
		queueingTimeSumAll = 0;
        queueingTimeSumCategory = new double[H];		
		
        serviceTimeSumCategory = new double[H];
        arrivalCountCategory = new int[H];
        serviceCountCategory = new int[H];
		System.out.println(K + "," + H + "," + N + "," + R + "," + P);
        for (int r = 0; r < R; r++) {
            initialize();
            simulate();
			eventQueue.clear();
			eventQueueEnd.clear();
			
        }
		//System.out.println("hi");
        printResults();
    } catch (IOException e) {
        e.printStackTrace();
    }
}

    // Read input parameters from file
    private static void readInput(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line = reader.readLine();
        String[] params = line.split(",");
        K = Integer.parseInt(params[0]);
        H = Integer.parseInt(params[1]);
        N = Integer.parseInt(params[2]);
        R = Integer.parseInt(params[3]);
        P = Integer.parseInt(params[4]);

        lambdaArrival = new double[H];
        lambdaService = new double[H];
        seedArrival = new long[H];
        seedService = new long[H];

        arrivalGenerators = new Random[H];
        serviceGenerators = new Random[H];
//*************generator parameters from input fill are being initialized here
        for (int i = 0; i < H; i++) {
            line = reader.readLine();
            params = line.split(",");
            lambdaArrival[i] = Double.parseDouble(params[0]);
            lambdaService[i] = Double.parseDouble(params[1]);
            seedArrival[i] = Long.parseLong(params[2]);
            seedService[i] = Long.parseLong(params[3]);
        }

        reader.close();
		
	
    }

    // Initialize random generators
    private static void initializeGenerators() {
        arrivalGenerators = new Random[H];
        serviceGenerators = new Random[H];

        for (int i = 0; i < H; i++) {
            arrivalGenerators[i] = new Random(seedArrival[i]);
            serviceGenerators[i] = new Random(seedService[i]);
        }
    }

    // Initialize generators and servers
    private static void initialize() {
        eventQueue = new PriorityQueue<>();
        eventQueueEnd = new PriorityQueue<>(Comparator.comparingDouble(event -> event.end));
        servers = new Server[K];
        for (int i = 0; i < K; i++) {
            servers[i] = new Server();
        }

        queueingTimeSumAll = 0.0;/*
        queueingTimeSumCategory = new double[H];
        serviceTimeSumCategory = new double[H];
        arrivalCountCategory = new int[H];
        serviceCountCategory = new int[H];*/

        for (int i = 0; i < H; i++) {
            double arrivalTime = generateExponential(lambdaArrival[i], arrivalGenerators[i]);
            double serverTime = generateExponential(lambdaService[i], serviceGenerators[i]);

            Event arrivalEvent = new Event(arrivalTime, serverTime, 0, i, true);
            eventQueue.add(arrivalEvent);
        }
    }

    // Simulation logic
    private static void simulate() {
    
lastend = 0.0;
        while (!eventQueue.isEmpty() && running < N) {
	    
            Event currentEvent = eventQueue.poll();
            double currentTime = currentEvent.time;
            

            if (currentEvent.isArrival) {
            	updateServerIfJobLeft(currentTime);
                int category = currentEvent.category;

                int selectedServer = selectServer(currentEvent);
                Server executingServer = servers[selectedServer];
                double arrivalTime = currentTime + generateExponential(lambdaArrival[category], arrivalGenerators[category]);
                
                double serviceTime = generateExponential(lambdaService[category], serviceGenerators[category]);
                Event newArrivalEvent = new Event(arrivalTime, serviceTime, 0, category, true);
                eventQueue.add(newArrivalEvent);
/*******	SERVER ASIIGNMENT AND MANAGEMENT
each server has two two variables us when the jobs arrived. serverQueuedOrNot is for checking if the server is still queued and serverTime is to tell if the server is busy or not. the time serverTime is to tell if the service is busy. if arrival is at the same time or less than serverTime, it means the sever is busy. if greater than serverTime means server is free, so we update serverTime to arrivaltime plus the service time of this new arrival to tell other job events the server is busy.


serverQueueOrNot is to tell if the server is queued or not,. when an arrival come when the server is busy, it check compares its time with this varible, it is lss than the serverQueued value, it means that it has to be queued. it updates the servers queuetime by adding its service time to it. 
if grreater than serverQueueorNot, it means the server is just only busy there is no one in the queue, arrival job queues it self by setting the server queue time to its servicetime. since the next arrival in the queue, will have to waits for this arrivals service que alo, if this arrival doesnt make it to the server by then. 
all arrivals have to update the serverTime by adding their servicetime to it.

 */
                // server is free, so tel other new arrivals they will be the first in the queue if the come while the server is busy with this arrival
                if (currentTime > executingServer.serverTime) {
                    executingServer.serverTime = currentTime + currentEvent.service;
                    executingServer.total = 0.0;
                    currentEvent.que = 0.0;
                    currentEvent.end = currentTime + currentEvent.service;
                    executingServer.serverQueuedOrNot = currentEvent.end;
                    
                } 
     
     
     
/* #################################################################################################################
     
     this means the server is busy and there are non in the queue, so its puts it queue time to the server total variable which tells the total amount of time new arrival has to wait before starting its pocess at the server*/
     
		else if (currentTime < executingServer.serverTime && currentTime > executingServer.serverQueuedOrNot) {
                    executingServer.serverTime = executingServer.serverTime + currentEvent.service;
                    
 /*this arrival is first to enter the queue and tell other arrivals that the anyones that comes earlier than this time, will have someoen before them in the queue   . and change the queue time to its servicetime*/             
                    executingServer.serverQueuedOrNot = currentTime + currentEvent.service;
                    //executingServer.serverQueuedOrNot = executingServer.serverQueuedOrNot + executingServer.total;
                    executingServer.total = currentEvent.service ;
                    currentEvent.end = executingServer.serverTime;
                    }
                    
                    else if (currentTime < executingServer.serverTime && currentTime < executingServer.serverQueuedOrNot && executingServer.total== 0 ) {
                    executingServer.serverTime = executingServer.serverTime + currentEvent.service;
                    executingServer.total = executingServer.total + currentEvent.service;
                    currentEvent.end = executingServer.serverTime;
                    executingServer.lastQue= currentEvent.service;
                    //executingServer.serverQueuedOrNot = currentTime + currentEvent.service;
                    //executingServer.serverQueuedOrNot = executingServer.serverQueuedOrNot + executingServer.total;
                } 
                
                
                
     //this means the sever is busy and this arrial has some people before it in the queue
                
                else if (currentTime < executingServer.serverTime && currentTime < executingServer.serverQueuedOrNot) {
                    executingServer.serverTime = executingServer.serverTime + currentEvent.service;
                    executingServer.total = executingServer.total + currentEvent.service;
                    currentEvent.end = executingServer.serverTime;
                    //executingServer.serverQueuedOrNot = currentTime + currentEvent.service;
                    //executingServer.serverQueuedOrNot = executingServer.serverQueuedOrNot + executingServer.total;
                } 
                
// !!!!!!!!!!!!
                else{;}
                
                currentEvent.que =  currentEvent.end - currentEvent.time - currentEvent.service;
                double queueingTime = currentEvent.que;
                //executingServer.lastQue =  queueingTime - currentEvent.service;
                
                //System.out.println("Simulation :" +  running + " end time -----------"  + queueingTime);

                double endTime = currentEvent.end;
                Event endEvent = new Event(endTime, currentEvent.service, currentEvent.que, category, false );
				endEvent.end = currentEvent.end;
                eventQueueEnd.add(endEvent);
		
		lastend = currentEvent.end;;
//updates variables for that are useful for the ststistics
                updateStatistics(queueingTime, category, currentEvent.service);
            } else {
                ;
            }
            queueingTimeSumAll1 = queueingTimeSumAll;
            
            queueingTimeSumAll = 0.0;
            running++;
            		//System.out.println("####### at:" + queueingTimeSumAll1);
			//System.out.println("*run :" + running);
        }
        
        		//System.out.println("####### at:" + queAll);
        
 /*********if the the case of the situation in the if below, we print here as per the requirement of the project to print for R == 1 && N <= 10 && P == 0 like below*/
 if (R == 1 && N <= 10 && P == 0) {
       // System.out.println("Events:");
double avgQueueingTimeAll = R > 0 && N > 0 ? queAll / R : 0;

    //System.out.println("End Time (ET):");

    
    System.out.println(lastend);

    //System.out.println("Average Queuing Time of All Jobs (AQT-all):");
    System.out.println(queueingTimeSumAll1/N);

        PriorityQueue<Event> allEvents = new PriorityQueue<>(eventQueue);
        allEvents.addAll(eventQueueEnd);

        for (Event event : allEvents) {
            double time = event.time;
            double service = event.service;
			
            

            System.out.println(time + "," + service + "," + event.end);
        }
    }

/*when one simulation ends, we add the endtime to this variable to later calculate the averge end time of R runs*/

    endAll = endAll + lastend;



// after each simulation we are updating the queuing time for all for all runs, by adding the average queing time of each run
		//System.out.println("####### at:" + queueingTimeSumAll1);
queAll = queAll + (queueingTimeSumAll1 / N);
//queAll = queAll + 1;
		//System.out.println("***** at:" + queAll);
		//System.out.println("####### at:" + queueingTimeSumAll1);
	lastend = 0;
	



    }

private static void printResults() {
    // Print parameters
    

    
    if (!(R == 1 && N <= 10 && P == 0)) {
       // System.out.println("Events:");
        PriorityQueue<Event> allEvents = new PriorityQueue<>(eventQueue);
        allEvents.addAll(eventQueueEnd);

        for (Event event : allEvents) {
            double time = event.time;
            double service = event.service;
            

            System.out.println(time + "," + service + "," + event.end);
        }
 
    // Calculate and print averages
    
    double avgQueueingTimeAll = R > 0 && N > 0 ? queAll / R : 0;

    //System.out.println("End Time (ET):");

    double avgEndTime  = endAll/R;
    System.out.println(avgEndTime);

    //System.out.println("Average Queuing Time of All Jobs (AQT-all):");
    System.out.println(avgQueueingTimeAll);

   for (int r = 0; r < H; r++) {
        int Nr = arrivalCountCategory[r];
        double avgQueueingTimeCategory = Nr > 0 ? queueingTimeSumCategory[r] / Nr : 0;
        double avgServiceTimeCategory = Nr > 0 ? serviceTimeSumCategory[r] / Nr : 0;

        System.out.print(Nr);
        if (Nr > 0) {
            System.out.print("," + avgQueueingTimeCategory + "," + avgServiceTimeCategory);
        }
        System.out.println();
    //} for the else above
}}

}
/******************* here after each simulation of one job, I update queung, arriva and service time of each category at this run and the total quing time of this run*/
    // Helper method to update statistics
    private static void updateStatistics(double queueingTime, int category, double service) {
        queueingTimeSumAll = queueingTimeSumAll + queueingTime;
        queueingTimeSumCategory[category] += queueingTime;

        arrivalCountCategory[category]++;
        serviceCountCategory[category]++;

        serviceTimeSumCategory[category] += service;
    }

    // Helper method to select the server based on scheduling policy





/*##########################    THE SERVER SELECTION POLICY ###################

for the server slection pocily I chose to work, each server is assigned a variable called total that gets update by each arrival to when it see server busy or there are others waiting before it. if while the server was busy there was no one in the queue, the arrial job sets this total variable of the server to its servicetime, otherwise if there were other before it just adds to it its service time. and upda

*/

    private static int selectServer(Event arrivalEvent) {
        if (P == 0) {
            return arrivalEvent.category % K;
        } else {
            // Custom policy (P = 1)
            int minTotalIndex = 0;
            double minTotal = servers[0].total;

            for (int i = 1; i < K; i++) {
                if (servers[i].total <= minTotal ) {
                    minTotal = servers[i].total;
                    minTotalIndex = i;
                }
            }

            return minTotalIndex;
        }
    }

    // Helper method to generate exponential random variables
    private static double generateExponential(double lambda, Random generator) {
        double alpha = generator.nextDouble();
        return -1 / lambda * Math.log(1 - alpha);
    }

    // Classes
    private static class Event implements Comparable<Event> {
        private double time;
        private double service;
        private double que; // Queueing time

        private int category;
        private boolean isArrival;
        private double end;

        public Event(double time, double service, double que, int category, boolean isArrival) {
            this.time = time;
            this.service = service;
            this.que = que;
            this.category = category;
            this.isArrival = isArrival;
            this.end = 0; // Initialize end time to 0
        }

        @Override
        public int compareTo(Event other) {
            return Double.compare(this.time, other.time);
        }
    }
 //this function will be used to make sure every server's queue is update when a job leaves
private static void updateServerIfJobLeft(double checker) {
	int n  = 0;
	double total2 = 0.0;
	
    for (Server server : servers) {
    if ((server.serverQueuedOrNot + server.lastQue) <= server.serverTime)
	{server.total = server.total - server.lastQue;}
    //System.out.println("idle at " + server.total);
    total2 = total2 + server.total;
        if ((server.serverQueuedOrNot + server.total) <= checker) {
            server.total = 0.0; 
                //System.out.println("idle at " + n);
        }
       // System.out.println("idle at " + total2);
        n++;
    }
}
    public static class Server {
        public double serverTime;
        public double total;
        public double serverQueuedOrNot;
        public double lastQue;

        public Server() {
            this.total = 0.0;
            this.serverTime = 0.0;
            this.serverQueuedOrNot = 0.0;
            this.lastQue = 0.0;
        }
    }
}

