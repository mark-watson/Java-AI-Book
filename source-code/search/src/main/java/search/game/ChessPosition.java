package search.game;

public final class ChessPosition implements Position {
    public static final int BLANK = 0;
    public static final int HUMAN = 1;
    public static final int PROGRAM = -1;
    public static final int PAWN = 1;
    public static final int KNIGHT = 2;
    public static final int BISHOP = 3;
    public static final int ROOK = 4;
    public static final int QUEEN = 5;
    public static final int KING = 9;
    int [] board = new int[120];
    @Override
    public String toString() {
        var sb = new StringBuilder("[");
        for (int i = 22; i < 100; i++) {
            sb.append(board[i]).append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}
