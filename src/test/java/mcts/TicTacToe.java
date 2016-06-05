package mcts;

import java.util.ArrayList;
import java.util.List;

public class TicTacToe {

    public static TicTacToeState start(int dim) {
        return new TicTacToeState((byte) dim, (byte) dim);
    }

    public static TicTacToeState start(int dim, int needed) {
        return new TicTacToeState((byte) dim, (byte) needed);
    }

    static class Action {
        private final byte row;
        private final byte col;

        Action(byte row, byte col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public String toString() {
            return "[row=" + row + ", col=" + col + "]";
        }
    }

    public static class TicTacToeState implements State<Action> {
        private static final byte DRAW = 0;
        private static final byte NOT_OVER_YET = (byte) 99;

        private byte agent;
        private final byte[][] board;
        private final byte needed;

        private byte winner;
        private int round;

        public TicTacToeState(byte dims, byte needed) {
            this.needed = needed;
            this.agent = 1;
            this.board = new byte[dims][dims];
            this.winner = NOT_OVER_YET;
        }

        public TicTacToeState(TicTacToeState o) {
            agent = o.agent;
            int dim = o.board.length;
            board = new byte[dim][dim];
            for (int r = 0; r < dim; r++) {
                board[r] = o.board[r].clone();
            }
            needed = o.needed;
            round = o.round;
            winner = o.winner;
        }

        public TicTacToeState(TicTacToeState o, Action action) {
            agent = (byte) (3 - o.agent);
            int dim = o.board.length;
            board = new byte[dim][dim];
            for (int r = 0; r < dim; r++) {
                board[r] = o.board[r].clone();
            }
            needed = o.needed;
            round = o.round;
            winner = updateWith(action);
        }

        @Override
        public State<Action> copy() {
            return new TicTacToeState(this);
        }

        @Override
        public boolean isTerminal() {
            return winner < NOT_OVER_YET;
        }

        @Override
        public List<Action> getAvailableActions() {
            List<Action> actions = new ArrayList<>();
            int dim = board.length;
            for (byte r = 0; r < dim; r++) {
                for (byte c = 0; c < dim; c++) {
                    if (board[r][c] == 0) {
                        Action action = new Action(r, c);
                        actions.add(action);
                    }
                }
            }
            return actions;
        }

        @Override
        public int getAgent() {
            return agent;
        }

        @Override
        public int getPreviousAgent() {
            return 3 - agent;
        }

        @Override
        public double getRewardFor(int agent) {
            assert winner < NOT_OVER_YET;
            if (winner == DRAW)
                return .5;

            return winner == agent
                ? 1.
                : 0;
        }

        private byte updateWith(Action action) {
            byte prevAgent = (byte) (3 - agent);
            round++;
            board[action.row][action.col] = prevAgent;
            int dim = board.length;

            int contiguous = 0;
            for (int r = 0; r < dim; r++) {
                if (board[r][action.col] != prevAgent) {
                    contiguous = 0;
                } else {
                    if (++contiguous == needed)
                        return prevAgent;
                }
            }

            contiguous = 0;
            for (int c = 0; c < dim; c++) {
                if (board[action.row][c] != prevAgent) {
                    contiguous = 0;
                } else {
                    if (++contiguous == needed)
                        return prevAgent;
                }
            }

            if (action.row == action.col) {
                contiguous = 0;
                for (int x = 0; x < dim; x++) {
                    if (board[x][x] != prevAgent) {
                        contiguous = 0;
                    } else {
                        if (++contiguous == needed)
                            return prevAgent;
                    }
                }
            }

            if (action.row == dim - 1 - action.col) {
                contiguous = 0;
                for (int x = 0; x < dim; x++) {
                    if (board[x][dim - 1 - x] != prevAgent) {
                        contiguous = 0;
                    } else {
                        if (++contiguous == needed)
                            return prevAgent;
                    }
                }
            }

            return round == dim * dim
                ? DRAW
                : NOT_OVER_YET;
        }

        @Override
        public State<Action> takeAction(Action action) {
            return new TicTacToeState(this, action);
        }

        @Override
        public void applyAction(Action action) {
            agent = (byte) (3 - agent);
            winner = updateWith(action);
        }

        @Override
        public int getWinner() {
            return winner;
        }

        @Override
        public String toString() {
            StringBuffer b = new StringBuffer("State=\n");
            for (int r = 0; r < board.length; r++) {
                for (int c = 0; c < board.length; c++) {
                    switch (board[r][c]) {
                        case 0:
                            b.append(".");
                            break;
                        case 1:
                            b.append("X");
                            break;
                        case 2:
                            b.append("O");
                            break;
                    }
                }
                b.append("\n");
            }
            return b.toString();
        }

    }

}
