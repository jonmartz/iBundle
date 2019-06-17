package Main;

import AuctionStrategies.*;
import Components.*;
import Searchers.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.*;

public class Main extends Application {

    public static int iteration;
    public static Controller controller;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource("view.fxml").openStream());
        controller = fxmlLoader.getController();
        primaryStage.setTitle("iBundle");
        primaryStage.setScene(new Scene(root, 800 , 540));
        primaryStage.show();

        //main

//        List<Agent> list = new ArrayList<>();
//        list.add(null);
//        list.add(null);
//        list.add(null);
//        list.add(null);
//        list.add(null);
//        list.add(null);
//
//        IWinnerDeterminator iWinnerDeterminator = new WinnerDeterminator();
//        iWinnerDeterminator.getWinners(list);

//        // simple test
//        GridGraph graph = new GridGraph("./Resources/test.map");
//        Agent agent1 = new Agent(graph.nodes.get(1), graph.nodes.get(3), new AStarSearcher(), graph);

        // big test
        // create graph from map
        GridGraph graph = new GridGraph("./Resources/den312d.map");
        HashSet<Node> startAndGoalNodes = new HashSet<>();

        // Get random start and goal states from graph
        GridNode nodeA = (GridNode)graph.getRandomNode();
        startAndGoalNodes.add(nodeA);
        GridNode nodeB = (GridNode)graph.getRandomNode();
        while (startAndGoalNodes.contains(nodeB)) nodeB = (GridNode)graph.getRandomNode();
        startAndGoalNodes.add(nodeB);
        GridNode nodeC = (GridNode)graph.getRandomNode();
        while (startAndGoalNodes.contains(nodeC)) nodeC = (GridNode)graph.getRandomNode();
        startAndGoalNodes.add(nodeC);
        GridNode nodeD = (GridNode)graph.getRandomNode();
        while (startAndGoalNodes.contains(nodeD)) nodeD = (GridNode)graph.getRandomNode();
        startAndGoalNodes.add(nodeD);
        GridNode nodeE = (GridNode)graph.getRandomNode();
        while (startAndGoalNodes.contains(nodeE)) nodeE = (GridNode)graph.getRandomNode();
        startAndGoalNodes.add(nodeE);
        GridNode nodeF = (GridNode)graph.getRandomNode();
        while (startAndGoalNodes.contains(nodeF)) nodeF = (GridNode)graph.getRandomNode();
        startAndGoalNodes.add(nodeF);
        GridNode nodeG = (GridNode)graph.getRandomNode();
        while (startAndGoalNodes.contains(nodeG)) nodeG = (GridNode)graph.getRandomNode();
        startAndGoalNodes.add(nodeG);
        GridNode nodeI = (GridNode)graph.getRandomNode();
        while (startAndGoalNodes.contains(nodeI)) nodeI = (GridNode)graph.getRandomNode();
        startAndGoalNodes.add(nodeI);

//        // create nodes
//        GridNode nodeA = new GridNode(0, 0,0);
//        GridNode nodeB = new GridNode(1, 0,0);
//        GridNode nodeC = new GridNode(2,0,0);
//        GridNode nodeD = new GridNode(3,0,0);
//        GridNode nodeE = new GridNode(4,0,0);
//        GridNode nodeF = new GridNode(5,0,0);
//        GridNode nodeG = new GridNode(6,0,0);
//        GridNode nodeH = new GridNode(7,0,0);
//        GridNode nodeI = new GridNode(8,0,0);
//
//        // connect nodes
//        nodeA.addNeighbor(nodeB);//0-1
//        nodeA.addNeighbor(nodeC);//0-2
//        nodeB.addNeighbor(nodeD);//1-3
//        nodeC.addNeighbor(nodeD);//2-3
//        nodeC.addNeighbor(nodeE);//2-4
//        nodeD.addNeighbor(nodeF);//3-5
//        nodeD.addNeighbor(nodeG);//3-6
//        nodeE.addNeighbor(nodeG);//4-6
//        nodeE.addNeighbor(nodeI);//4-8
//        nodeF.addNeighbor(nodeH);//5-7
//
//        // add nodes to graph
//        Graph graph = new Graph();
//        graph.addNode(nodeA);
//        graph.addNode(nodeB);
//        graph.addNode(nodeC);
//        graph.addNode(nodeD);
//        graph.addNode(nodeE);
//        graph.addNode(nodeF);
//        graph.addNode(nodeG);
//        graph.addNode(nodeH);
//        graph.addNode(nodeI);

        // create agents
        Agent agent1 = new Agent(nodeA, nodeG, new BFSearcher(), graph);
        Agent agent2 = new Agent(nodeB, nodeE, new BFSearcher(), graph);
        Agent agent3 = new Agent(nodeF, nodeI, new BFSearcher(), graph);
        Agent agent4 = new Agent(nodeE, nodeD, new BFSearcher(), graph);
//        Agent agent5 = new Agent(nodeH, nodeA, new BFSearcher(), graph);
//        Agent agent6 = new Agent(nodeG, nodeC, new BFSearcher(), graph);

        ArrayList<Agent> agents = new ArrayList<>();
        agents.add(agent1);
        agents.add(agent2);
        agents.add(agent3);
        agents.add(agent4);
//        agents.add(agent5);
//        agents.add(agent6);

        // create auction
        Auction auction = new Auction(1, new WinnerDeterminator());
        Set<Agent> remain = new HashSet<>(agents);
        // run
        while(!auction.finished){
            iteration++;
            System.out.println("iteration " +iteration);
            // STAGE 1 - bidding
            for (Agent agent : agents) {
                if (agent.allocation == null) auction.addBid(agent.getNextBid());
            }
            // STAGE 2 - winner determination
            auction.determineWinners();
            // STAGE 3 - price update
            auction.updatePrices();

            System.out.println("The agent(s) that a path was assigned to them are:");
            Iterator<Agent> iter = remain.iterator();
            while(iter.hasNext())
            {
                Agent agent = iter.next();
                if (agent.allocation != null)
                {
                    iter.remove();
                    printAgentPath(agent);
                }
            }
            System.out.println("*****************");
        }

        System.out.println("The conclusion:");
        for(int i=0;i<agents.size();i++)
        {
            printAgentPath(agents.get(i));
        }
        System.out.println("*****************");

        // launch GUI
        controller.initialize(graph.intGrid);
        for (Agent agent : agents) controller.addAgent(agent.allocation);
        controller.draw();
    }

    public static void printAgentPath(Agent agent)
    {
        String pathInString = "";
        int [] path = agent.allocation;

        pathInString += agent.allocation[0];
        for(int i=1;i<path.length;i++)
        {
            pathInString+= ","+agent.allocation[i];
        }

        System.out.println("Agent "+agent.id+" path: "+pathInString);
    }
}
