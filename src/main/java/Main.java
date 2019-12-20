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
    public SidePanel sidePanel;

    public List<WorldMap> mapList;
    public List<MapStats> mapStatsList;
    public List<BoardPanel> boardPanelList;

    public int width, height, startEnergy, moveEnergy, plantEnergy;
    public double jungleRatio;

    public int initialAnimals, initialPlants, plantsGrowth, stepPause, mapNumber;

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

        this.sidePanel = new SidePanel(sidebarWidth, boardHeight, this.mapStatsList);
        this.sidePanel.addActionListener(this);
        frame.add(this.sidePanel);

        for(int i = 0; i < this.mapNumber; i++) {
            BoardPanel boardPanel = new BoardPanel(this.width, this.height, gap, boardWidth, boardHeight, this.mapList.get(i), this.mapStatsList.get(i));
            this.boardPanelList.add(boardPanel);
            frame.add(boardPanel);
        }

//        frame.setSize((boardWidth + 10) * this.mapNumber + sidebarWidth, boardHeight + 10);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.pack();
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
        initialAnimals = params.getInt("initialAnimals");
        initialPlants = params.getInt("initialPlants");
        plantsGrowth = params.getInt("plantsGrowth");
        stepPause = params.getInt("stepPause");
        mapNumber = params.getInt("mapNumber");
    }

    public void run() throws InterruptedException {
        paused = new AtomicBoolean(false);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (paused.get()) {
                        synchronized (threadObject) {
                            try {
                                threadObject.wait();
                            } catch (InterruptedException ignored) {
                            }
                        }
                    }

                    for (WorldMap map : mapList) {
                        map.run();
                    }
                    for (BoardPanel boardPanel : boardPanelList) {
                        boardPanel.renderMap();
                    }
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
            synchronized(this.threadObject) {
                this.threadObject.notify();
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
