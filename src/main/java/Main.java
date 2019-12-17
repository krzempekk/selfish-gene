import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import javax.swing.*;
import org.json.*;

public class Main implements ActionListener {
    public int gap = 0;
    public int boardWidth = 1000, boardHeight = 1000, sidebarWidth = 400;
    public int stepSkip = 1;

    public WorldMap map;
    public MapStats mapStats;

    public BoardPanel boardPanel;
    public SidePanel sidePanel;

    public int width, height, startEnergy, moveEnergy, plantEnergy;
    public double jungleRatio;

    public int initialAnimals = 10, initialPlants = 10, plantsGrowth = 1, stepPause = 200;

    private AtomicBoolean paused;

    private Thread threadObject;
    private Runnable runnable;

    Main() {
        this.readParameters();

        JFrame frame = new JFrame();
        frame.setLayout(new FlowLayout(FlowLayout.LEFT, 0,0 ));

        this.map = new WorldMap(width, height, initialAnimals, initialPlants, plantsGrowth, jungleRatio, startEnergy, moveEnergy, plantEnergy);
        this.mapStats = new MapStats(map);

        this.sidePanel = new SidePanel(sidebarWidth, boardHeight, mapStats);
        this.sidePanel.addActionListener(this);

        this.boardPanel = new BoardPanel(width, height, gap, boardWidth, boardHeight, map, this.sidePanel);

        frame.add(boardPanel);
        frame.add(sidePanel);
        frame.setSize(boardWidth + sidebarWidth, boardHeight);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private String getParametersFile() {
        StringBuilder contentBuilder = new StringBuilder();
        try (Stream<String> stream = Files.lines(Paths.get(getClass().getResource("parameters.json").toURI()), StandardCharsets.UTF_8)) {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        return contentBuilder.toString();
    }

    private void readParameters() {
        String paramsFile = getParametersFile();
        JSONObject params = new JSONObject(paramsFile);

        width = params.getInt("width");
        height = params.getInt("height");
        startEnergy = params.getInt("startEnergy");
        moveEnergy = params.getInt("moveEnergy");
        plantEnergy = params.getInt("plantEnergy");
        jungleRatio = params.getDouble("jungleRatio");
    }

    public void run() throws InterruptedException {
        paused = new AtomicBoolean(false);

        this.runnable = new Runnable() {
            @Override
            public void run() {
                while(true) {
                    if(paused.get()) {
                        synchronized(threadObject) {
                            try {
                                threadObject.wait();
                            } catch (InterruptedException ignored) { }
                        }
                    }

                    map.run();
                    boardPanel.renderMap();
                    sidePanel.update();

                    try {
                        Thread.sleep(stepPause);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        threadObject = new Thread(runnable);
        threadObject.start();
    }

    public static void main(String[] args) throws InterruptedException {
        Main gui = new Main();
        gui.run();
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        String command = actionEvent.getActionCommand();
        if(command.equals("PAUSE")) {
            this.paused.set(true);
        } else if(command.equals("RESUME")) {
            this.paused.set(false);
            synchronized(threadObject) {
                threadObject.notify();
            }
        }
    }
}
