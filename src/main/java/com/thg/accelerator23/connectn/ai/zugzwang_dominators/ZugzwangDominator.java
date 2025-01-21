package com.thg.accelerator23.connectn.ai.zugzwang_dominators;

import com.thehutgroup.accelerator.connectn.player.Board;
import com.thehutgroup.accelerator.connectn.player.Counter;
import com.thehutgroup.accelerator.connectn.player.Player;
import com.thehutgroup.accelerator.connectn.player.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ZugzwangDominator extends Player {
  public ZugzwangDominator(Counter counter) {
    //TODO: fill in your name here
    super(counter, ZugzwangDominator.class.getName());
  }

  @Override
  public int makeMove(Board board) {
    //TODO: some crazy analysis
    //TODO: make sure said analysis uses less than 2G of heap and returns within 10 seconds on whichever machine is running it

//    RANDOM COLUMN TO MOVE TO
//    int column = (int) (Math.random() * board.getConfig().getWidth());
//
//    if (board.getCounterAtPosition(new Position(column, board.getConfig().getHeight())) == null) {
//      return column;
//    } else {
//      return makeMove(board);
//    }

      // if player O - even columns (0, 2, 4, 6, 8, 10)
      // if player x - odd columns (1, 3, 5, 7, 9)

    int width = board.getConfig().getWidth();
    int height = board.getConfig().getHeight();

    List<Integer> evenColumns = new ArrayList<>();
    List<Integer> oddColumns = new ArrayList<>();

    for (int col = 0; col < width; col++){
      if (board.hasCounterAtPosition(new Position(col, height))){
        if (col % 2 == 0){
          evenColumns.add(col);
        } else {
          oddColumns.add(col);
        }
      }
    }

    List<Integer> preferredColumns = Counter.O.equals(getCounter()) ? evenColumns : oddColumns;
    List<Integer> fallbackColumns = Counter.O.equals(getCounter()) ? oddColumns : evenColumns;

    if (!preferredColumns.isEmpty()) {
      return preferredColumns.get((int) (Math.random() * preferredColumns.size()));
    } else {
      return fallbackColumns.get((int) (Math.random() * fallbackColumns.size()));
    }
  }

//  public Map<Counter, Integer> checkRunByOpponent(Board board) {
//    return
//  }
}
