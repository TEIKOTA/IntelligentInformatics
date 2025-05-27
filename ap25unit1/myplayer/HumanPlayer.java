package myplayer;

import ap25.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class HumanPlayer extends ap25.Player{
    static final String MY_NAME = "human";
    public HumanPlayer(Color color){
        super(MY_NAME, color);
    }
    public Move think(Board board){
        var mover = board.findLegalMoves(getColor());
        var i = null;
        
        return moves.get(i);
    }

}
