package Main;

import AuctionStrategies.*;
import Components.*;
import Searchers.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.FileWriter;
import java.util.*;

public class Main extends Application {

    public static int iteration;
    public static Controller controller;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
//        FXMLLoader fxmlLoader = new FXMLLoader();
//        Parent root = fxmlLoader.load(getClass().getResource("view.fxml").openStream());
//        controller = fxmlLoader.getController();
//        primaryStage.setTitle("iBundle");
//        primaryStage.setScene(new Scene(root, 800 , 540));
//        primaryStage.show();

//        String[] graphPaths = {"./Resources/den502d.map", "./Resources/ost003d.map", "./Resources/brc202d.map"};
        String[] graphPaths = {"./Resources/den312d.map"};
//        int[] agentCounts = {10, 15, 20, 25, 30, 35, 40};
        int[] agentCounts = {10};
        List<List<String>> rows = new ArrayList<>(); // to write results into csv

        for (Integer numAgents : agentCounts) {
            for (String graphPath : graphPaths) {
                // create graph from map
                GridGraph graph = new GridGraph(graphPath);
                ArrayList<Agent> agents = new ArrayList<>();

                // 1) random start and goals
                HashSet<GridNode> startAndGoalNodes = new HashSet<>();
                GridNode node;
                for (int i = 0; i < numAgents * 2; i++) {
                    node = (GridNode) graph.getRandomNode();
                    while (startAndGoalNodes.contains(node))
                        node = (GridNode) graph.getRandomNode();
                    startAndGoalNodes.add(node);
                }
                int j = 0;
                GridNode startNode = null;
                for (GridNode gridNode : startAndGoalNodes) {
                    if (j % 2 == 0) startNode = gridNode;
                    else agents.add(new Agent(startNode.x, startNode.y, gridNode.x, gridNode.y, new BFSearcher(), graphPath));
                    j++;
                }

                // create auction
                Auction auction = new Auction(1, new WinnerDeterminator());

                // run
                while (!auction.finished) {
                    iteration++;

                    // STAGE 1 - bidding
                    for (Agent agent : agents) {
                        if (agent.allocation == null)
                            auction.addBid(agent.getNextBid());
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
                    System.out.println("iteration " + iteration);
                    System.out.println("The agent(s) that a path was assigned to them are:");
                }

                System.out.println("The conclusion:");
                for(int i=0;i<agents.size();i++)
                {
                    printAgentPath(agents.get(i));
                }
                System.out.println("*****************");

//                // launch GUI
//                controller.initialize(graph.intGrid);
//                for (Agent agent : agents) controller.addAgent(agent.allocation);
//                controller.draw();
            }
        }

        FileWriter csvWriter = new FileWriter("iBundleResults.csv");
        csvWriter.append("Grid Name");
        csvWriter.append(",");
        csvWriter.append("Num Of Agents");
        csvWriter.append(",");
        csvWriter.append("iBundle Runtime");
        csvWriter.append(",");
        csvWriter.append("iBundle Solution Cost");
        csvWriter.append("\n");

        for (List<String> rowData : rows) {
            csvWriter.append(String.join(",", rowData));
            csvWriter.append("\n");
        }

        csvWriter.flush();
        csvWriter.close();

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
