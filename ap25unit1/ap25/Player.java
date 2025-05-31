package ap25;
//プレイヤーの抽象クラス
public abstract class Player {
  String name;
  Color color;
  Board board;

  //名前, 石の色は黒か白か
  public Player(String name, Color color) {
    this.name = name;
    this.color = color;
  }

  public void setBoard(Board board) { this.board = board; }
  public Color getColor() { return this.color; }
  public String toString() { return this.name; }
  //思考して意思を置いた後の盤面を返す
  public Move think(Board board) { return null; }
}
