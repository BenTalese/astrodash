void hudControl() {
  if (rocket.engineOn && !gameWon) {
    hud.display();
  }
}

class HeadsUpDisplay {
  void display() {
    PVector hudPos = new PVector (width * 0.1, height - height * 0.03); // position of health and fuel bar
    PVector barDimensions = new PVector (width * 0.05, height * 0.03);
    float roundedness = width*0.01;
    strokeWeight(width*0.004);             // outline width of bars

    renderHealthGauge(hudPos, barDimensions, roundedness);
    renderHealthGaugeIcon(hudPos, barDimensions);
    renderFuelGauge(hudPos, barDimensions, roundedness);
    renderFuelGaugeIcon(hudPos, barDimensions);
    renderProgressGauge();
  }

  void renderHealthGauge(PVector pos, PVector dimensions, float corners) {
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

  void renderHealthGaugeIcon(PVector pos, PVector dimensions) {
    PVector iconPos = new PVector(pos.x - dimensions.x*1.2, pos.y);
    if (rocket.healthLow()) {
      noFill();
    }
    if (rocket.health == 0) {
      stroke(100, 0, 0);
    }
    strokeWeight(width*0.004);
    circle(iconPos.x, iconPos.y, dimensions.x*0.8);
    strokeWeight(width*0.006);
    line(iconPos.x - dimensions.x/4, pos.y, iconPos.x + dimensions.x/4, pos.y);
    line(iconPos.x, iconPos.y + dimensions.y/3.5, iconPos.x, iconPos.y - dimensions.y/3.7);
  }

  void renderFuelGauge(PVector pos, PVector dimensions, float corners) {
    for (float i = 0; i < rocket.fuel*dimensions.x; i += dimensions.x) {       // display a bar for each lot of 30 fuel, increase the x pos of the next fuel bar by the same width
      if (rocket.fuelLow()) {
        fill(200, 0, 0);
        stroke(100, 0, 0);
      } else {
        fill(255, 255, 0);                             // display fuel bars in yellow
        stroke(100, 100, 0);
      }
      rect(pos.x + i, pos.y - dimensions.y*1.2, dimensions.x, dimensions.y, corners);    // draw the bars of fuel based off values
    }
  }

  void renderFuelGaugeIcon(PVector pos, PVector dimensions) {
    PVector iconPos = new PVector(pos.x - dimensions.x*1.2, pos.y - dimensions.y*1.05);
    textAlign(CENTER);
    textFont(screens.font);
    textSize(width/30);
    if (rocket.hasFuel()) {
      fill(255, 255, 0);
      quad(iconPos.x-width*0.026, iconPos.y-height*0.017, iconPos.x+width*0.026, iconPos.y-height*0.017, iconPos.x+width*0.01, iconPos.y+height*0.006, iconPos.x-width*0.01, iconPos.y+height*0.006);
      fill(0);
      text("F", iconPos.x, iconPos.y+height*0.003); // full
    } else {
      noFill();
      stroke(100, 100, 0);
      quad(iconPos.x-width*0.026, iconPos.y-height*0.017, iconPos.x+width*0.026, iconPos.y-height*0.017, iconPos.x+width*0.01, iconPos.y+height*0.006, iconPos.x-width*0.01, iconPos.y+height*0.006);
      fill(255, 0, 0);
      text("E", iconPos.x, iconPos.y+height*0.003); // empty
    }
  }

  void renderProgressGauge() {
    float progressBarXPos = width - width * 0.03125;            // x position of progress bar
    float progressBarYPos = height - height * 0.1;          // y position of progress bar
    float progressBarWidth = width * 0.03;
    float progressBarHeight = height * 0.18;                 // height of the progress bar 155.52
    float progress = ((distance / 1000.0) / progressBarHeight) * 100;              // divide the current distance by 1000 so it can be more easily displayed in the progress bar (values: 0 - 250)

    noFill();
    stroke(200, 200, 200);
    rect(progressBarXPos, progressBarYPos, progressBarWidth, progressBarHeight);       // draw the progress bar based off values

    // display checkpoints on progress bar
    for (float i = progressBarHeight * 0.2; i <= progressBarHeight * 0.8; i += progressBarHeight * 0.2) {          // for each 50,000m (50/250), display a horizontal line on the progress bar showing where the checkpoint are
      line(progressBarXPos - width*0.015, (progressBarYPos + progressBarHeight/2) - i, progressBarXPos + width*0.015, (progressBarYPos + progressBarHeight/2) - i);
    }

    // rocket position indicator
    fill(0, 255, 0);                // based off the current progress value, display a green arrow which indicates where the rocket is
    stroke(0, 200, 0);
    strokeWeight(width*0.004);
    PVector indicatorTopCorner = new PVector(progressBarXPos, (progressBarYPos + progressBarHeight/2 - progressBarHeight*0.01) - progress);
    PVector indicatorLeftCorner = new PVector(progressBarXPos - width*0.0052, (progressBarYPos + progressBarHeight/2 + progressBarHeight*0.04) - progress);
    PVector indicatorRightCorner = new PVector(progressBarXPos + width*0.0053, (progressBarYPos + progressBarHeight/2 + progressBarHeight*0.04) - progress);
    triangle(indicatorTopCorner.x, indicatorTopCorner.y, indicatorLeftCorner.x, indicatorLeftCorner.y, indicatorRightCorner.x, indicatorRightCorner.y);

    // moon indicator
    fill(200, 200, 200);            // display a tiny moon at the top of the progress bar
    stroke(150, 150, 150);
    circle(progressBarXPos, progressBarYPos - height*0.099, width*0.05);

    int distanceToMoon = (int)(goalDistance - distance)/1000;
    textAlign(RIGHT);
    textFont(stopwatch.timeFont);
    textSize(width/30);
    text(distanceToMoon+"km", progressBarXPos - progressBarWidth*1.3, progressBarYPos + progressBarHeight/2);
  }
}
