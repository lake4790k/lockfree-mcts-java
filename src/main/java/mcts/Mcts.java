package mcts;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Mcts<ActionT extends Action, StateT extends State<ActionT>> {
    private final Random random = ThreadLocalRandom.current();

    private final long timePerActionMillis;
    private final int maxIterations;

    public Mcts(long timePerActionMillis, int maxIterations) {
        this.timePerActionMillis = timePerActionMillis;
        this.maxIterations = maxIterations;
    }

    public State<ActionT> takeAction(StateT state) {
        long started = System.currentTimeMillis();
        Node<ActionT, StateT> root = new Node<>(null, null, state);
        int i = 0;
        while (i++ < maxIterations && System.currentTimeMillis() - started < timePerActionMillis
            || !root.isExpanded()) {
            growTree(root);
        }
        Node<ActionT, StateT> actionNode = root.childToExploit();
        ActionT action = actionNode.getAction();
        return state.takeAction(action);
    }

    private void growTree(Node<ActionT, StateT> root) {
        Node<ActionT, StateT> child = selectOrExpand(root);
        StateT terminalState = simulate(child);
        backPropagate(child, terminalState);
    }

    private Node<ActionT, StateT> selectOrExpand(Node<ActionT, StateT> root) {
        Node<ActionT, StateT> node = root;
        while (!node.isTerminal()) {
            if (!node.isExpanded()) {
                return node.expand();
            }
            node = node.childToExplore();
        }
        return node;
    }

    @SuppressWarnings("unchecked")
    private StateT simulate(Node<ActionT, StateT> node) {
        StateT state = node.getState();
        while (!state.isTerminal()) {
            ActionT action = randomBiasedAction(state);
            state = (StateT) state.takeAction(action);
        }
        return state;
    }

    private ActionT randomBiasedAction(StateT state) {
        List<ActionT> actions = state.getAvailableActions();
        // for (int i = 0; i < actions.size(); i++) {
        // ActionT action = actions.get(i);
        // State<ActionT> next = state.takeAction(action);
        // if (next.isTerminal())
        // return action;
        // }
        int randomIdx = random.nextInt(actions.size());
        return actions.get(randomIdx);

    }

    private void backPropagate(Node<ActionT, StateT> node, StateT terminalState) {
        while (node != null) {
            double reward = terminalState.getRewardFor(node.getPreviousAgent());
            node.updateRewards(reward);
            node = node.getParent();
        }
    }

}
