class Meteoroid {
  PVector pos = new PVector();
  float size;
  float xSpeed;
  float ySpeed;
  int fillShade;
  int strokeShade;

  void spawn() {
    pos.x = random(5, width-4);
    pos.y = (random(height*0.21, height*1.39)) * -1;
    fillShade = (int)random(100, 220);
    strokeShade = fillShade - (int)random(15, 35);
    size = random(width*0.021, width*0.063) * meteorfield.sizeMultiplier;
    xSpeed = random(-width*0.005, width*0.005);
    ySpeed = random(height*0.0027, height*0.005) * meteorfield.speedMultiplier;
    if (size > width*0.15) {                      // stop meteoroids that are too big from moving at max possible speed (too difficult otherwise)
      ySpeed = ySpeed/2;
    }
    calculateVertexPoints();
  }

  float[] xPoints = new float[21]; // 2*PI / 0.3 = 20.94 points
  float[] yPoints = new float[21];
  float offset = width*0.03;

  void calculateVertexPoints() {
    int idx = 0;
    for (float a = 0; a < TWO_PI; a += 0.3) {
      float r = random((size-offset)/2, (size+offset)/2);
      float x = (r * cos(a));
      float y = (r * sin(a));
      xPoints[idx] = x;
      yPoints[idx] = y;
      idx++;
    }
  }

  void render() {
    fill(fillShade);
    stroke(strokeShade);
    strokeWeight(width*0.006);
    beginShape();
    for (int i = 0; i < xPoints.length; i++) { //<>//
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

  boolean tooClose(float rktXPos, float rktYPos) {
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

  void impact() {
    impactPos = new PVector(pos.x, pos.y);
    spawn();
    impacted = true;

    for (int i = 0; i < numCircles; i++) {
      explodeDistance[i] = 0;
      explodeSpeed[i] = random(10);
      explodeSize[i] = random(size/2);
    }
  }

  void explode() {
    for (int i = 0; i < numCircles; i++) {
      if (explodeDistance[i] > height * 1.2) {
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

  boolean needsRespawning() {
    if (pos.y > height + size && !checkpoint.inCheckpointZone && !gameWon && !gameLost) {
      return true;
    }
    return false;
  }
}
