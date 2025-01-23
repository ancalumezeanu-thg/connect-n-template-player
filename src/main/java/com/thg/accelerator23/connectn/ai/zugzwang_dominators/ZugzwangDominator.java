package com.thg.accelerator23.connectn.ai.zugzwang_dominators;

import com.thehutgroup.accelerator.connectn.player.*;
import com.thg.accelerator23.connectn.ai.zugzwang_dominators.analysis.BoardAnalyser;
import com.thg.accelerator23.connectn.ai.zugzwang_dominators.analysis.GameState;

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

        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;
        int bestScore = Integer.MIN_VALUE;
        int bestMove = -1;

        for (int i = 0; i < board.getConfig().getWidth(); i++) {
            try {
                Board simulatedBoard = new Board(board, i, getCounter());
                int score = minimaxWithPruning(simulatedBoard, MAX_DEPTH, alpha, beta, true);

                System.out.println(alpha + " " + beta + " " + score);

                if (score > bestScore || bestMove == -1) {
                    bestScore = score;
                    bestMove = i;
                }
            } catch (InvalidMoveException ignored) {
            }
        }

        return bestMove;
    }

    private int minimaxWithPruning(Board board, int depth, int alpha, int beta, boolean isMaximizingPlayer) {
        BoardAnalyser boardAnalyser = new BoardAnalyser(board.getConfig());
        GameState gameState = boardAnalyser.calculateGameState(board);

        if (depth == 0 || gameState.isWin() || gameState.isDraw()) {
            int a = evaluateBoard(board, boardAnalyser, gameState);
            System.out.println("Will evaluate board " + a);

            return a;
        }

        if (isMaximizingPlayer) {
            int maxEval = Integer.MIN_VALUE;

            for (int i = 0; i < board.getConfig().getWidth(); i++) {
                try {
                    Board simulatedBoard = new Board(board, i, getCounter());
                    int eval = minimaxWithPruning(simulatedBoard, depth - 1, alpha, beta, false);
                    maxEval = Math.max(maxEval, eval);
                    alpha = Math.max(alpha, eval);

                    if (beta <= alpha) {
                        break;
                    }
                } catch (InvalidMoveException ignored) {
                }
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;

            for (int i = 0; i < board.getConfig().getWidth(); i++) {
                try {
                    Board simulatedBoard = new Board(board, i, getCounter().getOther());
                    int eval = minimaxWithPruning(simulatedBoard, depth - 1, alpha, beta, true);
                    minEval = Math.min(minEval, eval);
                    beta = Math.min(beta, eval);

                    if (beta <= alpha) {
                        break;
                    }
                } catch (InvalidMoveException ignored) {
                }
            }
            return minEval;
        }
    }

    private int evaluateBoard(Board board, BoardAnalyser boardAnalyser, GameState gameState) {
        if (gameState.isWin()) {
            return gameState.getWinner() == getCounter() ? Integer.MAX_VALUE : Integer.MIN_VALUE;
        }
        if (gameState.isDraw()) {
            return 0;
        }

        return boardAnalyser.getScore(board, getCounter());
    }
}