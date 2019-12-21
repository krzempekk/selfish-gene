import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.json.*;
import java.util.List;

public class Main implements ActionListener, ChangeListener {
    private SidePanel sidePanel;

    private List<WorldMap> mapList;
    private List<MapStats> mapStatsList;
    private List<BoardPanel> boardPanelList;

    private int width, height, startEnergy, moveEnergy, plantEnergy;
    private double jungleRatio;

    private int initialAnimals, initialPlants, plantsGrowth, stepPause, mapNumber;

    private AtomicBoolean paused;

    private Thread threadObject;

    Main() {
        this.readParameters();

        int boardHeight = Math.min(height * 50, 500);
        int boardWidth = (int) (boardHeight * ((double) this.width / this.height));

        int sidebarWidth = 400, gap = 0;

        JFrame frame = new JFrame();
        frame.setLayout(new FlowLayout(FlowLayout.LEFT, 10,10 ));

        this.mapList = new ArrayList<>();
        this.mapStatsList = new ArrayList<>();
        this.boardPanelList = new ArrayList<>();

        for(int i = 0; i < this.mapNumber; i++) {
            WorldMap map = new WorldMap(this.width, this.height, this.initialAnimals, this.initialPlants, this.plantsGrowth, this.jungleRatio, this.startEnergy, this.moveEnergy, this.plantEnergy);
            this.mapList.add(map);
            this.mapStatsList.add(new MapStats(map));
        }

        this.sidePanel = new SidePanel(sidebarWidth, boardHeight, this.mapStatsList, this.stepPause);
        this.sidePanel.addActionListener(this);
        frame.add(this.sidePanel);

        for(int i = 0; i < this.mapNumber; i++) {
            BoardPanel boardPanel = new BoardPanel(this.width, this.height, gap, boardWidth, boardHeight, this.mapList.get(i), this.mapStatsList.get(i), this.sidePanel);
            this.boardPanelList.add(boardPanel);
            frame.add(boardPanel);
        }

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.pack();
    }

    private String getParametersFile() {
        StringBuilder contentBuilder = new StringBuilder();
        try (Stream<String> stream = Files.lines(Paths.get(this.getClass().getResource("parameters.json").toURI()), StandardCharsets.UTF_8)) {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        return contentBuilder.toString();
    }

    private void readParameters() {
        String paramsFile = this.getParametersFile();
        JSONObject params = new JSONObject(paramsFile);

        this.width = params.getInt("width");
        this.height = params.getInt("height");
        this.startEnergy = params.getInt("startEnergy");
        this.moveEnergy = params.getInt("moveEnergy");
        this.plantEnergy = params.getInt("plantEnergy");
        this.jungleRatio = params.getDouble("jungleRatio");
        this.initialAnimals = params.getInt("initialAnimals");
        this.initialPlants = params.getInt("initialPlants");
        this.plantsGrowth = params.getInt("plantsGrowth");
        this.stepPause = params.getInt("stepPause");
        this.mapNumber = params.getInt("mapNumber");
    }

    public void run() throws InterruptedException {
        this.paused = new AtomicBoolean(false);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (Main.this.paused.get()) {
                        synchronized (Main.this.threadObject) {
                            try {
                                Main.this.threadObject.wait();
                            } catch (InterruptedException ignored) {
                            }
                        }
                    }

                    for (WorldMap map : Main.this.mapList) {
                        map.run();
                    }
                    for (BoardPanel boardPanel : Main.this.boardPanelList) {
                        boardPanel.renderMap();
                    }
                    Main.this.sidePanel.update();

                    try {
                        Thread.sleep(Main.this.stepPause);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        this.threadObject = new Thread(runnable);
        this.threadObject.start();
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
            synchronized(this.threadObject) {
                this.threadObject.notify();
            }
        } else if(command.equals("SHOW DOM. GENOME")) {
            for(MapStats mapStats: this.mapStatsList) {
                mapStats.setShowDominatingGenome(true);
            }
            for(BoardPanel boardPanel: this.boardPanelList) {
                boardPanel.renderMap();
            }
        } else if(command.equals("HIDE DOM. GENOME")) {
            for(MapStats mapStats: this.mapStatsList) {
                mapStats.setShowDominatingGenome(false);
            }
            for(BoardPanel boardPanel: this.boardPanelList) {
                boardPanel.renderMap();
            }

        }
    }

    public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider) e.getSource();
        if (!source.getValueIsAdjusting()) {
            this.stepPause = source.getValue();
        }
    }
}
