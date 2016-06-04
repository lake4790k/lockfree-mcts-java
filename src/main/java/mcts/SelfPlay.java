package mcts;

import java.util.Random;

public class SelfPlay<Action, StateT extends State<Action>> {
    private final Random random = new Random();

    private final Mcts<Action, StateT> mcts1;
    private final Mcts<Action, StateT> mcts2;

    private StateT state;

    public SelfPlay(
        StateT state,
        int timePerActionSec1,
        int timePerActionSec2,
        int maxIterations1,
        int maxIterations2) {

        this.state = state;
        mcts1 = new Mcts<>(timePerActionSec1, maxIterations1);
        mcts2 = new Mcts<>(timePerActionSec2, maxIterations2);
    }

    @SuppressWarnings("unchecked")
    public int play() {
        int c = random.nextInt(2);
        int player = 0;
        while (!state.isTerminal()) {
            player = 1 + c++ % 2;
            Mcts<Action, StateT> mcts = player == 1
                ? mcts1
                : mcts2;

            state = (StateT) mcts.takeAction(state);
        }
        return state.getWinner() != 0
            ? player
            : 0;
    }

}
