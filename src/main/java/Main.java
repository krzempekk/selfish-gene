import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import javax.swing.*;
import org.json.*;

public class Main implements ActionListener {
    public int gap = 0;
    public int boardWidth = 1000, boardHeight = 1000, sidebarWidth = 400;
    public int stepSkip = 1;
    public WorldMap map;

    public JButton restartButton;
    public JButton pauseButton;

    public String selectedGenome;

    public BoardPanel boardPanel;
    public SidePanel sidePanel;

    public int width, height, startEnergy, moveEnergy, plantEnergy;
    public double jungleRatio;
    public int stepPause;

    private AtomicBoolean paused;
    private AtomicBoolean restart;
    private Thread threadObject;

    private Runnable runnable;

    Main() {
        this.readParameters();
        this.stepPause = 200;

        JFrame frame = new JFrame();
        frame.setLayout(new FlowLayout(FlowLayout.LEFT, 0,0 ));

        this.map = new WorldMap(width, height, 10, 100, 1, jungleRatio, startEnergy, moveEnergy, plantEnergy);

        this.boardPanel = new BoardPanel(width, height, gap, boardWidth, boardHeight, map);
        this.sidePanel = new SidePanel(sidebarWidth, boardHeight);

        frame.add(boardPanel);
        frame.add(sidePanel);
        frame.setSize(boardWidth + sidebarWidth, boardHeight);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private String getParametersFile() {
        StringBuilder contentBuilder = new StringBuilder();
        try (Stream<String> stream = Files.lines(Paths.get("/home/sans/IdeaProjects/selfish-gene/src/main/resources/parameters.json"), StandardCharsets.UTF_8)) {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        } catch (IOException e) {
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
        if(command.equals("RESTART")) {
        } else if(command.equals("PAUSE")) {
            this.pauseButton.setText("RESUME");
            this.paused.set(true);
        } else if(command.equals("RESUME")) {
            this.pauseButton.setText("PAUSE");
            this.paused.set(false);
            synchronized(threadObject) {
                threadObject.notify();
            }
        }
    }
}
