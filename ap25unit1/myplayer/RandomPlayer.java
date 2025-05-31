package myplayer;

import ap25.*;
import java.util.Random;

public class RandomPlayer extends Player {
  Random rand = new Random();

  public RandomPlayer(Color color) {
    super("R", color);
  }

  @Override
  public Move think(Board board) {
    var moves = board.findLegalMoves(getColor()); //動かせる場所のListを取得
    var i = this.rand.nextInt(moves.size());  //石を置ける範疇のランダムな整数を取得
    return moves.get(i);  //石を置けるマスのうちランダムな所に石を置く
  }
}
