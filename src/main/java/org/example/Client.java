package org.example;

import java.rmi.*;
import java.util.List;

public class Client {
    public static void main(String[] args) {
        try {
            MasterServerInterface master = (MasterServerInterface) Naming.lookup("//localhost/MasterServer");
            System.out.println("Fetching replica locations...");
            List<ReplicaLoc> replicas = master.getReplicaLocations("anyFile");

            if (replicas.isEmpty()) {
                System.out.println("No replicas available.");
                return;
            }

            String fileName = "testFile.txt";
            String fileContent = "Hello from Client!";
            boolean writeSuccess = false;

            // Attempt to write to a replica
            for (ReplicaLoc replicaLoc : replicas) {
                try {
                    System.out.println("Trying to connect to replica: " + replicaLoc);
                    ReplicaServerInterface replica = (ReplicaServerInterface) Naming.lookup(
                            "//" + replicaLoc.getHost() + "/ReplicaServer" + replicaLoc.getId());
                    replica.write(fileName, fileContent);
                    System.out.println("File written to: " + replicaLoc);
                    writeSuccess = true;
                    break; // Exit the loop if write is successful
                } catch (Exception e) {
                    System.err.println("Failed to connect to replica: " + replicaLoc + ". Trying next...");
                }
            }

            if (!writeSuccess) {
                System.out.println("Write operation failed on all replicas.");
                return;
            }

            // Attempt to read from the last replica in the chain
            String retrievedData = null;
            for (int i = replicas.size() - 1; i >= 0; i--) {
                ReplicaLoc replicaLoc = replicas.get(i);
                try {
                    System.out.println("Trying to connect to last replica: " + replicaLoc);
                    ReplicaServerInterface replica = (ReplicaServerInterface) Naming.lookup(
                            "//" + replicaLoc.getHost() + "/ReplicaServer" + replicaLoc.getId());
                    retrievedData = replica.read(fileName);
                    System.out.println("File content retrieved from last replica: " + retrievedData);
                    break; // Exit the loop if read is successful
                } catch (Exception e) {
                    System.err.println("Failed to connect to last replica: " + replicaLoc + ". Trying previous...");
                }
            }

            if (retrievedData == null) {
                System.out.println("Read operation failed on all replicas.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

