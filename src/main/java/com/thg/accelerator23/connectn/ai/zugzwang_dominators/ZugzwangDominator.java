package com.thg.accelerator23.connectn.ai.zugzwang_dominators;

import com.thehutgroup.accelerator.connectn.player.*;
import com.thg.accelerator23.connectn.ai.zugzwang_dominators.analysis.BoardAnalyser;
import com.thg.accelerator23.connectn.ai.zugzwang_dominators.analysis.GameState;
import com.thg.accelerator23.connectn.ai.zugzwang_dominators.exception.NoMoveFoundException;

import java.util.ArrayList;
import java.util.List;
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
            return findWinningMove(board);
        } catch (NoMoveFoundException ignored){
        }

        try {
            return findBlockingMove(board);
        } catch (NoMoveFoundException ignored){
        }

        int bestMove = findBestMoveUsingMinimax(board);
        System.currentTimeMillis();
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
        List<Integer> playableColumns = new ArrayList<>();
        int height = board.getConfig().getHeight();

        for (int i = 0; i < board.getConfig().getWidth(); i++) {
            int topRow = height - 1;
            Position positionToCheck = new Position(i, topRow);

            if (board.getCounterAtPosition(positionToCheck) == null) {
                playableColumns.add(i);
            }
        }

        return playableColumns.get((int) (Math.random() * playableColumns.size()));
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
    public boolean isColumnPlayable(int col, Board board) {
        int topRow = board.getConfig().getHeight() - 1;
        Position positionToCheck = new Position(col, topRow);
        return board.isWithinBoard(positionToCheck) && board.getCounterAtPosition(new Position(col, topRow)) == null;
    }

    private int findBestMoveUsingMinimax(Board board) {
        int bestMove = -1;
        int bestScore = Integer.MIN_VALUE;

        for (int i = 0; i < board.getConfig().getWidth(); i++) {
            if (isColumnPlayable(i, board)) {
                try {
                    Board simulatedBoard = new Board(board, i, getCounter());
                    int score = minimaxWithPruning(simulatedBoard, MAX_DEPTH, Integer.MIN_VALUE,
                            Integer.MAX_VALUE, false);

                    if (score > bestScore) {
                        bestScore = score;
                        bestMove = i;
                    }
                } catch (InvalidMoveException ignored) {
                }
            }
        }

        return bestMove;
    }

    private int minimaxWithPruning(Board board, int depth, int alpha, int beta, boolean isMaximizingPlayer) {
        BoardAnalyser boardAnalyser = new BoardAnalyser(board.getConfig());
        GameState gameState = boardAnalyser.calculateGameState(board);

        if (depth == 0 || gameState.isWin() || gameState.isDraw()) {
            return evaluateBoard(board, gameState);
        }

        if (isMaximizingPlayer) {
            int maxEval = Integer.MIN_VALUE;

            for (int i = 0; i < board.getConfig().getWidth(); i++) {
                if (isColumnPlayable(i, board)) {
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
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;

            for (int i = 0; i < board.getConfig().getWidth(); i++) {
                if (isColumnPlayable(i, board)) {
                    try {
                        Board childBoard = new Board(board, i, getCounter().getOther());
                        int eval = minimaxWithPruning(childBoard, depth - 1, alpha, beta, true);
                        minEval = Math.min(minEval, eval);
                        beta = Math.min(beta, eval);

                        if (beta <= alpha) {
                            break;
                        }
                    } catch (InvalidMoveException ignored) {
                    }
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

        int score = 0;

        // update score as per existing board
        for (Map.Entry<Counter, Integer> entry : gameState.getMaxInARowByCounter().entrySet()) {
            if (entry.getKey() == getCounter()) {
                score += entry.getValue();
            } else {
                score -= entry.getValue();
            }
        }

        score += evaluateForPotentialWinningMoves(board, getCounter());

        score -= evaluateForPotentialWinningMoves(board, getCounter().getOther());

        return score;
    }

    // SCORING SYSTEM
    private int evaluateForPotentialWinningMoves(Board board, Counter counter) {
        int score = 0;

        // go through columns to check 2-in-a-rows or 3-in-a-rows
        for (int col = 0; col < board.getConfig().getWidth(); col++) {
            for (int row = 0; row < board.getConfig().getHeight(); row++) {
                Position position = new Position(col, row);
                if (board.getCounterAtPosition(position) == null) {
                    int rowCount = countInARow(position, counter, board);
                    if (rowCount == 2) {
                        score += 10;
                    } else if (rowCount == 3) {
                        score += 100;
                    }
                }
            }
        }

        return score;
    }

    public int countInARow(Position position, Counter counter, Board board) {
        int maxCount = 0;

        int[] dx = {1, 0, 1, -1};  // X direction (right, none, right-up, right-down)
        int[] dy = {0, 1, 1, 1};   // Y direction (none, down, up-right, down-left)

        for (int direction = 0; direction < 4; direction++) {
            int count = 1; // Start with 1 since we're counting the piece at the position itself
            int x = position.getX();
            int y = position.getY();

            // Look in the positive direction (right, down, up-right, down-left)
            int nx = x + dx[direction];
            int ny = y + dy[direction];

            while (isWithinBounds(nx, ny, board) && board.getCounterPlacements()[nx][ny] != null
                    && board.getCounterPlacements()[nx][ny].equals(counter)) {
                count++;
                nx += dx[direction];
                ny += dy[direction];
            }

            // Look in the negative direction (left, up, down-left, up-right)
            nx = x - dx[direction];
            ny = y - dy[direction];

            while (isWithinBounds(nx, ny, board) && board.getCounterPlacements()[nx][ny] != null
                    && board.getCounterPlacements()[nx][ny].equals(counter)) {
                count++;
                nx -= dx[direction];
                ny -= dy[direction];
            }

            maxCount = Math.max(maxCount, count);
        }

        return maxCount;
    }

    private boolean isWithinBounds(int x, int y, Board board) {
        return x >= 0 && x < board.getCounterPlacements().length
                && y >= 0 && y < board.getCounterPlacements()[0].length;
    }
}
