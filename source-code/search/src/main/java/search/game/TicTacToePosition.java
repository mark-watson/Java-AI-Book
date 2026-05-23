package search.game;

public final class TicTacToePosition implements Position {
    public static final int BLANK = 0;
    public static final int HUMAN = 1;
    public static final int PROGRAM = -1;
    int [] board = new int[9];
    @Override
    public String toString() {
        var sb = new StringBuilder("[");
        for (int i = 0; i < 9; i++) {
            sb.append(board[i]).append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}
