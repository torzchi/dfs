package org.example;

import java.io.Serializable;
public class ReplicaLoc implements Serializable {
    private String id;
    private String host;
    private boolean isAlive;
    public ReplicaLoc(String id, String host, boolean isAlive) {
        this.id = id;
        this.host = host;
        this.isAlive = isAlive;
    }
    public String getId() {
        return id;
    }
    public String getHost() {
        return host;
    }
    public boolean isAlive() {
        return isAlive;
    }
    @Override
    public String toString() {
        return "ReplicaLoc{" + "id='" + id + '\'' + ", host='" + host + '\'' + ", isAlive=" +
                isAlive + '}';
    }
}
