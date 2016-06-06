package mcts;

public class TicTacToe implements State {

    public static TicTacToe start(int dim) {
        return new TicTacToe((byte) dim, (byte) dim);
    }

    public static TicTacToe start(int dim, int needed) {
        return new TicTacToe((byte) dim, (byte) needed);
    }

    private static final byte DRAW = 0;
    private static final byte NOT_OVER_YET = (byte) 99;

    private byte agent;
    private final byte[][] board;
    private final byte needed;

    private byte winner;
    private int round;

    public TicTacToe(byte dims, byte needed) {
        this.needed = needed;
        this.agent = 1;
        this.board = new byte[dims][dims];
        this.winner = NOT_OVER_YET;
    }

    public TicTacToe(TicTacToe o) {
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

    public TicTacToe(TicTacToe o, short action) {
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
    public State copy() {
        return new TicTacToe(this);
    }

    @Override
    public boolean isTerminal() {
        return winner < NOT_OVER_YET;
    }

    @Override
    public short[] getAvailableActions() {
        int dim = board.length;
        int remaining = dim * dim - round;
        short[] actions = new short[remaining];
        int idx = 0;
        for (byte r = 0; r < dim; r++) {
            for (byte c = 0; c < dim; c++) {
                if (board[r][c] == 0) {
                    actions[idx++] = (short) (r * dim + c);
                }
            }
        }
        assert idx == remaining;
        return actions;
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

    private byte updateWith(short action) {
        byte prevAgent = (byte) (3 - agent);
        int dim = board.length;
        int row = action / dim;
        int col = action % dim;
        round++;
        board[row][col] = prevAgent;

        int contiguous = 0;
        for (int r = 0; r < dim; r++) {
            if (board[r][col] != prevAgent) {
                contiguous = 0;
            } else {
                if (++contiguous == needed)
                    return prevAgent;
            }
        }

        contiguous = 0;
        for (int c = 0; c < dim; c++) {
            if (board[row][c] != prevAgent) {
                contiguous = 0;
            } else {
                if (++contiguous == needed)
                    return prevAgent;
            }
        }

        if (row == col) {
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

        if (row == dim - 1 - col) {
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
    public State takeAction(short action) {
        return new TicTacToe(this, action);
    }

    @Override
    public void applyAction(short action) {
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
