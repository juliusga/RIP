package com.base;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Router extends Thread {
    private String ipAdress;
    private UserInterface userInterface;
    private List<Integer> routingTable;
    private List<Neighbour> neighbourList;
    private Queue<Message> messageQueue;

    Router(String ipAdress, UserInterface userInterface, boolean print)
    {
        this.ipAdress = ipAdress;
        this.userInterface = userInterface;
        messageQueue = new LinkedList<Message>();
        userInterface.printConfirm("Router with an IP adress " + ipAdress + " added.");
    }

    public List<Integer> getRoutingTable() {
        return routingTable;
    }

    public void updateRoutingTable(boolean printRoutingTable)
    {
        int index = userInterface.getRouterIndex(ipAdress);
        routingTable = userInterface.getMatrix().get(index);
        neighbourList = new ArrayList<Neighbour>();
        for (int i = 0; i < routingTable.size(); i++)
        {
            if (routingTable.get(i) != 16 || routingTable.get(i) != 0)
            {
                String ipAdress = userInterface.getRouterList().get(i).getIpAdress();
                neighbourList.add(new Neighbour(ipAdress, userInterface.getMatrix().get(i)));
            }
        }
        if (printRoutingTable) printRoutingTable();
    }

    public void printRoutingTable()
    {
        String printStr = "[" + ipAdress + "] - My routing table is: ";
        for (int i = 0; i < routingTable.size(); i++)
        {
            printStr += routingTable.get(i) + "  ";
        }
        userInterface.print(printStr + "\n");
    }

    public void receiveMessage(Message message)
    {
        messageQueue.add(message);
        /*userInterface.printConfirm("[" + ipAdress + "] Received a message from " + message.getSender());*/
    }

    private void sendMessage(Message message)
    {
        userInterface.getNetwork().receiveMessage(message);
    }

    private void applyDVA(int index)
    {
        for (int i = 0; i < routingTable.size(); i++)
        {
            if (i != userInterface.getRouterIndex(ipAdress))
            {
                int newWeight = neighbourList.get(index).getRoutingTable().get(i)
                        + neighbourList.get(index).getRoutingTable().get(userInterface.getRouterIndex(ipAdress));
                if (routingTable.get(i) > newWeight)
                    routingTable.set(i, newWeight);
            }
        }
    }

    public String getIpAdress() {
        return ipAdress;
    }

    public void run() {
        Message message = null;
        while (userInterface.isRunThreads())
        {
            for (int i = 0; i < routingTable.size(); i++)
            {
                if (routingTable.get(i) != 16 && routingTable.get(i) != 0) {
                    message = new Message(ipAdress, userInterface.routerList.get(i).getIpAdress(), null);
                    sendMessage(message);
                }
            }
            while (messageQueue.size() > 0)
            {
                message = messageQueue.remove();
                int index = userInterface.getRouterIndex(message.getSender());
                if (message.getRoutingTable() == null) {
                    if (routingTable.get(index) != 16 && routingTable.get(index) != 0)
                    {
                        message = new Message(ipAdress, message.getSender(), routingTable);
                        sendMessage(message);
                    }
                }
                else
                {
                    int i = 0;
                    String sender = message.getSender();
                    while (!neighbourList.get(i).getIpAdress().equals(sender)) i++;
                    if (true) {
                        neighbourList.get(i).setRoutingTable(message.getRoutingTable());
                        applyDVA(i);
                    }
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
