package com.thg.accelerator23.connectn.ai.zugzwang_dominators.analysis;

import com.thehutgroup.accelerator.connectn.player.Board;
import com.thehutgroup.accelerator.connectn.player.Counter;
import com.thehutgroup.accelerator.connectn.player.GameConfig;
import com.thehutgroup.accelerator.connectn.player.Position;
import com.thg.accelerator23.connectn.ai.zugzwang_dominators.model.Line;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class BoardAnalyser {
    private final Map<Function<Position, Position>, List<Position>> positionsByFunction;

    public BoardAnalyser(GameConfig config) {
        positionsByFunction = new HashMap<>();
        List<Position> leftEdge = IntStream.range(0, config.getHeight())
                .mapToObj(i -> new Position(0, i))
                .collect(Collectors.toList());
        List<Position> bottomEdge = IntStream.range(0, config.getWidth())
                .mapToObj(i -> new Position(i, 0))
                .collect(Collectors.toList());
        List<Position> rightEdge = leftEdge.stream()
                .map(p -> new Position(config.getWidth() - 1, p.getY()))
                .toList();

        List<Position> leftBottom = Stream.concat(leftEdge.stream(),
                bottomEdge.stream()).distinct().collect(Collectors.toList());
        List<Position> rightBottom = Stream.concat(rightEdge.stream(),
                bottomEdge.stream()).distinct().collect(Collectors.toList());

        Function<Position, Position> hMover = p -> new Position(p.getX() + 1, p.getY());
        positionsByFunction.put(hMover, leftEdge);

        Function<Position, Position> vMover = p -> new Position(p.getX(), p.getY() + 1);
        positionsByFunction.put(vMover, bottomEdge);

        Function<Position, Position> diagUpRightMover = hMover.compose(vMover);
        positionsByFunction.put(diagUpRightMover, leftBottom);

        Function<Position, Position> diagUpLeftMover = p -> new Position(p.getX() - 1, p.getY() + 1);
        positionsByFunction.put(diagUpLeftMover, rightBottom);
    }

    public GameState calculateGameState(Board board) {
        Map<Counter, Integer> overallScoreByCounter = new HashMap<>();
        overallScoreByCounter.put(Counter.O, 0);
        overallScoreByCounter.put(Counter.X, 0);

        for (Line line : getLines(board)) {
            Map<Counter, Integer> scoreInLineByCounter = getScoreByCounter(line, board.getConfig().getnInARowForWin());

            for (Counter counter : Counter.values()) {
                if (scoreInLineByCounter.get(counter) == Integer.MAX_VALUE) {
                    overallScoreByCounter.put(counter, Integer.MAX_VALUE);
                } else {
                    overallScoreByCounter.put(counter, overallScoreByCounter.get(counter) + scoreInLineByCounter.get(counter));
                }
            }

            if (overallScoreByCounter.get(Counter.O) == Integer.MAX_VALUE || overallScoreByCounter.get(Counter.X) == Integer.MAX_VALUE) {
                break;
            }
        }

        boolean boardFull = isBoardFull(board);
        return new GameState(overallScoreByCounter, boardFull);
    }

    private boolean isBoardFull(Board board) {
        return IntStream.range(0, board.getConfig().getWidth())
                .allMatch(
                        i -> board.hasCounterAtPosition(new Position(i, board.getConfig().getHeight() - 1)));
    }

    private List<Line> getLines(Board board) {
        ArrayList<Line> lines = new ArrayList<>();
        for (Map.Entry<Function<Position, Position>, List<Position>> entry : positionsByFunction
                .entrySet()) {
            Function<Position, Position> function = entry.getKey();
            List<Position> startPositions = entry.getValue();
            lines.addAll(startPositions.stream().map(p -> new BoardLine(board, p, function))
                    .toList());
        }
        return lines;
    }

    private Map<Counter, Integer> getScoreByCounter(Line line, int nInARowForWin) {
        HashMap<Counter, Integer> scoreByCounter = new HashMap<>();
        for (Counter c : Counter.values()) {
            scoreByCounter.put(c, 0);
        }

        Counter current = null;
        int currentRunLength = 0;
        while (line.hasNext()) {
            Counter next = line.next();
            if (current != next) {
                if (current != null) {
                    if (currentRunLength >= 2 && scoreByCounter.get(current) != Integer.MAX_VALUE) {
                        if (currentRunLength >= nInARowForWin) {
                            scoreByCounter.put(current, Integer.MAX_VALUE);
                            break;
                        } else {
                            scoreByCounter.put(current, (int) (scoreByCounter.get(current) + Math.pow(10, currentRunLength - 1)));
                        }
                    }
                }
                currentRunLength = 1;
                current = next;
            } else {
                currentRunLength++;
            }
        }

        if (current != null && currentRunLength >= 2 && scoreByCounter.get(current) != Integer.MAX_VALUE) {
            if (currentRunLength >= nInARowForWin) {
                scoreByCounter.put(current, Integer.MAX_VALUE);
            } else {
                scoreByCounter.put(current, (int) (scoreByCounter.get(current) + Math.pow(10, currentRunLength - 1)));
            }
        }

        return scoreByCounter;
    }
}
