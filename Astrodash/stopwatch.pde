void stopwatchControl() {
  if (rocket.engineOn && !gameWon && !gameLost) {
    stopwatch.setStartTime();
    stopwatch.displayStopwatch(width*0.11, height*0.03);
    if (stopwatch.timeAltered) {
      stopwatch.displayTimeAlteration(width*0.11, height*0.08, stopwatch.timeAlteration);
    }
  } else if (gameWon || gameLost) {
    if (gameWon && rocket.health > 1) {
      stopwatch.awardTime((rocket.health-1)*5000);
    }
    stopwatch.setStopTime();
    stopwatch.displayTimeResult(width/2, height/2 + height/3);
  }

  if (gamePaused) {
    stopwatch.setPauseTime();
  }
}

class Stopwatch {
  int startTime;
  int stopTime;
  int pauseTime;
  int lastCheckpointTime = 0;
  boolean startTimeSet;
  boolean stopTimeSet;
  boolean pauseTimeSet;
  boolean checkpointTimeSet;
  boolean timeAltered = false;
  int timeAlteration;
  //int timeLimit = 22000; // 22 seconds (start is 25, then limit is increased for each checkpoint)
  int timeLimit = 35000; // 35s to get to checkpoint (fixed for all checkpoints)
  int fade = 255;
  PFont timeFont = loadFont("LucidaFax-Demi-48.vlw");

  Stopwatch() {
    startTime = 0;
    stopTime = 0;
    lastCheckpointTime = 0;
    startTimeSet = false;
    stopTimeSet = false;
    checkpointTimeSet = false;
    timeAltered = false;
    timeAlteration = 0;
    fade = 255;
  }

  void rewindTimeAfterPause() {
    int rewindAmount = millis() - pauseTime;
    startTime += rewindAmount;
    pauseTimeSet = false;
  }

  void setPauseTime() {
    if (!pauseTimeSet) {
      pauseTime = millis();
    }
    pauseTimeSet = true;
  }

  void setStartTime() {
    if (!startTimeSet) {
      startTime = millis();
    }
    startTimeSet = true;
  }

  void setStopTime() {
    if (!stopTimeSet) {
      stopTime = millis() - startTime;
    }
    stopTimeSet = true;
  }

  int getTimeSinceStart() {
    return millis() - startTime;
  }

  void getCheckpointTime() {
    int timeToCheckpoint = getTimeSinceStart() - lastCheckpointTime;
    //timeLimit = timeLimit + 3000 + checkpoint.nextCheckpoint*1000;  // 25s -> 29s -> 34s -> 40s
    if (timeToCheckpoint < timeLimit) {  // took less than time limit to reach next checkpoint
      awardTime(10000); // 10 seconds
    }
    lastCheckpointTime = getTimeSinceStart();  // must set after awarding time bonus
    checkpointTimeSet = true;
  }

  void awardTime(int amount) {
    timeAlteration = amount;
    startTime += timeAlteration;
    timeAltered = true;
  }

  void penaliseTime() {
    if (!checkpoint.inCheckpointZone && rocket.health > 1) {    // in case of collision during checkpoint
      timeAlteration = -5000;
      startTime += timeAlteration; // 5 seconds
      timeAltered = true;
    }
  }

  void warpTime() {
    timeAlteration = (int)random(-5000, 5000);
    startTime += timeAlteration; // +5 to -5 seconds
    timeAltered = true;
  }

  void displayTimeAlteration(float xPos, float yPos, float value) {
    if (fade > 0) {
      textFont(timeFont);
      textSize(width/30);
      if (value < 0) {    // negative
        fill(255, 80, 80, fade);
        text("+ " + (value*-1)/1000 + "s", xPos, yPos);
      } else {
        fill(80, 255, 80, fade);
        text("- " + value/1000 + "s", xPos, yPos);
      }
      fade -= 2;
    } else {
      fade = 255;
      timeAltered = false;
    }
  }

  String timeToString(int timeInMillis, boolean withMillis) {
    int minutes = (timeInMillis / 1000) / 60;
    int seconds = (timeInMillis / 1000) % 60;
    int millis = timeInMillis % 1000;

    if (withMillis) {
      return minutes + "m : " + seconds + "s : " + millis + "ms";
    } else {
      return minutes + "m : " + seconds + "s";
    }
  }

  void displayStopwatch(float xPos, float yPos) {
    String currentTime = timeToString(getTimeSinceStart(), false);
    drawStopwatchBox(xPos, yPos);
    fill(200, 200, 200);
    textFont(timeFont);
    textSize(width/40);
    textAlign(CENTER);
    text(currentTime, xPos, yPos + height*0.007);
  }

  void drawStopwatchBox(float xPos, float yPos) {
    stroke(170, 170, 170);
    fill(100, 100, 100, 200);
    strokeWeight(width*0.004);
    rectMode(CENTER);
    rect(xPos, yPos, width*0.18, height*0.03, width*0.01);
  }

  void displayTimeResult(float xPos, float yPos) {
    String timeResult = timeToString(stopTime, true);
    drawTimeDialogueBox(xPos, yPos);
    fill(200, 200, 200);
    textFont(timeFont);
    textSize(width/25);
    textAlign(CENTER);
    text("Your time...", xPos, yPos - height*0.015);
    text(timeResult, xPos, yPos + height*0.03);
  }

  void drawTimeDialogueBox(float xPos, float yPos) {
    stroke(100, 180, 180);
    fill(120, 120, 120, 200);
    strokeWeight(width*0.01);
    rectMode(CENTER);
    rect(xPos, yPos, width*0.6, height*0.12, width*0.02);
  }
}
