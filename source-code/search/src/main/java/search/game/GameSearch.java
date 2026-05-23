package search.game;

import java.util.*;

public abstract class GameSearch {

    public static final boolean DEBUG = false;

    /*
     * Note: the abstract Position also needs to be
     *       subclassed to write a new game program.
     */
    /*
     * Note: the abstract class Move also needs to be subclassed.
     *       
     */

    public static final boolean PROGRAM = false;
    public static final boolean HUMAN = true;

    /**
     *  Notes:  PROGRAM false -1,  HUMAN true 1
     */

    /*
     * Abstract methods:
     */

    public abstract boolean drawnPosition(Position p);
    public abstract boolean wonPosition(Position p, boolean player);
    public abstract float positionEvaluation(Position p, boolean player);
    public abstract void printPosition(Position p);
    public abstract Position [] possibleMoves(Position p, boolean player);
    public abstract Position makeMove(Position p, boolean player, Move move);
    public abstract boolean reachedMaxDepth(Position p, int depth);
    public abstract Move createMove();

    /*
     * Search utility methods:
     */

    protected List<Object> alphaBeta(int depth, Position p, boolean player) {
        List<Object> v = alphaBetaHelper(depth, p, player, 1000000.0f, -1000000.0f);
        return v;
    }

    protected List<Object> alphaBetaHelper(int depth, Position p,
                                     boolean player, float alpha, float beta) {
        if (GameSearch.DEBUG) System.out.println("alphaBetaHelper("+depth+","+p+","+alpha+","+beta+")");
        if (reachedMaxDepth(p, depth)) {
            List<Object> v = new ArrayList<>(2);
            float value = positionEvaluation(p, player);
            v.add(value);
            v.add(null);
            if(GameSearch.DEBUG) {
                System.out.println(" alphaBetaHelper: mx depth at " + depth+
                                   ", value="+value);
            }
            return v;
        }
        List<Object> best = new ArrayList<>();
        Position [] moves = possibleMoves(p, player);
        for (int i=0; i<moves.length; i++) {
            List<Object> v2 = alphaBetaHelper(depth + 1, moves[i], !player, -beta, -alpha);
            float value = -((Float)v2.get(0));
            if (value > beta) {
                if(GameSearch.DEBUG) System.out.println(" ! ! ! value="+value+", beta="+beta);
                beta = value;
                best = new ArrayList<>();
                best.add(moves[i]);
                for (int j = 1; j < v2.size(); j++) {
                    Object o = v2.get(j);
                    if (o != null) best.add(o);
                }
            }
            /**
             * Use the alpha-beta cutoff test to abort search if we
             * found a move that proves that the previous move in the
             * move chain was dubious
             */
            if (beta >= alpha) {
                break;
            }
        }
        List<Object> v3 = new ArrayList<>();
        v3.add(beta);
        v3.addAll(best);
        return v3;
    }
    public void playGame(Position startingPosition, boolean humanPlayFirst) {
        if (!humanPlayFirst) {
            List<Object> v = alphaBeta(0, startingPosition, PROGRAM);
            startingPosition = (Position)v.get(1);
        }
        while (true) {
            printPosition(startingPosition);
            if (wonPosition(startingPosition, PROGRAM)) {
                System.out.println("Program won");
                break;
            }
            if (wonPosition(startingPosition, HUMAN)) {
                System.out.println("Human won");
                break;
            }
            if (drawnPosition(startingPosition)) {
                System.out.println("Drawn game");
                break;
            }
            System.out.println("Your move:");
            Move move = createMove();
            startingPosition = makeMove(startingPosition, HUMAN, move);
            printPosition(startingPosition);
            List<Object> v = alphaBeta(0, startingPosition, PROGRAM);
            for (Object element : v) {
                System.out.println(" next element: " + element);
            }
            startingPosition = (Position)v.get(1);        
        }
    }
}
