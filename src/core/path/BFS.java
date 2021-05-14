package core.path;

import core.graph.Graph;

import java.util.*;

public class BFS implements PathFindingAlgorithm {

    private Graph g;
    private boolean[] visited;
    private int[] prev;

    @Override
    public void solve(Graph g, int start) {
        this.g = g;
        bfs(start);
    }

    private void bfs(int start){
        int size = g.getSize();

        visited = new boolean[size];
        prev = new int[size];
        Queue<Integer> queue = new LinkedList<>();

        visited[start] = true;
        queue.add(start);

        while(!queue.isEmpty()){
            int cur = queue.poll();

            for (int neigh : g.getNeighborsList(cur)){
                if (!visited[neigh]){
                    visited[neigh] = true;
                    prev[neigh] = cur;
                    queue.add(neigh);
                }
            }
        }
    }

    @Override
    public boolean checkPath(int end) {
        return visited[end];
    }

    @Override
    public List<Integer> getBacktrace(int start, int end) {

        List<Integer> ret = new ArrayList<>();
        int current = prev[end];
        while (current != start) {
            ret.add(current);
            current = prev[current];
        }

        Collections.reverse(ret);
        return ret;
    }
}
