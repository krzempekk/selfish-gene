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
    public JButton pauseButton;
    public JButton saveButton;

    List<MapStats> mapStatsList;

    JLabel epochLabel;

    JTable statTable;
    JTable trackedAnimalTable;
    JTable genomeTable;

    JSlider animationSpeed;

    SidePanel(int sidebarWidth, int boardHeight, List<MapStats> mapStatsList) {
        this.mapStatsList = mapStatsList;

        this.setPreferredSize(new Dimension(sidebarWidth, boardHeight));
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

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

        this.statTable = new JTable(statTableModel);

        JScrollPane statTablePane = new JScrollPane(this.statTable);
        statTablePane.setPreferredSize(new Dimension(sidebarWidth, 110));

        TableModel trackedAnimalsTableModel = new AbstractTableModel() {
            @Override
            public String getColumnName(int col) {
                if(col == 0) return "Stat name";
                return "Map " + col;
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
                return "-";
            }
        };

        this.trackedAnimalTable = new JTable(trackedAnimalsTableModel);

        JScrollPane trackedAnimalTablePane = new JScrollPane(this.trackedAnimalTable);
        trackedAnimalTablePane.setPreferredSize(new Dimension(sidebarWidth, 80));

        TableModel genomeTableModel = new AbstractTableModel() {
            @Override
            public String getColumnName(int col) {
                if(col == 0) return "Map";
                if(col == 1) return "Type";
                return String.valueOf(col - 1);
            }

            @Override
            public int getRowCount() {
                return mapStatsList.size() * 2;
            }

            @Override
            public int getColumnCount() {
                return 10;
            }

            @Override
            public Object getValueAt(int row, int col) {
                int mapIndex = row / 2;
                if(col == 0) { return String.valueOf(mapIndex + 1); }
                if(col == 1) { return (row % 2 == 0) ? "Dominating" : "Selected"; };
                Genome g = null;
                if(row % 2 == 0) {
                    g = mapStatsList.get(mapIndex).getDominatingGenome();
                } else if(mapStatsList.get(mapIndex).getTrackedAnimal() != null) {
                    g = mapStatsList.get(mapIndex).getTrackedAnimal().getGenome();
                }
                if(g != null) {
                    return g.getGeneCount()[col - 2];
                }
                return null;
            }
        };

        this.genomeTable = new JTable(genomeTableModel);

        JScrollPane genomeTablePane = new JScrollPane(this.genomeTable);
        genomeTablePane.setPreferredSize(new Dimension(sidebarWidth, 80));

        this.epochLabel = new JLabel("Epoch number " + mapStatsList.get(0).getEpoch());

        this.animationSpeed = new JSlider(JSlider.HORIZONTAL, 10, 500, 200);

        this.animationSpeed.setMajorTickSpacing(100);
        this.animationSpeed.setMinorTickSpacing(10);
        this.animationSpeed.setPaintTicks(true);
        this.animationSpeed.setInverted(true);

        this.pauseButton = new JButton("PAUSE");
        this.pauseButton.addActionListener(this);

        this.saveButton = new JButton("SAVE");
        this.saveButton.addActionListener(this);

        this.add(this.epochLabel);
        this.add(new JLabel("Basic stats"));
        this.add(statTablePane);
        this.add(new JLabel("Tracked animals stats"));
        this.add(trackedAnimalTablePane);
        this.add(new JLabel("Genome stats"));
        this.add(genomeTablePane);
        this.add(new JLabel("Simulation speed"));
        this.add(this.animationSpeed);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(this.pauseButton);
        buttonPanel.add(this.saveButton);
        this.add(buttonPanel);
    }

    public void update() {
        this.epochLabel.setText("Epoch number " + this.mapStatsList.get(0).getEpoch());
        this.statTable.repaint();
        this.trackedAnimalTable.repaint();
        this.genomeTable.repaint();
    }

    public void addActionListener(EventListener acl) {
        this.pauseButton.addActionListener((ActionListener) acl);
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
            case "ANIMALS WITH DOMINATING GENOME":

                break;
        }
    }
}
