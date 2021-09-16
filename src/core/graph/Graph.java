package core.graph;

import java.util.ArrayList;
import java.util.List;

public class Graph {

    private final List<List<Integer>> adj;
    private final List<Integer> vertices;
    private final int size;

    public Graph(boolean[][] grid, boolean diagonal) {
        adj = new ArrayList<>();
        vertices = new ArrayList<>();
        size = grid.length * grid[0].length;

        for (int i = 0; i < size; i++)
            adj.add(new ArrayList<>());

        for (int i = 0; i < grid.length; i++)
            for (int j = 0; j < grid[0].length ; j++)
                checkAdjacentNodes(grid, j, i, diagonal);

        generateVertexList();
    }

    private void checkAdjacentNodes(boolean[][] grid, int x, int y, boolean diagonal){
        // If there is a blockage at that point, don't process
        if (!grid[y][x]) return;

        if (diagonal){
            for (int i = -1; i <= 1; i++)
                for (int j = -1; j <= 1; j++)
                    checkNode(i, j, x, y, grid);
        } else {
            checkNode(0, -1, x, y, grid);
            checkNode(-1, 0, x, y, grid);
            checkNode(0, 1, x, y, grid);
            checkNode(1, 0, x, y, grid);
        }
    }

    private void checkNode(int i, int j, int x, int y, boolean[][] grid){
        int h = grid.length;
        int w = grid[0].length;

        int newX = x + i;
        int newY = y + j;

        int node1 = node(w, x, y);
        int node2 = node(w, newX, newY);

        if (node1 == node2)
            return;

        boolean inBounds = (newX < w && newX >= 0) && (newY < h && newY >= 0);
        if (inBounds && grid[newY][newX]) {
            adj.get(node1).add(node2);
        }
    }

    private int node(int size, int x, int y){
        return (y * size) + x;
    }

    public List<Integer> getNeighborsList(int idx){
        return adj.get(idx);
    }

    public int getSize(){
        return size;
    }

    public int getVertexCount(){
        return vertices.size();
    }

    private void generateVertexList(){
        for (int i = 0; i < adj.size(); i++) {
            if (!adj.get(i).isEmpty())
                vertices.add(i);
        }
    }

    public List<Integer> getVertexList(){
        return vertices;
    }

}
