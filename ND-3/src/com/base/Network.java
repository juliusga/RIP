package com.base;

import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

public class Network extends Thread{
    private Queue<Message> messages;
    private UserInterface userInterface;

    public Network(UserInterface userInterface)
    {
        this.userInterface = userInterface;
        messages = new LinkedList<Message>();
    }

    public void receiveMessage(Message message)
    {
        messages.add(message);
        /* userInterface.printInfo("[NETWORK] received a message from " + message.getSender()
                + " to " + message.getReceiver() + ".\n");*/
    }

    public void run() {
        Message current;
        while (userInterface.isRunThreads())
        {
            if(!messages.isEmpty());
            {
                try {
                    current = messages.poll();
                    if (current != null)
                    {
                        int receiverIndex = userInterface.getRouterIndex(current.getReceiver());
                        userInterface.routerList.get(receiverIndex).receiveMessage(current);
                        /*userInterface.printInfo("[" + current.getSender()
                                + "] sent message to [" + current.getReceiver() + "].\n");*/
                    }
                }
                catch (NoSuchElementException e)
                {
                    userInterface.printError(messages.size() + " NoSuchElementException");
                }
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
