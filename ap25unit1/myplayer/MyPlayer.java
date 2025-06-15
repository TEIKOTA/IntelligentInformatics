package myplayer;

import static ap25.Board.*;
import static ap25.Color.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import java.util.Comparator;
import java.util.stream.Collectors;

import ap25.*;

public class MyPlayer extends ap25.Player {
  static final String MY_NAME = "MY24";
  MyEval eval;
  int depthLimit;
  Move move;
  MyBoard board;

  public MyPlayer(Color color) {
    this(MY_NAME, color, new MyEval(), 2);
  }

  public MyPlayer(Color color, MyEval eval) {
    super(MY_NAME, color);
    this.eval = eval;
    this.depthLimit = 2;
    this.board = new MyBoard();
  }

  public MyPlayer(String name, Color color, MyEval eval) {
    this(name, color, new MyEval(), 2);
  }

  public MyPlayer(String name, Color color, int depthLimit) {
    this(name, color, new MyEval(), depthLimit);
  }

  public MyPlayer(Color color, MyEval eval, int depthLimit) {
    super(MY_NAME, color);
    this.eval = eval;
    this.depthLimit = depthLimit;
    this.board = new MyBoard();
  }

  public MyPlayer(String name, Color color, MyEval eval, int depthLimit) {
    super(name, color);
    this.eval = eval;
    this.depthLimit = depthLimit;
    this.board = new MyBoard();
  }

  public void setBoard(Board board) {
    for (var i = 0; i < LENGTH; i++) {
      this.board.set(i, board.get(i));
    }
  }

  boolean isBlack() {
    return getColor() == BLACK;
  }

  public Move think(Board board) {
    this.board = this.board.placed(board.getMove());//盤面を更新

    if (this.board.findNoPassLegalIndexes(getColor()).size() == 0) {//自身の色の合法手の数で判定
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

  float maxSearch(Board board, float alpha, float beta, int depth) {
    if (isTerminal(board, depth))
      return this.eval.value(board);

    var moves = board.findLegalMoves(BLACK);
    moves = orderWhenMax(board, moves);

    if (depth == 0)
      this.move = moves.get(0);

    for (var move : moves) {
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
    if (isTerminal(board, depth))
      return this.eval.value(board);

    var moves = board.findLegalMoves(WHITE);
    moves = orderWhenMin(board, moves);

    for (var move : moves) {
      var newBoard = board.placed(move);
      float v = maxSearch(newBoard, alpha, beta, depth + 1);
      beta = Math.min(beta, v);
      if (alpha >= beta)
        break;
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

  //浅い探索を行いその結果を元に並べ替え

/**
 * 自分のターン（maxノード）：
 * 1手進めたあとの評価値が高い順に並べ替え
 */
  List<Move> orderWhenMax(Board board, List<Move> moves) {
  return moves.stream()
    .map(move -> {
      Board next = board.placed(move);
      float raw = eval.value(next);
      float score = (board.getTurn() == Color.BLACK) ? raw : -raw;
      return new MoveScore(move, score);
    })
    // 降順
    .sorted(Comparator.comparingDouble(ms -> -ms.score))
    .map(ms -> ms.move)
    .toList();
  }
/**
 * 相手のターン（minノード）：
 * 1手進めたあとの評価値が低い順に並べ替え
 */
  List<Move> orderWhenMin(Board board, List<Move> moves) {
    return moves.stream()
      .map(move -> {
        Board next = board.placed(move);
        float raw = eval.value(next);
        float score = (board.getTurn() == Color.BLACK) ? raw : -raw;
        return new MoveScore(move, score);
      })
      // 昇順
      .sorted(Comparator.comparingDouble(ms -> ms.score))
      .map(ms -> ms.move)
      .toList();
  }

  //move orderで使う
  //手とその際のスコアを保持
  static class MoveScore {
    final Move move;
    final float score;
    MoveScore(Move move, float score) {
      this.move = move;
      this.score = score;
    }
  }
}