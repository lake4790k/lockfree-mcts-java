package mcts;

import java.util.Arrays;

import org.junit.Test;

import mcts.TicTacToe.TicTacToeAction;
import mcts.TicTacToe.TicTacToeState;

public class TestTicTacToe {

    @Test
    public void test() {
        int[] score = new int[3];
        for (int i = 0; i < 100; i++) {
            // System.out.println(i);
            TicTacToeState startState = TicTacToe.start(6, 3);
            SelfPlay<TicTacToeAction, TicTacToeState> play = new SelfPlay<TicTacToeAction, TicTacToeState>(
                startState,
                10000,
                10000,
                10,
                50);

            int winner = play.play();

            score[winner]++;

            System.out.println(Arrays.toString(score));
            // assertEquals("" + endState, 0, endState.getWinner());
        }
    }

}
