package org.example;


import java.rmi.*;
import java.rmi.server.*;
import java.util.*;

public class ReplicaServer extends UnicastRemoteObject implements ReplicaServerInterface {
    private String name;
    private Map<String, String> fileStorage = new HashMap<>();
    private ReplicaServerInterface nextReplica;

    public ReplicaServer(String name) throws RemoteException {
        super();
        this.name = name;
    }

    @Override
    public void setNextReplica(ReplicaServerInterface nextReplica) throws RemoteException {
        this.nextReplica = nextReplica;
        System.out.println("[" + name + "] Next replica set to: " + nextReplica);
    }

    @Override
    public synchronized void write(String fileName, String data) throws RemoteException {
        // Write to local storage
        fileStorage.put(fileName, data);
        System.out.println("[" + name + "] File written locally: " + fileName + " -> " + data);

        // Forward to the next replica in the chain, if it exists
        if (nextReplica != null) {
            try {
                nextReplica.write(fileName, data);
                System.out.println("[" + name + "] Forwarded write to next replica.");
            } catch (RemoteException e) {
                System.err.println("[" + name + "] Failed to forward write to next replica.");
            }
        }
    }

    @Override
    public synchronized String read(String fileName) throws RemoteException {
        String content = fileStorage.getOrDefault(fileName, "File not found");
        System.out.println("[" + name + "] Read file: " + fileName + " -> " + content);
        return content;
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java ReplicaServer <replica_name>");
            return;
        }
        String replicaName = args[0];
        try {
            ReplicaServer replica = new ReplicaServer(replicaName);
            Naming.rebind("ReplicaServer" + replicaName, replica);

            // Register with the master server
            MasterServerInterface master = (MasterServerInterface) Naming.lookup("//localhost/MasterServer");
            ReplicaLoc location = new ReplicaLoc(replicaName, "localhost", true);
            master.registerReplicaServer(replicaName, location);

            System.out.println("Replica Server " + replicaName + " is running...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

