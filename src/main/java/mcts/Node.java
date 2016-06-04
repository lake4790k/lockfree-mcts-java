package mcts;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

class Node<ActionT extends Action, StateT extends State<ActionT>> {
    private static final Random random = ThreadLocalRandom.current();
    private static final double C = Math.sqrt(2);

    private final List<Node<ActionT, StateT>> children;
    private final List<ActionT> untakenActions;
    private final StateT state;
    private final Node<ActionT, StateT> parent;
    private final ActionT action;
    private final boolean hasActions;

    private int visits;
    private volatile double rewards;

    Node(Node<ActionT, StateT> parent, ActionT action, StateT state) {
        this.parent = parent;
        this.action = action;
        this.state = state;
        this.children = new ArrayList<>();
        this.untakenActions = state.getAvailableActions();
        hasActions = !untakenActions.isEmpty();
    }

    ActionT getAction() {
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
    Node<ActionT, StateT> expand() {
        int randomIdx = random.nextInt(untakenActions.size());
        ActionT untakenAction = untakenActions.remove(randomIdx);
        StateT actionState = (StateT) state.takeAction(untakenAction);
        Node<ActionT, StateT> child = new Node<>(this, untakenAction, actionState);
        children.add(child);
        return child;
    }

    Node<ActionT, StateT> getParent() {
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

    Node<ActionT, StateT> childToExploit() {
        // for (Node<ActionT, StateT> child : children)
        // System.out.println(child.getAction() + " = " + child);
        return getBestChild(.0);
    }

    Node<ActionT, StateT> childToExplore() {
        return getBestChild(C);
    }

    private Node<ActionT, StateT> getBestChild(double c) {
        assert isExpanded();
        double bestValue = Double.NEGATIVE_INFINITY;
        Node<ActionT, StateT> best = null;
        for (int i = 0; i < children.size(); i++) {
            Node<ActionT, StateT> child = children.get(i);
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
        return "Node [visits=" + visits + ", rewards=" + rewards + ", v=" + getUctValue(0.) + "]";
    }

}
