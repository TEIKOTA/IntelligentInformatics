package ap25;

import java.util.Map;
//盤面上のあるマスの色を表現する列挙子=これが"1マス"の情報を表現
public enum Color {
  //黒:1 白:-1 何もない:0 置けない:3
  BLACK(1),
  WHITE(-1),
  NONE(0),
  BLOCK(3);

  //CLIで盤面を表示:今自分が表現するマスのもつ"値"を読み取って書く文字にマッピングする
  static Map<Color, String> SYMBOLS =
      Map.of(BLACK, "o", WHITE, "x", NONE, " ", BLOCK, "#");

  //値=自分が表現するマスの色を表す
  private int value;

  //コンストラクタ
  private Color(int value) {
    this.value = value;
  }

  //マスの状態=色を渡す
  public int getValue() {
    return this.value;
  }


  public Color flipped() {
    switch (this) {
    case BLACK: return WHITE;
    case WHITE: return BLACK;
    default: return this;
    }
  }

  public String toString() {
    return SYMBOLS.get(this);
  }

  public Color parse(String str) {
    return Map.of("o", BLACK, "x" , WHITE).getOrDefault(str, NONE);
  }
}
