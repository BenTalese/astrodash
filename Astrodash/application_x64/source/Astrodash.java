import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.sound.*; 
import java.util.Collections; 
import java.io.FileWriter; 
import java.io.BufferedWriter; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class Astrodash extends PApplet {

Rocket rocket;        // game objects
Starfield starfield;
Meteorfield meteorfield;
HeadsUpDisplay hud;
MenuScreen screens;
Shield shield;
Checkpoint checkpoint;
Stopwatch stopwatch;
Leaderboard leaderboard;

float distance;                                       // distance from start
int goalDistance = 250000;                          // 250,000m distance to moon from start
boolean gameWon;
boolean gameLost;
int loadState = 0;
boolean gamePaused = false;

public void setup() {              // initialise the game properties
             // 1920p x 1080p = 576,864 .... 3200p x 1800p = 900,1500
  gameReset();
}

public void draw() {
  if (loadState == 2) {
    musicControl();
    sfxControl();
    background(0);
    starfield.display();
    beginSpawningMeteorfield();
    checkpointControl();
    rocketControl();
    collisionControl();
    damageControl();
    checkGameWon();
    hudControl();
    screenControl();
    stopwatchControl();
    leaderboardControl();
    if (gamePaused) {
      noLoop();
    }
  } else if (loadState == 1) {
    loadAudio();
    loadImages();
    loadState++;
  } else {
    screens.displayLoadingScreen();
    loadState++;
  }
}

public void checkGameWon() {
  if (distance >= goalDistance) {
    gameWon = true;
  }
}

public void gameReset() {
  gameWon = false;
  gameLost = false;
  distance = 0;
  sfxRktExplodeHasPlayed = false;
  scoreRecorded = false;

  rocket = new Rocket();
  hud = new HeadsUpDisplay();
  shield = new Shield();
  starfield = new Starfield();
  meteorfield = new Meteorfield();
  meteorfield.generateMeteoroids();
  screens = new MenuScreen();
  checkpoint = new Checkpoint();
  stopwatch = new Stopwatch();
  leaderboard = new Leaderboard();
}

public void keyPressed() {                                      // if any of the control keys are pressed, the corresponding boolean is set to true
  if (key == 'w' || keyCode == UP) {                     // this controls up, down, left, right
    if (!screens.guideScreenDisplaying) {
      rocket.engineOn = true;
    }
  }

  if (screens.guideScreenDisplaying) {
    if (keyCode == UP) {
      screens.scrollDown = true;
    }
    if (keyCode == DOWN) {
      screens.scrollUp = true;
    }
  }

  if (rocket.engineOn) {
    if (key == 'w' || keyCode == UP) {
      rocket.accelerate = true;
    }
    if (key == 's' || keyCode == DOWN) {
      rocket.decelerate = true;
    }
    if (key == 'a' || keyCode == LEFT) {
      rocket.bankLeft = true;
    }
    if (key == 'd' || keyCode == RIGHT) {
      rocket.bankRight = true;
    }
  }

  if (key == 'f' || key == '.') {
    leaderboard.displaying = true;
  }

  if (key == 'p' && rocket.engineOn && !gameWon && !gameLost) {
    gamePaused = true;
  } else if (keyPressed && gamePaused) {
    gamePaused = false;
    stopwatch.rewindTimeAfterPause();
    loop();
  }

  if (gameLost || gameWon) {                  // if the game has been lost or won, the player can press the spacebar
    if (keyCode == ' ') {                          // to activate the reset function
      gameReset();
    }
  }
}


public void  keyReleased () {     // if any control keys (up, down, left or right) are released, the rocket will stop moving
  if (key == 'g') {
    screens.guideScreenDisplaying = !screens.guideScreenDisplaying;
  }
  if (screens.guideScreenDisplaying) {
    if (keyCode == UP) {
      screens.scrollDown = false;
    }
    if (keyCode == DOWN) {
      screens.scrollUp = false;
    }
  }

  if (key == 'w' || keyCode == UP) {               // in the corresponding direction
    rocket.accelerate = false;
  }
  if (key == 's' || keyCode == DOWN) {
    rocket.decelerate = false;
  }
  if (key == 'a' || keyCode == LEFT) {
    rocket.bankLeft = false;
  }
  if (key == 'd' || keyCode == RIGHT) {
    rocket.bankRight = false;
  }

  float leftBoostBoundary = width * 0.21f;
  float rightBoostBoundary = width - leftBoostBoundary;
  float boostDistance = width*0.15f;
  if (rocket.canMove() && rocket.hasFuel()) {                // if the player has started the game, the rocket fuel isn't empty and
    //BOOST LEFT                                                                  // the game isn't won or lost, the player can use boost
    if (rocket.bankLeft && keyCode == SHIFT && rocket.pos.x > leftBoostBoundary) {              // if the player was banking left, isn't too far left and also released the shift key,
      rocket.pos.x -= boostDistance;                                                 // the rocket will jump to the left and consume 1 bar (30) of fuel
      rocket.fuel--;
    }
    //BOOST RIGHT                                                         // if the player was banking right, isn't too far right and also released the shift key,
    if (rocket.bankRight && keyCode == SHIFT && rocket.pos.x < rightBoostBoundary) {     // the rocket will jump to the right and consume 1 bar (30) of fuel
      rocket.pos.x += boostDistance;
      rocket.fuel--;
    }
    // WARP SPEED                                                         // if the player was accelerating, released the shift key and the rocket has full fuel,
    if (rocket.accelerate && keyCode == SHIFT && rocket.fuel >= rocket.maxBarAmount/2) {            // warp speed is activated, all the rocket fuel is consumed and
      rocket.warpDriveOn = true;                                                 // warp time is set to 4 seconds
      rocket.fuel -= rocket.maxBarAmount/2;
      rocket.warpDuration = 200;
    }
  }
}
// name for leaderboard score
// pause instructions
// create another audio clip of warp with just the end part, so that if the player reaches a checkpoint while still warping, it cuts the warp sound short
// side booster mini flame, and make it a smoother animation when boosting
// meteoroids that collide should be repelled in opposite directions (needs to follow real physics, where depending on speed and mass, the other will be affected accordingly)
// clear leaderboard scores (needs confirmation screen (Y/N key check))
// could randomise the amount of meteors being spawned each time? (would have to be based on a randomised respawn timer for each meteor)
// make explosion of meteor randomised even more
// add exclamation mark in triangle for damaged screen
// when low on health, fade the exclamation mark in and out slowly
// pulse animates from rocket when checkpoint reached? (two transparent white circles, one has thicker stroke to make it look like a glow)

SoundFile mainMenuMusic;    // music
SoundFile gamePlayMusic;
SoundFile gameOverMusic;
SoundFile gameWonMusic;

SoundFile sfxLowHealth;    // sound effects
SoundFile sfxWarp;
SoundFile sfxRktExplode;
boolean sfxRktExplodeHasPlayed;
SoundFile sfxImpact;
SoundFile sfxRktAccelerate;
SoundFile sfxRktNeutral;
SoundFile sfxRktDecelerate;

public void loadAudio() {
  mainMenuMusic = new SoundFile(this, "main_menu.mp3");
  gamePlayMusic = new SoundFile(this, "game_play.mp3");
  gameOverMusic = new SoundFile(this, "game_over.mp3");
  gameWonMusic = new SoundFile(this, "game_won.mp3");

  sfxLowHealth = new SoundFile(this, "warning.mp3");
  sfxWarp = new SoundFile(this, "warp.mp3");
  sfxRktExplode = new SoundFile(this, "rocket_explode.mp3");
  sfxImpact = new SoundFile(this, "impact.mp3");
  sfxRktAccelerate = new SoundFile(this, "engine_accelerate.mp3");
  sfxRktNeutral = new SoundFile(this, "engine_neutral.mp3");
  sfxRktDecelerate = new SoundFile(this, "engine_decelerate.mp3");
}

public void musicControl() {
  if (!rocket.engineOn) {        // main menu
    gameOverMusic.stop();
    gameWonMusic.stop();
    if (!mainMenuMusic.isPlaying()) {
      mainMenuMusic.amp(1);
      mainMenuMusic.loop();
    }
  }

  if (rocket.engineOn) {        // playing
    mainMenuMusic.stop();
    if (!gamePlayMusic.isPlaying()) {
      gamePlayMusic.amp(0.4f);
      gamePlayMusic.play();
    }
  }

  if (gamePaused) {    // pause screen
    gamePlayMusic.pause();
    if (!mainMenuMusic.isPlaying()) {
      mainMenuMusic.amp(0.2f);
      mainMenuMusic.loop();
    }
  }

  if (gameWon) {          // won
    gamePlayMusic.stop();
    if (!gameWonMusic.isPlaying()) {
      gameWonMusic.loop();
    }
  }

  if (gameLost) {        // lost
    gamePlayMusic.stop();
    if (!gameOverMusic.isPlaying()) {
      gameOverMusic.loop();
    }
  }
}



public void sfxControl() {
  if (rocket.health == 1) {
    if (!sfxLowHealth.isPlaying()) {
      sfxLowHealth.play();
    }
  }

  if (gameWon || gameLost || gamePaused) {
    sfxLowHealth.stop();
    sfxWarp.stop();
    sfxRktNeutral.stop();
    sfxRktDecelerate.stop();
    sfxRktAccelerate.stop();
  }

  if (rocket.health == 0) {
    if (!sfxRktExplodeHasPlayed) {
      sfxRktExplode.amp(0.5f);
      sfxRktExplode.play();
      sfxRktExplodeHasPlayed = true;
    }
  }

  if (rocket.warpDriveOn) {
    if (!sfxWarp.isPlaying()) {
      sfxWarp.play();
    }
  }

  // moved to meteorfield class because the first impact wouldn't play
  //if (meteorfield.rocketHasCollided()) {
  //  sfxImpact.play();
  //}

  if (rocket.accelerate && rocket.canMove()) {
    sfxRktNeutral.stop();
    sfxRktDecelerate.stop();
    if (!sfxRktAccelerate.isPlaying()) {
      sfxRktAccelerate.loop();
    }
  } else {
    sfxRktAccelerate.stop();
  }

  if (rocket.decelerate && rocket.canMove()) {
    sfxRktNeutral.stop();
    sfxRktAccelerate.stop();
    if (!sfxRktDecelerate.isPlaying()) {
      sfxRktDecelerate.loop();
    }
  } else {
    sfxRktDecelerate.stop();
  }

  if (!rocket.accelerate && !rocket.decelerate && rocket.canMove()) {
    sfxRktAccelerate.stop();
    sfxRktDecelerate.stop();
    if (!sfxRktNeutral.isPlaying()) {
      sfxRktNeutral.loop();
    }
  } else {
    sfxRktNeutral.stop();
  }
}
public void checkpointControl() {
  if (checkpoint.nextCheckpoint < 4) {    // 4 because there are 4 checkpoints in total
    checkpoint.triggerZone();

    if (checkpoint.zoneTriggered()) {
      meteorfield.explodeAll();
      checkpoint.activateZone();
      checkpoint.displayAlert();
      if (!stopwatch.checkpointTimeSet) {
        stopwatch.getCheckpointTime();
      }
    }

    if (checkpoint.timer <= 0) {                          // if rest period has ended, the rocket is pushed out of the checkpoint zone (+250m) to avoid
      checkpoint.deactivateZone();
    }
  }
}

class Checkpoint {
  boolean inCheckpointZone;
  boolean[] checkpointStatus = new boolean[4];
  int timer;
  int nextCheckpoint;

  Checkpoint() {
    inCheckpointZone = false;
    for (int i = 0; i < checkpointStatus.length; i++) {
      checkpointStatus[i] = false;
    }
    timer = 300;
    nextCheckpoint = 0;
  }

  public void triggerZone() {
    if (distance >= (nextCheckpoint+1)*50000) {
      checkpointStatus[nextCheckpoint] = true;
    }
  }

  public boolean zoneTriggered() {
    if (checkpointStatus[nextCheckpoint]) {
      return true;
    }
    return false;
  }

  public void displayAlert() {
    fill(150, 230, 255, timer);
    textAlign(CENTER);
    textFont(screens.titleFont);
    textSize(width/12);
    text("Checkpoint Reached", width/2, height/8);
  }

  public void activateZone() {
    inCheckpointZone = true;
    if (!rocket.repaired) {
      rocket.repair();
    }
    timer--;                                   // and instantly gets hit by a meteoroid
  }

  public void deactivateZone() {
    inCheckpointZone = false;                                // continuous checkpoint bug and rest timer is reset
    nextCheckpoint++;
    stopwatch.checkpointTimeSet = false;
    rocket.repaired = false;
    meteorfield.increaseDifficulty();
    timer = 300;
  }
}
public void hudControl() {
  if (rocket.engineOn && !gameWon) {
    hud.display();
  }
}

class HeadsUpDisplay {
  public void display() {
    PVector hudPos = new PVector (width * 0.1f, height - height * 0.03f); // position of health and fuel bar
    PVector barDimensions = new PVector (width * 0.05f, height * 0.03f);
    float roundedness = width*0.01f;
    strokeWeight(width*0.004f);             // outline width of bars

    renderHealthGauge(hudPos, barDimensions, roundedness);
    renderHealthGaugeIcon(hudPos, barDimensions);
    renderFuelGauge(hudPos, barDimensions, roundedness);
    renderFuelGaugeIcon(hudPos, barDimensions);
    renderProgressGauge();
  }

  public void renderHealthGauge(PVector pos, PVector dimensions, float corners) {
    for (float i = 0; i < rocket.health*dimensions.x; i += dimensions.x) {      // display a bar for each lot of 30 health, increase the x pos of the next health bar by the same width
      if (rocket.damageTaken) {                      // display bars in blue if shield is active
        fill(0, 200, 200);
        stroke(0, 50, 100);
      } else if (rocket.healthLow()) {     // display the last bar of health in red
        fill(200, 0, 0);
        stroke(100, 0, 0);
      } else {                             // if all other conditions are false, display health bars in green
        fill(20, 200, 20);
        stroke(0, 100, 0);
      }
      rect(pos.x + i, pos.y, dimensions.x, dimensions.y, corners);      // draw the bars of health based off values
    }
  }

  public void renderHealthGaugeIcon(PVector pos, PVector dimensions) {
    PVector iconPos = new PVector(pos.x - dimensions.x*1.2f, pos.y);
    if (rocket.healthLow()) {
      noFill();
    }
    if (rocket.health == 0) {
      stroke(100, 0, 0);
    }
    strokeWeight(width*0.004f);
    circle(iconPos.x, iconPos.y, dimensions.x*0.8f);
    strokeWeight(width*0.006f);
    line(iconPos.x - dimensions.x/4, pos.y, iconPos.x + dimensions.x/4, pos.y);
    line(iconPos.x, iconPos.y + dimensions.y/3.5f, iconPos.x, iconPos.y - dimensions.y/3.7f);
  }

  public void renderFuelGauge(PVector pos, PVector dimensions, float corners) {
    for (float i = 0; i < rocket.fuel*dimensions.x; i += dimensions.x) {       // display a bar for each lot of 30 fuel, increase the x pos of the next fuel bar by the same width
      if (rocket.fuelLow()) {
        fill(200, 0, 0);
        stroke(100, 0, 0);
      } else {
        fill(255, 255, 0);                             // display fuel bars in yellow
        stroke(100, 100, 0);
      }
      rect(pos.x + i, pos.y - dimensions.y*1.2f, dimensions.x, dimensions.y, corners);    // draw the bars of fuel based off values
    }
  }

  public void renderFuelGaugeIcon(PVector pos, PVector dimensions) {
    PVector iconPos = new PVector(pos.x - dimensions.x*1.2f, pos.y - dimensions.y*1.05f);
    textAlign(CENTER);
    textFont(screens.font);
    textSize(width/30);
    if (rocket.hasFuel()) {
      fill(255, 255, 0);
      quad(iconPos.x-width*0.026f, iconPos.y-height*0.017f, iconPos.x+width*0.026f, iconPos.y-height*0.017f, iconPos.x+width*0.01f, iconPos.y+height*0.006f, iconPos.x-width*0.01f, iconPos.y+height*0.006f);
      fill(0);
      text("F", iconPos.x, iconPos.y+height*0.003f); // full
    } else {
      noFill();
      stroke(100, 100, 0);
      quad(iconPos.x-width*0.026f, iconPos.y-height*0.017f, iconPos.x+width*0.026f, iconPos.y-height*0.017f, iconPos.x+width*0.01f, iconPos.y+height*0.006f, iconPos.x-width*0.01f, iconPos.y+height*0.006f);
      fill(255, 0, 0);
      text("E", iconPos.x, iconPos.y+height*0.003f); // empty
    }
  }

  public void renderProgressGauge() {
    float progressBarXPos = width - width * 0.03125f;            // x position of progress bar
    float progressBarYPos = height - height * 0.1f;          // y position of progress bar
    float progressBarWidth = width * 0.03f;
    float progressBarHeight = height * 0.18f;                 // height of the progress bar 155.52
    float progress = ((distance / 1000.0f) / progressBarHeight) * 100;              // divide the current distance by 1000 so it can be more easily displayed in the progress bar (values: 0 - 250)

    noFill();
    stroke(200, 200, 200);
    rect(progressBarXPos, progressBarYPos, progressBarWidth, progressBarHeight);       // draw the progress bar based off values

    // display checkpoints on progress bar
    for (float i = progressBarHeight * 0.2f; i <= progressBarHeight * 0.8f; i += progressBarHeight * 0.2f) {          // for each 50,000m (50/250), display a horizontal line on the progress bar showing where the checkpoint are
      line(progressBarXPos - width*0.015f, (progressBarYPos + progressBarHeight/2) - i, progressBarXPos + width*0.015f, (progressBarYPos + progressBarHeight/2) - i);
    }

    // rocket position indicator
    fill(0, 255, 0);                // based off the current progress value, display a green arrow which indicates where the rocket is
    stroke(0, 200, 0);
    strokeWeight(width*0.004f);
    PVector indicatorTopCorner = new PVector(progressBarXPos, (progressBarYPos + progressBarHeight/2 - progressBarHeight*0.01f) - progress);
    PVector indicatorLeftCorner = new PVector(progressBarXPos - width*0.0052f, (progressBarYPos + progressBarHeight/2 + progressBarHeight*0.04f) - progress);
    PVector indicatorRightCorner = new PVector(progressBarXPos + width*0.0053f, (progressBarYPos + progressBarHeight/2 + progressBarHeight*0.04f) - progress);
    triangle(indicatorTopCorner.x, indicatorTopCorner.y, indicatorLeftCorner.x, indicatorLeftCorner.y, indicatorRightCorner.x, indicatorRightCorner.y);

    // moon indicator
    fill(200, 200, 200);            // display a tiny moon at the top of the progress bar
    stroke(150, 150, 150);
    circle(progressBarXPos, progressBarYPos - height*0.099f, width*0.05f);

    int distanceToMoon = (int)(goalDistance - distance)/1000;
    textAlign(RIGHT);
    textFont(stopwatch.timeFont);
    textSize(width/30);
    text(distanceToMoon+"km", progressBarXPos - progressBarWidth*1.3f, progressBarYPos + progressBarHeight/2);
  }
}



PrintWriter scoreWriter;
boolean scoreRecorded;

public void leaderboardControl() {
  if (!screens.guideScreenDisplaying) {
    if (leaderboard.displaying) {
      leaderboard.displayTopTimes(width/2, height/2 - height/5.5f);
    }
    if (gameWon && !scoreRecorded) {
      leaderboard.recordNewTime(stopwatch.stopTime+"");
    }
  }
}

class Leaderboard {
  int displayDuration = 200;
  boolean displaying = false;

  public void recordNewTime(String score) {
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

  public void displayTopTimes(float xPos, float yPos) {
    if (displayDuration > 0) {
      ArrayList<Integer> times = getTimes();
      Collections.sort(times);
      float lineSpacing = height*0.03f;
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

  public String buildLeaderboardString(int place, int time, int lineLength) {
    StringBuilder sb = new StringBuilder();
    String timeString = stopwatch.timeToString(time, true);
    sb.append(place);
    while (sb.length() < lineLength - timeString.length()) {
      sb.append(" ");
    }
    sb.append(timeString);
    return sb.toString();
  }

  public void displayLeaderboardBox(float xPos, float yPos) {
    stroke(255, 255, 100);
    fill(100, 100, 100, 140);
    strokeWeight(width*0.006f);
    rectMode(CENTER);
    rect(xPos, yPos + height*0.037f, width*0.7f, height*0.21f, width*0.03f);
  }

  public ArrayList<Integer> getTimes() {
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
public void screenControl() {
  if (!rocket.engineOn) {
    if (!screens.guideScreenDisplaying) {
      screens.displayStartScreen();
    } else {
      screens.displayGuideScreen();
    }
  } else if (gameLost) {
    screens.displayGameOverScreen();
  } else if (gameWon) {
    screens.displayWinningScreen();
  }

  if (gamePaused) {
    screens.displayPauseScreen();
  }
}

PImage imgHealthFuel;
PImage imgProgress;
PImage imgStopwatch;
PImage imgWarp;
PImage imgBoost;
PImage imgShield;
PImage imgMoreHealth;
public void loadImages() {
  imgHealthFuel = loadImage("fuelhealth.PNG");
  imgProgress = loadImage("progress.PNG");
  imgStopwatch = loadImage("stopwatch.PNG");
  imgBoost = loadImage("boost.PNG");
  imgShield = loadImage("shield.PNG");
  imgMoreHealth = loadImage("morehealth.PNG");
  imgWarp = loadImage("warp.PNG");
}

class MenuScreen {
  PFont titleFont = loadFont("Algerian-68.vlw");
  PFont font = loadFont("Candara-48.vlw");
  boolean guideScreenDisplaying = false;
  float speed = 2.5f; 
  float value = 0.0f;
  int max = 255;
  int fadeIn = 255;

  float moonYPos;
  float flagYPos;
  float innerFade;
  float outerFade;

  MenuScreen() {
    moonYPos = -(width*0.41f);
    flagYPos = -(width*0.14f);
    innerFade = 130;
    outerFade = 80;
  }

  public void menuFadeIn() {
    if (fadeIn > 0) {
      fill(0, 0, 0, fadeIn);
      noStroke();
      rectMode(CENTER);
      rect(width/2, height/2, width, height);
      fadeIn--;
    }
  }

  public void displayStartScreen() {
    fill(255, 255, 100);
    textAlign(CENTER);
    textFont(titleFont);
    textSize(width/8);
    text("Astro Dash", width/2, height/2 - height/2.8f);
    strokeWeight(3);

    stroke(200);
    line(width/2 - width/3, height/2 - height/2.9f, width/2 + width/3, height/2 - height/2.9f);
    fill(230);
    textFont(font);
    textSize(width/19);
    text("Created by Ben Talese", width/2, height/2 - height/3.3f);
    fill(230);
    textSize(width/25);
    text("For Xixi", width/2, height/2 - height/3.7f);

    value+=speed;
    float fade = max - abs(value % (2*max) - max);
    fill(255, 255, 100, fade);
    textSize(width/25);
    text("Press W or UP ARROW to start", width/2, height/2);

    fill(255, 255, 100);
    textSize(width/20);
    text("Press G to view guide", width/2, height/2 + height/2.5f);

    menuFadeIn();
  }

  PVector guidePos = new PVector(width/2, height/6);
  float guideScrollLimit = (height*5.15f)*-1;
  boolean scrollUp;
  boolean scrollDown;
  public void displayGuideScreen() {
    menuScroll();
    float lineSpacing = height*0.03f;
    float columnSpacing = width*0.35f;
    float paragraphWidth = width*0.88f;
    float paragraphWidthWithImage = width*0.6f;
    float paragraphXAlign = guidePos.x - columnSpacing*1.23f;
    float imageSpacing = height*0.2f;
    float imageXAlign = width*0.81f;

    drawGuideBox(new PVector(guidePos.x - columnSpacing*1.3f, guidePos.y - height*0.05f), new PVector(width*0.92f, height*6.05f));

    headingFormat();
    text("CONTROLS", guidePos.x, guidePos.y);
    paragraphOneFormat();
    textAlign(LEFT);
    String[] controlActions = {"ROCKET CONTROLS:", "ACCELERATE", "DECELERATE", "BANK LEFT", "BANK RIGHT", "WARP SPEED", 
      "BOOST LEFT", "BOOST RIGHT", "", "MENU CONTROLS:", "LEADERBOARD", "SCROLL UP", "SCROLL DOWN", "SCROLL FAST", "PAUSE"};
    String[] controlButtons = {"", "W / UP ARROW", "S / DOWN ARROW", "A / LEFT ARROW", "D / RIGHT ARROW", 
      "ACCELERATE + SHIFT", "BANK LEFT + SHIFT", "BANK RIGHT + SHIFT", "", "", "PERIOD / F", "DOWN ARROW", "UP ARROW", "UP/DOWN + SHIFT", "P"};
    for (int i = 0; i < controlActions.length; i++) {
      text(controlActions[i], guidePos.x - columnSpacing, guidePos.y + lineSpacing*(i+1));
    }
    textAlign(RIGHT);
    for (int i = 0; i < controlActions.length; i++) {
      text(controlButtons[i], guidePos.x + columnSpacing, guidePos.y + lineSpacing*(i+1));
    }

    float introductionYPos = guidePos.y + height*0.55f;
    headingFormat();
    text("INTRODUCTION", guidePos.x, introductionYPos);
    paragraphTwoFormat();
    text("Ahoy there captain, and welcome to Astrodash. This is a fast-paced race against the clock, so get your helmet ready and blast off into space!"+
      " Your goal is the moon. Get there as fast as you can to secure your place on the leaderboards! Watch out though, the path "+
      "ahead is not safe. Unluckily for you, an asteroid collision has sent a lot of meteoroids coming your way. Think fast captain!"+
      "\n\nBefore I send you on your way though, you may want to be aware of some useful tips...", 
      paragraphXAlign, introductionYPos + lineSpacing, paragraphWidth, height);

    float objectiveYPos = introductionYPos + height*0.48f;
    headingFormat();
    text("MISSION OBJECTIVE", guidePos.x, objectiveYPos);
    paragraphTwoFormat();
    text("To succeed in your mission, simply reach the moon by dodging those pesky meteoroids and place our flag on the moon! Although this is no simple task.", 
      paragraphXAlign, objectiveYPos + lineSpacing, paragraphWidth, height);

    float hudYPos = objectiveYPos + height*0.23f;
    headingFormat();
    text("HUD", guidePos.x, hudYPos);
    paragraphTwoFormat();
    text("Your heads up display will continue to monitor your status and collect data as the mission progresses. Here's the breakdown...", 
      paragraphXAlign, hudYPos + lineSpacing, paragraphWidth, height);
    text("~Health and Fuel~\nThe most important will be your health and fuel, which is displayed at the bottom left of your screen. At first when you take a"+
      " hit, your health bars will decrease and show in blue to signal you are protected. If your health drops to only one bar, your HUD will"+
      " show your health in red to warn you and an alarm will sound. Be sure to keep an eye on this...if it drops to zero it's game over."+
      "\n\n~Progress Bar~\nAs you navigate the incoming meteoroids, you will see your location update on the progress"+
      " bar, which is displayed on the bottom right of the screen. Your rocket is indicated by a green triangle, and the lines represent the"+
      " checkpoints along the way. To the left of the progress bar is the remaining distance to your target displayed in kliometres.\n\n~Stopwatch~\n"+
      "Lastly, you can see your current time on the stopwatch displayed at the top left corner of the screen, which records your time in minutes"+
      " and seconds. If you are awarded a bonus or given a penalty, it will display as either green or red respectively.", 
      paragraphXAlign, hudYPos + lineSpacing*5, paragraphWidthWithImage, height*2);
    imageMode(CENTER);
    image(imgHealthFuel, imageXAlign, hudYPos + imageSpacing*1.1f);
    createImageBorder(imgHealthFuel, imageXAlign, hudYPos + imageSpacing*1.1f);
    image(imgProgress, imageXAlign, hudYPos + imageSpacing*3.65f);
    createImageBorder(imgProgress, imageXAlign, hudYPos + imageSpacing*3.65f);
    image(imgStopwatch, imageXAlign, hudYPos + imageSpacing*5.43f);
    createImageBorder(imgStopwatch, imageXAlign, hudYPos + imageSpacing*5.43f);

    float checkpointsYPos = objectiveYPos + height*1.65f;
    headingFormat();
    text("CHECKPOINTS", guidePos.x, checkpointsYPos);
    paragraphTwoFormat();
    text("Along your journey you will pass through a number of checkpoints. These are 'safe zones' where you will be protected from"+
      " the meteoroids around you. In this zone, you will receive repairs to your ship where two bars of health and fuel are replenished."+
      " In total there are five stages with four checkpoints separating them at every 50km. Use them wisely!", 
      paragraphXAlign, checkpointsYPos + lineSpacing, paragraphWidth, height);

    float stagesYPos = checkpointsYPos + height*0.35f;
    headingFormat();
    text("STAGES", guidePos.x, stagesYPos);
    paragraphTwoFormat();
    text("The mission progresses in stages. There are a total of five volleys of meteoroids and each one is difficult to navigate"+
      " in its own way. Be extra careful, as these meteoroids vary widely in shape, size, speed and direction. You may choose to speed"+
      " through...just be prepared for the unexpected.", 
      paragraphXAlign, stagesYPos + lineSpacing, paragraphWidth, height);

    float yourRocketYPos = stagesYPos + height*0.32f;
    headingFormat();
    text("YOUR ROCKET", guidePos.x, yourRocketYPos);
    paragraphTwoFormat();
    text("Ah yes, a state-of-the-art spacecraft indeed. Your rocket is outfitted with the latest warp drive and shields on the"+
      " market to make your space travel a pleasant experience. She's an expensive one, so you should probably revise the"+
      " instruction manual before embarking on your mission. I'll give you the rundown on what she's packing though.", 
      paragraphXAlign, yourRocketYPos + lineSpacing, paragraphWidth, height);
    text("~Health and Fuel~\nYour rocket is currently holding four bars of health and fuel, however if you're careful enough"+
      " you will find these can be filled to a maximum of 8 through checkpoint repairs. Also, any bars you gain will be carried"+
      " over to the next stage so no need to worry.\n\n~Shield~\nIn the event you take a hit on your mission, you will lose 1"+
      " bar of health, but for a short duration you will be invulnerable as your shield protects you. It will prevent any"+
      " further damage while it is active, but once it goes down you are immediately vulnerable to impact again. Use it wisely."+
      "\n\n~Boosters~\nTo make your navigation a little easier, your rocket has the ability to quickly evade incoming danger with"+
      " the aid of its side boosters. Using these boosters will save you in the event of imminent danger, however it will cost"+
      " you 1 bar of fuel for every activation.\n\n~Warp Drive~\nThe most exciting part is definitely the warp drive. Feeling"+
      " overwhelmed by meteoroids? Want to reach the checkpoint much faster? Fret not, the warp drive is here for you. Simply"+
      " activate and you will be sent shooting ahead in an instant. Use with caution however, during warp you have no control"+
      " over where you emerge on the other side and you may be put in the path of inescapable danger. Also, the warp drive is"+
      " expensive to operate. It will cost you 4 bars of fuel for every jump you make.", 
      paragraphXAlign, yourRocketYPos + lineSpacing*9.6f, paragraphWidthWithImage, height*2);
    imageMode(CENTER);
    image(imgMoreHealth, imageXAlign, yourRocketYPos + imageSpacing*1.8f);
    createImageBorder(imgMoreHealth, imageXAlign, yourRocketYPos + imageSpacing*1.8f);
    image(imgShield, imageXAlign, yourRocketYPos + imageSpacing*3.7f);
    createImageBorder(imgShield, imageXAlign, yourRocketYPos + imageSpacing*3.7f);
    image(imgBoost, imageXAlign, yourRocketYPos + imageSpacing*5.43f);
    createImageBorder(imgBoost, imageXAlign, yourRocketYPos + imageSpacing*5.43f);
    image(imgWarp, imageXAlign, yourRocketYPos + imageSpacing*7.5f);
    createImageBorder(imgWarp, imageXAlign, yourRocketYPos + imageSpacing*7.5f);

    float timeYPos = yourRocketYPos + height*1.88f;
    headingFormat();
    text("TIME TRIALS", guidePos.x, timeYPos);
    paragraphTwoFormat();
    text("You main goal is getting the moon. However, your secondary objective is doing so in the fastest possible time to"+
      " set a new time record! Every second counts. As such, you should be aware of a few things that may impact (no pun intended)"+
      " your time. During your mission you may wish to use your warp drive, however doing so will bend space and time and you may"+
      " find yourself in the future or the past, so use it sparingly. Then there's the meteoroids. For every meteoroid you hit, it"+
      " will send you backwards and cost you 5 seconds. At this point you may think its best to stay steady and fly cautiously,"+
      " however it is in your favour to fly dangerously. If you reach the next checkpoint in under 35 seconds you will be awarded"+
      " a time bonus of 10 seconds, enough to offset a couple time penalties. Additionally, for every health bar above 1 that you keep"+
      ", you will be awarded 5 seconds upon finishing.\n\nGetting to the moon faster than others will earn you"+
      " a place on the leaderboard. Best of luck captain! I know you can do it.", 
      paragraphXAlign, timeYPos + lineSpacing, paragraphWidth, height);

    rectMode(CENTER);
    fill(0, 0, 0);
    noStroke();
    rect(width/2, height, width, height*0.2f);  // backing to hide scrolling text
    rect(width/2, 0, width, height*0.2f);

    headingFormat();
    textSize(width/10);
    text("GUIDE", width/2, height/15.3f);

    textFont(font);
    textSize(width/20);
    fill(255, 255, 100);
    text("Press G to return to title screen", width/2, height/2 + height/2.17f);

    displayScrollbar(guidePos);
    menuFadeIn();
  }

  public void displayScrollbar(PVector pos) {
    float scrollBarWidth = width*0.01f;
    float scrollBarHeight = height*0.88f - height*0.145f;
    float guideStartPos = height/6;    // starting position of top of page
    float guidePageHeight = guideScrollLimit - guideStartPos;    // difference between top and bottom scroll boundary of page
    float guidePagePosPercent = pos.y / guidePageHeight;    // turn current page position into percentage
    PVector scrollPos = new PVector(width*0.984f, height*0.155f + guidePagePosPercent*scrollBarHeight);    // multiply percentage based off scroll bar height

    stroke(170);
    strokeWeight(width*0.005f);
    line(scrollPos.x, height*0.12f, scrollPos.x, height*0.88f);    // circle at top and bottom, line connecting and a horizontal line that travels between
    stroke(255);
    strokeWeight(width*0.009f);
    line(scrollPos.x - scrollBarWidth, scrollPos.y, scrollPos.x + scrollBarWidth, scrollPos.y);
    noStroke();
    fill(255, 255, 100);
    circle(scrollPos.x, height*0.12f, width*0.025f);
    circle(scrollPos.x, height*0.88f, width*0.025f);
  }

  public void createImageBorder(PImage img, float xPos, float yPos) {
    stroke(255, 255, 100);
    noFill();
    rectMode(CENTER);
    rect(xPos, yPos, img.width, img.height);
  }

  public void drawGuideBox(PVector pos, PVector dimensions) {
    stroke(255, 255, 100);
    fill(40, 40, 40, 220);
    strokeWeight(width*0.004f);
    rectMode(CORNER);
    rect(pos.x, pos.y, dimensions.x, dimensions.y, width*0.03f);
  }

  public void headingFormat() {
    textFont(titleFont);
    fill(255, 255, 100);
    textSize(width/15);
    textAlign(CENTER);
  }

  public void paragraphOneFormat() {
    textFont(font);
    textSize(width/25);
    fill(230);
  }

  public void paragraphTwoFormat() {
    textFont(font);
    textSize(width/25);
    fill(230);
    textAlign(LEFT);
    rectMode(CORNER);  // for textbox
  }

  public void menuScroll() {
    if (scrollUp && guidePos.y > guideScrollLimit) {
      guidePos.y -= height*0.006f;
      if (keyCode == SHIFT) {
        guidePos.y -= height*0.015f;
      }
    }
    if (scrollDown && guidePos.y < height/6) {
      guidePos.y += height*0.006f;
      if (keyCode == SHIFT) {
        guidePos.y += height*0.015f;
      }
    }
  }

  public void displayPauseScreen() {
    fill(0, 0, 0, 170);
    noStroke();
    rectMode(CENTER);
    rect(width/2, height/2, width, height);
    textFont(font);
    fill(255);
    textAlign(CENTER);
    text("PAUSED", width/2, height/2 - height/20);
    textSize(width*0.05f);
    text("Press any button to resume", width/2, height/2 + height/20);
  }

  public void displayGameOverScreen() {
    // if the game has been lost (health = 0), display the rocket explosion
    // and the game over message with restart instructions

    // game over message
    fill(255, 200, 165);
    textAlign(CENTER);
    textFont(titleFont);
    textSize(width/8);
    text("GAME OVER", width/2, height/2 - height/7);

    // game restart message
    textFont(font);
    textSize(width/20);
    text("Press spacebar to try again...", width/2, height/2 - height/10);
  }

  public void displayWinningScreen() {
    // if the player reaches the goal without losing all health, they finish the game
    float moonSize = height*0.8f;                               // draw the moon (size of 1200px and in the centre of the screen)
    float moonXPos = width/2;
    fill(200, 200, 200);                               // moon shade
    strokeWeight(width*0.02f);                                  // moon stroke size
    stroke(130, 130, 130);
    circle(moonXPos, moonYPos, moonSize);              // moon circle
    noStroke();
    fill(100, 100, 100);
    circle(moonXPos - width*0.31f, moonYPos - height*0.05f, width*0.05f);         // moon spot
    circle(moonXPos - width*0.28f, moonYPos - height*0.03f, width*0.08f);         // moon spot
    fill(150, 150, 150);
    circle(moonXPos + width*0.1f, moonYPos - height*0.07f, width*0.26f);       // moon spot
    circle(moonXPos - width*0.21f, moonYPos + height*0.21f, width*0.31f);       // moon spot
    fill(170, 170, 170);
    circle(moonXPos + width*0.1f, moonYPos + height*0.14f, width*0.21f);       // moon spot
    fill(190, 190, 190);
    circle(moonXPos - width*0.1f, moonYPos - height*0.28f, width*0.16f);       // moon spot
    circle(moonXPos - width*0.19f, moonYPos - height*0.17f, width*0.23f);       // moon spot
    circle(moonXPos + width*0.16f, moonYPos - height*0.25f, width*0.07f);        // moon spot
    circle(moonXPos + width*0.31f, moonYPos + height*0.21f, width*0.21f);       // moon spot

    if (moonYPos < height + height*0.045f) {
      moonYPos += height*0.003f;                                   // if the moon hasn't reached the bottom of the screen, increase the y position of the moon
    }

    if (moonYPos > height/2 || moonYPos > rocket.pos.y) {    // if the moon has crossed half way down the screen or passed the rocket, set
      rocket.pos.x = width/2;                                // the rocket position at the centre of the screen and sitting on top of the moon's
      rocket.pos.y = height/1.65f;                            // stop position
    }

    if (moonYPos > height/2) {
      fill(255, 255, 100);             // display winning message and restart instructions
      textFont(titleFont);
      textSize(width/8);
      textAlign(CENTER);
      text("YOU WON!", width/2, height/8);
      textFont(font);
      textSize(width/20);
      fill(195, 220, 255);
      text("Press spacebar to try again", width/2, height/6);
    }

    if (gameWon) {                                     // if the game has finished, display a green flag on a pole flying down to
      float flagXPos = rocket.pos.x + width*0.1f;                   // rest next to the rocket on the moon
      fill(230, 230, 230);
      rect(flagXPos, flagYPos, width*0.01f, height*0.1f, width*0.005f);
      fill(0, 255, 0);
      triangle(flagXPos, flagYPos - height*0.05f, flagXPos + width*0.04f, flagYPos - height*0.04f, flagXPos, flagYPos - height*0.02f);

      if (flagYPos < rocket.pos.y - 0.014f && moonYPos >= height + height*0.04f) {
        flagYPos += height*0.01f;
      }
    }
  }

  public void displayDamagedScreen() {
    if (innerFade != 0) {
      noStroke();
      fill(255, 0, 0, outerFade);
      ellipse(width/2, 0, width*1.5f, height*0.2f);
      ellipse(width/2, height, width*1.5f, height*0.2f);
      fill(255, 0, 0, innerFade);
      ellipse(width/2, -height/10, width*1.5f, height*0.3f);
      ellipse(width/2, height+height/10, width*1.5f, height*0.3f);
      innerFade -= 0.5f;
      outerFade -= 0.5f;
    }
  }

  public void displayLoadingScreen() {
    textFont(font);
    fill(255);
    background(0);
    textAlign(CENTER);
    text("LOADING...", width/2, height/2);
  }
}
public void collisionControl() {
  if (!rocket.warpDriveOn) {
    if (!rocket.damageTaken && !gameWon) {
      if (meteorfield.rocketHasCollided()) {
        rocket.takeDamage();
      }
    }

    for (int i = 0; i < meteorfield.totalMeteoroids; i++) {
      if (meteorfield.meteoroids[i].impacted) {
        meteorfield.meteoroids[i].explode();
      }
    }
  }
}

public void beginSpawningMeteorfield() {
  if (distance > 3000) {
    meteorfield.renderMeteoroids();
  }
}

class Meteorfield {
  int totalMeteoroids;
  int activeMeteoroidCount;                             // meteoroid properties (amount, position, size)
  int sizeMultiplier;
  int speedMultiplier;
  Meteoroid[] meteoroids;

  Meteorfield() {
    totalMeteoroids = 9;
    activeMeteoroidCount = 9;                             // meteoroid properties (amount, position, size)
    sizeMultiplier = 5;
    speedMultiplier = 1;
  }

  public void generateMeteoroids() {
    meteoroids = new Meteoroid[totalMeteoroids];
    for (int i = 0; i < activeMeteoroidCount; i++) {          // initialise meteoroid values (position, shade, size and speed)
      meteoroids[i] = new Meteoroid();
      meteoroids[i].spawn();
    }
  }

  public void renderMeteoroids() {
    for (int i = 0; i < activeMeteoroidCount; i++) {            // and increment their y position so they come towards the player from the top
      meteoroids[i].render();

      if (rocket.warpDriveOn) {                                   // if player has activated warp speed, move the meteoroids down the screen faster
        meteoroids[i].pos.y += height*0.06f;
      }

      if (rocket.canMove() && rocket.accelerate) {
        meteoroids[i].pos.y += height*0.004f * speedMultiplier;
      }

      if (rocket.canMove() && rocket.decelerate) {
        meteoroids[i].pos.y -= height*0.0008f * speedMultiplier;
      }

      if (meteoroids[i].needsRespawning()) {          // if a meteoroid falls off the screen
        meteoroids[i].spawn();
      }
    }
  }

  public void explodeAll() {
    if (!checkpoint.inCheckpointZone) {
      for (int i = 0; i < activeMeteoroidCount; i++) {
        meteoroids[i].impact();
      }
    }
  }

  public boolean rocketHasCollided() {
    for (int i = 0; i < activeMeteoroidCount; i++) {      // for each meteoroid, each collision point is checked
      if (meteoroids[i].tooClose(rocket.pos.x, rocket.pos.y - rocket.rktH*0.5f)) {    // nosetip collision check
        return collisionEffect(i);
      }
      if (meteoroids[i].tooClose(rocket.pos.x - rocket.rktW*0.45f, rocket.pos.y + rocket.rktH*0.025f)) {    // left wing collision check
        return collisionEffect(i);
      }
      if (meteoroids[i].tooClose(rocket.pos.x + rocket.rktW*0.45f, rocket.pos.y + rocket.rktH*0.025f)) {    // right wing collision check
        return collisionEffect(i);
      }
      if (meteoroids[i].tooClose(rocket.pos.x - rocket.rktW*0.24f, rocket.pos.y - rocket.rktH*0.21f)) {    // left-mid wing collision check
        return collisionEffect(i);
      }
      if (meteoroids[i].tooClose(rocket.pos.x + rocket.rktW*0.24f, rocket.pos.y - rocket.rktH*0.21f)) {    // right-mid wing collision check
        return collisionEffect(i);
      }
    }
    return false;
  }

  public boolean collisionEffect(int i) {
    sfxImpact.play();
    meteoroids[i].impact();
    stopwatch.penaliseTime();
    return true;
  }

  public void increaseDifficulty() {
    sizeMultiplier--;
    speedMultiplier++;
    activeMeteoroidCount--;
    resetPosition();
  }

  // this might be redundant now, since collisions are detected only for the active meteors
  public void resetPosition() {
    for (int i = 0; i < totalMeteoroids; i++) {        // fixes a bug where unused meteoroids are frozen in place but not drawn due to meteoroidCount
      meteoroids[i].pos.y = (random(height*0.21f, height*1.39f)) * -1;            // decreasing, making an invisible collision
    }
  }
}
class Meteoroid {
  PVector pos = new PVector();
  float size;
  float xSpeed;
  float ySpeed;
  int fillShade;
  int strokeShade;

  public void spawn() {
    pos.x = random(5, width-4);
    pos.y = (random(height*0.21f, height*1.39f)) * -1;
    fillShade = (int)random(100, 220);
    strokeShade = fillShade - (int)random(15, 35);
    size = random(width*0.021f, width*0.063f) * meteorfield.sizeMultiplier;
    xSpeed = random(-width*0.005f, width*0.005f);
    ySpeed = random(height*0.0027f, height*0.005f) * meteorfield.speedMultiplier;
    if (size > width*0.15f) {                      // stop meteoroids that are too big from moving at max possible speed (too difficult otherwise)
      ySpeed = ySpeed/2;
    }
    calculateVertexPoints();
  }

  float[] xPoints = new float[21]; // 2*PI / 0.3 = 20.94 points
  float[] yPoints = new float[21];
  float offset = width*0.03f;

  public void calculateVertexPoints() {
    int idx = 0;
    for (float a = 0; a < TWO_PI; a += 0.3f) {
      float r = random((size-offset)/2, (size+offset)/2);
      float x = (r * cos(a));
      float y = (r * sin(a));
      xPoints[idx] = x;
      yPoints[idx] = y;
      idx++;
    }
  }

  public void render() {
    fill(fillShade);
    stroke(strokeShade);
    strokeWeight(width*0.006f);
    beginShape();
    for (int i = 0; i < xPoints.length; i++) {
      vertex(xPoints[i] + pos.x, yPoints[i] + pos.y);
    }
    endShape(CLOSE);
    
    if (pos.x < size/2) {
      xSpeed *= -1;
      pos.x += 5;
    }
    if (pos.x > width - size/2) {
      xSpeed *= -1;
      pos.x -= 5;
    }
    
    if (!checkpoint.inCheckpointZone) {
      pos.x += xSpeed;
      pos.y += ySpeed;
    }
  }

  public boolean tooClose(float rktXPos, float rktYPos) {
    if (dist(rktXPos, rktYPos, pos.x, pos.y) < size/2) {
      return true;
    }
    return false;
  }


  PVector impactPos;
  boolean impacted = false;
  int numCircles = 8;
  float[] explodeDistance = new float[numCircles];
  float[] explodeSize = new float[numCircles];
  float[] explodeSpeed = new float[numCircles];

  public void impact() {
    impactPos = new PVector(pos.x, pos.y);
    spawn();
    impacted = true;

    for (int i = 0; i < numCircles; i++) {
      explodeDistance[i] = 0;
      explodeSpeed[i] = random(10);
      explodeSize[i] = random(size/2);
    }
  }

  public void explode() {
    for (int i = 0; i < numCircles; i++) {
      if (explodeDistance[i] > height * 1.2f) {
        impacted = false;
      }
      explodeDistance[i] += explodeSpeed[i];
    }
    fill(fillShade);
    noStroke();
    circle(impactPos.x + explodeDistance[0], impactPos.y, explodeSize[0]);
    circle(impactPos.x - explodeDistance[1], impactPos.y, explodeSize[1]);
    circle(impactPos.x, impactPos.y + explodeDistance[2], explodeSize[2]);
    circle(impactPos.x, impactPos.y - explodeDistance[3], explodeSize[3]);

    circle(impactPos.x + explodeDistance[4], impactPos.y + explodeDistance[4], explodeSize[4]);
    circle(impactPos.x - explodeDistance[5], impactPos.y - explodeDistance[5], explodeSize[5]);
    circle(impactPos.x + explodeDistance[6], impactPos.y - explodeDistance[6], explodeSize[6]);
    circle(impactPos.x - explodeDistance[7], impactPos.y + explodeDistance[7], explodeSize[7]);
    impactPos.y += ySpeed;
  }

  public boolean needsRespawning() {
    if (pos.y > height + size && !checkpoint.inCheckpointZone && !gameWon && !gameLost) {
      return true;
    }
    return false;
  }
}
public void damageControl() {
  if (rocket.damageTaken) {
    if (rocket.health == 0) {
      rocket.explode();
      gameLost = true;
    } else {
      screens.displayDamagedScreen();
      shield.engageShield();
    }
  }
}

public void rocketControl() {
  if (!gameLost && !screens.guideScreenDisplaying) {
    rocket.update();
    rocket.display();
  }
}

class Rocket {
  PVector pos;               // rocket properties
  int health;
  int startBarAmount = 4;
  int maxBarAmount = 8;
  int repairAmount = 2;
  boolean repaired = false;
  boolean damageTaken;
  int fuel;
  boolean engineOn;

  boolean accelerate = false;      // rocket movement
  boolean decelerate = false;
  boolean bankLeft = false;
  boolean bankRight = false;
  boolean warpDriveOn = false;
  int warpDuration;

  float rktW = 0.147569444f * width;    // rocket width
  float rktH = 0.138888889f * height;   // rocket height

  float wingSize = rktW * 0.14f;
  float wingPos = rktW * 0.04f;
  float flamePos = rktW * 0.03f;
  float bodyTilt = rktW * 0.02f;
  float tiltDir = 0;            // tilt direction
  float bodyTiltDir = 0;

  float leftWingPosDir = 0;
  float rightWingPosDir = 0;

  float explosionSize;
  int explosionTime;

  Rocket() {
    pos = new PVector(width/2, height-height*0.38f);        // rocket starting position
    health = startBarAmount;
    fuel = startBarAmount;
    engineOn = false;                                    // control whether the ship appears active or static
    explosionSize = width*0.05f;                                  // starting size of rocket explosion (drawn if rocket health = 0)
    explosionTime = 255;                                 // starting time of rocket explosion (controls opacity of explosion)
    damageTaken = false;                                    // control condition for rocket shield
  }

  public void repair() {
    if (health <= maxBarAmount - repairAmount) {
      health += repairAmount;
    }
    if (fuel <= maxBarAmount - repairAmount) {
      fuel += repairAmount;
    }
    warpDuration = 0;                         // set warp time to 0 so player doesn't go into the next stage at warp speed
    repaired = true;
  }

  public void update() {
    float leftBoundary = width*0.07f;
    float rightBoundary = width - leftBoundary;
    float topBoundary = height*0.43f;
    float bottomBoundary = height - height*0.06f;
    float verticalSpeed = height*0.003f;
    float horizontalSpeed = width*0.0107f;

    if (canMove()) {         // if the player has started the game, it isn't a check point, the game hasn't
      distance += 20;                                                    // been won and the game hasn't been lost, distance will increase by 20 (1200m/s)

      if (accelerate) {
        if (pos.y > topBoundary) {
          pos.y -= verticalSpeed;                                // if player is accelerating and rocket hasn't reached the top of the screen, move the rocket
        }
        distance += 20;                                  // up the screen and increase speed of rocket (2400m/s)
      }
      if (decelerate && pos.y < bottomBoundary) {     // if player is decelerating and rocket hasn't reached the bottom of the screen, move the rocket
        pos.y += verticalSpeed;                                // down the screen and decrease the speed of the rocket (600m/s)
        distance -= 10;
      }
      if (bankLeft && pos.x > leftBoundary) {                // if player is banking left and the rocket hasn't reached the left side of the screen, move the
        pos.x -= horizontalSpeed;                               // rocket to the left
      }
      if (bankRight && pos.x < rightBoundary) {       // if the player is banking right and the rocket hasn't reached the right side of the screen, move
        pos.x += horizontalSpeed;                               // the rocket to the right
      }
    }

    if (warpDriveOn) {
      engageWarpDrive();
    }
  }

  public void engageWarpDrive() {                        // if warp time hasn't reached 0, warp time decreases and speed is increased by 60 (6,000m/s)
    if (warpDuration != 0) {                    // otherwise, if warp time reaches 0, warp speed is deactivated
      warpDriveOn = true;
      warpDuration--;
      distance += 60;
    } else {
      stopwatch.warpTime();
      warpDriveOn = false;
    }
  }

  public void takeDamage() {
    if (!warpDriveOn) {              // if player hasn't activated warp speed, and if a collision has been detected,
      health--;                  // reduce rocket health by 1 bar (30) and activate the immunity shield
      damageTaken = true;
    }
  }

  public void display() {            // display the rocket if the game hasn't been lost

    if (bankLeft && !gameWon) {                // if banking left and game isn't finished, display the rocket tilting left
      tiltDir = 1;
      bodyTiltDir = -1;
      leftWingPosDir = 0;
      rightWingPosDir = 1;
    } else if (bankRight && !gameWon) {                // if banking right and game isn't finished, display the rocket tilting right
      tiltDir = -1;
      bodyTiltDir = -1;
      leftWingPosDir = 1;
      rightWingPosDir = 0;
    } else {                                      // else, reset tilt to middle
      tiltDir = 0;
      bodyTiltDir = 0;
      leftWingPosDir = 0;
      rightWingPosDir = 0;
    }

    drawEngineFlames();
    drawRocketBody();
  }

  public void drawRocketBody() {
    rectMode(CENTER);
    noStroke();
    fill(180);
    rect(pos.x + bodyTilt*tiltDir, pos.y + rktH*0.25f, rktW*0.2f, rktH*0.06f);                                                                           // engine
    fill(255);
    ellipse(pos.x, pos.y - rktH*0.25f, rktW*0.28f, rktH*0.4f);                                                                         // cockpit
    triangle(pos.x, pos.y - rktH*0.6f, pos.x - rktW*0.1f, pos.y - rktH*0.25f, pos.x + rktW*0.1f, pos.y - rktH*0.25f);        // nosetip
    triangle(pos.x + wingPos*leftWingPosDir, pos.y - rktH*0.45f, pos.x - rktW*0.5f - (wingSize*tiltDir)*-1, pos.y + rktH*0.05f, pos.x, pos.y - rktH*0.05f);                  // front left wing
    triangle(pos.x - wingPos*rightWingPosDir, pos.y - rktH*0.45f, pos.x + rktW*0.5f + wingSize*tiltDir, pos.y + rktH*0.05f, pos.x, pos.y - rktH*0.05f);                  // front right wing
    arc(pos.x - rktW*0.12f - (wingPos*tiltDir)*-1, pos.y + rktH*0.25f, rktW*0.42f + (wingSize*tiltDir)*-1, rktH*0.4f, PI-QUARTER_PI, PI+HALF_PI);                                        // back left wing
    arc(pos.x + rktW*0.12f + wingPos*tiltDir, pos.y + rktH*0.25f, rktW*0.42f + wingSize*tiltDir, rktH*0.4f, PI+HALF_PI, TWO_PI+QUARTER_PI);                                    // back right wing
    rect(pos.x + bodyTilt*tiltDir, pos.y, rktW*0.28f - bodyTilt*bodyTiltDir, rktH*0.5f);                                                                    // main rocket body

    // rocket details
    fill(0, 255, 255);
    strokeWeight(1);
    stroke(200, 100, 20);
    arc(pos.x - bodyTilt*tiltDir, pos.y - rktH*0.3f, rktW*0.21f, rktH*0.25f, PI, TWO_PI);                                                             // cockpit window
    //line(pos.x - rktW*0.18, pos.y + rktH*0.175, pos.x - rktW*0.3 + wingPos*tiltDir, pos.y + rktH*0.325);      // back wing stripes
    //line(pos.x - rktW*0.18, pos.y + rktH*0.125, pos.x - rktW*0.28 + wingPos*tiltDir, pos.y + rktH*0.25);
    line(pos.x + rktW*0.06f + (bodyTilt*tiltDir)*-1, pos.y - rktH*0.25f, pos.x + rktW*0.06f + (bodyTilt*tiltDir)*-1, pos.y + rktH*0.15f);    // body stripes
    line(pos.x + rktW*0.1f + (bodyTilt*tiltDir)*-1, pos.y - rktH*0.175f, pos.x + rktW*0.1f + (bodyTilt*tiltDir)*-1, pos.y + rktH*0.2f);
    textAlign(RIGHT);
    textFont(screens.font);
    textSize(rktW*0.18f);
    text("B", pos.x, pos.y+rktH*0.1f);
    text("X", pos.x, pos.y+rktH*0.2f);
  }

  public void drawEngineFlames() {
    float flameDistance = rktH*0.3f;                                                        // flame distance from the engine
    PVector bigFlameSize = new PVector();            // randomised values for flame sizes (small, medium and big)
    PVector mediumFlameSize = new PVector();
    PVector smallFlameSize = new PVector();
    int bigFlameColour;                                                          // variables to store the colours for the 3 flames
    int mediumFlameColour;
    int smallFlameColour;
    float flameSizeModifier = 1;

    if (engineOn && !checkpoint.inCheckpointZone && !gameWon) {                                // if the player has started the game, it isn't a checkpoint zone and
      if (warpDriveOn) {                                                           // the game hasn't been won, display the engine exhaust flames
        flameSizeModifier = 3;
      } else {
        if (accelerate) {                                                          // if player is accelerating, display bigger
          flameSizeModifier = 2;
        } else if (decelerate) {                                                   // if player is decelerating, display smallest
          flameSizeModifier = 0.5f;
        } else {                                                                   // if no controls are being activated, display
          flameSizeModifier = 1;
        }
      }
      bigFlameSize.x = random(rktW*0.14f * flameSizeModifier, rktH*0.12f * flameSizeModifier);                                         // normal randomised flame sizes
      bigFlameSize.y = random(rktW*0.21f * flameSizeModifier, rktH*0.26f * flameSizeModifier);
      mediumFlameSize.x = random(rktW*0.08f * flameSizeModifier, rktH*0.09f * flameSizeModifier);
      mediumFlameSize.y = random(rktW*0.18f * flameSizeModifier, rktH*0.24f * flameSizeModifier);
      smallFlameSize.x = random(rktW*0.07f * flameSizeModifier, rktH*0.075f * flameSizeModifier);
      smallFlameSize.y = random(rktW*0.14f * flameSizeModifier, rktH*0.02f * flameSizeModifier);

      if (warpDriveOn) {                                                // if player has activated warp speed,
        bigFlameColour = color(30, 30, 220);                            // display flame colours in shades of blue,
        mediumFlameColour = color(90, 190, 255);                        // otherwise display flames in shades of red
        smallFlameColour = color(190, 255, 255);
      } else {
        bigFlameColour = color(220, 30, 30, 200);
        mediumFlameColour = color(255, 190, 90);
        smallFlameColour = color(255, 255, 190);
      }

      noStroke();        // disable outline for all flames
      // big flame
      fill(bigFlameColour);
      ellipse(pos.x + flamePos*tiltDir, pos.y + flameDistance, bigFlameSize.x, bigFlameSize.y);

      // medium flame
      fill(mediumFlameColour);
      ellipse(pos.x + flamePos*tiltDir, pos.y + flameDistance, mediumFlameSize.x, mediumFlameSize.y);

      // small flame
      fill(smallFlameColour);
      ellipse(pos.x + flamePos*tiltDir, pos.y + flameDistance, smallFlameSize.x, smallFlameSize.y);
    }
  }

  public void explode() {
    noStroke();
    fill(255, 0, 0, explosionTime);
    circle(rocket.pos.x, rocket.pos.y, explosionSize);
    if (explosionSize < width * 7) {
      explosionSize += width*0.02f;
      explosionTime--;
    }
  }

  public boolean hasFuel() {
    if (fuel > 0) {
      return true;
    }
    return false;
  }

  public boolean fuelLow() {
    if (fuel <= 1) {
      return true;
    }
    return false;
  }

  public boolean healthLow() {
    if (health <= 1) {
      return true;
    }
    return false;
  }

  public boolean canMove() {
    if (rocket.engineOn && !checkpoint.inCheckpointZone && !warpDriveOn && !gameWon && !gameLost && !gamePaused) {
      return true;
    }
    return false;
  }
}
class Shield {
  int immunityTime = 255;

  public void engageShield() {
    if (immunityTime != 0 && !gameLost) {        // if the rocket shield has been activated, the shield time hasn't run out and
      immunityTime--;                                        // the game hasn't been lost (health isn't 0), display a blue shield around
      fill(100, 200, 200, immunityTime);                     // the rocket with opacity controlled by the shield timer (immunityTime)
      strokeWeight(rocket.rktW*0.04f);
      stroke(200, 255, 255, 150);
      circle(rocket.pos.x, rocket.pos.y - rocket.rktW*0.075f, rocket.rktH*1.15f);
    } else {
      rocket.damageTaken = false;                                      // once immunity time reaches 0, stop displaying the shield
      immunityTime = 255;                                    // set 4.25 seconds of immunity (255 / 60fps = 4.25), 255 for max opacity
      
      screens.innerFade = 130;    // quick fix where damaged screen doesn't show when two consecutive hits are taken
      screens.outerFade = 80;
    }
  }
}
class Starfield {
  int starCount;                                 // star properties (amount, position, size, trail length)
  float[] starXPos;
  float[] starYPos;
  float[] starSize;
  float[] starTrailLength;

  Starfield() {
    starCount = 40;                                 // star properties (amount, position, size, trail length)
    starXPos = new float[starCount];
    starYPos = new float[starCount];
    starSize = new float[starCount];
    starTrailLength = new float[starCount];
    for (int i = 0; i < starXPos.length; i++) {          // initialise star values (position, size and trail length)
      starXPos[i] = random(-15, width-30);
      starYPos[i] = random(-15, height-5);
      starSize[i] = random(1, 7);
      starTrailLength[i] = random(height*0.012f, height*0.12f);
    }
  }

  public void display() {
    fill(255);      // set initial colour to white with no outline
    noStroke();
    for (int i = 0; i < starXPos.length; i++) {             // cycle through all stars
      circle(starXPos[i], starYPos[i], starSize[i]);        // draw stars at current attributes stored in the star arrays
      fill(255);                                            // reset colour to white (only has effect after warp has finished
      if (!rocket.engineOn || checkpoint.inCheckpointZone) {
        starYPos[i] += 3;
      } else if (gameWon) {
        starYPos[i] += 2;
      } else if (gameLost) {
        starYPos[i] += 1;
      } else if (rocket.warpDriveOn) {
        starYPos[i] += random(50, 120);   // update star position
        stroke(255);                      // colour trail as white
        strokeWeight(starSize[i]);        // make thickness of trail same as star size
        line(starXPos[i], starYPos[i], starXPos[i], starYPos[i] + starTrailLength[i]);    // draw trail
        noStroke();                       // reset to no outline
        noFill();                         // make star shape disappear (they don't display correctly with the trails)
      } else if (rocket.accelerate) {
        starYPos[i] += 25;            // if accelerating, stars move faster
      } else if (rocket.decelerate) {
        starYPos[i] += 10;            // if decelerating, stars move slower
      } else {
        starYPos[i] += 15;            // otherwise, stars move at normal speed
      }
      if (starYPos[i] > height + 15) {   // if stars reach the bottom of the screen, respawn them at the top with randomised locations and size
        starXPos[i] = random(-15, width + 15);
        starYPos[i] = random(-30, -5);
        starSize[i] = random(1, 7);
      }
    }
  }
}
public void stopwatchControl() {
  if (rocket.engineOn && !gameWon && !gameLost) {
    stopwatch.setStartTime();
    stopwatch.displayStopwatch(width*0.11f, height*0.03f);
    if (stopwatch.timeAltered) {
      stopwatch.displayTimeAlteration(width*0.11f, height*0.08f, stopwatch.timeAlteration);
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

  public void rewindTimeAfterPause() {
    int rewindAmount = millis() - pauseTime;
    startTime += rewindAmount;
    pauseTimeSet = false;
  }

  public void setPauseTime() {
    if (!pauseTimeSet) {
      pauseTime = millis();
    }
    pauseTimeSet = true;
  }

  public void setStartTime() {
    if (!startTimeSet) {
      startTime = millis();
    }
    startTimeSet = true;
  }

  public void setStopTime() {
    if (!stopTimeSet) {
      stopTime = millis() - startTime;
    }
    stopTimeSet = true;
  }

  public int getTimeSinceStart() {
    return millis() - startTime;
  }

  public void getCheckpointTime() {
    int timeToCheckpoint = getTimeSinceStart() - lastCheckpointTime;
    //timeLimit = timeLimit + 3000 + checkpoint.nextCheckpoint*1000;  // 25s -> 29s -> 34s -> 40s
    if (timeToCheckpoint < timeLimit) {  // took less than time limit to reach next checkpoint
      awardTime(10000); // 10 seconds
    }
    lastCheckpointTime = getTimeSinceStart();  // must set after awarding time bonus
    checkpointTimeSet = true;
  }

  public void awardTime(int amount) {
    timeAlteration = amount;
    startTime += timeAlteration;
    timeAltered = true;
  }

  public void penaliseTime() {
    if (!checkpoint.inCheckpointZone && rocket.health > 1) {    // in case of collision during checkpoint
      timeAlteration = -5000;
      startTime += timeAlteration; // 5 seconds
      timeAltered = true;
    }
  }

  public void warpTime() {
    timeAlteration = (int)random(-5000, 5000);
    startTime += timeAlteration; // +5 to -5 seconds
    timeAltered = true;
  }

  public void displayTimeAlteration(float xPos, float yPos, float value) {
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

  public String timeToString(int timeInMillis, boolean withMillis) {
    int minutes = (timeInMillis / 1000) / 60;
    int seconds = (timeInMillis / 1000) % 60;
    int millis = timeInMillis % 1000;

    if (withMillis) {
      return minutes + "m : " + seconds + "s : " + millis + "ms";
    } else {
      return minutes + "m : " + seconds + "s";
    }
  }

  public void displayStopwatch(float xPos, float yPos) {
    String currentTime = timeToString(getTimeSinceStart(), false);
    drawStopwatchBox(xPos, yPos);
    fill(200, 200, 200);
    textFont(timeFont);
    textSize(width/40);
    textAlign(CENTER);
    text(currentTime, xPos, yPos + height*0.007f);
  }

  public void drawStopwatchBox(float xPos, float yPos) {
    stroke(170, 170, 170);
    fill(100, 100, 100, 200);
    strokeWeight(width*0.004f);
    rectMode(CENTER);
    rect(xPos, yPos, width*0.18f, height*0.03f, width*0.01f);
  }

  public void displayTimeResult(float xPos, float yPos) {
    String timeResult = timeToString(stopTime, true);
    drawTimeDialogueBox(xPos, yPos);
    fill(200, 200, 200);
    textFont(timeFont);
    textSize(width/25);
    textAlign(CENTER);
    text("Your time...", xPos, yPos - height*0.015f);
    text(timeResult, xPos, yPos + height*0.03f);
  }

  public void drawTimeDialogueBox(float xPos, float yPos) {
    stroke(100, 180, 180);
    fill(120, 120, 120, 200);
    strokeWeight(width*0.01f);
    rectMode(CENTER);
    rect(xPos, yPos, width*0.6f, height*0.12f, width*0.02f);
  }
}
  public void settings() {  size(576, 864); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "Astrodash" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
