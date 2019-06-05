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

        // create nodes
        Node nodeA = new Node();
        Node nodeB = new Node();
        Node nodeC = new Node();
        Node nodeD = new Node();
        Node nodeE = new Node();
        Node nodeF = new Node();
        Node nodeG = new Node();
        Node nodeH = new Node();
        Node nodeI = new Node();

        // connect nodes
        nodeA.addNeighbor(nodeB);
        nodeA.addNeighbor(nodeC);
        nodeB.addNeighbor(nodeD);
        nodeC.addNeighbor(nodeD);
        nodeC.addNeighbor(nodeE);
        nodeD.addNeighbor(nodeF);
        nodeD.addNeighbor(nodeG);
        nodeE.addNeighbor(nodeG);
        nodeE.addNeighbor(nodeI);
        nodeF.addNeighbor(nodeH);

        // add nodes to graph
        Graph graph = new Graph();
        graph.addNode(nodeA);
        graph.addNode(nodeB);
        graph.addNode(nodeC);
        graph.addNode(nodeD);
        graph.addNode(nodeE);
        graph.addNode(nodeF);
        graph.addNode(nodeG);
        graph.addNode(nodeH);
        graph.addNode(nodeI);

        // create agents
        Agent agent1 = new Agent(nodeA, nodeC, new BFSearcher());
        Agent agent2 = new Agent(nodeB, nodeE, new BFSearcher());
        Agent agent3 = new Agent(nodeF, nodeI, new BFSearcher());
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
            for (Agent agent : agents) auction.addBid(agent.getNextBid());
            // STAGE 2 - winner determination
            auction.determineWinners();
            // STAGE 3 - price update
            auction.updatePrices();
            break; //todo: for now just one iteration for testing
        }
    }
}
