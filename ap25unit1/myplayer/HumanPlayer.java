package myplayer;

import ap25.*;
import java.util.Random;
import java.util.Scanner;

public class HumanPlayer extends Player {
  Random rand = new Random();
  private final Scanner stdIn = new Scanner(System.in);

  public HumanPlayer(Color color) {
    super("YOU", color);
  }

  public Move think(Board board) {
    showMove(board);
    Move selected = selectMove(board);
    return selected;
  }

  private void showMove(Board board){
    var moves = board.findLegalMoves(getColor());
        System.out.println("Available moves:");
    
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
