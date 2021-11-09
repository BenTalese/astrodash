void screenControl() {
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
void loadImages() {
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
  float speed = 2.5; 
  float value = 0.0;
  int max = 255;
  int fadeIn = 255;

  float moonYPos;
  float flagYPos;
  float innerFade;
  float outerFade;

  MenuScreen() {
    moonYPos = -(width*0.41);
    flagYPos = -(width*0.14);
    innerFade = 130;
    outerFade = 80;
  }

  void menuFadeIn() {
    if (fadeIn > 0) {
      fill(0, 0, 0, fadeIn);
      noStroke();
      rectMode(CENTER);
      rect(width/2, height/2, width, height);
      fadeIn--;
    }
  }

  void displayStartScreen() {
    fill(255, 255, 100);
    textAlign(CENTER);
    textFont(titleFont);
    textSize(width/8);
    text("Astro Dash", width/2, height/2 - height/2.8);
    strokeWeight(3);

    stroke(200);
    line(width/2 - width/3, height/2 - height/2.9, width/2 + width/3, height/2 - height/2.9);
    fill(230);
    textFont(font);
    textSize(width/19);
    text("Created by Ben Talese", width/2, height/2 - height/3.3);
    fill(230);
    textSize(width/25);
    text("For Xixi", width/2, height/2 - height/3.7);

    value+=speed;
    float fade = max - abs(value % (2*max) - max);
    fill(255, 255, 100, fade);
    textSize(width/25);
    text("Press W or UP ARROW to start", width/2, height/2);

    fill(255, 255, 100);
    textSize(width/20);
    text("Press G to view guide", width/2, height/2 + height/2.5);

    menuFadeIn();
  }

  PVector guidePos = new PVector(width/2, height/6);
  float guideScrollLimit = (height*5.15)*-1;
  boolean scrollUp;
  boolean scrollDown;
  void displayGuideScreen() {
    menuScroll();
    float lineSpacing = height*0.03;
    float columnSpacing = width*0.35;
    float paragraphWidth = width*0.88;
    float paragraphWidthWithImage = width*0.6;
    float paragraphXAlign = guidePos.x - columnSpacing*1.23;
    float imageSpacing = height*0.2;
    float imageXAlign = width*0.81;

    drawGuideBox(new PVector(guidePos.x - columnSpacing*1.3, guidePos.y - height*0.05), new PVector(width*0.92, height*6.05));

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

    float introductionYPos = guidePos.y + height*0.55;
    headingFormat();
    text("INTRODUCTION", guidePos.x, introductionYPos);
    paragraphTwoFormat();
    text("Ahoy there captain, and welcome to Astrodash. This is a fast-paced race against the clock, so get your helmet ready and blast off into space!"+
      " Your goal is the moon. Get there as fast as you can to secure your place on the leaderboards! Watch out though, the path "+
      "ahead is not safe. Unluckily for you, an asteroid collision has sent a lot of meteoroids coming your way. Think fast captain!"+
      "\n\nBefore I send you on your way though, you may want to be aware of some useful tips...", 
      paragraphXAlign, introductionYPos + lineSpacing, paragraphWidth, height);

    float objectiveYPos = introductionYPos + height*0.48;
    headingFormat();
    text("MISSION OBJECTIVE", guidePos.x, objectiveYPos);
    paragraphTwoFormat();
    text("To succeed in your mission, simply reach the moon by dodging those pesky meteoroids and place our flag on the moon! Although this is no simple task.", 
      paragraphXAlign, objectiveYPos + lineSpacing, paragraphWidth, height);

    float hudYPos = objectiveYPos + height*0.23;
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
    image(imgHealthFuel, imageXAlign, hudYPos + imageSpacing*1.1);
    createImageBorder(imgHealthFuel, imageXAlign, hudYPos + imageSpacing*1.1);
    image(imgProgress, imageXAlign, hudYPos + imageSpacing*3.65);
    createImageBorder(imgProgress, imageXAlign, hudYPos + imageSpacing*3.65);
    image(imgStopwatch, imageXAlign, hudYPos + imageSpacing*5.43);
    createImageBorder(imgStopwatch, imageXAlign, hudYPos + imageSpacing*5.43);

    float checkpointsYPos = objectiveYPos + height*1.65;
    headingFormat();
    text("CHECKPOINTS", guidePos.x, checkpointsYPos);
    paragraphTwoFormat();
    text("Along your journey you will pass through a number of checkpoints. These are 'safe zones' where you will be protected from"+
      " the meteoroids around you. In this zone, you will receive repairs to your ship where two bars of health and fuel are replenished."+
      " In total there are five stages with four checkpoints separating them at every 50km. Use them wisely!", 
      paragraphXAlign, checkpointsYPos + lineSpacing, paragraphWidth, height);

    float stagesYPos = checkpointsYPos + height*0.35;
    headingFormat();
    text("STAGES", guidePos.x, stagesYPos);
    paragraphTwoFormat();
    text("The mission progresses in stages. There are a total of five volleys of meteoroids and each one is difficult to navigate"+
      " in its own way. Be extra careful, as these meteoroids vary widely in shape, size, speed and direction. You may choose to speed"+
      " through...just be prepared for the unexpected.", 
      paragraphXAlign, stagesYPos + lineSpacing, paragraphWidth, height);

    float yourRocketYPos = stagesYPos + height*0.32;
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
      paragraphXAlign, yourRocketYPos + lineSpacing*9.6, paragraphWidthWithImage, height*2);
    imageMode(CENTER);
    image(imgMoreHealth, imageXAlign, yourRocketYPos + imageSpacing*1.8);
    createImageBorder(imgMoreHealth, imageXAlign, yourRocketYPos + imageSpacing*1.8);
    image(imgShield, imageXAlign, yourRocketYPos + imageSpacing*3.7);
    createImageBorder(imgShield, imageXAlign, yourRocketYPos + imageSpacing*3.7);
    image(imgBoost, imageXAlign, yourRocketYPos + imageSpacing*5.43);
    createImageBorder(imgBoost, imageXAlign, yourRocketYPos + imageSpacing*5.43);
    image(imgWarp, imageXAlign, yourRocketYPos + imageSpacing*7.5);
    createImageBorder(imgWarp, imageXAlign, yourRocketYPos + imageSpacing*7.5);

    float timeYPos = yourRocketYPos + height*1.88;
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
    rect(width/2, height, width, height*0.2);  // backing to hide scrolling text
    rect(width/2, 0, width, height*0.2);

    headingFormat();
    textSize(width/10);
    text("GUIDE", width/2, height/15.3);

    textFont(font);
    textSize(width/20);
    fill(255, 255, 100);
    text("Press G to return to title screen", width/2, height/2 + height/2.17);

    displayScrollbar(guidePos);
    menuFadeIn();
  }

  void displayScrollbar(PVector pos) {
    float scrollBarWidth = width*0.01;
    float scrollBarHeight = height*0.88 - height*0.145;
    float guideStartPos = height/6;    // starting position of top of page
    float guidePageHeight = guideScrollLimit - guideStartPos;    // difference between top and bottom scroll boundary of page
    float guidePagePosPercent = pos.y / guidePageHeight;    // turn current page position into percentage
    PVector scrollPos = new PVector(width*0.984, height*0.155 + guidePagePosPercent*scrollBarHeight);    // multiply percentage based off scroll bar height

    stroke(170);
    strokeWeight(width*0.005);
    line(scrollPos.x, height*0.12, scrollPos.x, height*0.88);    // circle at top and bottom, line connecting and a horizontal line that travels between
    stroke(255);
    strokeWeight(width*0.009);
    line(scrollPos.x - scrollBarWidth, scrollPos.y, scrollPos.x + scrollBarWidth, scrollPos.y);
    noStroke();
    fill(255, 255, 100);
    circle(scrollPos.x, height*0.12, width*0.025);
    circle(scrollPos.x, height*0.88, width*0.025);
  }

  void createImageBorder(PImage img, float xPos, float yPos) {
    stroke(255, 255, 100);
    noFill();
    rectMode(CENTER);
    rect(xPos, yPos, img.width, img.height);
  }

  void drawGuideBox(PVector pos, PVector dimensions) {
    stroke(255, 255, 100);
    fill(40, 40, 40, 220);
    strokeWeight(width*0.004);
    rectMode(CORNER);
    rect(pos.x, pos.y, dimensions.x, dimensions.y, width*0.03);
  }

  void headingFormat() {
    textFont(titleFont);
    fill(255, 255, 100);
    textSize(width/15);
    textAlign(CENTER);
  }

  void paragraphOneFormat() {
    textFont(font);
    textSize(width/25);
    fill(230);
  }

  void paragraphTwoFormat() {
    textFont(font);
    textSize(width/25);
    fill(230);
    textAlign(LEFT);
    rectMode(CORNER);  // for textbox
  }

  void menuScroll() {
    if (scrollUp && guidePos.y > guideScrollLimit) {
      guidePos.y -= height*0.006;
      if (keyCode == SHIFT) {
        guidePos.y -= height*0.015;
      }
    }
    if (scrollDown && guidePos.y < height/6) {
      guidePos.y += height*0.006;
      if (keyCode == SHIFT) {
        guidePos.y += height*0.015;
      }
    }
  }

  void displayPauseScreen() {
    fill(0, 0, 0, 170);
    noStroke();
    rectMode(CENTER);
    rect(width/2, height/2, width, height);
    textFont(font);
    fill(255);
    textAlign(CENTER);
    text("PAUSED", width/2, height/2 - height/20);
    textSize(width*0.05);
    text("Press any button to resume", width/2, height/2 + height/20);
  }

  void displayGameOverScreen() {
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

  void displayWinningScreen() {
    // if the player reaches the goal without losing all health, they finish the game
    float moonSize = height*0.8;                               // draw the moon (size of 1200px and in the centre of the screen)
    float moonXPos = width/2;
    fill(200, 200, 200);                               // moon shade
    strokeWeight(width*0.02);                                  // moon stroke size
    stroke(130, 130, 130);
    circle(moonXPos, moonYPos, moonSize);              // moon circle
    noStroke();
    fill(100, 100, 100);
    circle(moonXPos - width*0.31, moonYPos - height*0.05, width*0.05);         // moon spot
    circle(moonXPos - width*0.28, moonYPos - height*0.03, width*0.08);         // moon spot
    fill(150, 150, 150);
    circle(moonXPos + width*0.1, moonYPos - height*0.07, width*0.26);       // moon spot
    circle(moonXPos - width*0.21, moonYPos + height*0.21, width*0.31);       // moon spot
    fill(170, 170, 170);
    circle(moonXPos + width*0.1, moonYPos + height*0.14, width*0.21);       // moon spot
    fill(190, 190, 190);
    circle(moonXPos - width*0.1, moonYPos - height*0.28, width*0.16);       // moon spot
    circle(moonXPos - width*0.19, moonYPos - height*0.17, width*0.23);       // moon spot
    circle(moonXPos + width*0.16, moonYPos - height*0.25, width*0.07);        // moon spot
    circle(moonXPos + width*0.31, moonYPos + height*0.21, width*0.21);       // moon spot

    if (moonYPos < height + height*0.045) {
      moonYPos += height*0.003;                                   // if the moon hasn't reached the bottom of the screen, increase the y position of the moon
    }

    if (moonYPos > height/2 || moonYPos > rocket.pos.y) {    // if the moon has crossed half way down the screen or passed the rocket, set
      rocket.pos.x = width/2;                                // the rocket position at the centre of the screen and sitting on top of the moon's
      rocket.pos.y = height/1.65;                            // stop position
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
      float flagXPos = rocket.pos.x + width*0.1;                   // rest next to the rocket on the moon
      fill(230, 230, 230);
      rect(flagXPos, flagYPos, width*0.01, height*0.1, width*0.005);
      fill(0, 255, 0);
      triangle(flagXPos, flagYPos - height*0.05, flagXPos + width*0.04, flagYPos - height*0.04, flagXPos, flagYPos - height*0.02);

      if (flagYPos < rocket.pos.y - 0.014 && moonYPos >= height + height*0.04) {
        flagYPos += height*0.01;
      }
    }
  }

  void displayDamagedScreen() {
    if (innerFade != 0) {
      noStroke();
      fill(255, 0, 0, outerFade);
      ellipse(width/2, 0, width*1.5, height*0.2);
      ellipse(width/2, height, width*1.5, height*0.2);
      fill(255, 0, 0, innerFade);
      ellipse(width/2, -height/10, width*1.5, height*0.3);
      ellipse(width/2, height+height/10, width*1.5, height*0.3);
      innerFade -= 0.5;
      outerFade -= 0.5;
    }
  }

  void displayLoadingScreen() {
    textFont(font);
    fill(255);
    background(0);
    textAlign(CENTER);
    text("LOADING...", width/2, height/2);
  }
}
