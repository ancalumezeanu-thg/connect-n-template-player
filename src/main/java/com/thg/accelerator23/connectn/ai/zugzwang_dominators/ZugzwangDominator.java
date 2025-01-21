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

        return generateRandomMove(board);

        // if player O - even columns (0, 2, 4, 6, 8, 10)
        // if player x - odd columns (1, 3, 5, 7, 9)

//        List<Integer> evenColumns = new ArrayList<>();
//        List<Integer> oddColumns = new ArrayList<>();
//
//        for (int col = 0; col < width; col++) {
//            if (board.hasCounterAtPosition(new Position(col, height))) {
//                if (col % 2 == 0) {
//                    evenColumns.add(col);
//                } else {
//                    oddColumns.add(col);
//                }
//            }
//        }
//
//        List<Integer> preferredColumns = Counter.O.equals(getCounter()) ? evenColumns : oddColumns;
//        List<Integer> fallbackColumns = Counter.O.equals(getCounter()) ? oddColumns : evenColumns;
//
//        if (!preferredColumns.isEmpty()) {
//            return preferredColumns.get((int) (Math.random() * preferredColumns.size()));
//        } else {
//            return fallbackColumns.get((int) (Math.random() * fallbackColumns.size()));
//        }
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
}
