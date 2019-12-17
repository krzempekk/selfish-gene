import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class BoardPanel extends JPanel {
    public Map<Vector2D,JLabel> labels = new HashMap<>();
    public int width, height, gap, boardWidth, boardHeight;
    public WorldMap map;
    public Map<String, ImageIcon> sprites = new HashMap<>();

    BoardPanel(int width, int height, int gap, int boardWidth, int boardHeight, WorldMap map) {
        this.width = width; this.height = height; this.gap = gap;
        this.boardWidth = boardWidth; this.boardHeight = boardHeight;
        this.map = map;
        this.setLayout(new GridLayout(width, height, gap, gap));
        this.generateSprites();

        for(int y = height - 1; y >= 0; y--) {
            for(int x = 0; x <= width - 1; x++) {
                JLabel l = new JLabel("", JLabel.CENTER);
                l.setOpaque(true);
                l.setForeground(Color.WHITE);
                l.setBackground(Color.DARK_GRAY);

//                int _x = x, _y = y;
//
//                l.addMouseListener(new MouseAdapter() {
//                    @Override
//                    public void mouseClicked(MouseEvent mouseEvent) {
//                        super.mouseClicked(mouseEvent);
//                        if(pausePending) {
//                            selectAnimal(_x, _y);
//                        }
//                    }
//                });

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
                Collection objectsAtPosition = map.objectAt(position);
                JLabel l = labels.get(position);
                l.setText("");
                ImageIcon background = map.getMapSegmentType(position) == PlaceType.NORMAL ? sprites.get("background1") : sprites.get("background2");
                if (objectsAtPosition.size() == 0) {
                    l.setIcon(background);
                } else if (objectsAtPosition.toArray()[0] instanceof Food) {
                    CompoundIcon icon;
                    icon = new CompoundIcon(CompoundIcon.Axis.Z_AXIS, 0, CompoundIcon.CENTER, CompoundIcon.CENTER, background, sprites.get("coffee"));
                    if(map.getMapSegmentType(position) == PlaceType.JUNGLE) {
                        icon = new CompoundIcon(CompoundIcon.Axis.Z_AXIS, 0, CompoundIcon.CENTER, CompoundIcon.CENTER, background, sprites.get("beer"));
                    }
                    l.setIcon(icon);
                } else if (objectsAtPosition.size() == 1) {
                    CompoundIcon icon = new CompoundIcon(CompoundIcon.Axis.Z_AXIS, 0, CompoundIcon.CENTER, CompoundIcon.CENTER, background, sprites.get("creature"));
                    l.setIcon(icon);
                } else if (objectsAtPosition.size() == 2) {
                    CompoundIcon icon = new CompoundIcon(CompoundIcon.Axis.Z_AXIS, 0, CompoundIcon.CENTER, CompoundIcon.CENTER, background, sprites.get("creature2"));
                    l.setIcon(icon);
                } else if (objectsAtPosition.size() == 3) {
                    CompoundIcon icon = new CompoundIcon(CompoundIcon.Axis.Z_AXIS, 0, CompoundIcon.CENTER, CompoundIcon.CENTER, background, sprites.get("creature3"));
                    l.setIcon(icon);
                } else if (objectsAtPosition.size() == 4) {
                    CompoundIcon icon = new CompoundIcon(CompoundIcon.Axis.Z_AXIS, 0, CompoundIcon.CENTER, CompoundIcon.CENTER, background, sprites.get("creature4"));
                    l.setIcon(icon);
                }
            }
        }
    }

    public void generateSprites() {
        int iconWidth = (boardWidth - (width - 1) * gap) / width;
        int iconHeight = (boardHeight - (height - 1) * gap) / height;

        ImageIcon creature = new ImageIcon(getClass().getResource("apohllo.png"));
        Image image = creature.getImage();
        Image newimg = image.getScaledInstance(iconWidth, iconHeight,  java.awt.Image.SCALE_SMOOTH);
        creature = new ImageIcon(newimg);
        sprites.put("creature", creature);

        ImageIcon creature2 = new ImageIcon(getClass().getResource("apohllo2.png"));
        image = creature2.getImage();
        newimg = image.getScaledInstance(iconWidth, iconHeight,  java.awt.Image.SCALE_SMOOTH);
        creature2 = new ImageIcon(newimg);
        sprites.put("creature2", creature2);

        ImageIcon creature3 = new ImageIcon(getClass().getResource("apohllo3.png"));
        image = creature3.getImage();
        newimg = image.getScaledInstance(iconWidth, iconHeight,  java.awt.Image.SCALE_SMOOTH);
        creature3 = new ImageIcon(newimg);
        sprites.put("creature3", creature3);

        ImageIcon creature4 = new ImageIcon(getClass().getResource("apohllo4.png"));
        image = creature4.getImage();
        newimg = image.getScaledInstance(iconWidth, iconHeight,  java.awt.Image.SCALE_SMOOTH);
        creature4 = new ImageIcon(newimg);
        sprites.put("creature4", creature4);

        ImageIcon beer = new ImageIcon(getClass().getResource("ruby.png"));
        image = beer.getImage();
        newimg = image.getScaledInstance(iconHeight/2, iconHeight/2,  java.awt.Image.SCALE_SMOOTH);
        beer = new ImageIcon(newimg);
        sprites.put("beer", beer);

        ImageIcon coffee = new ImageIcon(getClass().getResource("java.png"));
        image = coffee.getImage();
        newimg = image.getScaledInstance(iconHeight/2, iconHeight/2,  java.awt.Image.SCALE_SMOOTH);
        coffee = new ImageIcon(newimg);
        sprites.put("coffee", coffee);

        ImageIcon background1 = new ImageIcon(getClass().getResource("cave.png"));
        image = background1.getImage();
        newimg = image.getScaledInstance(iconHeight, iconHeight,  java.awt.Image.SCALE_SMOOTH);
        background1 = new ImageIcon(newimg);
        sprites.put("background1", background1);

        ImageIcon background2 = new ImageIcon(getClass().getResource("idea.jpg"));
        image = background2.getImage();
        newimg = image.getScaledInstance(iconHeight, iconHeight,  java.awt.Image.SCALE_SMOOTH);
        background2 = new ImageIcon(newimg);
        sprites.put("background2", background2);
    }
}
