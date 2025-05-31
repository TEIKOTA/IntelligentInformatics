package myplayer;

import ap25.*;
import java.util.Random;
import java.util.Scanner;

//HumanPlayer:人間がマニュアル操作するPlayer
public class HumanPlayer extends Player {
  Random rand = new Random();
  private final Scanner stdIn = new Scanner(System.in);

  //コンストラクタ:ラッパー
  public HumanPlayer(Color color) {
    super("YOU", color);
  }

  //Playerクラスのthinkを実装
  @Override
  public Move think(Board board) {
    showMove(board);  //石を置ける箇所をCLIで表示
    Move selected = selectMove(board);  //コマンドで石を配置するところを受け取る
    return selected;  //受け取った配置を返す
  }

  private void showMove(Board board){
    //自分(getColorで色取得)が置けるマスの位置をListで取得(findLegalMoves)
    var moves = board.findLegalMoves(getColor()); //Boardを継承したクラスでfindLegalMoves実装
        System.out.println("Available moves:");
    //石を置けるマスをCLIで表示
    for (int idx = 0; idx < moves.size(); idx++) {
      System.out.println((idx + 1) + ": " + moves.get(idx));
    }
  }

  private Move selectMove(Board board){
    var moves = board.findLegalMoves(getColor());
    Move selected = null;
    while (selected == null) {
      System.out.print("Enter move number (1-" + moves.size() + "): ");
      String line = stdIn.nextLine().trim();
      try {
        int choice = Integer.parseInt(line);
        if (choice >= 1 && choice <= moves.size()) {
          selected = moves.get(choice - 1);
        } else {
          System.out.println("Invalid choice. Try again.");
        }
      } catch (NumberFormatException e) {
        System.out.println("Please enter a valid number.");
      }
    }
    return selected;
  }
}
