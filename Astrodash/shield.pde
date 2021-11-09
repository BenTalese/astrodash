class Shield {
  int immunityTime = 255;

  void engageShield() {
    if (immunityTime != 0 && !gameLost) {        // if the rocket shield has been activated, the shield time hasn't run out and
      immunityTime--;                                        // the game hasn't been lost (health isn't 0), display a blue shield around
      fill(100, 200, 200, immunityTime);                     // the rocket with opacity controlled by the shield timer (immunityTime)
      strokeWeight(rocket.rktW*0.04);
      stroke(200, 255, 255, 150);
      circle(rocket.pos.x, rocket.pos.y - rocket.rktW*0.075, rocket.rktH*1.15);
    } else {
      rocket.damageTaken = false;                                      // once immunity time reaches 0, stop displaying the shield
      immunityTime = 255;                                    // set 4.25 seconds of immunity (255 / 60fps = 4.25), 255 for max opacity
      
      screens.innerFade = 130;    // quick fix where damaged screen doesn't show when two consecutive hits are taken
      screens.outerFade = 80;
    }
  }
}
