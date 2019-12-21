import org.json.JSONObject;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class SidePanel extends JPanel implements ActionListener {
    private JButton pauseButton;
    private JButton saveButton;
    private JButton genomeButton;

    private List<MapStats> mapStatsList;

    private JLabel epochLabel;

    private JTable statTable;
    private JTable trackedAnimalTable;
    private JTable genomeTable;

    private JSlider animationSpeed;

    SidePanel(int sidebarWidth, int boardHeight, List<MapStats> mapStatsList, int initialStepPause) {
        this.mapStatsList = mapStatsList;

        this.setPreferredSize(new Dimension(sidebarWidth, boardHeight));

        this.setLayout(new GridBagLayout());
        GridBagConstraints cons = new GridBagConstraints();
        cons.fill = GridBagConstraints.HORIZONTAL;
        cons.weightx = 1;
        cons.gridx = 0;

        TableModel statTableModel = new AbstractTableModel() {
            @Override
            public String getColumnName(int col) {
                if(col == 0) return "Stat name";
                return  "Map " + col;
            }

            @Override
            public int getRowCount() {
                return MapStatsType.values().length;
            }

            @Override
            public int getColumnCount() {
                return mapStatsList.size() + 1;
            }

            @Override
            public Object getValueAt(int row, int col) {
                MapStatsType stat = MapStatsType.values()[row];
                if(col == 0) return stat.toString();
                return mapStatsList.get(col - 1).getStat(stat);
            }
        };
        this.statTable = new JTable(statTableModel);
        JScrollPane statTablePane = new JScrollPane(this.statTable);
        statTablePane.setPreferredSize(new Dimension(sidebarWidth, 110));
        this.statTable.getColumnModel().getColumn(0).setMinWidth(120);


        TableModel trackedAnimalsTableModel = new AbstractTableModel() {
            @Override
            public String getColumnName(int col) {
                if(col == 0) return "Stat name";
                return "Map " + col;
            }

            @Override
            public int getRowCount() {
                return TrackedAnimalStatsType.values().length;
            }

            @Override
            public int getColumnCount() {
                return mapStatsList.size() + 1;
            }

            @Override
            public Object getValueAt(int row, int col) {
                TrackedAnimalStatsType stat = TrackedAnimalStatsType.values()[row];
                if(col == 0) return stat.toString();
                MapStats mapStats = mapStatsList.get(col - 1);
                if(mapStats.isTracking()) return mapStats.getTrackedAnimalStat(stat);
                return "-";
            }
        };
        this.trackedAnimalTable = new JTable(trackedAnimalsTableModel);
        JScrollPane trackedAnimalTablePane = new JScrollPane(this.trackedAnimalTable);
        trackedAnimalTablePane.setPreferredSize(new Dimension(sidebarWidth, 80));
        this.trackedAnimalTable.getColumnModel().getColumn(0).setMinWidth(120);


        TableModel genomeTableModel = new AbstractTableModel() {
            @Override
            public String getColumnName(int col) {
                if(col == 0) return "Map";
                if(col == 1) return "Type";
                if(col == 2) return "Count";
                return String.valueOf(col - 3);
            }

            @Override
            public int getRowCount() {
                return mapStatsList.size() * 2;
            }

            @Override
            public int getColumnCount() {
                return 11;
            }

            @Override
            public Object getValueAt(int row, int col) {
                int mapIndex = row / 2;
                if(col == 0) { return String.valueOf(mapIndex + 1); }
                Genome currentGenome = null;
                if(row % 2 == 0) {
                    if(col == 1) return "Dominating";
                    Map.Entry<Genome, Integer> genomeEntry = mapStatsList.get(mapIndex).getDominatingGenome();
                    if(genomeEntry != null) {
                        if(col == 2) return genomeEntry.getValue();
                        currentGenome = genomeEntry.getKey();
                    }
                } else {
                    if(col == 1) return "Selected";
                    if(col == 2) return "-";
                    if(mapStatsList.get(mapIndex).getTrackedAnimal() != null) {
                        currentGenome = mapStatsList.get(mapIndex).getTrackedAnimal().getGenome();
                    }
                }
                if(currentGenome != null) {
                    return currentGenome.getGeneCount()[col - 3];
                }
                return null;
            }
        };
        this.genomeTable = new JTable(genomeTableModel);
        JScrollPane genomeTablePane = new JScrollPane(this.genomeTable);
        genomeTablePane.setPreferredSize(new Dimension(sidebarWidth, 80));
        this.genomeTable.getColumnModel().getColumn(1).setMinWidth(80);
        this.genomeTable.getColumnModel().getColumn(2).setMinWidth(40);
        for(int i = 3; i < 11; i++) this.genomeTable.getColumnModel().getColumn(i).setPreferredWidth(15);


        this.epochLabel = new JLabel("Epoch number " + mapStatsList.get(0).getEpoch(), JLabel.CENTER);

        this.animationSpeed = new JSlider(JSlider.HORIZONTAL, 10, 500, initialStepPause);

        this.animationSpeed.setMajorTickSpacing(100);
        this.animationSpeed.setMinorTickSpacing(10);
        this.animationSpeed.setPaintTicks(true);
        this.animationSpeed.setInverted(true);

        this.pauseButton = new JButton("PAUSE");
        this.pauseButton.addActionListener(this);

        this.saveButton = new JButton("SAVE");
        this.saveButton.addActionListener(this);

        this.genomeButton = new JButton("SHOW DOM. GENOME");
        this.genomeButton.addActionListener(this);

        this.add(this.epochLabel, cons);
        this.add(new JLabel("Basic stats"), cons);
        this.add(statTablePane, cons);
        this.add(new JLabel("Tracked animals stats"), cons);
        this.add(trackedAnimalTablePane, cons);
        this.add(new JLabel("Genome stats"), cons);
        this.add(genomeTablePane, cons);
        this.add(new JLabel("Simulation speed"), cons);
        this.add(this.animationSpeed, cons);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(this.pauseButton);
        buttonPanel.add(this.saveButton);
        buttonPanel.add(this.genomeButton);
        this.add(buttonPanel, cons);
    }

    public void update() {
        this.epochLabel.setText("Epoch number " + this.mapStatsList.get(0).getEpoch());
        this.statTable.repaint();
        this.trackedAnimalTable.repaint();
        this.genomeTable.repaint();
    }

    public void addActionListener(EventListener acl) {
        this.pauseButton.addActionListener((ActionListener) acl);
        this.genomeButton.addActionListener((ActionListener) acl);
        this.animationSpeed.addChangeListener((ChangeListener) acl);
    }

    public void saveStatsToFile() {
        JSONObject statObject = new JSONObject();
        List<JSONObject> statList = new ArrayList<>();
        for(MapStats mapStats: this.mapStatsList) {
            JSONObject stats = new JSONObject();
            for(MapStatsType mapStat: MapStatsType.values()) {
                stats.put(String.valueOf(mapStat), mapStats.getAvgStat(mapStat));
            }
            stats.put("Dominating genome", mapStats.getGloballyDominatingGenome());
            statList.add(stats);
        }
        statObject.put("maps", statList);
        try (FileWriter file = new FileWriter("stats.json")) {
            file.write(statObject.toString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        String command = actionEvent.getActionCommand();
        switch (command) {
            case "PAUSE":
                this.pauseButton.setText("RESUME");
                break;
            case "RESUME":
                this.pauseButton.setText("PAUSE");
                break;
            case "SAVE":
                this.saveStatsToFile();
                break;
            case "SHOW DOM. GENOME":
                this.genomeButton.setText("HIDE DOM. GENOME");
                break;
            case "HIDE DOM. GENOME":
                this.genomeButton.setText("SHOW DOM. GENOME");
                break;
        }
    }
}
