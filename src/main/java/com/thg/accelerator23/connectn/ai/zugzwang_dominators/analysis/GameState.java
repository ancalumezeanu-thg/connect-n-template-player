package com.thg.accelerator23.connectn.ai.zugzwang_dominators.analysis;

import com.thehutgroup.accelerator.connectn.player.Counter;
import com.thehutgroup.accelerator.connectn.player.GameConfig;

import java.util.ArrayList;
import java.util.Map;

public class GameState {

    private final Map<Counter, Integer> scoreByCounter;
    private boolean win;
    private boolean draw;
    private Counter winner;

    public GameState(Map<Counter, Integer> scoreByCounter, boolean isFull) {
        this.scoreByCounter = scoreByCounter;
        calculateProperties(isFull);
    }

    private void calculateProperties(boolean isFull) {
        win = scoreByCounter.entrySet().stream()
                .anyMatch(e -> e.getValue() == Integer.MAX_VALUE);
        draw = !win && isFull;
        winner = win ? scoreByCounter.entrySet().stream()
                .filter(e -> e.getValue() == Integer.MAX_VALUE)
                .findFirst()
                .get()
                .getKey() : null;
    }

    public boolean isEnd() {
        return win || draw;
    }

    public boolean isWin() {
        return win;
    }

    public boolean isDraw() {
        return draw;
    }

    public Counter getWinner() {
        return winner;
    }

    public Map<Counter, Integer> getScoreByCounter() {
        return scoreByCounter;
    }
}
