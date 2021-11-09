void collisionControl() {
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

void beginSpawningMeteorfield() {
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

  void generateMeteoroids() {
    meteoroids = new Meteoroid[totalMeteoroids];
    for (int i = 0; i < activeMeteoroidCount; i++) {          // initialise meteoroid values (position, shade, size and speed)
      meteoroids[i] = new Meteoroid();
      meteoroids[i].spawn();
    }
  }

  void renderMeteoroids() {
    for (int i = 0; i < activeMeteoroidCount; i++) {            // and increment their y position so they come towards the player from the top
      meteoroids[i].render();

      if (rocket.warpDriveOn) {                                   // if player has activated warp speed, move the meteoroids down the screen faster
        meteoroids[i].pos.y += height*0.06;
      }

      if (rocket.canMove() && rocket.accelerate) {
        meteoroids[i].pos.y += height*0.004 * speedMultiplier;
      }

      if (rocket.canMove() && rocket.decelerate) {
        meteoroids[i].pos.y -= height*0.0008 * speedMultiplier;
      }

      if (meteoroids[i].needsRespawning()) {          // if a meteoroid falls off the screen
        meteoroids[i].spawn();
      }
    }
  }

  void explodeAll() {
    if (!checkpoint.inCheckpointZone) {
      for (int i = 0; i < activeMeteoroidCount; i++) {
        meteoroids[i].impact();
      }
    }
  }

  boolean rocketHasCollided() {
    for (int i = 0; i < activeMeteoroidCount; i++) {      // for each meteoroid, each collision point is checked
      if (meteoroids[i].tooClose(rocket.pos.x, rocket.pos.y - rocket.rktH*0.5)) {    // nosetip collision check
        return collisionEffect(i);
      }
      if (meteoroids[i].tooClose(rocket.pos.x - rocket.rktW*0.45, rocket.pos.y + rocket.rktH*0.025)) {    // left wing collision check
        return collisionEffect(i);
      }
      if (meteoroids[i].tooClose(rocket.pos.x + rocket.rktW*0.45, rocket.pos.y + rocket.rktH*0.025)) {    // right wing collision check
        return collisionEffect(i);
      }
      if (meteoroids[i].tooClose(rocket.pos.x - rocket.rktW*0.24, rocket.pos.y - rocket.rktH*0.21)) {    // left-mid wing collision check
        return collisionEffect(i);
      }
      if (meteoroids[i].tooClose(rocket.pos.x + rocket.rktW*0.24, rocket.pos.y - rocket.rktH*0.21)) {    // right-mid wing collision check
        return collisionEffect(i);
      }
    }
    return false;
  }

  boolean collisionEffect(int i) {
    sfxImpact.play();
    meteoroids[i].impact();
    stopwatch.penaliseTime();
    return true;
  }

  void increaseDifficulty() {
    sizeMultiplier--;
    speedMultiplier++;
    activeMeteoroidCount--;
    resetPosition();
  }

  // this might be redundant now, since collisions are detected only for the active meteors
  void resetPosition() {
    for (int i = 0; i < totalMeteoroids; i++) {        // fixes a bug where unused meteoroids are frozen in place but not drawn due to meteoroidCount
      meteoroids[i].pos.y = (random(height*0.21, height*1.39)) * -1;            // decreasing, making an invisible collision
    }
  }
}
