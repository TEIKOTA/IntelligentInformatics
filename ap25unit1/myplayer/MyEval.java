package myplayer;

import static ap25.Board.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.stream.IntStream;

import ap25.*;

public class MyEval {
  float[][] M = {
      { 10, 10, 10, 10, 10, 10 },
      { 10, -5, 1, 1, -5, 10 },
      { 10, 1, 1, 1, 1, 10 },
      { 10, 1, 1, 1, 1, 10 },
      { 10, -5, 1, 1, -5, 10 },
      { 10, 10, 10, 10, 10, 10 },
  };

  public MyEval(float[][] M) {
    this.M = M;
  }

  public MyEval() {

  }

  public float value(Board board) {
    if (board.isEnd())
      return 1000000 * board.score();

    return (float) IntStream.range(0, LENGTH)
        .mapToDouble(k -> score(board, k))
        .reduce(Double::sum).orElse(0);
  }

  float score(Board board, int k) {
    return M[k / SIZE][k % SIZE] * board.get(k).getValue();
  }

   // M を CSV に書き出す
    public void saveWeights(String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (int r = 0; r < SIZE; r++) {
                StringBuilder sb = new StringBuilder();
                for (int c = 0; c < SIZE; c++) {
                    sb.append(M[r][c]);
                    if (c < SIZE - 1) sb.append(",");
                }
                writer.write(sb.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error: 重み行列 M の保存に失敗: " + e.getMessage());
        }
    }

    // CSV から M を読み込んで上書きする
    public void loadWeights(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            int r = 0;
            while ((line = reader.readLine()) != null && r < SIZE) {
                StringTokenizer st = new StringTokenizer(line, ",");
                for (int c = 0; c < SIZE; c++) {
                    if (!st.hasMoreTokens()) {
                        throw new IOException("フォーマットエラー: 行" + (r+1) + "の列数不足");
                    }
                    M[r][c] = Float.parseFloat(st.nextToken().trim());
                }
                r++;
            }
            if (r < SIZE) {
                throw new IOException("フォーマットエラー: 行数が不足 (期待:6行, 実際:" + r + "行)");
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error: 重み行列 M の読み込みに失敗: " + e.getMessage());
        }
    }
}