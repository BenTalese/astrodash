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

void setup() {              // initialise the game properties
  size(576, 864);           // 1920p x 1080p = 576,864 .... 3200p x 1800p = 900,1500
  gameReset();
}

void draw() {
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

void checkGameWon() {
  if (distance >= goalDistance) {
    gameWon = true;
  }
}

void gameReset() {
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

void keyPressed() {                                      // if any of the control keys are pressed, the corresponding boolean is set to true
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


void  keyReleased () {     // if any control keys (up, down, left or right) are released, the rocket will stop moving
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

  float leftBoostBoundary = width * 0.21;
  float rightBoostBoundary = width - leftBoostBoundary;
  float boostDistance = width*0.15;
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
