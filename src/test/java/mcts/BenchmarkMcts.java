package mcts;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BenchmarkMcts {
    private final int numCpu = Runtime.getRuntime().availableProcessors();
    private final int times = 400;
    private final int maxIterations2 = 300;
    private final int timePerActionSec = 999000;
    private final int dim = 5;
    private final int needed = 3;

    private void run() {
        int t = 1;
        while (t <= numCpu) {
            int[] scores = testScores(1, t * maxIterations2);
            int winPercent = (int) (100. * scores[1] / times);
            System.out.printf(
                "1x%d vs 1x%d: %d%% %s\n",
                t * maxIterations2,
                maxIterations2,
                winPercent,
                Arrays.toString(scores));

            if (t == 1) {
                t *= 2;
                continue;
            }
            scores = testScores(t, maxIterations2);
            winPercent = (int) (100. * scores[1] / times);
            System.out.printf(
                "%dx%d vs 1x%d: %d%% %s\n",
                t,
                maxIterations2,
                maxIterations2,
                winPercent,
                Arrays.toString(scores));

            t *= 2;
        }
    }

    private int[] testScores(int threads1, int maxIterations1) {
        int threads2 = 1;
        ExecutorService executor1 = threads1 > 1
            ? Executors.newFixedThreadPool(threads1)
            : null;
        ExecutorService executor2 = threads2 > 1
            ? Executors.newFixedThreadPool(threads2)
            : null;

        int[] scores = new int[3];
        for (int i = 0; i < times; i++) {
            TicTacToe startState = TicTacToe.start(dim, needed);
            SelfPlay<TicTacToe> play = new SelfPlay<>(
                startState,
                executor1,
                executor2,
                threads1,
                threads2,
                timePerActionSec,
                timePerActionSec,
                maxIterations1,
                maxIterations2);

            int winner = play.play();

            scores[winner]++;
        }

        if (executor1 != null)
            executor1.shutdown();
        if (executor2 != null)
            executor2.shutdown();

        return scores;
    }

    public static void main(String[] args) {
        new BenchmarkMcts().run();
    }

}
