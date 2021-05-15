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

    private enum TaleType {
        TALE_TYPE_NONE,
        TALE_TYPE_START,
        TALE_TYPE_END,
        TALE_TYPE_WALL,
        TALE_TYPE_PATH
    }

    private final JFrame frame;

    private static final int BUTTON_LEFT = 1;
    private static final int BUTTON_RIGHT= 2;

    private static int TALE_SIZE = 25;
    private static final int BORDER_SIZE = 1;
    private static int HEIGHT = 20;
    private static int WIDTH = 20;

    private static final int CANVAS_OFFSET = 60;

    private final AtomicInteger clicked;
    private TaleType[][] grid;

    private final Map<TaleType, Color> colorMap;
    private final Map<ButtonModel, TaleType> radioButtonMap;
    private final Map<String, PathFindingAlgorithm> comboBoxMap;

    private final ButtonGroup buttonGroup;
    private JComboBox<String> combo;
    private JTextArea resultField;

    private final Point startPoint;
    private final Point endPoint;

    private int tempWidth;
    private int tempHeight;
    private int tempTaleSize;

    private final PathCanvasMouseHandler mouseHandler;

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
                    g.fillRect(xOff, yOff, TALE_SIZE - BORDER_SIZE, TALE_SIZE - BORDER_SIZE);
                    xOff += TALE_SIZE;
                }
                xOff = 0;
                yOff += TALE_SIZE;
            }
        }
    }

    private class PathCanvasMouseHandler implements MouseMotionListener, MouseListener {

        private PathCanvas canvas;

        public void setCanvas(PathCanvas canvas){
            this.canvas = null;
            this.canvas = canvas;
        }

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

            if (flag && setTaleByMouseEvent(e, false))
                canvas.repaint();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1 || e.getButton() == MouseEvent.BUTTON3)
                clicked.set(0);
        }

        @Override
        public void mouseEntered(MouseEvent e) { }

        @Override
        public void mouseExited(MouseEvent e) { }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (clicked.get() > 0 && setTaleByMouseEvent(e, true))
                canvas.repaint();
        }

        @Override
        public void mouseMoved(MouseEvent e) { }
    }

    public MainFrame() {

        tempWidth = 0;
        tempHeight = 0;
        tempTaleSize = 0;

        mouseHandler = new PathCanvasMouseHandler();

        clicked = new AtomicInteger(0);
        colorMap = new HashMap<>();
        radioButtonMap = new HashMap<>();
        comboBoxMap = new LinkedHashMap<>();
        buttonGroup = new ButtonGroup();

        startPoint = new Point();
        endPoint = new Point();

        colorMap.put(TaleType.TALE_TYPE_NONE, Color.white);
        colorMap.put(TaleType.TALE_TYPE_START, Color.green);
        colorMap.put(TaleType.TALE_TYPE_END, Color.red);
        colorMap.put(TaleType.TALE_TYPE_WALL, Color.black);
        colorMap.put(TaleType.TALE_TYPE_PATH, Color.blue);

        comboBoxMap.put("DFS", new DFS());
        comboBoxMap.put("BFS", new BFS());
        comboBoxMap.put("Dijkstra", new Dijkstra());
        comboBoxMap.put("BellmanFord", new BellmanFord());

        frame = new JFrame();
        frame.setTitle("Path Finding");
        setFrameSize();
        frame.setFocusable(true);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        startPoint.setLocation(2 ,2);
        endPoint.setLocation(4 , 4);

        initialize();

        frame.setVisible(true);
    }

    private void setFrameSize(){
        int w = (TALE_SIZE * WIDTH) + 125;
        int h = (TALE_SIZE * HEIGHT) + CANVAS_OFFSET + 60;
        frame.setSize(Integer.max(w, 480), Integer.max(h, 320));
    }

    private void validatePoint(Point p, int defaultValue){
        if (p.x > WIDTH || p.x < 0)
            p.x = defaultValue;
        if (p.y > HEIGHT || p.y < 0)
            p.y = defaultValue;
    }

    private void initGrid(){
        for (TaleType[] taleArr : grid){
            Arrays.fill(taleArr, TaleType.TALE_TYPE_NONE);
        }

        validatePoint(startPoint, 0);
        validatePoint(endPoint, Integer.min(WIDTH, HEIGHT) - 1);

        grid[startPoint.y][startPoint.x] = TaleType.TALE_TYPE_START;
        grid[endPoint.y][endPoint.x] = TaleType.TALE_TYPE_END;
    }

    private void applyFrameChange(JPanel panel){
        if (tempWidth == 0 && tempHeight == 0 && tempTaleSize == 0)
            return;

        WIDTH = tempWidth == 0 ? WIDTH : tempWidth;
        HEIGHT = tempHeight == 0 ? HEIGHT : tempHeight;
        TALE_SIZE = tempTaleSize == 0 ? TALE_SIZE : tempTaleSize;
        tempWidth = tempHeight = tempTaleSize = 0;
        frame.remove(panel);
        initialize();
        setFrameSize();
    }

    private void addSpinnerExplanationText(String text, int x, int y, JPanel panel){

        JTextArea textArea = new JTextArea(text);
        textArea.setFont(new Font(textArea.getFont().getName(), Font.PLAIN, 12));
        textArea.setBounds(x, y, 80, 18);
        textArea.setOpaque(false);
        textArea.setEditable(false);

        panel.add(textArea);
    }

    private void addTaleTypeRadioButton(String text, int x,  TaleType type, JPanel panel){
        JRadioButton temp = new JRadioButton(text);
        temp.setBounds(x, 5, 60, 25);
        panel.add(temp);
        buttonGroup.add(temp);
        radioButtonMap.put(temp.getModel(), type);

        buttonGroup.setSelected(temp.getModel(), true);
    }

    private void initialize() {
        JPanel panel = new JPanel();
        grid = new TaleType[HEIGHT][WIDTH];
        initGrid();
        // Add top shelf components
        String[] keys = comboBoxMap.keySet().toArray(new String[0]);
        combo = new JComboBox<>(keys);
        combo.setBounds(5, 5, 100, 25);
        panel.add(combo);

        addTaleTypeRadioButton("Start", 110, TaleType.TALE_TYPE_START, panel);
        addTaleTypeRadioButton("End", 170, TaleType.TALE_TYPE_END, panel);
        addTaleTypeRadioButton("Wall", 230, TaleType.TALE_TYPE_WALL, panel);

        resultField = new JTextArea();
        resultField.setBounds(5, 30, frame.getWidth()-10, 25);
        resultField.setEditable(false);
        resultField.setOpaque(false);

        resultField.setFont(new Font(resultField.getFont().getName(), Font.PLAIN, 20));
        panel.add(resultField);

        int spinnerX = (WIDTH * TALE_SIZE) + 15;

        addSpinnerExplanationText("Width", spinnerX,60, panel);
        addSpinnerExplanationText("Height", spinnerX, 110, panel);
        addSpinnerExplanationText("Tale Size", spinnerX, 160, panel);

        JSpinner widthSpinner = new JSpinner(new SpinnerNumberModel(WIDTH, 5, 75, 1));
        widthSpinner.addChangeListener(e -> tempWidth = (int) widthSpinner.getValue());
        widthSpinner.setBounds(spinnerX, 80, 80, 25);
        panel.add(widthSpinner);

        JSpinner heightSpinner = new JSpinner(new SpinnerNumberModel(HEIGHT, 5, 75, 1));
        heightSpinner.addChangeListener(e -> tempHeight = (int) heightSpinner.getValue());
        heightSpinner.setBounds(spinnerX, 130, 80, 25);
        panel.add(heightSpinner);

        JSpinner taleSizeSpinner = new JSpinner(new SpinnerNumberModel(TALE_SIZE, 10, 25, 1));
        taleSizeSpinner.addChangeListener(e -> tempTaleSize = (int) taleSizeSpinner.getValue());
        taleSizeSpinner.setBounds(spinnerX, 180, 80, 25);
        panel.add(taleSizeSpinner);

        JButton applyButton = new JButton("Apply");
        applyButton.setBounds(spinnerX, 240, 80, 40);
        applyButton.addActionListener(e -> applyFrameChange(panel));
        panel.add(applyButton);

        PathCanvas canvas = new PathCanvas();
        canvas.setBounds(5, CANVAS_OFFSET, WIDTH * TALE_SIZE, HEIGHT * TALE_SIZE);
        panel.add(canvas);
        this.mouseHandler.setCanvas(canvas);

        // Add panel to frame
        frame.getContentPane().add(panel, BorderLayout.CENTER);
        panel.setLayout(null);

        // Add listeners
        panel.addMouseMotionListener(this.mouseHandler);
        panel.addMouseListener(this.mouseHandler);

        // Add buttons
        JButton pathButton = new JButton("Find");
        pathButton.setBounds(290, 5, 80, 25);
        pathButton.addActionListener(e -> {
            findPath();
            canvas.repaint();
        });
        panel.add(pathButton);

        JButton clearButton = new JButton("Clear");
        clearButton.setBounds(380, 5, 80, 25);
        clearButton.addActionListener(e -> {
            initGrid();
            resultField.setText("");
            canvas.repaint();
        });
        panel.add(clearButton);

    }

    private boolean addTale(int x, int y, TaleType type, boolean drag){
        boolean ret = false;
        if (drag){
            if (type == TaleType.TALE_TYPE_WALL){
                grid[y][x] = type;
                ret = true;
            }
        } else {
            if (type == TaleType.TALE_TYPE_START){
                grid[startPoint.y][startPoint.x] = TaleType.TALE_TYPE_NONE;
                startPoint.setLocation(x, y);
                validatePoint(startPoint, 0);
            } else if (type == TaleType.TALE_TYPE_END){
                grid[endPoint.y][endPoint.x] = TaleType.TALE_TYPE_NONE;
                endPoint.setLocation(x, y);
                validatePoint(endPoint, Integer.min(WIDTH, HEIGHT) - 1);
            }
            grid[y][x] = type;
            ret = true;
        }
        return ret;
    }

    private boolean removeTale(int x, int y){
        if (grid[y][x] == TaleType.TALE_TYPE_WALL || grid[y][x] == TaleType.TALE_TYPE_PATH){
            grid[y][x] = TaleType.TALE_TYPE_NONE;
            return true;
        }
        return false;
    }

    private boolean setTaleByMouseEvent(MouseEvent e, boolean drag){
        int x = e.getX();
        int y = e.getY() - CANVAS_OFFSET;
        boolean inBounds = (x >= 0 && x < WIDTH * TALE_SIZE) && (y >= 0 && y < HEIGHT * TALE_SIZE);

        if (inBounds) {
            x = x / TALE_SIZE;
            y = y / TALE_SIZE;

            if (clicked.get() == BUTTON_LEFT){
                if (grid[y][x] == TaleType.TALE_TYPE_NONE || grid[y][x] == TaleType.TALE_TYPE_PATH) {
                    TaleType type = radioButtonMap.get(buttonGroup.getSelection());
                    return addTale(x, y, type, drag);
                }
            } else {
                return removeTale(x, y);
            }
        }

        return false;
    }

    private void clearPrevPath(){
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                if (grid[i][j] == TaleType.TALE_TYPE_PATH)
                    grid[i][j] = TaleType.TALE_TYPE_NONE;
            }
        }
    }

    private void findPath() {
        clearPrevPath();
        // Copy current grid information as boolean grid
        boolean[][] tempGrid = new boolean[HEIGHT][WIDTH];

        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                if (grid[i][j] != TaleType.TALE_TYPE_WALL)
                    tempGrid[i][j] = true;
            }
        }

        PathFindingAlgorithm algo = comboBoxMap.get(String.valueOf(combo.getSelectedItem()));
        startAlgorithm(algo, tempGrid);
    }

    private void startAlgorithm(PathFindingAlgorithm algo, boolean[][] grid) {
        int start = (startPoint.y * WIDTH) + startPoint.x;
        int end = (endPoint.y * WIDTH) + endPoint.x;

        Graph g = new Graph(grid);
        algo.solve(g, start);
        if (algo.checkPath(end)) {
            List<Integer> backtrace = algo.getBacktrace(start, end);
            markPath(backtrace);
            resultField.setForeground(Color.black);
            resultField.setText("Path has been found and marked. Path distance: " + backtrace.size());
        } else {
            resultField.setForeground(Color.red);
            resultField.setText("NO PATH FOUND");
        }
    }

    private void markPath(List<Integer> path){
        for (int point : path){
            int x = point % WIDTH;
            int y = point / WIDTH;

            grid[y][x] = TaleType.TALE_TYPE_PATH;
        }

    }
}