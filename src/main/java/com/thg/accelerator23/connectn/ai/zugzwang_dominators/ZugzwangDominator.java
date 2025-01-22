package com.thg.accelerator23.connectn.ai.zugzwang_dominators;

import com.thehutgroup.accelerator.connectn.player.*;
import com.thg.accelerator23.connectn.ai.zugzwang_dominators.analysis.BoardAnalyser;
import com.thg.accelerator23.connectn.ai.zugzwang_dominators.analysis.GameState;
import com.thg.accelerator23.connectn.ai.zugzwang_dominators.exception.NoMoveFoundException;

import java.util.Map;


public class ZugzwangDominator extends Player {

    private static final int MAX_DEPTH = 5;

    public ZugzwangDominator(Counter counter) {
        //TODO: fill in your name here
        super(counter, ZugzwangDominator.class.getName());
    }

    @Override
    public int makeMove(Board board) {
        //TODO: some crazy analysis
        //TODO: make sure said analysis uses less than 2G of heap and returns within 10 seconds on whichever machine is running it

        try {
            System.out.println("findWinningMove called");
            return findWinningMove(board);
        } catch (NoMoveFoundException ignored){
        }

        try {
            System.out.println("findBlockingMove called");
            return findBlockingMove(board);
        } catch (NoMoveFoundException ignored){
        }

        // Use minimax to evaluate the best strategic move
        System.out.println("minimaxWithPruning called");
        int bestMove = -1;
        int bestScore = Integer.MIN_VALUE;

        for (int col = 0; col < board.getConfig().getWidth(); col++) {
            try {
                Board simulatedBoard = new Board(board, col, getCounter());
                int score = minimaxWithPruning(simulatedBoard, MAX_DEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE, false);

                if (score > bestScore) {
                    bestScore = score;
                    bestMove = col;
                }
            } catch (InvalidMoveException ignored) {
            }
        }

        // If no move is found, fall back to generating a random move
        return bestMove != -1 ? bestMove : generateRandomMove(board);

//        try {
//            System.out.println("maximiseBestRun called");
//            return maximiseBestRun(board);
//        } catch (NoMoveFoundException ignored) {
//        }
//
//        try {
//            System.out.println("blockOpponentFork called");
//            return blockOpponentFork(board);
//        } catch (NoMoveFoundException ignored) {
//        }
//
//        System.out.println("generateRandomMove called");
//        return generateRandomMove(board);
    }

    public boolean isWinningMove(int move, Board boardBeforeMove, Counter counter) throws InvalidMoveException {
        Board boardWithFutureMove = new Board(boardBeforeMove, move, counter);
        BoardAnalyser boardAnalyser = new BoardAnalyser(boardWithFutureMove.getConfig());
        GameState gameState = boardAnalyser.calculateGameState(boardWithFutureMove);
        return gameState.isWin();
    }

    public int generateRandomMove(Board board) {
        int column = (int) (Math.random() * board.getConfig().getWidth());

        if (board.getCounterAtPosition(new Position(column, board.getConfig().getHeight())) == null) {
          return column;
        } else {
          return generateRandomMove(board);
        }
    }

    public int findWinningMove(Board board) throws NoMoveFoundException {
        for (int i = 0; i < board.getConfig().getWidth(); i++) {
            try {
                if (isWinningMove(i, board, getCounter())) {
                    return i;
                }
            } catch (InvalidMoveException ignored) {}
        }

        throw new NoMoveFoundException();
    }

    public int findBlockingMove(Board board) throws NoMoveFoundException {
        for (int i = 0; i < board.getConfig().getWidth(); i++) {
            try {
                if (isWinningMove(i, board, getCounter().getOther())) {
                    return i;
                }
            } catch (InvalidMoveException ignored) {}
        }

        throw new NoMoveFoundException();
    }

    public int maximiseBestRun(Board board) throws NoMoveFoundException {
        int maxBestRun = 0;
        int move = -1;
        BoardAnalyser boardAnalyser = new BoardAnalyser(board.getConfig());
        GameState gameState = boardAnalyser.calculateGameState(board);

        for (int i = 0; i < board.getConfig().getWidth(); i++) {
            int gameStateMaxRun = gameState.getMaxInARowByCounter().values().size();
            if (gameStateMaxRun >maxBestRun) {
                maxBestRun = gameStateMaxRun;
                move = i;
            }
        }

        if (move == -1) {
            throw new NoMoveFoundException(); // No valid moves found
        }

        return move;
    }

    public int countWinningMoves(Board board, Counter counter) {
        int winningMoves = 0;

        for (int i = 0; i < board.getConfig().getWidth(); i++) {
            try {
                if (isWinningMove(i, board, counter)) {
                    winningMoves++;
                }
            } catch (InvalidMoveException ignored) {}

        }

        return winningMoves;
    }

    public int blockOpponentFork(Board board) throws NoMoveFoundException {
        try {
            for (int i = 0; i < board.getConfig().getWidth(); i++){
                Board futureOpponentBoard = new Board(board, i, getCounter().getOther());
                int opponentFork = countWinningMoves(futureOpponentBoard, getCounter().getOther());

                if (opponentFork > 1) {
                    return i;
                }
            }
        } catch (InvalidMoveException ignored) {}

        throw new NoMoveFoundException();
    }

    // Min Max
    private int minimaxWithPruning(Board board, int depth, int alpha, int beta, boolean isMaximizingPlayer) {
        BoardAnalyser boardAnalyser = new BoardAnalyser(board.getConfig());
        GameState gameState = boardAnalyser.calculateGameState(board);

        // Terminal state: win, loss, draw, or depth limit reached
        if (depth == 0 || gameState.isWin() || gameState.isDraw()) {
            return evaluateBoard(board, gameState);
        }

        if (isMaximizingPlayer) {
            int maxEval = Integer.MIN_VALUE;

            for (int col = 0; col < board.getConfig().getWidth(); col++) {
                try {
                    Board childBoard = new Board(board, col, getCounter());
                    int eval = minimaxWithPruning(childBoard, depth - 1, alpha, beta, false);
                    maxEval = Math.max(maxEval, eval);
                    alpha = Math.max(alpha, eval);

                    if (beta <= alpha) {
                        break; // Alpha-beta pruning
                    }
                } catch (InvalidMoveException ignored) {
                }
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;

            for (int col = 0; col < board.getConfig().getWidth(); col++) {
                try {
                    Board childBoard = new Board(board, col, getCounter().getOther());
                    int eval = minimaxWithPruning(childBoard, depth - 1, alpha, beta, true);
                    minEval = Math.min(minEval, eval);
                    beta = Math.min(beta, eval);

                    if (beta <= alpha) {
                        break; // Alpha-beta pruning
                    }
                } catch (InvalidMoveException ignored) {
                }
            }
            return minEval;
        }
    }

    private int evaluateBoard(Board board, GameState gameState) {
        if (gameState.isWin()) {
            return gameState.getWinner() == getCounter() ? Integer.MAX_VALUE : Integer.MIN_VALUE;
        }
        if (gameState.isDraw()) {
            return 0;
        }

        // Use your methods to compute heuristic scores
        int score = 0;

        // Prioritize longer sequences of the AI's counters
        for (Map.Entry<Counter, Integer> entry : gameState.getMaxInARowByCounter().entrySet()) {
            if (entry.getKey() == getCounter()) {
                score += entry.getValue(); // Favor the AI's advantage
            } else {
                score -= entry.getValue(); // Penalize the opponent's advantage
            }
        }

        return score;
    }
}
