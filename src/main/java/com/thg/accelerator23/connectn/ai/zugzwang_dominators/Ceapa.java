package com.thg.accelerator23.connectn.ai.zugzwang_dominators;

import com.thehutgroup.accelerator.connectn.player.Board;
import com.thehutgroup.accelerator.connectn.player.Counter;
import com.thehutgroup.accelerator.connectn.player.GameConfig;

public class Ceapa {

    public static void main(String[] args) {
        ZugzwangDominator p1 = new ZugzwangDominator(Counter.O);
        ZugzwangDominator p2 = new ZugzwangDominator(Counter.X);

        Board board = new Board(new GameConfig(10, 8, 4));

        p1.makeMove(board);
    }
}
