import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SidePanel extends JPanel implements ActionListener {
    public JButton pauseButton;

    public Map<String, JLabel> labels;
    public MapStats mapStats;

    public JLabel selectedAnimalLabel;

    SidePanel(int sidebarWidth, int boardHeight, MapStats mapStats) {
        this.setSize(sidebarWidth, boardHeight);
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        this.pauseButton = new JButton("PAUSE");
        pauseButton.addActionListener(this);
        this.add(pauseButton);

        this.mapStats = mapStats;

        this.labels = new HashMap<>();

        List<String> labelNames = Arrays.asList("animalsNumber", "plantsNumber", "dominatingGene", "averageEnergy", "averageLifespan", "averageChildCount");

        for(String name: labelNames) {
            JLabel label = new JLabel(name + ": ");
            this.labels.put(name, label);
            this.add(label);
        }

        this.selectedAnimalLabel = new JLabel("nothing selected");
        this.add(selectedAnimalLabel);
    }

    public void selectAnimal(int x, int y) {
        this.selectedAnimalLabel.setText(this.mapStats.getGenome(x, y).toString());
    }

    public void update() {
        for(Map.Entry<String, JLabel> entry: labels.entrySet()) {
            entry.getValue().setText(entry.getKey() + ": "  + mapStats.getStat(entry.getKey()));
        }
    }

    public void addActionListener(ActionListener acl) {
        pauseButton.addActionListener(acl);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        String command = actionEvent.getActionCommand();
        if(command.equals("PAUSE")) {
            this.pauseButton.setText("RESUME");
        } else if(command.equals("RESUME")) {
            this.pauseButton.setText("PAUSE");
        }

    }
}
