package mcts;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

class Node<Action, StateT extends State<Action>> {
    private static final Random random = ThreadLocalRandom.current();
    private static final double EXPLORATION_CONSTANT = Math.sqrt(2);
    private static final double NO_EXPLORATION = 0;

    private final List<Node<Action, StateT>> children;
    private final List<Action> untakenActions;
    private final StateT state;
    private final Node<Action, StateT> parent;
    private final Action action;
    private final boolean hasActions;

    private int visits;
    private volatile double rewards;

    Node(Node<Action, StateT> parent, Action action, StateT state) {
        this.parent = parent;
        this.action = action;
        this.state = state;
        this.children = new ArrayList<>();
        this.untakenActions = state.getAvailableActions();
        hasActions = !untakenActions.isEmpty();
    }

    Action getAction() {
        return action;
    }

    boolean isTerminal() {
        return state.isTerminal();
    }

    boolean hasActions() {
        return hasActions;
    }

    boolean isExpanded() {
        return untakenActions.isEmpty();
    }

    @SuppressWarnings("unchecked")
    Node<Action, StateT> expand() {
        int randomIdx = random.nextInt(untakenActions.size());
        Action untakenAction = untakenActions.remove(randomIdx);
        StateT actionState = (StateT) state.takeAction(untakenAction);
        Node<Action, StateT> child = new Node<>(this, untakenAction, actionState);
        children.add(child);
        return child;
    }

    Node<Action, StateT> getParent() {
        return parent;
    }

    int getPreviousAgent() {
        return state.getPreviousAgent();
    }

    void updateRewards(double reward) {
        assert reward >= 0;
        visits++;
        this.rewards += reward;
    }

    double getUctValue(double c) {
        return rewards / visits + c * Math.sqrt(Math.log(parent.visits) / visits);
    }

    Node<Action, StateT> childToExploit() {
        return getBestChild(NO_EXPLORATION);
    }

    Node<Action, StateT> childToExplore() {
        return getBestChild(EXPLORATION_CONSTANT);
    }

    private Node<Action, StateT> getBestChild(double c) {
        assert isExpanded();
        double bestValue = Double.NEGATIVE_INFINITY;
        Node<Action, StateT> best = null;
        for (int i = 0; i < children.size(); i++) {
            Node<Action, StateT> child = children.get(i);
            double chidrenValue = child.getUctValue(c);
            if (chidrenValue > bestValue) {
                best = child;
                bestValue = chidrenValue;
            }
        }
        return best;
    }

    StateT getState() {
        return state;
    }

    @Override
    public String toString() {
        return "Node [visits=" + visits + ", rewards=" + rewards + ", v="
            + getUctValue(NO_EXPLORATION) + "]";
    }

}
