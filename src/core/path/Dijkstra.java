package core.path;

import core.graph.Graph;

import java.util.*;

public class Dijkstra implements PathFindingAlgorithm{

    private Graph g;
    private int[] distance;
    private int[] prev;
    private boolean[] visited;

    private int INF;

    @Override
    public void solve(Graph g, int start) {
        this.g = g;
        dijkstra(start);
    }

    private void dijkstra(int start){
        int size = g.getSize();
        INF = size + 1;
        distance = new int[size];
        prev = new int[size];
        visited = new boolean[size];

        // This is needed to distinguish
        Arrays.fill(distance, -1);
        List<Integer> vertices = g.getVertexList();
        for (int v : vertices) {
            distance[v] = INF;
            prev[v] = -1;
        }

        distance[start] = 0;

        int current = getMinDistanceNode();
        // Current can be -1, distance array contains all possible nodes of the grid specification.
        // A better solution would be to implement this functionality with Maps instead of array indexing.
        // This works for a func project
        while (current != -1){
            visited[current] = true;
            for (int neigh : g.getNeighborsList(current)){
                int temp = distance[current] + 1;
                if (temp < distance[neigh]) {
                    distance[neigh] = temp;
                    prev[neigh] = current;
                }
            }
            current = getMinDistanceNode();
        }

    }

    private int getMinDistanceNode(){
        int min = INF;
        int minNode = -1;
        for (int i = 0; i < distance.length; i++) {
            if (!visited[i] && distance[i] < min && distance[i] >= 0) {
                min = distance[i];
                minNode = i;
            }
        }
        return minNode;
    }

    @Override
    public boolean checkPath(int end) {
        return visited[end];
    }

    @Override
    public List<Integer> getBacktrace(int start, int end) {
        List<Integer> ret = new ArrayList<>();
        int cur = prev[end];
        while (cur != start){
            ret.add(cur);
            cur = prev[cur];
        }
        Collections.reverse(ret);
        return ret;
    }
}
