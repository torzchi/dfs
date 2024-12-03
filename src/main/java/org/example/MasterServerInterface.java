package org.example;

import java.rmi.*;
import java.util.*;
public interface MasterServerInterface extends Remote {
    void registerReplicaServer(String name, ReplicaLoc location) throws RemoteException;
    List<ReplicaLoc> getReplicaLocations(String fileName) throws RemoteException;
}