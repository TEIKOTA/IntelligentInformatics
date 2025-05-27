package myplayer;

import ap25.*;
import java.util.Random;

public class RandomPlayer extends Player {
  Random rand = new Random();

  public RandomPlayer(Color color) {
    super("R", color);
  }

  public Move think(Board board) {
    var moves = board.findLegalMoves(getColor());
    var i = this.rand.nextInt(moves.size());
    return moves.get(i);
  }
}
