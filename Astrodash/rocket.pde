void damageControl() {
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

void rocketControl() {
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

  float rktW = 0.147569444 * width;    // rocket width
  float rktH = 0.138888889 * height;   // rocket height

  float wingSize = rktW * 0.14;
  float wingPos = rktW * 0.04;
  float flamePos = rktW * 0.03;
  float bodyTilt = rktW * 0.02;
  float tiltDir = 0;            // tilt direction
  float bodyTiltDir = 0;

  float leftWingPosDir = 0;
  float rightWingPosDir = 0;

  float explosionSize;
  int explosionTime;

  Rocket() {
    pos = new PVector(width/2, height-height*0.38);        // rocket starting position
    health = startBarAmount;
    fuel = startBarAmount;
    engineOn = false;                                    // control whether the ship appears active or static
    explosionSize = width*0.05;                                  // starting size of rocket explosion (drawn if rocket health = 0)
    explosionTime = 255;                                 // starting time of rocket explosion (controls opacity of explosion)
    damageTaken = false;                                    // control condition for rocket shield
  }

  void repair() {
    if (health <= maxBarAmount - repairAmount) {
      health += repairAmount;
    }
    if (fuel <= maxBarAmount - repairAmount) {
      fuel += repairAmount;
    }
    warpDuration = 0;                         // set warp time to 0 so player doesn't go into the next stage at warp speed
    repaired = true;
  }

  void update() {
    float leftBoundary = width*0.07;
    float rightBoundary = width - leftBoundary;
    float topBoundary = height*0.43;
    float bottomBoundary = height - height*0.06;
    float verticalSpeed = height*0.003;
    float horizontalSpeed = width*0.0107;

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

  void engageWarpDrive() {                        // if warp time hasn't reached 0, warp time decreases and speed is increased by 60 (6,000m/s)
    if (warpDuration != 0) {                    // otherwise, if warp time reaches 0, warp speed is deactivated
      warpDriveOn = true;
      warpDuration--;
      distance += 60;
    } else {
      stopwatch.warpTime();
      warpDriveOn = false;
    }
  }

  void takeDamage() {
    if (!warpDriveOn) {              // if player hasn't activated warp speed, and if a collision has been detected,
      health--;                  // reduce rocket health by 1 bar (30) and activate the immunity shield
      damageTaken = true;
    }
  }

  void display() {            // display the rocket if the game hasn't been lost

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

  void drawRocketBody() {
    rectMode(CENTER);
    noStroke();
    fill(180);
    rect(pos.x + bodyTilt*tiltDir, pos.y + rktH*0.25, rktW*0.2, rktH*0.06);                                                                           // engine
    fill(255);
    ellipse(pos.x, pos.y - rktH*0.25, rktW*0.28, rktH*0.4);                                                                         // cockpit
    triangle(pos.x, pos.y - rktH*0.6, pos.x - rktW*0.1, pos.y - rktH*0.25, pos.x + rktW*0.1, pos.y - rktH*0.25);        // nosetip
    triangle(pos.x + wingPos*leftWingPosDir, pos.y - rktH*0.45, pos.x - rktW*0.5 - (wingSize*tiltDir)*-1, pos.y + rktH*0.05, pos.x, pos.y - rktH*0.05);                  // front left wing
    triangle(pos.x - wingPos*rightWingPosDir, pos.y - rktH*0.45, pos.x + rktW*0.5 + wingSize*tiltDir, pos.y + rktH*0.05, pos.x, pos.y - rktH*0.05);                  // front right wing
    arc(pos.x - rktW*0.12 - (wingPos*tiltDir)*-1, pos.y + rktH*0.25, rktW*0.42 + (wingSize*tiltDir)*-1, rktH*0.4, PI-QUARTER_PI, PI+HALF_PI);                                        // back left wing
    arc(pos.x + rktW*0.12 + wingPos*tiltDir, pos.y + rktH*0.25, rktW*0.42 + wingSize*tiltDir, rktH*0.4, PI+HALF_PI, TWO_PI+QUARTER_PI);                                    // back right wing
    rect(pos.x + bodyTilt*tiltDir, pos.y, rktW*0.28 - bodyTilt*bodyTiltDir, rktH*0.5);                                                                    // main rocket body

    // rocket details
    fill(0, 255, 255);
    strokeWeight(1);
    stroke(200, 100, 20);
    arc(pos.x - bodyTilt*tiltDir, pos.y - rktH*0.3, rktW*0.21, rktH*0.25, PI, TWO_PI);                                                             // cockpit window
    //line(pos.x - rktW*0.18, pos.y + rktH*0.175, pos.x - rktW*0.3 + wingPos*tiltDir, pos.y + rktH*0.325);      // back wing stripes
    //line(pos.x - rktW*0.18, pos.y + rktH*0.125, pos.x - rktW*0.28 + wingPos*tiltDir, pos.y + rktH*0.25);
    line(pos.x + rktW*0.06 + (bodyTilt*tiltDir)*-1, pos.y - rktH*0.25, pos.x + rktW*0.06 + (bodyTilt*tiltDir)*-1, pos.y + rktH*0.15);    // body stripes
    line(pos.x + rktW*0.1 + (bodyTilt*tiltDir)*-1, pos.y - rktH*0.175, pos.x + rktW*0.1 + (bodyTilt*tiltDir)*-1, pos.y + rktH*0.2);
    textAlign(RIGHT);
    textFont(screens.font);
    textSize(rktW*0.18);
    text("B", pos.x, pos.y+rktH*0.1);
    text("X", pos.x, pos.y+rktH*0.2);
  }

  void drawEngineFlames() {
    float flameDistance = rktH*0.3;                                                        // flame distance from the engine
    PVector bigFlameSize = new PVector();            // randomised values for flame sizes (small, medium and big)
    PVector mediumFlameSize = new PVector();
    PVector smallFlameSize = new PVector();
    color bigFlameColour;                                                          // variables to store the colours for the 3 flames
    color mediumFlameColour;
    color smallFlameColour;
    float flameSizeModifier = 1;

    if (engineOn && !checkpoint.inCheckpointZone && !gameWon) {                                // if the player has started the game, it isn't a checkpoint zone and
      if (warpDriveOn) {                                                           // the game hasn't been won, display the engine exhaust flames
        flameSizeModifier = 3;
      } else {
        if (accelerate) {                                                          // if player is accelerating, display bigger
          flameSizeModifier = 2;
        } else if (decelerate) {                                                   // if player is decelerating, display smallest
          flameSizeModifier = 0.5;
        } else {                                                                   // if no controls are being activated, display
          flameSizeModifier = 1;
        }
      }
      bigFlameSize.x = random(rktW*0.14 * flameSizeModifier, rktH*0.12 * flameSizeModifier);                                         // normal randomised flame sizes
      bigFlameSize.y = random(rktW*0.21 * flameSizeModifier, rktH*0.26 * flameSizeModifier);
      mediumFlameSize.x = random(rktW*0.08 * flameSizeModifier, rktH*0.09 * flameSizeModifier);
      mediumFlameSize.y = random(rktW*0.18 * flameSizeModifier, rktH*0.24 * flameSizeModifier);
      smallFlameSize.x = random(rktW*0.07 * flameSizeModifier, rktH*0.075 * flameSizeModifier);
      smallFlameSize.y = random(rktW*0.14 * flameSizeModifier, rktH*0.02 * flameSizeModifier);

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

  void explode() {
    noStroke();
    fill(255, 0, 0, explosionTime);
    circle(rocket.pos.x, rocket.pos.y, explosionSize);
    if (explosionSize < width * 7) {
      explosionSize += width*0.02;
      explosionTime--;
    }
  }

  boolean hasFuel() {
    if (fuel > 0) {
      return true;
    }
    return false;
  }

  boolean fuelLow() {
    if (fuel <= 1) {
      return true;
    }
    return false;
  }

  boolean healthLow() {
    if (health <= 1) {
      return true;
    }
    return false;
  }

  boolean canMove() {
    if (rocket.engineOn && !checkpoint.inCheckpointZone && !warpDriveOn && !gameWon && !gameLost && !gamePaused) {
      return true;
    }
    return false;
  }
}
