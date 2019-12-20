import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

public class BoardPanel extends JPanel {
    public Map<Vector2D,JLabel> labels = new HashMap<>();
    public int width, height, gap, boardWidth, boardHeight;
    public WorldMap map;
    public MapStats mapStats;
    public Map<String, ImageIcon> sprites = new HashMap<>();

    BoardPanel(int width, int height, int gap, int boardWidth, int boardHeight, WorldMap map, MapStats mapStats) {
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
                    renderMap();
                    }
                });

                this.add(l);
                labels.put(new Vector2D(x, y), l);
            }
        }

        this.setSize(boardWidth, boardHeight);
    }

    public Icon getPositionIcon(Vector2D position) {
        PlaceType mapSegmentType = map.getMapSegmentType(position);

        ImageIcon background = sprites.get(mapSegmentType == PlaceType.NORMAL ? "platoCave" : "ideaWorld");

        Food foodOnPosition = map.getFoodFrom(position);
        List<Animal> animalsOnPosition = map.getSortedAnimalsFrom(position);

        Icon icon;

        if(position.equals(this.mapStats.getTrackedPosition()) && this.mapStats.getTrackedDeathEpoch() > 0) {
            icon = new CompoundIcon(CompoundIcon.Axis.Z_AXIS, 0, CompoundIcon.CENTER, CompoundIcon.CENTER, background, sprites.get("creatureDied"));
        } else if (foodOnPosition != null) {
            icon = new CompoundIcon(CompoundIcon.Axis.Z_AXIS, 0, CompoundIcon.CENTER, CompoundIcon.CENTER, background, sprites.get(mapSegmentType == PlaceType.JUNGLE ? "betterFood" : "food"));
        } else if (animalsOnPosition.size() != 0) {
            int creatureCount = Math.min(animalsOnPosition.size(), 4);
            int energyLevel = (4 * Math.min(animalsOnPosition.get(0).getEnergy(), map.startEnergy - 1)) / map.startEnergy;
            icon = new CompoundIcon(CompoundIcon.Axis.Z_AXIS, 0, CompoundIcon.CENTER, CompoundIcon.CENTER, background, sprites.get("creature" + creatureCount + "-" + energyLevel));
            if(position.equals(this.mapStats.getTrackedPosition())) {
                icon = new CompoundIcon(CompoundIcon.Axis.Z_AXIS, 0, CompoundIcon.CENTER, CompoundIcon.CENTER, icon, sprites.get("frame"));
            }
        } else {
            icon = background;
        }

        return icon;
    }

    public void renderMap() {
        for(int y = height - 1; y >= 0; y--) {
            for(int x = 0; x <= width - 1; x++) {
                Vector2D position = new Vector2D(x, y);
                JLabel l = labels.get(position);
                l.setText("");
                l.setIcon(getPositionIcon(position));
            }
        }
    }

    public void generateSprites() {
        int iconWidth = (boardWidth - (width - 1) * gap) / width;
        int iconHeight = (boardHeight - (height - 1) * gap) / height;

        List<String> spriteNames = Arrays.asList("platoCave", "ideaWorld", "frame", "creatureDied");

        for(String spriteName: spriteNames) {
            ImageIcon icon = new ImageIcon(getClass().getResource(spriteName + ".png"));
            Image image = icon.getImage();
            Image newimg = image.getScaledInstance(iconWidth, iconHeight,  java.awt.Image.SCALE_SMOOTH);
            icon = new ImageIcon(newimg);
            sprites.put(spriteName, icon);
        }

        for(int i = 1; i <= 4; i++) {
            for(int j = 1; j <= 4; j++) {
                ImageIcon icon = new ImageIcon(getClass().getResource("creature" + i + ".png"));
                Image image = icon.getImage();
                Image newimg = image.getScaledInstance(iconWidth * (j + 4) / 8, iconHeight * (j + 4) / 8,  java.awt.Image.SCALE_SMOOTH);
                icon = new ImageIcon(newimg);
                sprites.put("creature" + i + "-" + (j - 1), icon);
            }
        }

        ImageIcon icon = new ImageIcon(getClass().getResource("betterFood.png"));
        Image image = icon.getImage();
        Image newimg = image.getScaledInstance(iconHeight/2, iconHeight/2, java.awt.Image.SCALE_SMOOTH);
        icon = new ImageIcon(newimg);
        sprites.put("betterFood", icon);

        icon = new ImageIcon(getClass().getResource("food.png"));
        image = icon.getImage();
        newimg = image.getScaledInstance(iconHeight/2, iconHeight/2, java.awt.Image.SCALE_SMOOTH);
        icon = new ImageIcon(newimg);
        sprites.put("food", icon);
    }

}
