package myplayer;

import static ap25.Board.*;
import static ap25.Color.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import ap25.*;

public class CustomPlayer extends ap25.Player {
  static final String MY_NAME = "MY24";
  MyEval eval;
  int depthLimit;
  Move move;
  MyBoard board;

  public CustomPlayer(Color color) {
    this(MY_NAME, color, new MyEval(), 2);
  }

  public CustomPlayer(String name, Color color, MyEval eval, int depthLimit) {
    super(name, color);
    this.eval = eval;
    this.depthLimit = depthLimit;
    this.board = new MyBoard();
  }

  public CustomPlayer(String name, Color color, int depthLimit) {
    this(name, color, new MyEval(), depthLimit);
  }

  public void setBoard(Board board) {
    for (var i = 0; i < LENGTH; i++) {
      this.board.set(i, board.get(i));
    }
  }

  boolean isBlack() { return getColor() == BLACK; }

  public Move think(Board board) {
    this.board = this.board.placed(board.getMove());

    if (this.board.findNoPassLegalIndexes(getColor()).size() == 0) {
      this.move = Move.ofPass(getColor());
    } else {
      var newBoard = isBlack() ? this.board.clone() : this.board.flipped();
      this.move = null;

      maxSearch(newBoard, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, 0);

      this.move = this.move.colored(getColor());
    }

    this.board = this.board.placed(this.move);
    return this.move;
  }

  //maxsearchとminsearchは交互に呼ばれる

  float maxSearch(Board board, float alpha, float beta, int depth) {
    if (isTerminal(board, depth)) return this.eval.value(board);

    var moves = board.findLegalMoves(BLACK);//配置可能な点
    moves = order(moves);

    if (depth == 0)
      this.move = moves.get(0);

    for (var move: moves) {
      var newBoard = board.placed(move);
      float v = minSearch(newBoard, alpha, beta, depth + 1);

      if (v > alpha) {
        alpha = v;
        if (depth == 0)
          this.move = move;
      }

      if (alpha >= beta)
        break;
    }

    return alpha;
  }


  float minSearch(Board board, float alpha, float beta, int depth) {
    
    if (isTerminal(board, depth)) return this.eval.value(board);

    var moves = board.findLegalMoves(WHITE);
    moves = order(moves);

    for (var move: moves) {
      var newBoard = board.placed(move);
      float v = maxSearch(newBoard, alpha, beta, depth + 1);
      beta = Math.min(beta, v);
      if (alpha >= beta) break;
    }
    return beta;
  }
  
  
  boolean isTerminal(Board board, int depth) {//探索の終了の判定
    return board.isEnd() || depth > this.depthLimit;
  }

  List<Move> order(List<Move> moves) {
    var shuffled = new ArrayList<Move>(moves);
    Collections.shuffle(shuffled);
    return shuffled;
  }
}
