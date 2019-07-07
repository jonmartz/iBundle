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

        // create graph from map
        String graphPath = "./Resources/ca_cave.map";
        GridGraph graph = new GridGraph(graphPath);
        ArrayList<Agent> agents = new ArrayList<>();

//        // 1) random start and goals
//        int numAgents = 10;
//        HashSet<GridNode> startAndGoalNodes = new HashSet<>();
//        GridNode node;
//        for (int i = 0; i < numAgents*2; i++){
//            node = (GridNode) graph.getRandomNode();
//            while (startAndGoalNodes.contains(node))
//                node = (GridNode)graph.getRandomNode();
//            startAndGoalNodes.add(node);
//        }
//        int j = 0;
//        Node startNode = null;
//        for (GridNode gridNode : startAndGoalNodes){
//            if (j % 2 == 0) startNode = gridNode;
//            else agents.add(new Agent(startNode, gridNode, new BFSearcher(), graph));
//            j++;
//        }

//        // 2) manual start and goal nodes
//        // test.map
//        Agent agent1 = new Agent(graph.nodes.get(2), graph.nodes.get(15), new BFSearcher(), graph);
//        Agent agent2 = new Agent(graph.nodes.get(10), graph.nodes.get(13), new BFSearcher(), graph);
//        Agent agent3 = new Agent(graph.nodes.get(5), graph.nodes.get(3), new BFSearcher(), graph);
//        Agent agent4 = new Agent(graph.nodes.get(22), graph.nodes.get(16), new BFSearcher(), graph);
//        Agent agent5 = new Agent(graph.nodes.get(24), graph.nodes.get(5), new BFSearcher(), graph);
//        Agent agent6 = new Agent(graph.nodes.get(21), graph.nodes.get(7), new BFSearcher(), graph);
//        // test2.map
//        Agent agent1 = new Agent(graph.nodes.get(2), graph.nodes.get(14), new BFSearcher(), graph);
//        Agent agent2 = new Agent(graph.nodes.get(9), graph.nodes.get(12), new BFSearcher(), graph);
//        Agent agent3 = new Agent(graph.nodes.get(5), graph.nodes.get(3), new BFSearcher(), graph);
//        Agent agent4 = new Agent(graph.nodes.get(21), graph.nodes.get(15), new BFSearcher(), graph);
//        Agent agent5 = new Agent(graph.nodes.get(23), graph.nodes.get(5), new BFSearcher(), graph);
//        Agent agent6 = new Agent(graph.nodes.get(20), graph.nodes.get(7), new BFSearcher(), graph);
        // ca_cave.map
//        Node start = graph.getNode(117, 199);
//        Node goal = graph.getNode(100, 191);
        agents.add(new Agent(117, 199, 100, 191, new BFSearcher(), graphPath));
        agents.add(new Agent(70, 174, 79, 156, new BFSearcher(), graphPath));
        agents.add(new Agent(82, 59, 73, 78, new BFSearcher(), graphPath));
        agents.add(new Agent(90, 192, 94, 212, new BFSearcher(), graphPath));
        agents.add(new Agent(118, 194, 119, 216, new BFSearcher(), graphPath));
        agents.add(new Agent(133, 173, 135, 153, new BFSearcher(), graphPath));
        agents.add(new Agent(81, 43, 82, 64, new BFSearcher(), graphPath));
        agents.add(new Agent(104, 205, 92, 188, new BFSearcher(), graphPath));
        agents.add(new Agent(131, 82, 116, 68, new BFSearcher(), graphPath));
        agents.add(new Agent(97, 168, 116, 158, new BFSearcher(), graphPath));

        // create auction
        Auction auction = new Auction(1, new WinnerDeterminator());
        Set<Agent> remain = new HashSet<>(agents);
        // run
        while(!auction.finished){
            iteration++;

            if (iteration == 4){
                int x=5;
            }

            // STAGE 1 - bidding
            for (Agent agent : agents) {
                if (agent.allocation == null)
                {

                    auction.addBid(agent.getNextBid());
                }
               else {
                    printAgentPath(agent);
                    agent.allocation = null;
                }
            }
            System.out.println("*****************");
            // STAGE 2 - winner determination
            auction.determineWinners();

            // STAGE 3 - price update
            auction.updatePrices();
            System.out.println("iteration " +iteration);
            System.out.println("The agent(s) that a path was assigned to them are:");

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
            pathInString += ","+agent.allocation[i];
        }

        System.out.println("Agent "+agent.id+" path: "+pathInString);
    }
}
