package mcts;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Mcts<Action, StateT extends State<Action>> {
    private final Random random = ThreadLocalRandom.current();

    private final long timePerActionMillis;
    private final int maxIterations;

    public Mcts(long timePerActionMillis, int maxIterations) {
        this.timePerActionMillis = timePerActionMillis;
        this.maxIterations = maxIterations;
    }

    public State<Action> takeAction(StateT state) {
        long started = System.currentTimeMillis();
        Node<Action, StateT> root = new Node<>(null, null, state);
        int i = 0;
        while (i++ < maxIterations && System.currentTimeMillis() - started < timePerActionMillis
            || !root.isExpanded()) {

            growTree(root);
        }
        Node<Action, StateT> actionNode = root.childToExploit();
        Action action = actionNode.getAction();
        return state.takeAction(action);
    }

    private void growTree(Node<Action, StateT> root) {
        Node<Action, StateT> child = selectOrExpand(root);
        StateT terminalState = simulate(child);
        backPropagate(child, terminalState);
    }

    private Node<Action, StateT> selectOrExpand(Node<Action, StateT> root) {
        Node<Action, StateT> node = root;
        while (!node.isTerminal()) {
            if (!node.isExpanded()) {
                return node.expand();
            }
            node = node.childToExplore();
        }
        return node;
    }

    @SuppressWarnings("unchecked")
    private StateT simulate(Node<Action, StateT> node) {
        StateT state = node.getState();
        while (!state.isTerminal()) {
            Action action = randomBiasedAction(state);
            state = (StateT) state.takeAction(action);
        }
        return state;
    }

    private Action randomBiasedAction(StateT state) {
        List<Action> actions = state.getAvailableActions();
        // for (int i = 0; i < actions.size(); i++) {
        // ActionT action = actions.get(i);
        // State<ActionT> next = state.takeAction(action);
        // if (next.isTerminal())
        // return action;
        // }
        int randomIdx = random.nextInt(actions.size());
        return actions.get(randomIdx);
    }

    private void backPropagate(Node<Action, StateT> node, StateT terminalState) {
        while (node != null) {
            double reward = terminalState.getRewardFor(node.getPreviousAgent());
            node.updateRewards(reward);
            node = node.getParent();
        }
    }

}
