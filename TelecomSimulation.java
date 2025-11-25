
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.TimeUnit;




/**
 * Represents a network packet.
 * Attributes: pkt_id, src, dst, size_bytes, priority, timestamp, current_router.
 * Behaviors: set_router, size_mb.
 */
class Packet { // Packet Class Represents Network Packet
    private final int pktId; // Unique identifier
    private final String src; // Source  
    private final String dst; // Destination
    private final int sizeBytes; // Packet size in bytes
    private final int priority; // Level of importance
    private final long timestamp; // Using long for milliseconds- Time recording from the system time
    private String currentRouter; // Mutable field- Location

    public Packet(int pktId, String src, String dst, int sizeBytes, int priority) {
        this.pktId = pktId;
        this.src = src;
        this.dst = dst;
        this.sizeBytes = sizeBytes;
        this.priority = priority;
        this.timestamp = System.currentTimeMillis();
        this.currentRouter = null;
    }

    // Constructor without optional priority
    public Packet(int pktId, String src, String dst, int sizeBytes) {
        this(pktId, src, dst, sizeBytes, 0); // Default priority is 0
    }

    // Behavior: set current router
    //Updates the location of each packet
    public void setRouter(String routerId) {
        this.currentRouter = routerId;
    }

    // Calculates the size of the packet in megabytes
    public double sizeMb() {
        return (double) this.sizeBytes / (1024.0 * 1024.0); //field in the class to be stored 
    }

    // Getters (to access attributes)
    public int getPktId() { return pktId; }
    public String getSrc() { return src; }
    public String getDst() { return dst; }
    public int getSizeBytes() { return sizeBytes; }
    public long getTimestamp() { return timestamp; }
    public String getCurrentRouter() { return currentRouter; }
}




/**
 * Represents a network router node.
 * Attributes: node_id, status, routing_table, queue.
 * To receive_packet, forward_packet, set_route.
 */
class RouterNode { // RouterNode Class
    // Shared counter, similar to itertools.count(1)
    private static final AtomicInteger _serial = new AtomicInteger(1);

    private final String nodeId; //Unique identifier
    private String status; //operational status
    private final Map<String, String> routingTable; // Used to show where the packet goes next
    private final List<Packet> queue; // packets queued for processing

    public RouterNode(String nodeId) {
        this.nodeId = nodeId; // unique identifier of the router
        this.status = "active"; // operational state
        this.routingTable = new HashMap<>(); // Destination determinant
        this.queue = new ArrayList<>(); // List of packets to be processes
    }

    // To receive a packet
    public void receivePacket(Packet pkt) {
        pkt.setRouter(this.nodeId); // Settig the packets router to itself
        this.queue.add(pkt); // Adds the packets to the queue
    }

    // to forward a packet to next hop
    public Packet forwardPacket(RouterNode dstRouter) {
        if (this.queue.isEmpty()) {
            return null;
        }
        // remove from queue (first-in, first-out)
        Packet pkt = this.queue.remove(0);
        // send to next router
        dstRouter.receivePacket(pkt);
        return pkt;
    }

    // to update routing table
    public void setRoute(String dst, String nextHop) {
        this.routingTable.put(dst, nextHop);
    }

    // Getters
    public String getNodeId() { return nodeId; }
    public List<Packet> getQueue() { return queue; }
}



/**
 * Represents a user session.
 * Attributes: session_id, user_id, start_time, duration_s, authenticated.
 * Behaviors: authenticate, end_session.
 */
class UserSession { // UserSession Class
    private final int sessionId;
    private final String userId;
    private final long startTime; // Time the session began in milliseconds
    private Double durationS; //Session length in seconds
    private boolean authenticated; // Status of boolean

    public UserSession(int sessionId, String userId) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.startTime = System.currentTimeMillis();
        this.durationS = null;
        this.authenticated = false;
    }

    // Helper method to reverse a string
    private String reverseString(String s) {
        return new StringBuilder(s).reverse().toString();
    }

    // To authenticate user
    public boolean authenticate(String password) { // Accepts the password string
        // simple demo: password == user_id reversed
        if (password.equals(reverseString(this.userId))) { // the password must be equal to the userid reversed
            this.authenticated = true;
        }
        return this.authenticated; // Return authentification status
    }

    // To end session
    public double endSession() {
        if (this.durationS == null) { // checking if the durationS is nul
            long durationMs = System.currentTimeMillis() - this.startTime; //Calculates the duration of the session
            // Convert milliseconds to seconds (double)
            this.durationS = durationMs / 1000.0;
        }
        return this.durationS;
    }

    // Getters
    public boolean isAuthenticated() { return authenticated; }
    public double getDurationS() { return durationS != null ? durationS : 0.0; } // Return 0 if not ended
}





   //To calculate_charge.
class BillingEngine { // BillingEngine Class
    // Attributes
    private final double ratePerMb; //charge rate per megabyte
    private final List<Map<String, Object>> records; // Creates List to record all billings

    public BillingEngine(double ratePerMb) {
        this.ratePerMb = ratePerMb;
        this.records = new ArrayList<>();
    }

    public BillingEngine() {
        this(0.01); // Default rate
    }

    // To calculate the billing for a packet
    public double calculateCharge(Packet pkt) {
        if (pkt == null) return 0.0; // Handle case where forward_packet returns null
        double charge = pkt.sizeMb() * this.ratePerMb; // calculates the charge using megabytes
        Map<String, Object> record = new HashMap<>(); //creates a record
        record.put("packet_id", pkt.getPktId()); // Adding the unique id to record
        record.put("src", pkt.getSrc()); // Source record
        record.put("dst", pkt.getDst()); // destination record
        record.put("router", pkt.getCurrentRouter()); // router record
        record.put("charge", charge); // record of the charge of the packet
        record.put("timestamp", pkt.getTimestamp()); // when the packet was sent
        this.records.add(record); //Recored storage
        return charge; // outputs calculated charge
    }

    // Getters
    public double getRatePerMb() { return ratePerMb; }
    public List<Map<String, Object>> getRecords() { return records; }
}




public class TelecomSimulation { // Main Class 

    public static void main(String[] args) throws InterruptedException {

        // Create objects (instances)
        RouterNode R1 = new RouterNode("R1");
        RouterNode R2 = new RouterNode("R2");
        BillingEngine billing = new BillingEngine();
        UserSession session1 = new UserSession(1, "alice");

        // Packet creation (attributes)
        Packet pkt1 = new Packet(101, "H1", "H2", 1500, 1);
        Packet pkt2 = new Packet(102, "H2", "H1", 2000); // Uses default priority

        // Router interactions Recieves both packets to store them in its queue
        R1.receivePacket(pkt1);
        R1.receivePacket(pkt2);

        // Forward packets
        Packet forwardedPkt1 = R1.forwardPacket(R2);
        Packet forwardedPkt2 = R1.forwardPacket(R2);

        // Billing calculation
        billing.calculateCharge(forwardedPkt1);
        billing.calculateCharge(forwardedPkt2);

        // User session interaction (behaviors)
         


        boolean authSuccess = session1.authenticate("ecila"); // reversed user_id
        double sessionDuration = session1.endSession();


       

        System.out.print("Router R2 queue: ["); // Summary output
        for (int i = 0; i < R2.getQueue().size(); i++) {
            System.out.print(R2.getQueue().get(i).getPktId());
            if (i < R2.getQueue().size() - 1) {
                System.out.print(", ");
            }
        }
        System.out.println("]");

        System.out.println(
            "Billing records: " + billing.getRecords()  // Lists the calculated charges
        );

        System.out.printf(
            "User session authenticated: %s, duration: %.2fs%n",  // User session time output
                          authSuccess, sessionDuration
                        );
    }
}

