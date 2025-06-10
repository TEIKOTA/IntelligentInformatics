package myplayer;

import static ap25.Color.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import ap25.*;

public class MyBoardForDev implements Board, Cloneable {
  Color board[];//ビットボード使うならいらなそう。。？
  Move move = Move.ofPass(NONE);
  long whiteBoard;
  long blackBoard;
  long banArea;
  
  public MyBoardForDev() {
    this.board = Stream.generate(() -> NONE).limit(LENGTH).toArray(Color[]::new);
    init();
  }

  MyBoardForDev(Color board[], Move move) {
    this.board = Arrays.copyOf(board, board.length);
    this.move = move;
  }

  public MyBoard clone() {
    return new MyBoard(this.board, this.move);
  }

  void init() {
    set(Move.parseIndex("c3"), BLACK);
    set(Move.parseIndex("d4"), BLACK);
    set(Move.parseIndex("d3"), WHITE);
    set(Move.parseIndex("c4"), WHITE);
  }
  //禁止エリアありよう
  void init(long banArea) {
    init();
    //禁止エリアの設定
    //this.banArea = banArea;
  }

  public Color get(int k) { 
    if ((banArea & (1L << k)) != 0) return BLOCK; //禁止エリアならBLOCKを返す
    if ((whiteBoard & (1L << k)) != 0) return WHITE; //白のビットが立っていればWHITEを返す
    if ((blackBoard & (1L << k)) != 0) return BLACK; //黒のビットが立っていればBLACKを返す
    return NONE; //どちらのビットも立っていなければNONEを返す　おかしい挙動
  }//盤上の色を取得
  public Move getMove() { return this.move; }

  public Color getTurn() {
    return this.move.isNone() ? BLACK : this.move.getColor().flipped();
  }

  public void set(int k, Color color) {
    this.board[k] = color;//bitboardを使うならいらなそう
    if(color == BLACK) {
      this.blackBoard |= (1L << k);//or演算で追加

    } else if(color == WHITE) {
      this.whiteBoard |= (1L << k);
    }
  }

  public boolean equals(Object otherObj) {
    if (otherObj instanceof MyBoardForDev) {
      var other = (MyBoardForDev) otherObj;
      return Arrays.equals(this.board, other.board);
    }
    return false;
  }

  public String toString() {
    return MyBoardFormatter.format(this);//Myboardformatterの書き換えが必要
  }

  public int count(Color color) {
    if(color == BLACK) return Long.bitCount(this.blackBoard);
    if(color == WHITE) return Long.bitCount(this.whiteBoard);
    return -1; // NONE or BLOCK
    //return countAll().getOrDefault(color, 0L).intValue();
  }

  public boolean isEnd() {//ゲーム終了の判定
    var lbs = findNoPassLegalIndexes(BLACK);
    var lws = findNoPassLegalIndexes(WHITE);
    return lbs.size() == 0 && lws.size() == 0;
  }

  public Color winner() {
    var v = score();
    if (isEnd() == false || v == 0 ) return NONE;
    return v > 0 ? BLACK : WHITE;
  }

  public void foul(Color color) {//反則処理
    //反則をした色の色を反転させて、全てのマスをその色にする
    var winner = color.flipped();
    IntStream.range(0, LENGTH).forEach(k -> this.board[k] = winner);
  }

  public int score() {
    var cs = countAll();//csは色ごとのカウントを持つMap
    var bs = cs.getOrDefault(BLACK, 0L);//getOrDefaultはキーが存在しない場合、デフォルト値を返す
    var ws = cs.getOrDefault(WHITE, 0L);
    var ns = LENGTH - bs - ws;
    int score = (int) (bs - ws);

    if (bs == 0 || ws == 0)
        score += Integer.signum(score) * ns;

    return score;
  }

  Map<Color, Long> countAll() {//キーが色、値がlong  ゲーム終了でしか呼ばれていないからいらないかも
    long blackCount = Long.bitCount(this.blackBoard);
    long whiteCount = Long.bitCount(this.whiteBoard);
    Map<Color, Long> counts = Map.of(
        BLACK, blackCount,
        WHITE, whiteCount
    );
    return counts;
    // 旧実装
    // return Arrays.stream(this.board).collect(
    //     Collectors.groupingBy(Function.identity(), Collectors.counting()));//色ごとに総計
  }

  public List<Move> findLegalMoves(Color color) {
    return findLegalIndexes(color).stream()
        .map(k -> new Move(k, color)).toList();
  }

  List<Integer> findLegalIndexes(Color color) {
    var moves = findNoPassLegalIndexes(color);
    if (moves.size() == 0) moves.add(Move.PASS);
    return moves;
  }

  List<Integer> findNoPassLegalIndexes(Color color) {
    var moves = new ArrayList<Integer>();
    for (int k = 0; k < LENGTH; k++) {
      var c = this.board[k];
      if (c != NONE) continue;
      for (var line : lines(k)) {
        var outflanking = outflanked(line, color);
        if (outflanking.size() > 0) moves.add(k);
      }
    }
    return moves;
  }

  List<List<Integer>> lines(int k) {//指定した位置kからの8方向のラインを取得
    var lines = new ArrayList<List<Integer>>();
    for (int dir = 0; dir < 8; dir++) {
      var line = Move.line(k, dir);
      lines.add(line);
    }
    return lines;
  }

  List<Move> outflanked(List<Integer> line, Color color) {//outflankedは挟まれているかどうか
    if (line.size() <= 1) return new ArrayList<Move>();
    var flippables = new ArrayList<Move>();
    for (int k: line) {
      var c = get(k);
      if (c == NONE || c == BLOCK) break;
      if (c == color) return flippables;
      flippables.add(new Move(k, color));
    }
    return new ArrayList<Move>();
  }

  public MyBoard placed(Move move) {//moveは
    var b = clone();
    b.move = move;

    if (move.isPass() | move.isNone())
      return b;

    var k = move.getIndex();
    var color = move.getColor();
    var lines = b.lines(k);
    for (var line: lines) {
      for (var p: outflanked(line, color)) {
        b.board[p.getIndex()] = color;
      }
    }
    b.set(k, color);

    return b;
  }

  public MyBoard flipped() {
    var b = clone();
    IntStream.range(0, LENGTH).forEach(k -> b.board[k] = b.board[k].flipped());
    b.move = this.move.flipped();
    return b;
  }
}
