package core.path;

import core.graph.Graph;

import java.util.*;

public class BellmanFord implements PathFindingAlgorithm {

    private Graph g;
    private boolean[] visited;
    private int[] backTrace;

    @Override
    public void solve(Graph g, int start) {
        this.g = g;
        bellmanFord(start);
    }

    private void bellmanFord(int start){

        int size = g.getSize();
        Queue<Integer> queue = new LinkedList<>();
        backTrace = new int[size];
        int[] weights = new int[size];
        visited = new boolean[size];

        Arrays.fill(weights, Integer.MAX_VALUE);
        Arrays.fill(visited, false);
        weights[start] = 0;
        queue.add(start);

        while(!queue.isEmpty()) {
            int current = queue.poll();
            visited[current] = true;
            int min = Integer.MAX_VALUE;
            int minValue = Integer.MAX_VALUE;
            boolean minFound = false;

            // Update weights from current vertex to all its neighbors
            for (int neigh : g.getNeighborsList(current)) {

                if (visited[neigh])
                    continue;

                if (weights[current] < weights[neigh]) {
                    weights[neigh] = weights[current] + 1;
                    backTrace[neigh] = current;
                }
            }

            for (int i = 0; i < weights.length; i++) {
                /*
                 * Iterate through all vertices
                 * Select the smallest weight, which is not visited
                 */
                if (!visited[i] && weights[i] < minValue) {
                    minFound = true;
                    min = i;
                    minValue = weights[i];
                }
            }

            if (minFound)
                queue.add(min);
        }
    }

    @Override
    public boolean checkPath(int end) {
        return visited[end];
    }

    @Override
    public List<Integer> getBacktrace(int start, int end) {
        List<Integer> ret = new ArrayList<>();
        int current = backTrace[end];
        while (current != start) {
            ret.add(current);
            current = backTrace[current];
        }

        Collections.reverse(ret);
        return ret;
    }
}
