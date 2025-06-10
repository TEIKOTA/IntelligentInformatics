package ap25;

import static ap25.Board.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * -1はパス、
 * -10は時間切れ、
 * -20は置けない所に置く、
 * -30はエラー発生のペナルティを表します。
 * indexは0から35の値で、8 * 列 + 行の形式で手を表現
 */
public class Move {
  //パス,時間切れ,置けない所に置く,エラー発生のペナルティ
  public final static int PASS = -1;
  final static int TIMEOUT = -10;
  final static int ILLEGAL = -20;
  final static int ERROR = -30;

  int index;//盤上の位置を表すインデックス  8 * 列 + 行 0～35の値で手を表現している
  Color color;
  //Move.of
  public static Move of(int index, Color color) {
    return new Move(index, color);
  }

  public static Move of(String pos, Color color) {
    return new Move(parseIndex(pos), color);
  }

  //Move.ofPass
  public static Move ofPass(Color color) {
    return new Move(PASS, color);
  }

  //Move.ofTimeout
  public static Move ofTimeout(Color color) {
    return new Move(TIMEOUT, color);
  }

  //Move.ofIllegal
  public static Move ofIllegal(Color color) {
    return new Move(ILLEGAL, color);
  }

  //Move.ofError
  public static Move ofError(Color color) {
    return new Move(ERROR, color);
  }

  public Move(int index, Color color) {
    this.index = index;
    this.color = color;
  }

  public int getIndex() { return this.index; }
  public int getRow() { return this.index / SIZE; }
  public int getCol() { return this.index % SIZE; }
  public Color getColor() { return this.color; }
  public int hashCode() { return Objects.hash(this.index, this.color); }

  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Move other = (Move) obj;
    return this.index == other.index && this.color == other.color;
  }

  public boolean isNone() { return this.color == Color.NONE; }

  public boolean isLegal() { return this.index >= PASS; }
  public boolean isPass() { return this.index == PASS; }

  public boolean isFoul() { return this.index < PASS; }
  public boolean isTimeout() { return this.index == TIMEOUT; }
  public boolean isIllega() { return this.index == ILLEGAL; }
  public boolean isError() { return this.index == ERROR; }

  public Move flipped() {
    return new Move(this.index, this.color.flipped());
  }

  public Move colored(Color color) {
    return new Move(this.index, color);
  }

  public static boolean isValid(int col, int row) {
    return 0 <= col && col < SIZE && 0 <= row && row < SIZE;
  }

  static int[][] offsets(int dist) {
    return new int[][] {
      { -dist, 0 }, { -dist, dist }, { 0, dist }, { dist, dist },
      { dist, 0 }, { dist, -dist }, { 0, -dist }, { -dist, -dist } };
  }

  public static List<Integer> adjacent(int k) {
    var ps = new ArrayList<Integer>();
    int col0 = k % SIZE, row0 = k / SIZE;

    for (var o : offsets(1)) {
      int col = col0 + o[0], row = row0 + o[1];
      if (Move.isValid(col, row)) ps.add(index(col, row));
    }

    return ps;
  }

  public static List<Integer> line(int k, int dir) {
    var line = new ArrayList<Integer>();
    int col0 = k % SIZE, row0 = k / SIZE;

    for (int dist = 1; dist < SIZE; dist++) {
      var o = offsets(dist)[dir];
      int col = col0 + o[0], row = row0 + o[1];
      if (Move.isValid(col, row) == false)
        break;
      line.add(index(col, row));
    }

    return line;
  }

  public static int index(int col, int row) {
    return SIZE * row + col;
  }

  public String toString() {
    return toIndexString(this.index);
  }

  public static int parseIndex(String pos) {
    return SIZE * (pos.charAt(1) - '1') + pos.charAt(0) - 'a';
  }

  public static String toIndexString(int index) {
    if (index == PASS) return "..";
    if (index == TIMEOUT) return "@";
    return toColString(index % SIZE) + toRowString(index / SIZE);
  }

  public static String toColString(int col) {
    return Character.toString('a' + col);
  }

  public static String toRowString(int row) {
    return Character.toString('1' + row);
  }

  public static List<String> toStringList(List<Integer> moves) {
    return moves.stream().map(k -> toIndexString(k)).toList();
  }
}
