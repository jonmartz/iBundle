package Main;

import AuctionStrategies.*;
import Components.*;
import Searchers.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.*;
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

//        String[] mapNames = {"den502d", "ost003d", "brc202d"};
        String[] mapNames = {"den502d"};
//        int[] agentCounts = {10, 15, 20, 25, 30, 35, 40};
        int[] agentCounts = {20};
        List<List<String>> rows = new ArrayList<>(); // to write results into csv

        for (Integer agentCount : agentCounts) {
            for (String mapName : mapNames) {
                boolean once = true;
                iteration = 0;
                MDD.resetCheckedSubsets();
                String mapPath = "./Resources/"+mapName+".map";
                GridGraph graph = new GridGraph(mapPath);

                // --- Choose start and goal allocation method ---
//                ArrayList<Agent> agents = randomStartAndGoal(graph, mapPath, agentCount);;
                ArrayList<Agent> agents = instanceStartAndGoal(mapName, mapPath, agentCount);
//                ArrayList<Agent> agents = manualStartAndGoal(mapPath);

                // create auction
                Auction auction = new Auction(1, new WinnerDeterminator());
                System.out.println("\n*****************");
                System.out.println("map: " + mapName+", agents: "+agentCount);

                // run
                long startTime = System.currentTimeMillis();
                while (!auction.finished) {
                    iteration++;

                    // STAGE 1 - bidding

                    int cost = 0; // for printing something
                    for (Agent agent : agents) {
                        if (agent.allocation == null) {
                            Bid bid = agent.getNextBid();
                            cost += bid.mdd.cost;
                            auction.addBid(bid);
                        }
                        else {
//                            printAgentPath(agent);
                            agent.allocation = null;
                        }
                    }
                    if (once) {
                        once = false;
                        System.out.println("relaxed problem cost = " + cost);
                    }

                    System.out.println("------------");
                    System.out.println("iteration " + iteration);

                    // STAGE 2 - winner determination
                    auction.determineWinners();

                    // STAGE 3 - price update
                    auction.updatePrices();
                }
                long runtime = System.currentTimeMillis() - startTime;
                int sumOfCosts = 0;
                for(Agent agent : agents)
                {
                    sumOfCosts += agent.allocation.length-1;
                }
                System.out.println("finished! runtime="+runtime+"ms, cost="+sumOfCosts);
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

    private ArrayList<Agent> manualStartAndGoal(String mapPath) {
        ArrayList<Agent> agents = new ArrayList<>();
        agents.add(new Agent(1, 0, 3, 4, new BFSearcher(), mapPath));
        agents.add(new Agent(0, 2, 2, 2, new BFSearcher(), mapPath));
        return agents;
    }

    private ArrayList<Agent> randomStartAndGoal(GridGraph graph, String mapPath, Integer agentCount) {
        ArrayList<Agent> agents = new ArrayList<>();
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
        return agents;
    }

    private ArrayList<Agent> instanceStartAndGoal(String mapName, String mapPath, int agentCount){
        ArrayList<Agent> agents = new ArrayList<>();
        try {
            File file = new File("./Resources/instances/"+mapName+"-"+agentCount+"-0");
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] s = line.trim().split(",");
                int startX = Integer.parseInt(s[4]);
                int startY = Integer.parseInt(s[3]);
                int goalX = Integer.parseInt(s[2]);
                int goalY = Integer.parseInt(s[1]);
                agents.add(new Agent(startX, startY, goalX, goalY, new BFSearcher(), mapPath));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return agents;
    }
}
