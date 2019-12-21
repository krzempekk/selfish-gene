import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

public class BoardPanel extends JPanel {
    private Map<Vector2D,JLabel> labels = new HashMap<>();
    private int width, height, gap, boardWidth, boardHeight;
    private WorldMap map;
    private MapStats mapStats;
    private Map<String, ImageIcon> sprites = new HashMap<>();

    BoardPanel(int width, int height, int gap, int boardWidth, int boardHeight, WorldMap map, MapStats mapStats, SidePanel sidePanel) {
        this.width = width; this.height = height; this.gap = gap;
        this.boardWidth = boardWidth; this.boardHeight = boardHeight;
        this.map = map;
        this.setLayout(new GridLayout(height, width, gap, gap));
        this.generateSprites();
        this.mapStats = mapStats;

        for(int y = height - 1; y >= 0; y--) {
            for(int x = 0; x <= width - 1; x++) {
                JLabel l = new JLabel("", JLabel.CENTER);
                l.setOpaque(true);
                l.setForeground(Color.WHITE);
                l.setBackground(Color.DARK_GRAY);

                int _x = x, _y = y;

                l.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent mouseEvent) {
                    super.mouseClicked(mouseEvent);
                    Vector2D pos = new Vector2D(_x, _y);
                    mapStats.trackAnimal(pos);
                    BoardPanel.this.renderMap();
                    sidePanel.update();
                    }
                });

                this.add(l);
                this.labels.put(new Vector2D(x, y), l);
            }
        }

        this.setPreferredSize(new Dimension(boardWidth, boardHeight));
    }

    private Icon getCompoundIcon(Icon background, Icon foreground) {
        return new CompoundIcon(CompoundIcon.Axis.Z_AXIS, 0, CompoundIcon.CENTER, CompoundIcon.CENTER, background, foreground);
    }

    private Icon getPositionIcon(Vector2D position) {
        PlaceType mapSegmentType = this.map.getMapSegmentType(position);

        ImageIcon background = this.sprites.get(mapSegmentType == PlaceType.NORMAL ? "platoCave" : "ideaWorld");

        Food foodOnPosition = this.map.getFoodFrom(position);
        List<Animal> animalsOnPosition = this.map.getSortedAnimalsFrom(position);
        List<Vector2D> animalsWithDominatingGenomePositions = this.mapStats.getAnimalsWithDominatingGenomePositions();

        Icon icon;

        Vector2D trackedPosition = this.mapStats.getTrackedAnimalPosition();

        if(position.equals(trackedPosition) && this.mapStats.getTrackedAnimalStat(TrackedAnimalStatsType.deathEpoch) > 0) {
            icon = this.getCompoundIcon(background, this.sprites.get("creatureDied"));
        } else if (foodOnPosition != null) {
            icon = this.getCompoundIcon(background, this.sprites.get(mapSegmentType == PlaceType.JUNGLE ? "betterFood" : "food"));
        } else if (animalsOnPosition.size() != 0) {
            int creatureCount = Math.min(animalsOnPosition.size(), 4);
            int energyLevel = (4 * Math.min(animalsOnPosition.get(0).getEnergy(), this.map.getStartEnergy() - 1)) / this.map.getStartEnergy();
            icon = this.getCompoundIcon(background, this.sprites.get("creature" + creatureCount + "-" + energyLevel));
            if(position.equals(trackedPosition)) {
                icon = this.getCompoundIcon(icon, this.sprites.get("frame"));
            }
            if(animalsWithDominatingGenomePositions.contains(position)) {
                icon = this.getCompoundIcon(icon, this.sprites.get("frame2"));
            }
        } else {
            icon = background;
        }

        return icon;
    }

    public void renderMap() {
        for(int y = this.height - 1; y >= 0; y--) {
            for(int x = 0; x <= this.width - 1; x++) {
                Vector2D position = new Vector2D(x, y);
                JLabel l = this.labels.get(position);
                l.setText("");
                l.setIcon(this.getPositionIcon(position));
            }
        }
    }

    private ImageIcon getIconFromFile(String filename, int width, int height) {
        ImageIcon icon = new ImageIcon(this.getClass().getResource(filename + ".png"));
        Image image = icon.getImage();
        Image newimg = image.getScaledInstance(width, height,  java.awt.Image.SCALE_SMOOTH);
        return new ImageIcon(newimg);
    }

    private void generateSprites() {
        int iconWidth = (this.boardWidth - (this.width - 1) * this.gap) / this.width;
        int iconHeight = (this.boardHeight - (this.height - 1) * this.gap) / this.height;

        List<String> spriteNames = Arrays.asList("platoCave", "ideaWorld", "frame", "frame2", "creatureDied");

        for(String spriteName: spriteNames) {
            ImageIcon icon = this.getIconFromFile(spriteName, iconWidth, iconHeight);
            this.sprites.put(spriteName, icon);
        }

        for(int i = 1; i <= 4; i++) {
            for(int j = 1; j <= 4; j++) {
                ImageIcon icon = this.getIconFromFile("creature" + i, iconWidth * (j + 4) / 8, iconHeight * (j + 4) / 8);
                this.sprites.put("creature" + i + "-" + (j - 1), icon);
            }
        }

        ImageIcon icon = this.getIconFromFile("betterFood", iconWidth/2, iconHeight/2);
        this.sprites.put("betterFood", icon);

        icon = this.getIconFromFile("food", iconWidth/2, iconHeight/2);
        this.sprites.put("food", icon);
    }

}
