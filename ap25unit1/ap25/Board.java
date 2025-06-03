package ap25;

import java.util.List;
/**
 * 盤面のインターフェース 
 */
public interface Board {
  int SIZE = 6; //6*6の盤面である
  int LENGTH = SIZE * SIZE;

  Color get(int k);
  Move getMove();
  Color getTurn();
  int count(Color color);
  boolean isEnd();
  Color winner();
  void foul(Color color);
  int score();
  List<Move> findLegalMoves(Color color);
  Board placed(Move move);
  Board flipped();
  Board clone();
}
