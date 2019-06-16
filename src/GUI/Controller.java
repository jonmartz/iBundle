package GUI;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.util.HashMap;

public class Controller {

    @FXML
    public Text timeText;
    public Canvas canvas;

    public GraphicsContext context;
    public int time = 0;
    public int[][] grid;
    public HashMap<String, int[]> paths;

    public void drawGrid(){
        context = canvas.getGraphicsContext2D();
        double cellWidth = canvas.getWidth()/grid.length;
        double cellHeight = canvas.getHeight()/grid[0].length;
        context.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length; col++) {
                if (grid[row][col] == -1) context.setFill(Color.BLACK);
                else context.setFill(Color.WHITE);
                context.fillRect(col * cellWidth, row * cellHeight, cellHeight, cellWidth);
            }
        }
    }
}
