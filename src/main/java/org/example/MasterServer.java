package org.example;

import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.util.*;

public class MasterServer extends UnicastRemoteObject implements MasterServerInterface {
    private final List<ReplicaLoc> replicaLocations = new ArrayList<>();

    public MasterServer() throws RemoteException {
        super();
    }

    @Override
    public synchronized void registerReplicaServer(String name, ReplicaLoc location) throws RemoteException {
        replicaLocations.add(location);
        System.out.println("Registered replica: " + name + " at " + location.getHost());

        // Notify the previous replica about the new one
        if (replicaLocations.size() > 1) {
            ReplicaLoc previous = replicaLocations.get(replicaLocations.size() - 2);
            try {
                ReplicaServerInterface previousReplica = (ReplicaServerInterface) Naming.lookup(
                        "//" + previous.getHost() + "/ReplicaServer" + previous.getId());
                ReplicaServerInterface currentReplica = (ReplicaServerInterface) Naming.lookup(
                        "//" + location.getHost() + "/ReplicaServer" + location.getId());
                previousReplica.setNextReplica(currentReplica);
                System.out.println("Updated chain: " + previous.getId() + " -> " + location.getId());
            } catch (Exception e) {
                System.err.println("Failed to update chain for replica: " + name);
            }
        }
    }

    @Override
    public synchronized List<ReplicaLoc> getReplicaLocations(String fileName) throws RemoteException {
        // Return all registered replica locations
        return new ArrayList<>(replicaLocations);
    }

    public static void main(String[] args) {
        try {
            LocateRegistry.createRegistry(1099);
            MasterServer master = new MasterServer();
            Naming.rebind("MasterServer", master);
            System.out.println("Master Server is running...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

