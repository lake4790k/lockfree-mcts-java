package mcts;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

class Node<Action, StateT extends State<Action>> {
    private static final double EXPLORATION_CONSTANT = Math.sqrt(2);
    private static final double NO_EXPLORATION = 0;

    private final AtomicInteger untakenIndex = new AtomicInteger();
    private final AtomicInteger visits = new AtomicInteger();

    private final AtomicReferenceArray<Node<Action, StateT>> children;
    private final List<Action> untakenActions;
    private final StateT state;
    private final Action action;

    private volatile Node<Action, StateT> parent;
    private volatile double rewards;

    Node(Node<Action, StateT> parent, Action action, StateT state) {
        this.parent = parent;
        this.action = action;
        this.state = state;
        this.untakenActions = state.getAvailableActions();
        this.children = new AtomicReferenceArray<>(untakenActions.size());
        this.untakenIndex.set(untakenActions.size() - 1);
    }

    Node<Action, StateT> findChildFor(Action action) {
        for (int i = 0; i < children.length(); i++) {
            Node<Action, StateT> child = children.get(i);
            if (child == null)
                continue;
            if (child.action.equals(action))
                return child;
        }
        return null;
    }

    void releaseParent() {
        parent = null;
    }

    Action getAction() {
        return action;
    }

    boolean isTerminal() {
        return state.isTerminal();
    }

    boolean isExpanded() {
        return untakenIndex.get() < 0;
    }

    @SuppressWarnings("unchecked")
    Node<Action, StateT> expand() {
        int untakenIdx = untakenIndex.getAndDecrement();
        if (untakenIdx < 0)
            return null;

        Action untakenAction = untakenActions.get(untakenIdx);
        StateT actionState = (StateT) state.takeAction(untakenAction);
        Node<Action, StateT> child = new Node<>(this, untakenAction, actionState);
        children.set(untakenIdx, child);
        return child;
    }

    Node<Action, StateT> getParent() {
        return parent;
    }

    int getPreviousAgent() {
        return state.getPreviousAgent();
    }

    void updateRewards(double reward) {
        visits.incrementAndGet();
        this.rewards += reward;
    }

    boolean isVisited() {
        return visits.get() > 0;
    }

    double getUctValue(double c) {
        int visits1 = visits.get();
        return rewards / visits1 + c * Math.sqrt(Math.log(parent.visits.get()) / visits1);
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
        for (int i = 0; i < children.length(); i++) {
            Node<Action, StateT> child = null;
            while (child == null)
                child = children.get(i);

            while (!child.isVisited()) {}

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
