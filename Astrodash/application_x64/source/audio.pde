import processing.sound.*;
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

void loadAudio() {
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

void musicControl() {
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
      gamePlayMusic.amp(0.4);
      gamePlayMusic.play();
    }
  }

  if (gamePaused) {    // pause screen
    gamePlayMusic.pause();
    if (!mainMenuMusic.isPlaying()) {
      mainMenuMusic.amp(0.2);
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



void sfxControl() {
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
      sfxRktExplode.amp(0.5);
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
