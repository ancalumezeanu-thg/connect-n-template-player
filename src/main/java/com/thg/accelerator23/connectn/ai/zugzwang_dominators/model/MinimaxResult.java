package com.thg.accelerator23.connectn.ai.zugzwang_dominators.model;

public class MinimaxResult {
    int move;
    int score;

    public MinimaxResult(int move, int score) {
        this.move = move;
        this.score = score;
    }

    public int getMove() {
        return move;
    }

    public int getScore() {
        return score;
    }

    @Override
    public String toString() {
        return "MinimaxResult{" +
                "move=" + move +
                ", score=" + score +
                '}';
    }
}
