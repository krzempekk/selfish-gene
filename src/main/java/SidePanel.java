import org.json.JSONObject;

import javax.swing.*;
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
    public JButton pauseButton;
    public JButton saveButton;

    List<MapStats> mapStatsList;

    JLabel epochLabel;

    JTable statTable;
    JTable trackedAnimalTable;
    JScrollPane statTablePane;
    JScrollPane trackedAnimalTablePane;

    List<JLabel> mapDominatingGenomes;
    List<JLabel> trackedAnimalGenomes;

    SidePanel(int sidebarWidth, int boardHeight, List<MapStats> mapStatsList) {
        this.mapStatsList = mapStatsList;

        this.setSize(sidebarWidth, boardHeight);
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

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
                if(col == 0) {
                    return stat.toString();
                }
                return mapStatsList.get(col - 1).getStat(stat);
            }
        };

        statTable = new JTable(statTableModel);

        statTablePane = new JScrollPane(statTable);
        statTablePane.setPreferredSize(new Dimension(500, 110));

        TableModel trackedAnimalsTableModel = new AbstractTableModel() {
            @Override
            public String getColumnName(int col) {
                if(col == 0) return "Stat name";
                return mapStatsList.get(col - 1).isTracking() ? "Map " + col : "No selected animal";
            }

            @Override
            public int getRowCount() {
                return 3;
            }

            @Override
            public int getColumnCount() {
                return mapStatsList.size() + 1;
            }

            @Override
            public Object getValueAt(int row, int col) {
                if(col == 0) {
                    switch (row) {
                        case 0:
                            return "Children count";
                        case 1:
                            return "Successors count";
                        case 2:
                            return "Epoch died";
                    }
                }
                MapStats mapStats = mapStatsList.get(col - 1);
                if(mapStats.isTracking()) {
                    switch (row) {
                        case 0:
                            return mapStats.getTrackedChildCount();
                        case 1:
                            return mapStats.getTrackedSuccessorsCount();
                        case 2:
                            return mapStats.getTrackedDeathEpoch();
                    }
                }
                return null;
            }
        };

        trackedAnimalTable = new JTable(trackedAnimalsTableModel);

        trackedAnimalTablePane = new JScrollPane(trackedAnimalTable);
        trackedAnimalTablePane.setPreferredSize(new Dimension(500, 80));

        this.epochLabel = new JLabel("Epoch number " + mapStatsList.get(0).getEpoch());


        this.mapDominatingGenomes = new ArrayList<>();
        this.trackedAnimalGenomes = new ArrayList<>();

        for(int i = 0; i < mapStatsList.size(); i++) {
            JLabel l = new JLabel("");
            mapDominatingGenomes.add(l);
            this.add(l);
            l = new JLabel("");
            trackedAnimalGenomes.add(l);
            this.add(l);
        }

        this.add(epochLabel);
        this.add(statTablePane);
        this.add(trackedAnimalTablePane);

        this.pauseButton = new JButton("PAUSE");
        pauseButton.addActionListener(this);
        this.add(pauseButton);

        this.saveButton = new JButton("SAVE");
        saveButton.addActionListener(this);
        this.add(saveButton);
    }

    public void update() {
        this.epochLabel.setText("Epoch number " + mapStatsList.get(0).getEpoch());
        statTable.repaint();
        trackedAnimalTable.repaint();

        for(int i = 0; i < mapStatsList.size(); i++) {
            mapDominatingGenomes.get(i).setText(mapStatsList.get(i).getDominatingGenome());
            trackedAnimalGenomes.get(i).setText(mapStatsList.get(i).getTrackedGenome());
        }

    }

    public void addActionListener(ActionListener acl) {
        pauseButton.addActionListener(acl);
    }

    public void saveStatsToFile() {
        JSONObject statObject = new JSONObject();
        List<JSONObject> statList = new ArrayList<>();
        for(MapStats mapStats: mapStatsList) {
            JSONObject stats = new JSONObject();
            for(MapStatsType mapStat: MapStatsType.values()) {
                stats.put(String.valueOf(mapStat), mapStats.getAvgStat(mapStat));
            }
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
        if(command.equals("PAUSE")) {
            this.pauseButton.setText("RESUME");
        } else if(command.equals("RESUME")) {
            this.pauseButton.setText("PAUSE");
        } else if(command.equals("SAVE")) {
            this.saveStatsToFile();
        } else if(command.equals("ANIMALS WITH DOMINATING GENOME")) {

        }
    }
}
