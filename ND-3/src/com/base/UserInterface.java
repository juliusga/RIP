package com.base;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class UserInterface {
    public static final String ANSI_RESET   = "\u001B[0m";
    public static final String ANSI_RED     = "\u001B[31m";
    public static final String ANSI_GREEN   = "\u001B[32m";
    public static final String ANSI_YELLOW  = "\u001B[33m";
    public static final String ANSI_BLUE    = "\u001B[34m";
    public static final String PATTERN =
            "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";

    String printStr;
    String input;
    Scanner scanner;
    List<Router> routerList;
    ArrayList<ArrayList<Integer>> matrix;
    ArrayList<ArrayList<Integer>> matrixCOPY;
    Network network;
    boolean runThreads;
    boolean threadInitialized = false;

    UserInterface(){
        printStr = null;
        input = null;
        scanner = new Scanner(System.in);
        routerList = new ArrayList<Router>();
        matrix = new ArrayList<ArrayList<Integer>>();
        matrixCOPY = new ArrayList<ArrayList<Integer>>();
        network = new Network(this);
    }

    public boolean isRunThreads() {
        return runThreads;
    }

    void printWelcome(){
        printInfo("RIP SIMULATION \n" + "Press ENTER to start.");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        printMain();
    }

    void updateRouters()
    {
        for (int i = 0; i < routerList.size(); i++)
        {
            routerList.get(i).updateRoutingTable(false);
        }
        printConfirm("Routing table updating finished.");
    }

    void printMatrix()
    {
        if (routerList.isEmpty()) {
            printError("Current network is empty.");
            return;
        }
        printInfo("Current matrix is: \n");
        printStr = "                  ";
        for (int k = 0; k < matrix.size(); k++) printStr += k + "  ";
        printStr += "\n";
        for (int i = 0; i < matrix.size(); i++)
        {
            printStr += i + " " + routerList.get(i).getIpAdress();
            int spaces = 16 -  routerList.get(i).getIpAdress().length();
            while (spaces > 0)
            {
                printStr += " ";
                spaces--;
            }
            for (int j = 0; j < matrix.size(); j++)
            {
                printStr += matrix.get(i).get(j).toString();
                if (matrix.get(i).get(j) / 10 == 0) printStr += "  ";
                else printStr += " ";
            }
            printStr += "\n";
        }
        print(printStr);
    }

    void printMain(){
        printStr = "\n" + ANSI_BLUE + "OPTIONS: \n" + ANSI_RESET +
                "   [1] View current network. \n" +
                "   [2] Add router. \n" +
                "   [3] Remove router. \n" +
                "   [4] Add link. \n" +
                "   [5] Remove link \n" +
                "   [6] Send packet. \n" +
                "   [7] Load default network configuration. \n" +
                "   [0] Exit. \n";
        print(printStr + "\n");
        Integer option;
        while (true)
        {
            printInfo("Enter option number (0 - 7): ");
            String optionStr = scanner.nextLine();
            try{
                option = Integer.parseInt(optionStr);
            }
            catch (NumberFormatException e)
            {
                printError("Input is invalid.");
                continue;
            }
            if (option < 0 || option > 7)
            {
                printError("Option number must be from 1 to 8 inclusive.");
                continue;
            }
            else break;
        }
        printConfirm("Starting option " + option + ".\n");
        switch (option)
        {
            case 1:
                printInfo("VIEW CURRENT NETWORK \n");
                printMatrix();
                break;
            case 2:
                printInfo("ADD ROUTER \n");
                addRouterUI();
                break;
            case 3:
                printInfo("REMOVE ROUTER \n");
                removeRouterUI();
                break;
            case 4:
                printInfo("ADD LINK \n");
                addLinkUI();
                break;
            case 5:
                printInfo("REMOVE LINK \n");
                removeLinkUI();
                break;
            case 6:
                printInfo("SEND PACKET \n");
                sendPacketUI();
                break;
            case 7:
                printInfo("Load default network configuration: \n");
                loadDefaults();
                break;
            case 0:
                printInfo("Exiting...");
                System.exit(0);
                break;
        }
        printMain();
    }

    private void sendPacketUI()
    {
        matrixCOPY = new ArrayList<ArrayList<Integer>>();
        cloneMatrix(matrix, matrixCOPY);
        runThreads = true;
        if (routerList.isEmpty())
        {
            printError("Router list is empty!");
            return;
        }
        printRouterList();
        int routerA = validateRouterChoice(0) - 1;
        int routerB = validateRouterChoice(routerA) - 1;
        printConfirm("Sending a packet between " + routerList.get(routerA).getIpAdress() + " and " +
            routerList.get(routerB).getIpAdress() + ".");
        printInfo("Please wait...\n");
        if (!threadInitialized)
        {
            network.start();
            Router router = null;
            for (int i = 0; i < routerList.size(); i++)
            {
                router = routerList.get(i);
                router.start();
            }
            threadInitialized = true;
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        printConfirm("Packet from router " + routerList.get(routerA).getIpAdress() + " to router " +
                routerList.get(routerB).getIpAdress() + " sent with a weight of "
                + routerList.get(routerA).getRoutingTable().get(routerB));
        runThreads = false;
        matrix = new ArrayList<ArrayList<Integer>>();
        cloneMatrix(matrixCOPY, matrix);
    }

    private void loadDefaults()
    {
        addRouter("1.100.100.100"); // 0
        addRouter("2.100.100.100"); // 1
        addRouter("3.100.100.100"); // 2
        addRouter("4.100.100.100"); // 3
        addRouter("5.100.100.100"); // 4
        addRouter("6.100.100.100"); // 5
        addRouter("7.100.100.100"); // 6
        addRouter("8.100.100.100"); // 7
        addLink(0, 1, 3);
        addLink(0, 2, 6);
        addLink(0, 3, 5);
        addLink(1, 4, 1);
        addLink(2, 4, 9);
        addLink(2, 5, 7);
        addLink(3, 6, 2);
        addLink(4, 7, 3);
        addLink(5, 7, 1);
        addLink(6, 7, 1);
    }

    private void addRouterUI()
    {
        String ipAdress;
        while (true) {
            printInfo("Enter router IP adress: ");
            input = scanner.nextLine();
            if (!validateIP(input))
                printError("IP adress doesn't match the pattern. Please try again.");
            else if (doesRouterExists(input))
            {
                printError("Router with this ip adress (" + input + ") already exists. Please try again.");
                continue;
            }
            else {
                ipAdress = input;
                break;
            }
        }
        addRouter(ipAdress);
    }

    private void addRouter(String ipAdress)
    {
        Router router = new Router(ipAdress, this, true);
        routerList.add(router);
        matrix.add(new ArrayList<Integer>());
        for(int i = 0; i < matrix.size() - 1; i++)
        {
            matrix.get(matrix.size() - 1).add(16);
        }
        for (int i = 0; i < matrix.size(); i++)
        {
            matrix.get(i).add(16);
        }
        matrix.get(routerList.size() - 1).set(routerList.size() - 1, 0);
        updateRouters();
    }

    boolean doesRouterExists(String ipAdress)
    {
        int i = 0;
        while (i < routerList.size())
        {
            if (routerList.get(i).getIpAdress().equals(ipAdress)) return true;
            i++;
        }
        return false;
    }

    private void addLinkUI()
    {
        int routerA, routerB, weight;
        if (routerList.size() < 2)
            printError("Router list is empty. Please create atleast 2 routers first.");
        else
        {
            printRouterList();
            routerA = validateRouterChoice(0);
            routerB = validateRouterChoice(routerA);
            while (true)
            {
                printInfo("Enter the weight of this link (1 - 15): ");
                input = scanner.nextLine();
                try{
                    weight = Integer.parseInt(input);
                }
                catch (NumberFormatException e)
                {
                    printError("Input is invalid.");
                    continue;
                }
                if (weight < 1 || weight > 15)
                {
                    printError("Weight must be from 1 to 15 inclusive.");
                    continue;
                }
                else break;
            }
            routerA--;
            routerB--;
            addLink(routerA, routerB, weight);
        }
    }

    private void addLink(int routerA, int routerB, int weight)
    {
        if (matrix.get(routerA).get(routerB) != 16)
            printWarning("Link between these routers already exists! It will be replaced with the new one.");
        matrix.get(routerA).set(routerB, weight);
        matrix.get(routerB).set(routerA, weight);
        printConfirm("A link between " + routerList.get(routerA).getIpAdress() + " and " +
                routerList.get(routerB).getIpAdress() + " with a weight of " + weight + " successfully created.");
    }

    private int validateRouterChoice(int exclude)
    {
        int router;
        while (true)
        {
            printInfo("Choose the router from the list (1 - " + routerList.size() + "): ");
            input = scanner.nextLine();
            try{
                router = Integer.parseInt(input);
            }
            catch (NumberFormatException e)
            {
                printError("Input is invalid.");
                continue;
            }
            if (router <= 0 || router > routerList.size())
            {
                printError("Router number must be from 1 to " + routerList.size() + " inclusive.");
                continue;
            }
            else if (router == exclude) printError("Cannot choose the same routers.");
            else break;
        }
        return router;
    }

    private void removeRouterUI()
    {
        if (routerList.isEmpty()) printError("Router list is empty.");
        else {
            printRouterList();
            int router = validateRouterChoice(0) - 1;
            printConfirm("Router " + routerList.get(router).getIpAdress() + " successfully removed.");
            removeRouter(router);
        }
    }

    private void removeRouter(int routerIndex)
    {
        Router router = routerList.get(routerIndex);
        routerList.remove(router);
        matrix.remove(routerIndex);
        for(int i = 0; i < matrix.size(); i++)
        {
            matrix.get(i).remove(routerIndex);
        }
    }

    private void removeLinkUI()
    {
        class Link {
            int routerA;
            int routerB;
            Link(int routerA, int routerB) {
                this.routerA = routerA;
                this.routerB = routerB;
            }
        }
        int weight, link, counter = 0;
        boolean linkExists = false;
        printStr = "LINKS: \n";
        List<Link> links = new ArrayList<Link>();
        for (int i = 0; i < routerList.size(); i++)
        {
            for (int j = i; j < routerList.size(); j++)
            {
                weight = matrix.get(i).get(j);
                if (weight != 0 && weight != 16)
                {
                    if(!linkExists) linkExists = true;
                    printStr += "   [" + (counter + 1) + "] " + routerList.get(i).getIpAdress() + " <-> " +
                            routerList.get(j).getIpAdress() + "\n";
                    links.add(new Link(i, j));
                    counter++;
                }
            }
        }
        if (linkExists){
            print(printStr);
            while (true)
            {
                printInfo("Choose a link to remove (1 - " + counter + "): ");
                input = scanner.nextLine();
                try{
                    link = Integer.parseInt(input);
                }
                catch (NumberFormatException e)
                {
                    printError("Input is invalid.");
                    continue;
                }
                if (link < 1 || link > counter)
                {
                    printError("Link number must be from 1 to " + counter + " inclusive.");
                    continue;
                }
                else break;
            }
            int r1 = links.get(link - 1).routerA;
            int r2 = links.get(link - 1).routerB;
            removeLink(r1, r2);
            printConfirm("Link between " + routerList.get(r1).getIpAdress() + " and " +
                    routerList.get(r2).getIpAdress() + " successfully removed.");
        }
        else printError("No links found.");
    }

    private void removeLink(int routerA, int routerB)
    {
        matrix.get(routerA).set(routerB, 16);
        matrix.get(routerB).set(routerA, 16);
    }

    private void printRouterList()
    {
        printStr = "ROUTERS:\n";
        for (int i = 0; i < routerList.size(); i++)
        {
            printStr += "   [" + (i + 1) + "] " + routerList.get(i).getIpAdress() + "\n";
        }
        print(printStr + "\n");
    }

    public static boolean validateIP(final String ip) {
        return ip.matches(PATTERN);
    }

    public int getRouterIndex(String ipAdress)
    {
        int i = 0;
        while (!routerList.get(i).getIpAdress().equals(ipAdress)) i++;
        return i;
    }

    public Network getNetwork() {
        return network;
    }

    public ArrayList<ArrayList<Integer>> getMatrix() {
        return matrix;
    }

    public List<Router> getRouterList() {
        return routerList;
    }

    public void print(String printable)
    {
        System.out.print(printable);
    }

    public void printError(String string)
    {
        System.out.println("[" + ANSI_RED + "ERROR" + ANSI_RESET + "] " + string);
    }

    public void printConfirm(String string)
    {
        System.out.println("[" + ANSI_GREEN + "OK" + ANSI_RESET + "] " + string);
    }

    public void printInfo(String string)
    {
        System.out.print(ANSI_BLUE + string + ANSI_RESET);
    }

    public void printWarning(String string)
    {
        System.out.println("[" + ANSI_YELLOW + "WARNING" + ANSI_RESET + "] " + string);
    }

    private void cloneMatrix(ArrayList<ArrayList<Integer>> oldMatrix, ArrayList<ArrayList<Integer>> newMatrix)
    {
        //newMatrix = new ArrayList<ArrayList<Integer>>();
        for (int i = 0; i < oldMatrix.size(); i++)
        {
            ArrayList<Integer> list = new ArrayList<Integer>();
            for (int j = 0; j < oldMatrix.size(); j++)
            {
                list.add(oldMatrix.get(i).get(j).intValue());
            }
            newMatrix.add(list);
        }
    }
}
