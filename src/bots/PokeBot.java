package bots;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

import arena.BattleBotArena;
import arena.BotInfo;
import arena.Bullet;

/**
 * The PokeBot is a Bot that prioritizes dodging bullets over shooting.
 * Thus, it tends to stay alive for longer periods of time. If there are
 * no bullets to evade, then the PokeBot looks for tombstones to evade,
 * since it does not want to get stuck. Shooting receives last priority.
 * The PokeBot searches for the closest Bot and closes in on it. Once it
 * is within a certain radius, it shoots.
 * 
 * DODGING MECHANISM
 * - First checks if there are any bullets on the field.
 * - Finds the important bullets within the defined range.
 * (The range is a large plus sign. Thus, the PokeBot
 * checks if a bullet is coming towards it from any
 * direction (up, down, left, right) from within this
 * defined plus sign).
 * - Finds the closest bullet in the defined range.
 * - Depending on the which side of the Bot (top/bottom or
 * left side/right side) the Bullet is coming, the PokeBot
 * moves optimally to dodge it.
 * - Boundary conditions are checked and accounted for
 * (when the PokeBot is on the edge).
 * 
 * EVADING TOMBSTONES MECHANISM
 * - The program checks if there are any tombstones within
 * a radius of 30.
 * - If the PokeBot is moving in the horizontal direction
 * when encountering the tombstone, it moves up or down
 * depending on where the tombstone is (above or below).
 * - If the PokeBot is moving in the vertical direction
 * when encountering the tombstone, it moves left or right
 * depending on where the tombstone is.
 * - Due to the primitive nature of this check, the PokeBot
 * cannot evade more than 1 tombstone at any given time.
 * - Thus, it may get stuck at certain points.
 * 
 * SHOOTING MECHANISM
 * - If the path is clear, the PokeBot then searches for
 * the closest Bot to kill.
 * - Depending on the location of the Bot, the PokeBot
 * moves towards it.
 * - Once the Bot is within a "plus sign", it shoots
 * according to the location of the Bot.
 * 
 * @author Roshan Munjal
 * @version 1.0 (May 11, 2017)
 */
public class PokeBot extends Bot {

	/**
	 * My name
	 */
	String name;
	/**
	 * My next message or null if nothing to say
	 */
	String nextMessage = null;
	/**
	 * Array of happy drone messages
	 */
	private String[] messages = {"This is the evolution of a MunjalBot.", "What's a Pikachu?", "I'm a Pikachu."};
	/**
	 * Image for drawing
	 */
	Image up, down, left, right, current;
	/**
	 * For deciding when it is time to change direction
	 */
	private int counter = 0;
	/**
	 * Current move
	 */
	private int move = BattleBotArena.UP;
	/**
	 * My last location - used for detecting when I am stuck
	 */
	private double x, y;

	/**
	 * Draw the current Drone image
	 */
	public void draw(Graphics g, int x, int y) {
		g.drawImage(current, x, y, Bot.RADIUS * 2, Bot.RADIUS * 2, null);
	}

	/**
	 * This is a PokeBot that can both dodge and shoot.
	 * Firstly, it checks for bullets to dodge. If there
	 * are no bullets to dodge, it checks if there are
	 * any tombstones in the way. If not, then the PokeBot
	 * moves towards the nearest bot in hopes of killing it.
	 */
	public int getMove(BotInfo me, boolean shotOK, BotInfo[] liveBots,
			BotInfo[] deadBots, Bullet[] bullets) {
		
		counter++; // Used to determine when it's OK to shoot.
		int importantBullets = 0; // Stores number of important bullets.
		int bottomEdge = 500; // Stores number of tiles to the bottom edge.
		int rightEdge = 700; // Stores number of tiles to the right edge.
		int increment = 10; // Additional units around the bot to check.
		int delta = Bot.RADIUS; // The radius of the bot.
		double lowestDistanceBullet = 150; // The lowest distance for a bullet to be considered "important".
		double lowestDistanceBot = 350; // The lowest distance for a bot to be considered "important".
		
		/*
		 * DODGING MECHANISM
		 */
		if (bullets.length > 0) {
			Bullet closestBullet = bullets[0]; // Stores the closest bullet.
			
			// Goes through each bullet.
			for (int i = 0; i < bullets.length; i++) {
				
				// If the bullet is within a certain vertical box.
				if (bullets[i].getX() >= me.getX() - increment && bullets[i].getX() <= me.getX() + 2 * delta + increment) {

					// If the bullet is coming towards the bot from above or below.
					if ((bullets[i].getY() >= me.getY() && bullets[i].getYSpeed() < 0) || (bullets[i].getY() <= me.getY() && bullets[i].getYSpeed() > 0)) {
						importantBullets++; // Adds to important bullets.

						// Updates the most important bullet.
						if (getDistanceBullet(me, bullets[i]) < lowestDistanceBullet) {
							closestBullet = bullets[i];
							lowestDistanceBullet = getDistanceBullet(me, bullets[i]);
						}
					}

				}
				// If the bullet is within a certain horizontal box.
				else if (bullets[i].getY() >= me.getY() - increment && bullets[i].getY() <= me.getY() + 2 * delta + increment) {
					
					// If the bullet is coming towards the bot from left or right.
					if ((bullets[i].getX() >= me.getX() && bullets[i].getXSpeed() < 0) || (bullets[i].getX() <= me.getX() && bullets[i].getXSpeed() > 0)) {
						importantBullets++; // Adds to important bullets.

						// Updates the most important bullet.
						if (getDistanceBullet(me, bullets[i]) < lowestDistanceBullet) {
							closestBullet = bullets[i];
							lowestDistanceBullet = getDistanceBullet(me, bullets[i]);
						}
					}

				}

			}
			
			// If there are important bullets.
			if (importantBullets > 0) {
				
				// If the closest bullet moves in the horizontal direction.
				if (closestBullet.getYSpeed() != 0) {
					
					// If the closest bullet is shot on the left side of the robot.
					if (closestBullet.getX() >= me.getX() - increment && closestBullet.getX() <= me.getX() + delta) {
						// If the bullet is shot near the right edge of the screen.
						if (rightEdge - me.getX() <= delta * 3 + increment) {
							return move = BattleBotArena.LEFT;
						}
						// Otherwise, move right;
						else {
							return move = BattleBotArena.RIGHT;
						}
					}
					// If the closest bullet is shot on the right side of the robot.
					else if (closestBullet.getX() >= me.getX() + delta && closestBullet.getX() <= me.getX() + delta * 2 + increment) {
						// If the bullet is shot near the left edge of the screen.
						if (me.getX() <= delta + increment) {
							return move = BattleBotArena.RIGHT;
						}
						// Otherwise, move left.
						else {
							return move = BattleBotArena.LEFT;
						}
					}
					
				}
				// If the closest bullet moves in the vertical direction.
				else if (closestBullet.getXSpeed() != 0) {
					
					// If the closest bullet is coming on the top half of the robot.
					if (closestBullet.getY() >= me.getY() - increment && closestBullet.getY() <= me.getY() + delta) {
						// If the bullet is shot near the bottom edge of the screen.
						if (bottomEdge - me.getY() <= delta * 3 + increment) {
							return move = BattleBotArena.UP;
						} else {
							return move = BattleBotArena.DOWN;
						}
					}
					// If the closest bullet is coming on the bottom half of the robot.
					else if (closestBullet.getY() >= me.getY() + delta && closestBullet.getY() <= me.getY() + delta * 2 + increment) {
						// If the bullet is shot near the top edge of the screen.
						if (me.getY() <= delta + increment) {
							return move = BattleBotArena.DOWN;
						} else {
							return move = BattleBotArena.UP;
						}
					}
				}
			}
			
		}

		/*
		 * EVADING TOMBSTOMES MECHANISM
		 */
		// Goes through each dead bot.
		for (int i = 0; i < deadBots.length; i++) {
			// If the distance between the PokeBot and a dead bot is less than 30, the PokeBot will move out of the way.
			if (getDistanceBot(me, deadBots[i]) <= 30) {
				// If the PokeBot is moving in the horizontal direction.
				if (move == BattleBotArena.LEFT || move == BattleBotArena.RIGHT) {
					// If the dead bot is above, the PokeBot moves down.
					if (deadBots[i].getY() <= me.getY()) {
						return move = BattleBotArena.DOWN;
					}
					// If the dead bot is below, the PokeBot moves up.
					else {
						return move = BattleBotArena.UP;
					}
				}
				// If the PokeBot is moving in the vertical direction.
				else {
					// If the dead bot is to the left, the PokeBot moves right.
					if (deadBots[i].getX() <= me.getX()) {
						return move = BattleBotArena.RIGHT;
					}
					// If the dead bot is to the right, the PokeBot moves left.
					else {
						return move = BattleBotArena.LEFT;
					}
				}
			}
		}
		
		/*
		 * SHOOTING MECHANISM
		 */
		// Shooting mechanism is only activated when there is no danger from bullets.
		if (importantBullets == 0) {
			
			BotInfo closestBot = liveBots[0]; // Stores the closest bot.
			
			// Goes through all live bots.
			for (int i = 0; i < liveBots.length; i++) {
				
				// Calculates minimum distance between PokeBot and a liveBot.
				if (getDistanceBot(me, liveBots[i]) < lowestDistanceBot) {
					closestBot = liveBots[i];
					lowestDistanceBot = getDistanceBot(me, liveBots[i]);
				}
				
			}
			
			// Checks if the closest bot is in a vertical box around PokeBot.
			if (closestBot.getX() >= me.getX() - delta && closestBot.getX() <= me.getX() + delta) {
				
				// Checks if the shot is ok to make.
				if (counter >= 25 && shotOK) {
					counter = 0;
					// Shoots down if the closest bot is below PokeBot.
					if (closestBot.getY() > me.getY()) {
						return BattleBotArena.FIREDOWN;
					}
					// Shoots up if the closest bot is above PokeBot.
					else {
						return BattleBotArena.FIREUP;
					}
				}
				
			}
			// Checks if the closest bot is in a horizontal box around PokeBot.
			else if (closestBot.getY() >= me.getY() - delta && closestBot.getY() <= me.getY() + delta) {
				
				// Checks if the shot is ok to make.
				if (counter >= 25 && shotOK) {
					counter = 0;
					// Shoots right if the closest bot is to the right of PokeBot.
					if (closestBot.getX() > me.getX()) {
						return BattleBotArena.FIRERIGHT;
					}
					// Shoots left if the closest bot is to the left of PokeBot.
					else {
						return BattleBotArena.FIRELEFT;
					}
				}
				
			}
			// These else if's are here if Pokebot cannot shoot the closest bot. The PokeBot then moves towards the closest bot.
			// If the closest bot is to the left of PokeBot.
			else if (closestBot.getX() < me.getX()) {
				
				// If the closest bot is above the Pokebot.
				if (closestBot.getY() < me.getY()) {
					if (Math.abs(closestBot.getX() - me.getX()) > Math.abs(closestBot.getY())) {
						return move = BattleBotArena.LEFT;
					}
					else {
						return move = BattleBotArena.UP;
					}
				}
				// If the closest bot is below the Pokebot.
				else {
					if (Math.abs(closestBot.getX() - me.getX()) > Math.abs(closestBot.getY())) {
						return move = BattleBotArena.LEFT;
					}
					else {
						return move = BattleBotArena.DOWN;
					}
				}
				
			}
			// If the closest bot is to the right of PokeBot.
			else if (closestBot.getX() > me.getX()) {
				
				// If the closest bot is above the Pokebot.
				if (closestBot.getY() < me.getY()) {
					if (Math.abs(closestBot.getX() - me.getX()) > Math.abs(closestBot.getY())) {
						return move = BattleBotArena.RIGHT;
					}
					else {
						return move = BattleBotArena.UP;
					}
				}
				// If the closest bot is below the Pokebot.
				else {
					if (Math.abs(closestBot.getX() - me.getX()) > Math.abs(closestBot.getY())) {
						return move = BattleBotArena.RIGHT;
					}
					else {
						return move = BattleBotArena.DOWN;
					}
				}
				
			}

		}
		
		// Return statement used by program to make sure a move is returned.
		return move;

	}
	
	/**
	 * Returns the distance between a bot (myself) and a bullet.
	 */
	public double getDistanceBullet(BotInfo me, Bullet bullet) {

		return Math.hypot(bullet.getX() - me.getX(), bullet.getY() - me.getY());

	}

	/**
	 * Returns the distance between a bot (myself) and another bot.
	 */
	public double getDistanceBot(BotInfo me, BotInfo bot) {

		return Math.hypot(bot.getX() - me.getX(), bot.getY() - me.getY());

	}

	/**
	 * Construct and return my name
	 */
	public String getName() {
		if (name == null)
			name = "PokeBot";
		return name;
	}

	/**
	 * Team Arena!
	 */
	public String getTeamName() {
		return "Arena";
	}

	/**
	 * Pick a random starting direction
	 */
	public void newRound() {
		int i = (int) (Math.random() * 4);
		if (i == 0) {
			move = BattleBotArena.UP;
			current = up;
		} else if (i == 1) {
			move = BattleBotArena.DOWN;
			current = down;
		} else if (i == 2) {
			move = BattleBotArena.LEFT;
			current = left;
		} else {
			move = BattleBotArena.RIGHT;
			current = right;
		}

	}

	/**
	 * Image names
	 */
	public String[] imageNames() {
		String[] images = { "PikachuNew.png", "PikachuNew.png",
				"PikachuNew.png", "PikachuNew.png" };
		return images;
	}

	/**
	 * Store the loaded images
	 */
	public void loadedImages(Image[] images) {
		if (images != null) {
			current = up = images[0];
			down = images[1];
			left = images[2];
			right = images[3];
		}
	}

	/**
	 * Send my next message and clear out my message buffer
	 */
	public String outgoingMessage() {
		String msg = nextMessage;
		nextMessage = null;
		return msg;
	}

	/**
	 * Required abstract method
	 */
	public void incomingMessage(int botNum, String msg) {

	}

}
