import java.util.Collections;
import java.io.FileWriter;
import java.io.BufferedWriter;
PrintWriter scoreWriter;
boolean scoreRecorded;

void leaderboardControl() {
  if (!screens.guideScreenDisplaying) {
    if (leaderboard.displaying) {
      leaderboard.displayTopTimes(width/2, height/2 - height/5.5);
    }
    if (gameWon && !scoreRecorded) {
      leaderboard.recordNewTime(stopwatch.stopTime+"");
    }
  }
}

class Leaderboard {
  int displayDuration = 200;
  boolean displaying = false;

  void recordNewTime(String score) {
    try {
      File file = new File(dataPath("leaderboard.txt"));
      if (!file.exists()) {
        file.createNewFile();
      }
      FileWriter fw = new FileWriter(file, true);
      BufferedWriter bw = new BufferedWriter(fw);
      PrintWriter scoreWriter = new PrintWriter(bw);
      scoreWriter.write(score+"\n");
      scoreWriter.close();
      scoreRecorded = true;
    } 
    catch (IOException ex) {
      System.out.println("Exception ");
      ex.printStackTrace();
    }
  }

  void displayTopTimes(float xPos, float yPos) {
    if (displayDuration > 0) {
      ArrayList<Integer> times = getTimes();
      Collections.sort(times);
      float lineSpacing = height*0.03;
      PFont leaderboardFont = loadFont("Monospaced.bold-48.vlw");

      displayLeaderboardBox(xPos, yPos);

      textAlign(CENTER);
      fill(255, 255, 100);
      textFont(leaderboardFont);
      textSize(width/30);
      text("LEADERBOARD (TOP 5)", xPos, yPos - lineSpacing);
      fill(200, 200, 200);
      for (int i = 0; i < times.size() && i < 5; i++) {
        text(buildLeaderboardString(i+1, times.get(i), 30), xPos, yPos + i*lineSpacing);
      }
      displayDuration--;
    } else {
      displayDuration = 200;
      displaying = false;
    }
  }

  String buildLeaderboardString(int place, int time, int lineLength) {
    StringBuilder sb = new StringBuilder();
    String timeString = stopwatch.timeToString(time, true);
    sb.append(place);
    while (sb.length() < lineLength - timeString.length()) {
      sb.append(" ");
    }
    sb.append(timeString);
    return sb.toString();
  }

  void displayLeaderboardBox(float xPos, float yPos) {
    stroke(255, 255, 100);
    fill(100, 100, 100, 140);
    strokeWeight(width*0.006);
    rectMode(CENTER);
    rect(xPos, yPos + height*0.037, width*0.7, height*0.21, width*0.03);
  }

  ArrayList<Integer> getTimes() {
    ArrayList<Integer> times = new ArrayList<Integer>();
    File file = new File(dataPath("leaderboard.txt"));
    if (file.exists()) {
      BufferedReader reader = createReader(dataPath("leaderboard.txt"));
      String line = null;

      try {
        while ((line = reader.readLine()) != null) {
          times.add(Integer.parseInt(line));
        }
        reader.close();
      } 
      catch (IOException ex) {
        ex.printStackTrace();
      }
    }
    return times;
  }
}
