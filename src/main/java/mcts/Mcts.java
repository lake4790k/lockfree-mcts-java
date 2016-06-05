package mcts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

public class Mcts<Action, StateT extends State<Action>> {
    private final ExecutorService executor;

    private final long timePerActionMillis;
    private final int maxIterations;
    private final int threads;

    private Node<Action, StateT> root;
    private Action lastAction;

    public Mcts(int threads, long timePerActionMillis, int maxIterations) {
        this.threads = threads;
        this.timePerActionMillis = timePerActionMillis;
        this.maxIterations = maxIterations;
        executor = threads > 1
            ? Executors.newFixedThreadPool(threads)
            : null;
    }

    public void stop() {
        if (executor != null)
            executor.shutdown();
    }

    public Action getLastAction() {
        return lastAction;
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
        if (threads == 1) {
            doThink();
            return;
        }

        Collection<Callable<Void>> tasks = new ArrayList<>();

        for (int i = 0; i < threads; i++)
            tasks.add(() -> {
                doThink();
                return null;
            });

        try {
            executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    private void doThink() {
        long started = System.currentTimeMillis();
        int i = 0;
        Random random = ThreadLocalRandom.current();
        while (i++ < maxIterations && System.currentTimeMillis() - started < timePerActionMillis
            || !root.isExpanded()) {

            growTree(random);
        }
    }

    public State<Action> takeAction() {
        Node<Action, StateT> actionNode = root.childToExploit();
        lastAction = actionNode.getAction();
        root = actionNode;
        return actionNode.getState();
    }

    private void growTree(Random random) {
        Node<Action, StateT> child = selectOrExpand(root);
        StateT terminalState = simulate(child, random);
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
    private StateT simulate(Node<Action, StateT> node, Random random) {
        StateT state = (StateT) node.getState().copy();
        while (!state.isTerminal()) {
            Action action = randomAction(random, state);
            state.applyAction(action);
        }
        return state;
    }

    private Action randomAction(Random random, StateT state) {
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

}
