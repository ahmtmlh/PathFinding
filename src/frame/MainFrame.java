package frame;

import core.graph.Graph;
import core.path.*;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MainFrame {

    private enum BoxType{
        BOX_TYPE_NONE,
        BOX_TYPE_START,
        BOX_TYPE_END,
        BOX_TYPE_WALL,
        BOX_TYPE_PATH
    }

    private final JFrame frame;

    private static final int BUTTON_LEFT = 1;
    private static final int BUTTON_RIGHT= 2;

    private static final int BOX_SIZE = 18;
    private static final int BORDER_SIZE = 1;
    private static final int HEIGHT = 40;
    private static final int WIDTH = 40;
    private static final int CANVAS_OFFSET = 60;

    private final AtomicInteger clicked;
    private final BoxType[][] grid;

    private final Map<BoxType, Color> colorMap;
    private final Map<ButtonModel, BoxType> radioButtonMap;
    private final Map<String, PathFindingAlgorithm> comboBoxMap;

    private final ButtonGroup buttonGroup;
    private JComboBox<String> combo;
    private JTextArea resultField;

    private final Point startPoint;
    private final Point endPoint;

    private class PathCanvas extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            int xOff = 0;
            int yOff = 0;

            g.setColor(Color.lightGray);
            g.fillRect(0, 0, super.getWidth(), super.getHeight());

            for (int i = 0; i < MainFrame.HEIGHT; i++) {
                for (int j = 0; j < MainFrame.WIDTH; j++) {
                    g.setColor(colorMap.get(grid[i][j]));
                    g.fillRect(xOff, yOff, BOX_SIZE - BORDER_SIZE, BOX_SIZE - BORDER_SIZE);
                    xOff += BOX_SIZE;
                }
                xOff = 0;
                yOff += BOX_SIZE;
            }
        }
    }

    public MainFrame() {

        grid = new BoxType[HEIGHT][WIDTH];

        clicked = new AtomicInteger(0);
        colorMap = new HashMap<>();
        radioButtonMap = new HashMap<>();
        comboBoxMap = new LinkedHashMap<>();
        buttonGroup = new ButtonGroup();

        startPoint = new Point();
        endPoint = new Point();

        colorMap.put(BoxType.BOX_TYPE_NONE, Color.white);
        colorMap.put(BoxType.BOX_TYPE_START, Color.green);
        colorMap.put(BoxType.BOX_TYPE_END, Color.red);
        colorMap.put(BoxType.BOX_TYPE_WALL, Color.black);
        colorMap.put(BoxType.BOX_TYPE_PATH, Color.blue);

        comboBoxMap.put("DFS", new DFS());
        comboBoxMap.put("BFS", new BFS());
        comboBoxMap.put("Dijkstra", new Dijkstra());
        comboBoxMap.put("BellmanFord", new BellmanFord());

        int w = (BOX_SIZE * WIDTH) + 25;
        int h = (BOX_SIZE * HEIGHT) + CANVAS_OFFSET + 40;

        frame = new JFrame();
        frame.setTitle("Path Finding");
        frame.setSize(Integer.max(w, 480), h);
        frame.setFocusable(true);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        startPoint.setLocation(2 ,2);
        endPoint.setLocation(4 , 4);

        initGrid();
        initialize();

        frame.setVisible(true);
    }

    private void initGrid(){
        for (BoxType[] boxArr : grid){
            Arrays.fill(boxArr, BoxType.BOX_TYPE_NONE);
        }
        grid[startPoint.y][startPoint.x] = BoxType.BOX_TYPE_START;
        grid[endPoint.y][endPoint.x] = BoxType.BOX_TYPE_END;
    }

    private void initialize() {
        JPanel panel = new JPanel();

        // Add top shelf components
        String[] keys = comboBoxMap.keySet().toArray(new String[0]);
        combo = new JComboBox<>(keys);
        combo.setBounds(5, 5, 100, 25);
        panel.add(combo);

        JRadioButton temp = new JRadioButton("Start");
        temp.setBounds(110, 5, 60, 25);
        panel.add(temp);
        buttonGroup.add(temp);
        radioButtonMap.put(temp.getModel(), BoxType.BOX_TYPE_START);

        temp = new JRadioButton("End");
        temp.setBounds(170, 5, 60, 25);
        panel.add(temp);
        buttonGroup.add(temp);
        radioButtonMap.put(temp.getModel(), BoxType.BOX_TYPE_END);

        temp = new JRadioButton("Wall");
        temp.setBounds(230, 5, 60, 25);
        panel.add(temp);
        buttonGroup.add(temp);
        radioButtonMap.put(temp.getModel(), BoxType.BOX_TYPE_WALL);
        buttonGroup.setSelected(temp.getModel(), true);

        resultField = new JTextArea();
        resultField.setBounds(5, 30, frame.getWidth() - 100, 25);
        resultField.setEditable(false);
        resultField.setOpaque(false);

        resultField.setFont(new Font(resultField.getFont().getName(), Font.PLAIN, 20));
        panel.add(resultField);

        PathCanvas canvas = new PathCanvas();
        canvas.setBounds(5, CANVAS_OFFSET, WIDTH * BOX_SIZE, HEIGHT * BOX_SIZE);
        panel.add(canvas);

        // Add panel to frame
        frame.getContentPane().add(panel, BorderLayout.CENTER);
        panel.setLayout(null);

        // Add listeners
        panel.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (clicked.get() > 0) {
                    setBox(e, true);
                    canvas.repaint();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) { }
        });

        // add mouse listener to handle clicks...
        panel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) { }

            @Override
            public void mousePressed(MouseEvent e) {
                boolean flag = true;
                switch (e.getButton()) {
                    case MouseEvent.BUTTON1:
                        clicked.set(BUTTON_LEFT);
                        break;
                    case MouseEvent.BUTTON3:
                        clicked.set(BUTTON_RIGHT);
                        break;
                    default:
                        flag = false;
                        break;
                }

                if (flag){
                    setBox(e, false);
                    canvas.repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 || e.getButton() == MouseEvent.BUTTON2)
                    clicked.set(0);
            }

            @Override
            public void mouseEntered(MouseEvent e) { }

            @Override
            public void mouseExited(MouseEvent e) { }
        });

        // Add buttons
        JButton pathButton = new JButton("Find");
        pathButton.setBounds(290, 5, 80, 25);
        pathButton.addActionListener(e -> {
            findPath();
            canvas.repaint();
        });
        panel.add(pathButton);

        JButton clearButton = new JButton("Clear");
        clearButton.setBounds(400, 5, 80, 25);
        clearButton.addActionListener(e -> {
            initGrid();
            resultField.setText("");
            canvas.repaint();
        });
        panel.add(clearButton);

    }

    private void addType(int x, int y, BoxType type, boolean drag){

        if (drag){
            if (type == BoxType.BOX_TYPE_WALL)
                grid[y][x] = type;
        } else {
            if (type == BoxType.BOX_TYPE_START){
                grid[startPoint.y][startPoint.x] = BoxType.BOX_TYPE_NONE;
                startPoint.setLocation(x, y);
            } else if (type == BoxType.BOX_TYPE_END){
                grid[endPoint.y][endPoint.x] = BoxType.BOX_TYPE_NONE;
                endPoint.setLocation(x, y);
            }

            grid[y][x] = type;
        }
    }

    private void delType(int x, int y){
        if (grid[y][x] == BoxType.BOX_TYPE_WALL || grid[y][x] == BoxType.BOX_TYPE_PATH)
            grid[y][x] = BoxType.BOX_TYPE_NONE;
    }

    private void setBox(MouseEvent e, boolean drag){
        int x = e.getX();
        int y = e.getY() - CANVAS_OFFSET;
        boolean inBounds = (x >= 0 && x < WIDTH * BOX_SIZE) && (y >= 0 && y < HEIGHT * BOX_SIZE);

        if (inBounds) {
            x = x / BOX_SIZE;
            y = y / BOX_SIZE;

            if (clicked.get() == BUTTON_LEFT){
                if (grid[y][x] == BoxType.BOX_TYPE_NONE || grid[y][x] == BoxType.BOX_TYPE_PATH) {
                    BoxType type = radioButtonMap.get(buttonGroup.getSelection());
                    addType(x, y, type, drag);
                }
            } else {
                delType(x, y);
            }
        }
    }

    private void clearPrevPath(){
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                if (grid[i][j] == BoxType.BOX_TYPE_PATH)
                    grid[i][j] = BoxType.BOX_TYPE_NONE;
            }
        }
    }

    private void findPath() {
        clearPrevPath();
        // Copy current grid information as boolean grid
        boolean[][] tempGrid = new boolean[HEIGHT][WIDTH];

        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                if (grid[i][j] != BoxType.BOX_TYPE_WALL)
                    tempGrid[i][j] = true;
            }
        }

        PathFindingAlgorithm algo = comboBoxMap.get(String.valueOf(combo.getSelectedItem()));
        startAlgorithm(algo, tempGrid);
    }

    private void startAlgorithm(PathFindingAlgorithm algo, boolean[][] grid){
        int start = (startPoint.y * WIDTH) + startPoint.x;
        int end = (endPoint.y * WIDTH) + endPoint.x;

        Graph g = new Graph(grid);
        algo.solve(g, start);
        if (algo.checkPath(end)) {
            resultField.setText("Path has been found and marked");
            List<Integer> backtrace = algo.getBacktrace(start, end);
            markPath(backtrace);
        } else {
            resultField.setForeground(Color.red);
            resultField.setText("NO PATH FOUND");
            resultField.setForeground(Color.black);
        }
    }

    private void markPath(List<Integer> path){
        for (int point : path){
            int x = point % WIDTH;
            int y = point / WIDTH;

            grid[y][x] = BoxType.BOX_TYPE_PATH;
        }

    }
}