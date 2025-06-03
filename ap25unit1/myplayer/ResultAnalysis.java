package myplayer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.Arrays;

public class ResultAnalysis {
    public static void main(String[] args) {
        String filename = "result.csv";
        String[] cols = null;
        try {
            List<String> lines = Files.readAllLines(Paths.get(filename));
            cols = new String[lines.size()];
            for (int i = 0; i < lines.size(); i++) {
                cols[i] = lines.get(i);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        int winNum = 0;
        int drawNum = 0;

        for (String col : cols) {
            switch (col) {
                case "R":
                    break;
                case "STATICMOD":
                    winNum++;
                    break;
                // defaultとrandom を戦わせるときただけコメントアウト外す
                case "MY24":
                    // winNum++;
                    break;
                case "DRAW":
                    drawNum++;
                    break;
            }
        }
        double winRate = (double) winNum / (double) cols.length * 100;
        System.out.printf("%n勝率【%.1f％】%n総試合数：%d%n勝利：%d%nドロー：%d%n敗北：%d%n",
                winRate, cols.length, winNum, drawNum, cols.length - winNum - drawNum);

        try {
            Files.deleteIfExists(Paths.get(filename));
        } catch (IOException e) {
            System.out.println("ResultAnalysis:エラーだお");
        }
    }
}
