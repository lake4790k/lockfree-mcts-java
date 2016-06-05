package mcts;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Mcts<Action, StateT extends State<Action>> {
    private final Random random = ThreadLocalRandom.current();

    private final long timePerActionMillis;
    private final int maxIterations;

    private Node<Action, StateT> root;
    private Action lastAction;

    public Mcts(long timePerActionMillis, int maxIterations) {
        this.timePerActionMillis = timePerActionMillis;
        this.maxIterations = maxIterations;
    }

    public void setRoot(Action action, StateT state) {
        if (root != null) {
            Node<Action, StateT> child = root.findChildFor(action);
            if (child != null) {
                root = child;
                root.releaseParent();
                return;
            }
        }
        root = new Node<>(null, null, state);
    }

    public void think() {
        long started = System.currentTimeMillis();
        int i = 0;
        while (i++ < maxIterations && System.currentTimeMillis() - started < timePerActionMillis
            || !root.isExpanded()) {

            growTree();
        }
    }

    public State<Action> takeAction() {
        Node<Action, StateT> actionNode = root.childToExploit();
        lastAction = actionNode.getAction();
        root = actionNode;
        return actionNode.getState();
    }

    private void growTree() {
        Node<Action, StateT> child = selectOrExpand(root);
        StateT terminalState = simulate(child);
        backPropagate(child, terminalState);
    }

    private Node<Action, StateT> selectOrExpand(Node<Action, StateT> root) {
        Node<Action, StateT> node = root;
        while (!node.isTerminal()) {
            if (!node.isExpanded()) {
                Node<Action, StateT> expandedNode = node.expand();
                if (expandedNode != null)
                    return expandedNode;
            }
            node = node.childToExplore();
        }
        return node;
    }

    @SuppressWarnings("unchecked")
    private StateT simulate(Node<Action, StateT> node) {
        StateT state = (StateT) node.getState().copy();
        while (!state.isTerminal()) {
            Action action = randomAction(state);
            state.applyAction(action);
        }
        return state;
    }

    private Action randomAction(StateT state) {
        List<Action> actions = state.getAvailableActions();
        // TODO select winner action?
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

    public Action getLastAction() {
        return lastAction;
    }

}
