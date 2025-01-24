package com.thg.accelerator23.connectn.ai.zugzwang_dominators;

import com.thehutgroup.accelerator.connectn.player.Board;
import com.thehutgroup.accelerator.connectn.player.Counter;
import com.thehutgroup.accelerator.connectn.player.InvalidMoveException;
import com.thehutgroup.accelerator.connectn.player.Player;
import com.thg.accelerator23.connectn.ai.zugzwang_dominators.analysis.BoardAnalyser;
import com.thg.accelerator23.connectn.ai.zugzwang_dominators.analysis.GameState;
import com.thg.accelerator23.connectn.ai.zugzwang_dominators.exception.NoMoveFoundException;
import com.thg.accelerator23.connectn.ai.zugzwang_dominators.model.MinimaxResult;

public class ZugzwangDominator extends Player {

    private static final int MAX_DEPTH = 7;

    public ZugzwangDominator(Counter counter) {
        //TODO: fill in your name here
        super(counter, ZugzwangDominator.class.getName());
    }

    @Override
    public int makeMove(Board board) {
        long startTime = System.currentTimeMillis();
        long timeLimit = 8500;
        int bestMove = -1;

        for (int i = 0; i < MAX_DEPTH; i++) {
            try {
                return findWinningMove(board);
            } catch (NoMoveFoundException ignored){
            }

            try {
                return findBlockingMove(board);
            } catch (NoMoveFoundException ignored){
            }

            try {
                bestMove = minimaxWithPruning(board, MAX_DEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE, true,
                        startTime, timeLimit)
                        .getMove();
            } catch (RuntimeException e) {
                System.err.println("Time limit reached at depth " + i);
                break;
            }
        }

        return bestMove;
    }

    public boolean isWinningMove(int move, Board boardBeforeMove, Counter counter) throws InvalidMoveException {
        Board boardWithFutureMove = new Board(boardBeforeMove, move, counter);
        BoardAnalyser boardAnalyser = new BoardAnalyser(boardWithFutureMove.getConfig());
        GameState gameState = boardAnalyser.calculateGameState(boardWithFutureMove);
        return gameState.isWin();
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

    private MinimaxResult minimaxWithPruning(Board board, int depth, int alpha, int beta, boolean isMaximizingPlayer,
                                             long startTime, long timeLimit) {

        if (System.currentTimeMillis() - startTime >= timeLimit) {
            throw new RuntimeException("Time limit exceeded");
        }

        BoardAnalyser boardAnalyser = new BoardAnalyser(board.getConfig());
        GameState gameState = boardAnalyser.calculateGameState(board);

        if (depth == 0 || gameState.isEnd()) {
            int score = evaluateBoard(gameState);
            return new MinimaxResult(-1, score);
        }

        if (isMaximizingPlayer) {
            MinimaxResult bestResult = new MinimaxResult(-1, Integer.MIN_VALUE);

            for (int i = 0; i < board.getConfig().getWidth(); i++) {
                try {
                    Board simulatedBoard = new Board(board, i, getCounter());
                    MinimaxResult eval = minimaxWithPruning(simulatedBoard, depth - 1, alpha, beta, false,
                            startTime, timeLimit);

                    if (eval.getScore() > bestResult.getScore() || bestResult.getMove() == -1) {
                        bestResult = new MinimaxResult(i, eval.getScore());
                    }

                    alpha = Math.max(alpha, eval.getScore());

                    if (beta <= alpha) {
                        break;
                    }
                } catch (InvalidMoveException ignored) {
                }
            }
            return bestResult;
        } else {
            MinimaxResult bestResult = new MinimaxResult(-1, Integer.MAX_VALUE);

            for (int i = 0; i < board.getConfig().getWidth(); i++) {
                try {
                    Board simulatedBoard = new Board(board, i, getCounter().getOther());
                    MinimaxResult eval = minimaxWithPruning(simulatedBoard, depth - 1, alpha, beta, true,
                            startTime, timeLimit);

                    if (eval.getScore() < bestResult.getScore() || bestResult.getMove() == -1) {
                        bestResult = new MinimaxResult(i, eval.getScore());
                    }

                    beta = Math.min(beta, eval.getScore());

                    if (beta <= alpha) {
                        break;
                    }
                } catch (InvalidMoveException ignored) {
                }
            }
            return bestResult;
        }
    }

    private int evaluateBoard(GameState gameState) {
        if (gameState.isWin()) {
            return gameState.getWinner() == getCounter() ? Integer.MAX_VALUE : Integer.MIN_VALUE;
        }

        if (gameState.isDraw()) {
            return 0;
        }

        return gameState.getScoreByCounter().get(getCounter()) - gameState.getScoreByCounter().get(getCounter().getOther());
    }
}