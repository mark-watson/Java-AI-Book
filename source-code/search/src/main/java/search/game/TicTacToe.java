package search.game;

public class TicTacToe extends GameSearch {

    public boolean drawnPosition(Position p) {
        if (GameSearch.DEBUG) System.out.println("drawnPosition("+p+")");
        boolean ret = true;
        TicTacToePosition pos = (TicTacToePosition)p;
        for (int i=0; i<9; i++) {
            if (pos.board[i] == TicTacToePosition.BLANK){
                ret = false;
                break;
            }
        }
        if (GameSearch.DEBUG) System.out.println("     ret="+ret);
        return ret;
    }
    public boolean wonPosition(Position p, boolean player) {
        if (GameSearch.DEBUG) System.out.println("wonPosition("+p+","+player+")");
        boolean ret = false;
        TicTacToePosition pos = (TicTacToePosition)p;
        if (winCheck(0,1,2, player, pos)) ret = true;
        else if (winCheck(3,4,5, player, pos)) ret = true;
        else if (winCheck(6,7,8, player, pos)) ret = true;
        else if (winCheck(2,5,8, player, pos)) ret = true;
        else if (winCheck(0,4,8, player, pos)) ret = true;
        else if (winCheck(2,4,6, player, pos)) ret = true;
        else if (winCheck(0,3,6, player, pos)) ret = true;
        else if (winCheck(1,4,7, player, pos)) ret = true;
        if (GameSearch.DEBUG) System.out.println("     ret="+ret);
        return ret;
    }
    private boolean winCheck(int i1, int i2, int i3,
                             boolean player, TicTacToePosition pos) {
        int b = player ? TicTacToePosition.HUMAN : TicTacToePosition.PROGRAM;
        return pos.board[i1] == b &&
               pos.board[i2] == b &&
               pos.board[i3] == b;
    }

    public float positionEvaluation(Position p, boolean player) {
        int count = 0;
        TicTacToePosition pos = (TicTacToePosition)p;
        for (int i=0; i<9; i++) {
            if (pos.board[i] == 0) count++;
        }
        count = 10 - count;
        // prefer the center square:
        float base = 1.0f;
        if (pos.board[4] == TicTacToePosition.HUMAN &&
            player) {
            base += 0.4f;
        }
        if (pos.board[4] == TicTacToePosition.PROGRAM &&
            !player) {
            base -= 0.4f;
        }
        float ret = (base - 1.0f);
        if (wonPosition(p, player))  {
            return base + (1.0f / count);
        }
        if (wonPosition(p, !player))  {
            return -(base + (1.0f / count));
        }
        return ret;
    }
    public void printPosition(Position p) {
        System.out.println("Board position:");
        TicTacToePosition pos = (TicTacToePosition)p;
        int count = 0;
        for (int row=0; row<3; row++) {
            System.out.println();
            for (int col=0; col<3; col++) {
                if (pos.board[count] == TicTacToePosition.HUMAN) {
                    System.out.print("H");
                } else if (pos.board[count] == TicTacToePosition.PROGRAM) {
                    System.out.print("P");
                } else {
                    System.out.print(" ");
                }
                count++;
            }
        }
        System.out.println();
    }
    public Position [] possibleMoves(Position p, boolean player) {
        if (GameSearch.DEBUG) System.out.println("posibleMoves("+p+","+player+")");
        TicTacToePosition pos = (TicTacToePosition)p;
        int count = 0;
        for (int i=0; i<9; i++) if (pos.board[i] == 0) count++;
        if (count == 0) return null;
        Position [] ret = new Position[count];
        count = 0;
        for (int i=0; i<9; i++) {
            if (pos.board[i] == 0) {
                TicTacToePosition pos2 = new  TicTacToePosition();
                System.arraycopy(pos.board, 0, pos2.board, 0, 9);
                if (player) pos2.board[i] = 1; else pos2.board[i] = -1;
                ret[count++] = pos2;
                if (GameSearch.DEBUG) System.out.println("    "+pos2);
            }
        }
        return ret;
    }
    public Position makeMove(Position p, boolean player, Move move) {
        if (GameSearch.DEBUG) System.out.println("Entered TicTacToe.makeMove");
        TicTacToeMove m = (TicTacToeMove)move;
        TicTacToePosition pos = (TicTacToePosition)p;
        TicTacToePosition pos2 = new  TicTacToePosition();
        System.arraycopy(pos.board, 0, pos2.board, 0, 9);
        int pp = player ? 1 : -1;
        if (GameSearch.DEBUG) System.out.println("makeMove: m.moveIndex = " + m.moveIndex());
        pos2.board[m.moveIndex()] = pp;
        return pos2;
    }
    public boolean reachedMaxDepth(Position p, int depth) {
        boolean ret = false;
        if (wonPosition(p, false)) ret = true;
        else if (wonPosition(p, true))  ret = true;
        else if (drawnPosition(p))      ret = true;
        if (GameSearch.DEBUG) {
            System.out.println("reachedMaxDepth: pos=" + p.toString() + ", depth="+depth
                               +", ret=" + ret);
        }
        return ret;
    }
    public Move createMove() {
        if (GameSearch.DEBUG) System.out.println("Enter blank square index [0,8]:");
        int i = 0;
        try {
            int ch = System.in.read();
            i = ch - 48;
            System.in.read();
            System.in.read();
            System.out.println("ch="+ch+", i=" + i);
        } catch (Exception e) { }
        return new TicTacToeMove(i);
    }
    static public void main(String [] args) {
        TicTacToePosition p = new TicTacToePosition();
        TicTacToe ttt = new TicTacToe();
        ttt.playGame(p, false);
    }
}
