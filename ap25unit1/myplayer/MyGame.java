package myplayer;

import ap25.*;
import static ap25.Color.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.*;

// 画面出力なしの分析時用に画面出力は以下を代替して
// System.out.println() -> info()
// System.err.println() -> error()

public class MyGame {
  public static void main(String args[]) {
    if(args.length > 0){
      if (args[0].equals("former")) {
        var player1 = new myplayer.MyPlayer(BLACK);
        var player2 = new myplayer.RandomPlayer(WHITE);
        var board = new MyBoard();
        var game = new MyGameForDev(board, player1, player2);
        game.play();
        return;
      } else if (args[0].equals("latter")) {
        var player1 = new myplayer.MyPlayer(WHITE);
        var player2 = new myplayer.RandomPlayer(BLACK);
        var board = new MyBoard();
        var game = new MyGameForDev(board, player1, player2);
        game.play();
        return;
      }
    }
    var player1 = new myplayer.MyPlayer(BLACK);
    var player2 = new myplayer.RandomPlayer(WHITE);
    var board = new MyBoard();
    var game = new MyGame(board, player1, player2);
    game.play();
    
  }

  static final float TIME_LIMIT_SECONDS = 60;

  Board board;
  Player black;
  Player white;
  Map<Color, Player> players;
  List<Move> moves = new ArrayList<>();
  Map<Color, Float> times = new HashMap<>(Map.of(BLACK, 0f, WHITE, 0f));

  public MyGame(Board board, Player black, Player white) {
    this.board = board.clone();
    this.black = black;
    this.white = white;
    this.players = Map.of(BLACK, black, WHITE, white);
  }

  public void play() {
    this.players.values().forEach(p -> p.setBoard(this.board.clone()));

    while (this.board.isEnd() == false) {
      var turn = this.board.getTurn();
      var player = this.players.get(turn);

      Error error = null;
      long t0 = System.currentTimeMillis();
      Move move;

      // play
      try {
        move = player.think(board.clone()).colored(turn);
      } catch (Error e) {
        error = e;
        move = Move.ofError(turn);
      }

      // record time
      long t1 = System.currentTimeMillis();
      final var t = (float) Math.max(t1 - t0, 1) / 1000.f;
      this.times.compute(turn, (k, v) -> v + t);

      // check
      move = check(turn, move, error);
      moves.add(move);

      // update board
      if (move.isLegal()) {
        board = board.placed(move);
      } else {
        board.foul(turn);
        break;
      }

      info(board);
    }

    printResult(board, moves);
  }

  Move check(Color turn, Move move, Error error) {
    if (move.isError()) {
      error(String.format("error: %s %s", turn, error));
      error(board);
      return move;
    }

    if (this.times.get(turn) > TIME_LIMIT_SECONDS) {
      error(String.format("timeout: %s %.2f", turn, this.times.get(turn)));
      error(board);
      return Move.ofTimeout(turn);
    }

    var legals = board.findLegalMoves(turn);
    if (move == null || legals.contains(move) == false) {
      error(String.format("illegal move: %s %s", turn, move));
      error(board);
      return Move.ofIllegal(turn);
    }

    return move;
  }

  public Player getWinner(Board board) {
    Player winner = this.players.get(board.winner());
    appendToFile("result.csv", winner.toString());
    return winner;
  }

  public void printResult(Board board, List<Move> moves) {
    var result = String.format("%5s%-9s", "", "draw");
    var score = Math.abs(board.score());
    if (score > 0){
      result = String.format("%-4s won by %-2d", getWinner(board), score);
    }else{
      appendToFile("result.csv", "DRAW");
    }
    var s = toString() + " -> " + result + "\t| " + toString(moves);
    info(s);
  }

  public String toString() {
    return String.format("%4s vs %4s", this.black, this.white);
  }

  public static String toString(List<Move> moves) {
    return moves.stream().map(x -> x.toString()).collect(Collectors.joining());
  }

  //勝敗記録のためだけに追加したメソッド
  public static void appendToFile(String filename, String text) {
    try {
      Files.write(
        Paths.get(filename),
        (text + System.lineSeparator()).getBytes(),
        StandardOpenOption.CREATE,
        StandardOpenOption.APPEND
      );
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /** General output (info-level) */
  protected void info(Object msg) {
    System.out.println(msg);
  }
  /** Error-level output */
  protected void error(Object msg) {
    System.err.println(msg);
  }
}
