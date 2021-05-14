package core.path;

import core.graph.Graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DFS implements PathFindingAlgorithm{

    private int[] backtrace;
    private boolean[] visited;
    private Graph graph;

    @Override
    public void solve(Graph g, int start) {
        visited = new boolean[g.getSize()];
        this.backtrace = new int[g.getSize()];
        Arrays.fill(backtrace, -1);
        this.graph = g;
        dfs(-1, start);
    }

    @Override
    public boolean checkPath(int end) {
        return visited[end];
    }

    @Override
    public List<Integer> getBacktrace(int start, int end) {
        List<Integer> ret = new ArrayList<>();
        int temp = backtrace[end];
        while (temp != start) {
            ret.add(temp);
            temp = backtrace[temp];
        }
        Collections.reverse(ret);
        return ret;
    }

    private void dfs(int s, int v){
        if (visited[v])
            return;
        visited[v] = true;
        backtrace[v] = s;

        for (int neigh : graph.getNeighborsList(v)){
            dfs(v, neigh);
        }
    }


}
