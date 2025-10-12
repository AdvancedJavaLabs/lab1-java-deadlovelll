package org.example;

import org.graph.Graph;
import org.graph.RandomGraphGenerator;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class App {
    public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {
        int[] sizes = new int[]{10, 100, 1000, 10_000, 50_000, 100_000, 500_000, 1_000_000, 2_000_000, 5_000_000};
        int[] connections = new int[]{50, 500, 5000, 50_000, 100_000, 1_000_000, 2_500_000, 5_000_000, 10_000_000, 20_000_000};
        Random r = new Random(42);

        try (FileWriter csv = new FileWriter("bfs_results.csv")) {
            csv.write("GraphSize,Connections,SerialTime(ms),ParallelTime(ms),TimeDifference(ms),PercentDifference\n");

            for (int i = 0; i < sizes.length; i++) {
                System.out.println("--------------------------");
                System.out.println("Generating graph of size " + sizes[i] + " ...wait");
                Graph g = new RandomGraphGenerator().generateGraph(r, sizes[i], connections[i]);

                long startP = System.currentTimeMillis();
                g.parallelBFS(0);
                long endP = System.currentTimeMillis();
                long timeP = endP - startP;

                long startS = System.currentTimeMillis();
                g.bfs(0);
                long endS = System.currentTimeMillis();
                long timeS = endS - startS;

                double percent = (timeS - timeP) * 100.0 / timeS;

                System.out.println("------------------------");
                System.out.println("Time for the serial bfs " + timeS + "ms");
                System.out.println("Time for the parallel bfs " + timeP + "ms");
                System.out.println("BFS (parallel-serial) time difference: " + (timeS-timeP) + " ms");
                System.out.println("BFS (parallel-serial) percent difference: " + percent + " %");

                csv.write(String.format("%d,%d,%d,%d,%d,%.2f\n",
                        sizes[i], connections[i], timeS, timeP, (timeS-timeP), percent));
            }
        }

        System.out.println("Results written to bfs_results.csv");
    }
}
