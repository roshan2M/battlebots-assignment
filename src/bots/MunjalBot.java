package bots;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import arena.BattleBotArena;
import arena.BotInfo;
import arena.Bullet;

/**
 * The Drone is a Bot that moves in squares and only ever fires in the direction
 * it is facing. It also turns if it detects that it has hit something.
 * Occasionally puts out a happy drone message.
 *
 * @author sam.scott
 * @version 1.0 (March 3, 2011)
 */
public class MunjalBot extends Bot {

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
	private String[] messages = { "What's a Munjal?", "I'm a Munjal." };
	/**
	 * Image for drawing
	 */
	Image up, down, left, right, current, overheated;
	/**
	 * For deciding when it is time to change direction
	 */
	private int counter = 50;
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
		g.drawImage(overheated, x, y, Bot.RADIUS * 2, Bot.RADIUS * 2, null);
	}

	/**
	 * Move in squares and fire every now and then.
	 */
	public int getMove(final BotInfo me, boolean shotOK, BotInfo[] liveBots,
			BotInfo[] deadBots, Bullet[] bullets) {

		counter--;

		ArrayList<Bullet> importantBullets = new ArrayList<Bullet>();
		int delta = 20;
		boolean x;
		boolean[] xDirection = new boolean[bullets.length];
		int bottomEdge = 500;
		int rightEdge = 700;

		for (int i = 0; i < bullets.length; i++) {

			if ((me.getX() - delta <= bullets[i].getX() && me.getX() + delta >= bullets[i].getX())
					&& ((me.getY() > bullets[i].getY() && bullets[i]
							.getYSpeed() > 0) || (me.getY() < bullets[i].getY() && bullets[i]
							.getYSpeed() < 0))) {
				importantBullets.add(bullets[i]);
				xDirection[i] = true;
			}
			else if ((me.getY() - delta <= bullets[i].getY() && me.getY() + delta >= bullets[i].getY())
					&& ((me.getX() > bullets[i].getX() && bullets[i]
							.getXSpeed() > 0) || (me.getX() < bullets[i].getX() && bullets[i]
							.getXSpeed() < 0))) {
				importantBullets.add(bullets[i]);
				xDirection[i] = false;
			}

			if (bullets[i].getX() >= me.getX() && bullets[i].getX() <= me.getX() + 2 * Bot.RADIUS) {

				if ((bullets[i].getY() >= me.getY() && bullets[i].getYSpeed() < 0) || (bullets[i].getY() <= me.getY() && bullets[i].getYSpeed() > 0)) {
					importantBullets.add(bullets[i]);
				}

			}
			else if (bullets[i].getY() >= me.getY() && bullets[i].getY() <= me.getY() + 2 * Bot.RADIUS) {

				if ((bullets[i].getX() >= me.getX() && bullets[i].getXSpeed() < 0) || (bullets[i].getX() <= me.getX() && bullets[i].getXSpeed() > 0)) {
					importantBullets.add(bullets[i]);
				}

			}

		}

		// Shooting mechanism is only activated when there is no danger from bullets.
		if (counter % 25 == 0 && importantBullets.size() == 0) {
			return BattleBotArena.FIREUP;
		}

		// Sorts the important bullets based on the distance to the robot.
		Comparator<Bullet> bulletOrder = new Comparator<Bullet>() {
			public int compare(Bullet b1, Bullet b2) {
				if (getDistance(me, b1) > getDistance(me, b2)) {
					return 1;
				}
				else {
					return -1;
				}
			}
		};

		importantBullets.sort(bulletOrder);
		Bullet closest = importantBullets.get(0);
		x = xDirection[importantBullets.indexOf(closest)];

		if (x == true) {
			move = BattleBotArena.LEFT;
		}
		else {
			move = BattleBotArena.UP;
		}

		return move;

	}

	public double getDistance(BotInfo me, Bullet bullet) {

		return Math.hypot(bullet.getX() - me.getX(), bullet.getY() - me.getY());

	}

	/**
	 * Construct and return my name
	 */
	public String getName() {
		if (name == null)
			name = "Drone" + (botNumber < 10 ? "0" : "") + botNumber;
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
		String[] images = { "roomba_up.png", "roomba_down.png",
				"roomba_left.png", "roomba_right.png" };
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
