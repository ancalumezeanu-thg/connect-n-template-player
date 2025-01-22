package com.thg.accelerator23.connectn.ai.zugzwang_dominators;

import com.thehutgroup.accelerator.connectn.player.*;
import com.thg.accelerator23.connectn.ai.zugzwang_dominators.analysis.BoardAnalyser;
import com.thg.accelerator23.connectn.ai.zugzwang_dominators.analysis.GameState;
import com.thg.accelerator23.connectn.ai.zugzwang_dominators.exception.NoMoveFoundException;


public class ZugzwangDominator extends Player {
    public ZugzwangDominator(Counter counter) {
        //TODO: fill in your name here
        super(counter, ZugzwangDominator.class.getName());
    }

    @Override
    public int makeMove(Board board) {
        //TODO: some crazy analysis
        //TODO: make sure said analysis uses less than 2G of heap and returns within 10 seconds on whichever machine is running it

        try {
            return findWinningMove(board);
        } catch (NoMoveFoundException ignored){
        }

        try {
            return findBlockingMove(board);
        } catch (NoMoveFoundException ignored){
        }

        try {
            return maximiseBestRun(board);
        } catch (NoMoveFoundException ignored) {
        }

        try {
            return blockOpponentFork(board);
        } catch (NoMoveFoundException ignored) {
        }

        return generateRandomMove(board);
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
            if (maxBestRun > gameStateMaxRun) {
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
        int move = generateRandomMove(board);

        try {
            for (int i = 0; i < board.getConfig().getWidth(); i++){
                Board futureOpponentBoard = new Board(board, i, getCounter().getOther());
                int opponentFork = countWinningMoves(futureOpponentBoard, getCounter().getOther());

                if (opponentFork > 1) {
                    move = i;
                }
            }
        } catch (InvalidMoveException ignored) {}

        return move;
    }
}
