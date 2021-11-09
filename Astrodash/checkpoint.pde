void checkpointControl() {
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

  void triggerZone() {
    if (distance >= (nextCheckpoint+1)*50000) {
      checkpointStatus[nextCheckpoint] = true;
    }
  }

  boolean zoneTriggered() {
    if (checkpointStatus[nextCheckpoint]) {
      return true;
    }
    return false;
  }

  void displayAlert() {
    fill(150, 230, 255, timer);
    textAlign(CENTER);
    textFont(screens.titleFont);
    textSize(width/12);
    text("Checkpoint Reached", width/2, height/8);
  }

  void activateZone() {
    inCheckpointZone = true;
    if (!rocket.repaired) {
      rocket.repair();
    }
    timer--;                                   // and instantly gets hit by a meteoroid
  }

  void deactivateZone() {
    inCheckpointZone = false;                                // continuous checkpoint bug and rest timer is reset
    nextCheckpoint++;
    stopwatch.checkpointTimeSet = false;
    rocket.repaired = false;
    meteorfield.increaseDifficulty();
    timer = 300;
  }
}
