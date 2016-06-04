package mcts;

import java.util.List;

public interface State<ActionT extends Action> {

    boolean isTerminal();

    List<ActionT> getAvailableActions();

    int getAgent();

    int getPreviousAgent();

    double getRewardFor(int agent);

    State<ActionT> takeAction(ActionT action);

    int getWinner();

}
