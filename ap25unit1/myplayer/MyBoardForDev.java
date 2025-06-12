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
  //Color board[];//ビットボード使うならいらなそう。。？
  Move move = Move.ofPass(NONE);
  long whiteBoard;
  long blackBoard;
  long banArea;
  long allzero  = Long.MAX_VALUE << 36;//全部0のビットマスク
  public MyBoardForDev() {
    //this.board = Stream.generate(() -> NONE).limit(LENGTH).toArray(Color[]::new);
    init();
  }

  MyBoardForDev(long white,long black , Move move) {
    this.whiteBoard = white;
    this.blackBoard = black;
    this.move = move;
  }

  public MyBoardForDev clone() {
    return new MyBoardForDev(this.whiteBoard,this.blackBoard, this.move);
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

  public Color get(int k) { //済
    if ((banArea & (1L << k)) != 0) return BLOCK; //禁止エリアならBLOCKを返す
    if ((whiteBoard & (1L << k)) != 0) return WHITE; //白のビットが立っていればWHITEを返す
    if ((blackBoard & (1L << k)) != 0) return BLACK; //黒のビットが立っていればBLACKを返す
    return NONE; //どちらのビットも立っていなければNONEを返す　おかしい挙動
  }//盤上の色を取得
  public Move getMove() { return this.move; }

  public Color getTurn() {
    return this.move.isNone() ? BLACK : this.move.getColor().flipped();
  }

  public void set(int k, Color color) {//済
    //this.board[k] = color;//bitboardを使うならいらなそう
    if(color == BLACK) {
      this.blackBoard |= (1L << k);//or演算で追加

    } else if(color == WHITE) {
      this.whiteBoard |= (1L << k);
    }
  }

  public boolean equals(long black,long white) {//済
    return (this.blackBoard == black)  && (this.whiteBoard == white);
  }

  public String toString() {
    return MyBoardFormatter.format(this);//Myboardformatterの書き換えが必要
  }

  public int count(Color color) {//済
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

  public void foul(Color color) {//反則した人の色がcolor
    //反則をした色の色を反転させて、全てのマスをその色にする
    var winner = color.flipped();
    if(winner == BLACK){
      
    }else if(winner == WHITE){

    }
    //IntStream.range(0, LENGTH).forEach(k -> this.board[k] = winner);
  }

  public int score() {//済
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

  public List<Move> findLegalMoves(Color color) {//（マスと色）のリスト
    //findLegalIndexesの修正をして  配置可能なマスだけ1のビットマップをもらうだけでもよさそう
    return findLegalIndexes(color).stream()
        .map(k -> new Move(k, color)).toList();
  }

  // List<Integer> findLegalIndexes(Color color) {//おける位置のインデックス
  //   var moves = findNoPassLegalIndexes(color);
  //   if (moves.size() == 0) moves.add(Move.PASS);
  //   return moves;
  // }

  List<Integer> findLegalIndexes(Color color){
    var moves = new ArrayList<Integer>(); 
    long LRwatchmask = 0x79E79E79EL; //端にLつけないといけなかった.....
    long UDwatchmask = 0x3FFFFFC0L; //上下のマスク
    long diag1watchmask = 0x1E79E780L; //左上から右下の斜めのマスク

    long monban;
    long tmp;
    long emptyArea = ~(whiteBoard | blackBoard | banArea);//何もないエリア
    long result;
    if(color == BLACK){
      monban = LRwatchmask & whiteBoard;
      tmp = LRwatchmask & (blackBoard>>1);
    }else{
      monban = LRwatchmask & blackBoard;
      tmp = whiteBoard;  
    }

    //横の探索
    monban = LRwatchmask & (color == BLACK ? (whiteBoard>>1) : (blackBoard>>1));
    tmp |= monban & (tmp >> 1);
    tmp |= monban & (tmp >> 1);
    tmp |= monban & (tmp >> 1);
    result = emptyArea & (tmp>>1);
    
    monban = UDwatchmask & (color == BLACK ? whiteBoard<<1 : blackBoard<<1);
    tmp |= monban & (tmp << 1);
    tmp |= monban & (tmp << 1);
    tmp |= monban & (tmp << 1);
    result |= emptyArea & (tmp << 1);

    //縦の探索
    monban = UDwatchmask & (color == BLACK ? whiteBoard<<6 : blackBoard<<6);
    tmp |= monban & (tmp << 6);
    tmp |= monban & (tmp << 6);
    tmp |= monban & (tmp << 6);
    result |= emptyArea & (tmp << 6);
    
    monban = UDwatchmask & (color == BLACK ? whiteBoard >>6 : blackBoard>>6);   
    tmp |= monban & (tmp >> 6);
    tmp |= monban & (tmp >> 6);
    tmp |= monban & (tmp >> 6);
    result |= emptyArea & (tmp >> 6);

    //斜めの探索
    monban = diag1watchmask & (color == BLACK ? whiteBoard>>7 : blackBoard>>7);
    tmp |= monban & (tmp >> 7);
    tmp |= monban & (tmp >> 7);
    tmp |= monban & (tmp >> 7);
    result |= emptyArea & (tmp >> 7);
    
    monban = UDwatchmask & (color == BLACK ? whiteBoard>>5 : blackBoard>>5);
    tmp |= monban & (tmp >> 5);
    tmp |= monban & (tmp >> 5);
    tmp |= monban & (tmp >> 5);
    result |= emptyArea & (tmp >> 5);
    
    monban = UDwatchmask & (color == BLACK ? whiteBoard<<5 : blackBoard<<5);
    tmp |= monban & (tmp << 5);
    tmp |= monban & (tmp << 5);
    tmp |= monban & (tmp << 5);
    result |= emptyArea & (tmp << 5);

    monban = UDwatchmask & (color == BLACK ? whiteBoard <<7 : blackBoard<<7);
    tmp |= monban & (tmp << 7);
    tmp |= monban & (tmp << 7);
    tmp |= monban & (tmp << 7);
    result |= emptyArea & (tmp << 7);

    while (tmp != 0) {
      int k = Long.numberOfTrailingZeros(tmp);
      moves.add(k);
      tmp &= ~(1L << k); // kのビットを0にする
    }
    return moves;
  }

  List<Integer> findNoPassLegalIndexes(Color color) {//挟めるインデックスの探索
    var moves = new ArrayList<Integer>();
    for (int k = 0; k < LENGTH; k++) {
      long c =  (whiteBoard | blackBoard) & (1L << k);
      if (c != 0) continue;
      for (var line : lines(k)) {
        var outflanking = outflanked(line, color);
        if (outflanking.size() > 0) moves.add(k);
      }
    }
    //outflankingとlinesを修正すればよさそう
    return moves;
  }

  List<List<Integer>> lines(int k) {//指定した位置kからの8方向のラインを取得
    var lines = new ArrayList<List<Integer>>();//0から7の配列
    for (int dir = 0; dir < 8; dir++) {
      var line = Move.line(k, dir);   
      lines.add(line);
    }
    return lines;
  }

  List<Move> outflanked(List<Integer> line, Color color) {//
    if (line.size() <= 1) return new ArrayList<Move>();
    var flippables = new ArrayList<Move>();
    for (int k: line) {
      var c = get(k);//色の取得
      if (c == NONE || c == BLOCK) break;
      if (c == color) return flippables;
      flippables.add(new Move(k, color));
    }
    return new ArrayList<Move>();
  }

  public MyBoardForDev placed(Move move) {//moveは
    var b = clone();
    b.move = move;

    if (move.isPass() | move.isNone())
      return b;

    var k = move.getIndex();
    var color = move.getColor();
    var lines = b.lines(k);
    for (var line: lines) {
      for (var p: outflanked(line, color)) {
        //b.board[p.getIndex()] = color;
        if(color == BLACK ){
          blackBoard |= (1L << p.getIndex());
          whiteBoard |= (0L << p.getIndex());
        }else if(color == WHITE){
          blackBoard |= (0L << p.getIndex());
          whiteBoard |= (1L << p.getIndex());
        }
      }
    }
    b.set(k, color);

    return b;
  }

  public MyBoardForDev flipped() {//
    var b = clone();
    long tmp = b.whiteBoard;
    b.whiteBoard = b.blackBoard;
    b.blackBoard = tmp;
    b.move = this.move.flipped();
    return b;
  }
}
