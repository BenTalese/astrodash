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
      starTrailLength[i] = random(height*0.012, height*0.12);
    }
  }

  void display() {
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
