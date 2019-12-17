import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SidePanel extends JPanel implements ActionListener {
    public JButton restartButton;
    public JButton pauseButton;

    public Map<String, JLabel> labels;

    SidePanel(int sidebarWidth, int boardHeight) {
        this.setSize(sidebarWidth, boardHeight);
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        this.restartButton = new JButton("RESTART");
        restartButton.addActionListener(this);
        this.add(restartButton);

        this.pauseButton = new JButton("PAUSE");
        pauseButton.addActionListener(this);
        this.add(pauseButton);

        List<String> labelNames = Arrays.asList("animalsNumber", "plantsNumber", "dominatingGene", "averageEnergy", "averageLifespan", "averageChildCount");

        for(String name: labelNames) {
            JLabel label = new JLabel(name + ": ");
            this.labels.put(name, label);
            this.add(label);
        }
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {

    }
}
