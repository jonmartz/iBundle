import AuctionStrategies.*;
import Components.*;
import Searchers.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.ArrayList;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("GUI/view.fxml"));
        primaryStage.setTitle("iBundle");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
    }

    public static void main(String[] args) {
//        launch(args);

        // create graph from map
        Graph graph = new Graph("./Resources/den312d.map");
        Node nodeA = graph.getRandomNode();
        Node nodeB = graph.getRandomNode();
        Node nodeC = graph.getRandomNode();
        Node nodeE = graph.getRandomNode();
        Node nodeF = graph.getRandomNode();
        Node nodeI = graph.getRandomNode();

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
        Agent agent1 = new Agent(nodeA, nodeC, new BFSearcher(), graph);
        Agent agent2 = new Agent(nodeB, nodeE, new BFSearcher(), graph);
        Agent agent3 = new Agent(nodeF, nodeI, new BFSearcher(), graph);
        ArrayList<Agent> agents = new ArrayList<>();
//        agents.add(agent1);
        agents.add(agent2);
        agents.add(agent3);

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
            break; //todo: for now just one iteration for testing
        }
    }
}
