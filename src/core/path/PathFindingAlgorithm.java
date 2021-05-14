package core.path;

import core.graph.Graph;

import java.util.List;

public interface PathFindingAlgorithm {

    void solve(Graph g, int start);
    boolean checkPath(int end);
    List<Integer> getBacktrace(int start, int end);

}
