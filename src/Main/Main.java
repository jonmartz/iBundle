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

        boolean launchGUI = true;

//        String[] graphPaths = {"./Resources/den502d.map", "./Resources/ost003d.map", "./Resources/brc202d.map"};
//        int[] agentCounts = {10, 15, 20, 25, 30, 35, 40};
        String[] mapNames = {"den312d"};
//        String[] graphPaths = {"./Resources/test2.map"};
        int[] agentCounts = {10};
        List<List<String>> rows = new ArrayList<>(); // to write results into csv

        for (Integer agentCount : agentCounts) {
            for (String mapName : mapNames) {
                String mapPath = "./Resources/"+mapName+".map";
                // create graph from map
                GridGraph graph = new GridGraph(mapPath);
                ArrayList<Agent> agents = new ArrayList<>();

                // 1) random start and goals
                HashSet<GridNode> startAndGoalNodes = new HashSet<>();
                GridNode node;
                for (int i = 0; i < agentCount * 2; i++) {
                    node = (GridNode) graph.getRandomNode();
                    while (startAndGoalNodes.contains(node))
                        node = (GridNode) graph.getRandomNode();
                    startAndGoalNodes.add(node);
                }
                int j = 0;
                GridNode startNode = null;
                for (GridNode gridNode : startAndGoalNodes) {
                    if (j % 2 == 0) startNode = gridNode;
                    else agents.add(new Agent(startNode.x, startNode.y, gridNode.x, gridNode.y, new BFSearcher(), mapPath));
                    j++;
                }

//                // 2) manual
//                agents.add(new Agent(1, 0, 3, 4, new BFSearcher(), mapPath));
//                agents.add(new Agent(0, 2, 2, 2, new BFSearcher(), mapPath));

                // create auction
                Auction auction = new Auction(1, new WinnerDeterminator());

                // run
                long startTime = System.currentTimeMillis();
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
                long runtime = System.currentTimeMillis() - startTime;

                int sumOfCosts = 0;
                System.out.println("The conclusion:");
                for(Agent agent : agents)
                {
                    sumOfCosts += agent.allocation.length;
                    printAgentPath(agent);
                }
                System.out.println("*****************");

                List<String> row = new ArrayList<>();
                row.add(String.valueOf(agentCount));
                row.add(mapName);
                row.add(String.valueOf(runtime));
                row.add(String.valueOf(sumOfCosts));

                rows.add(row);

                if (launchGUI) {
                    FXMLLoader fxmlLoader = new FXMLLoader();
                    Parent root = fxmlLoader.load(getClass().getResource("view.fxml").openStream());
                    controller = fxmlLoader.getController();
                    primaryStage.setTitle("iBundle");
                    primaryStage.setScene(new Scene(root, 800, 540));
                    primaryStage.show();
                    controller.initialize(graph.intGrid);
                    for (Agent agent : agents) controller.addAgent(agent.allocation);
                    controller.draw();
//                    System.out.println("Press enter for next scenario");
//                    System.in.read();
                }
            }
        }

        FileWriter csvWriter = new FileWriter("iBundleResults.csv");
        csvWriter.append("Num Of Agents,");
        csvWriter.append("Map Name,");
        csvWriter.append("iBundle Runtime,");
        csvWriter.append("iBundle Solution Cost\n");

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
