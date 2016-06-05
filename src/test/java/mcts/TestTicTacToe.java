package mcts;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Ignore;
import org.junit.Test;

import mcts.TicTacToe.TicTacToeState;

public class TestTicTacToe {
    private final int timePerActionSec1 = 10000;
    private final int timePerActionSec2 = 10000;
    private int maxIterations1 = 100;
    private int maxIterations2 = 100;

    private int dim = 3;
    private int needed = 3;

    @Test
    public void test() {
        dim = 3;
        needed = 3;
        maxIterations1 = 1000;
        maxIterations2 = 1000;
        int[] scores = testScores(100);
        System.out.println(Arrays.toString(scores));
        assertTrue(scores[0] > 95);
    }

    @Test
    @Ignore
    public void testBig() {
        dim = 10;
        needed = 8;
        maxIterations1 = 300;
        maxIterations2 = 1200;
        int[] scores = testScores(5);
        System.out.println(Arrays.toString(scores));
    }

    @Test
    public void test2() {
        dim = 6;
        needed = 3;
        maxIterations1 = 300;
        maxIterations2 = 1000;
        int[] scores = testScores(100);
        System.out.println(Arrays.toString(scores));
        assertEquals(0, scores[0]);
        assertTrue(scores[1] * 1.5 < scores[2]);
    }

    private int[] testScores(int times) {
        int[] scores = new int[3];
        for (int i = 0; i < times; i++) {
            TicTacToeState startState = TicTacToe.start(dim, needed);
            SelfPlay<TicTacToe.Action, TicTacToeState> play = new SelfPlay<TicTacToe.Action, TicTacToeState>(
                startState,
                timePerActionSec1,
                timePerActionSec2,
                maxIterations1,
                maxIterations2);

            int winner = play.play();

            scores[winner]++;
        }
        return scores;
    }

}
