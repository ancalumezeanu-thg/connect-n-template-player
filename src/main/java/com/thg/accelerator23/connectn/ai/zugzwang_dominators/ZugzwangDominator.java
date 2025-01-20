package com.thg.accelerator23.connectn.ai.zugzwang_dominators;

import com.thehutgroup.accelerator.connectn.player.Board;
import com.thehutgroup.accelerator.connectn.player.Counter;
import com.thehutgroup.accelerator.connectn.player.Player;
import com.thehutgroup.accelerator.connectn.player.Position;


public class ZugzwangDominator extends Player {
  public ZugzwangDominator(Counter counter) {
    //TODO: fill in your name here
    super(counter, ZugzwangDominator.class.getName());
  }

  @Override
  public int makeMove(Board board) {
    int column = (int) (Math.random() * board.getConfig().getWidth());

    if (board.getCounterAtPosition(new Position(column, board.getConfig().getHeight())) == null) {
      return column;
    } else {
      return makeMove(board);
    }

    //TODO: some crazy analysis
    //TODO: make sure said analysis uses less than 2G of heap and returns within 10 seconds on whichever machine is running it
  }
}
