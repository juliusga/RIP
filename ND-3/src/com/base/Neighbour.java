package com.base;

import java.util.List;

public class Neighbour {
    private String ipAdress;
    private List<Integer> routingTable;

    public Neighbour(String ipAdress, List<Integer> routingTable)
    {
        this.ipAdress = ipAdress;
        this.routingTable = routingTable;
    }

    public String getIpAdress() {
        return ipAdress;
    }

    public List<Integer> getRoutingTable() {
        return routingTable;
    }

    public void setRoutingTable(List<Integer> routingTable) {
        this.routingTable = routingTable;
    }
}
