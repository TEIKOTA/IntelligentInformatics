package myplayer;

import ap25.*;
import static ap25.Color.*;

public class MyGameForDev extends MyGame {
  public MyGameForDev(Board board, Player black, Player white) {
    super(board, black, white);
  }
  @Override protected void info(Object msg) {
    //
  }
  @Override protected void error(Object msg) {
    //
  }
}