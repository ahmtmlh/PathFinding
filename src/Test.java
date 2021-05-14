import core.path.DFS;
import core.graph.Graph;
import core.path.PathFindingAlgorithm;

import java.util.List;

public class Test {

    public static void main(String[] args){
        boolean[][] grid = {
                {false, false, true, true},
                {true, false, false, true},
                {true, true, false, true},
                {true, false, false, false}};

        int start = 0;
        int end = 1;

        Graph g = new Graph(grid);
        PathFindingAlgorithm algo = new DFS();
        algo.solve(g, start);

        boolean res = algo.checkPath(end);
        System.out.println("Path found?: " + res);
        if (res){
            List<Integer> backtrace = algo.getBacktrace(start, end);
            System.out.println("Backtrace: ");
            for (Integer i : backtrace)
                System.out.print(i + "-");
        }
    }
}
