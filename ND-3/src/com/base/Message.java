package com.base;

import java.util.List;

public class Message {
    private String sender;
    private String receiver;
    private List<Integer> routingTable;
    private boolean requestMessage;

    public Message(String sender, String receiver, List<Integer> routingTable)
    {
        this.sender = sender;
        this.receiver = receiver;
        if (routingTable == null) requestMessage = true;
        else {
            this.routingTable = routingTable;
            requestMessage = false;
        }
    }

    public List<Integer> getRoutingTable() {
        return routingTable;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getSender() {
        return sender;
    }
}
