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
    public Map<String, ImageIcon> sprites = new HashMap<>();

    BoardPanel(int index, int width, int height, int gap, int boardWidth, int boardHeight, WorldMap map, SidePanel sidePanel) {
        this.width = width; this.height = height; this.gap = gap;
        this.boardWidth = boardWidth; this.boardHeight = boardHeight;
        this.map = map;
        this.setLayout(new GridLayout(height, width, gap, gap));
        this.generateSprites();


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
                    sidePanel.animalSelected(index, new Vector2D(_x, _y));
                    }
                });

                this.add(l);
                labels.put(new Vector2D(x, y), l);
            }
        }

        this.setSize(boardWidth, boardHeight);
    }

    public void renderMap() {
        for(int y = height - 1; y >= 0; y--) {
            for(int x = 0; x <= width - 1; x++) {
                Vector2D position = new Vector2D(x, y);
                PlaceType mapSegmentType = map.getMapSegmentType(position);
                Collection<IMapElement> objectsAtPosition = map.objectAt(position);

                JLabel l = labels.get(position);
                l.setText("");

                ImageIcon background = sprites.get(mapSegmentType == PlaceType.NORMAL ? "platoCave" : "ideaWorld");
                Icon icon;
                if (objectsAtPosition.size() == 0) {
                    icon = background;
                } else if (objectsAtPosition.toArray()[0] instanceof Food) {
                    icon = new CompoundIcon(CompoundIcon.Axis.Z_AXIS, 0, CompoundIcon.CENTER, CompoundIcon.CENTER, background, sprites.get(mapSegmentType == PlaceType.JUNGLE ? "betterFood" : "food"));
                } else {
                    int creatureCount = Math.min(objectsAtPosition.size(), 4);
                    icon = new CompoundIcon(CompoundIcon.Axis.Z_AXIS, 0, CompoundIcon.CENTER, CompoundIcon.CENTER, background, sprites.get("creature" + creatureCount));
                }
                l.setIcon(icon);
            }
        }
    }

    public void generateSprites() {
        int iconWidth = (boardWidth - (width - 1) * gap) / width;
        int iconHeight = (boardHeight - (height - 1) * gap) / height;

        List<String> spriteNames = Arrays.asList("creature1", "creature2", "creature3", "creature4", "platoCave", "ideaWorld");

        for(String spriteName: spriteNames) {
            ImageIcon icon = new ImageIcon(getClass().getResource(spriteName + ".png"));
            Image image = icon.getImage();
            Image newimg = image.getScaledInstance(iconWidth, iconHeight,  java.awt.Image.SCALE_SMOOTH);
            icon = new ImageIcon(newimg);
            sprites.put(spriteName, icon);
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
