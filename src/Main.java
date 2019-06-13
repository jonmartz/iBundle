import AuctionStrategies.*;
import Components.*;
import Searchers.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Main{// extends Application {

    //@Override
    public void start(Stage primaryStage) throws Exception{
        /*
        Parent root = FXMLLoader.load(getClass().getResource("GUI/view.fxml"));
        primaryStage.setTitle("iBundle");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();*/
    }

    public static void main(String[] args) {
//        launch(args);
        List<Agent> list = new ArrayList<>();
        list.add(null);
        list.add(null);
        list.add(null);
        list.add(null);
        list.add(null);
        list.add(null);

        IWinnerDeterminator iWinnerDeterminator = new WinnerDeterminator();
        iWinnerDeterminator.getWinners(list);

       /* // simple test
        GridGraph graph = new GridGraph("./Resources/test.map");
        Agent agent1 = new Agent(graph.nodes.get(1), graph.nodes.get(3), new AStarSearcher(), graph);



        // big test
        // create graph from map
//        Graph graph = new Graph("./Resources/den312d.map");
//        HashSet<Node> startAndGoalNodes = new HashSet<>();

        // Get random start and goal states from graph
//        GridNode nodeA = (GridNode)graph.getRandomNode();
//        startAndGoalNodes.add(nodeA);
//        GridNode nodeB = (GridNode)graph.getRandomNode();
//        while (startAndGoalNodes.contains(nodeB)) nodeB = (GridNode)graph.getRandomNode();
//        startAndGoalNodes.add(nodeB);
//        GridNode nodeC = (GridNode)graph.getRandomNode();
//        while (startAndGoalNodes.contains(nodeC)) nodeC = (GridNode)graph.getRandomNode();
//        startAndGoalNodes.add(nodeC);
//        GridNode nodeE = (GridNode)graph.getRandomNode();
//        while (startAndGoalNodes.contains(nodeE)) nodeE = (GridNode)graph.getRandomNode();
//        startAndGoalNodes.add(nodeE);
//        GridNode nodeF = (GridNode)graph.getRandomNode();
//        while (startAndGoalNodes.contains(nodeF)) nodeF = (GridNode)graph.getRandomNode();
//        startAndGoalNodes.add(nodeF);
//        GridNode nodeI = (GridNode)graph.getRandomNode();
//        while (startAndGoalNodes.contains(nodeI)) nodeI = (GridNode)graph.getRandomNode();
//        startAndGoalNodes.add(nodeI);

        // create nodes
//        Node nodeA = new Node(0);
//        Node nodeB = new Node(1);
//        Node nodeC = new Node(2);
//        Node nodeD = new Node(3);
//        Node nodeE = new Node(4);
//        Node nodeF = new Node(5);
//        Node nodeG = new Node(6);
//        Node nodeH = new Node(7);
//        Node nodeI = new Node(8);
//
//        // connect nodes
//        nodeA.addNeighbor(nodeB);
//        nodeA.addNeighbor(nodeC);
//        nodeB.addNeighbor(nodeD);
//        nodeC.addNeighbor(nodeD);
//        nodeC.addNeighbor(nodeE);
//        nodeD.addNeighbor(nodeF);
//        nodeD.addNeighbor(nodeG);
//        nodeE.addNeighbor(nodeG);
//        nodeE.addNeighbor(nodeI);
//        nodeF.addNeighbor(nodeH);
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
//        Agent agent1 = new Agent(nodeA, nodeC, new AStarSearcher(), graph);
//        Agent agent2 = new Agent(nodeB, nodeE, new BFSearcher(), graph);
//        Agent agent3 = new Agent(nodeF, nodeI, new BFSearcher(), graph);

        ArrayList<Agent> agents = new ArrayList<>();
        agents.add(agent1);
//        agents.add(agent2);
//        agents.add(agent3);

        // create auction
        Auction auction = new Auction(1, new AuctionStrategy());

        // run
        int iteration = 0;
        while(!auction.finished){
            iteration++;
            // STAGE 1 - bidding
            for (Agent agent : agents) {
                if (agent.allocation == null) auction.addBid(agent.getNextBid());
            }
            // STAGE 2 - winner determination
            auction.determineWinners();
            // STAGE 3 - price update
            auction.updatePrices();
        }*/
    }
}
