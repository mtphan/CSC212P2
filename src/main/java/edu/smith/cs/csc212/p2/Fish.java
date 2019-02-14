package edu.smith.cs.csc212.p2;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

/**
 * Most Fish behavior lives up in WorldObject (a Fish just looks special!).
 * Or it is in PlayFish, where the missing/found and player fish all act different!
 * 
 * @author jfoley
 */
public class Fish extends WorldObject {
	/**
	 * A fish is only special because of its color now!
	 */
	public static Color[] COLORS = {
			Color.red,
			Color.green.darker(),
			Color.yellow,
			Color.magenta,
			Color.orange.darker(),
			Color.white,
			Color.cyan,
			Color.pink
	};
	/**
	 * This is an index into the {@link #COLORS} array.
	 */
	int color;
	
	/**
	 * Stores the value of the fish - score achieved if this fish is caught.
	 */
	public int value;
	
	/**
	 * 
	 */
	private int stepsTillBored = 20 + rand.nextInt(5);
	
	/**
	 * Whether or not this is the player;
	 */
	boolean player = false;
	
	/**
	 * Chance of being scared.
	 */
	public double scareChance;
	
	/**
	 * Called only on the Fish that is the player!
	 */
	public void markAsPlayer() {
		this.player = true;
	}


	/**
	 * A Fish knows what World it belongs to, because all WorldObjects do.
	 * @param color Color by number.
	 * @param world The world itself.
	 */
	public Fish(int color, World world) {
		super(world);
		this.color = color;
		
		// Pick chance of fish being scared here.
		if (rand.nextBoolean()) {
			scareChance = 0.3;
		} else {
			scareChance = 0.8;
		}
		
		// Give score based on how green it is. Magenta gives highest score. There are some elements of randomness.
		// More points for fish with higher scare chance.
		this.value = (int) ((10000/(getColor().getGreen()+100) + rand.nextInt(10)) * (scareChance*10));
		// Round it to the nearest 10 because it looks nicer that way.
		this.value = (this.value / 10)*10;
	}
	
	/**
	 * What actual color is this fish? We store an index, so get it here.
	 * @return the Color object from our array.
	 */
	public Color getColor() {
		return COLORS[this.color];
	}
	
	/**
	 * Animate our fish by facing left and then right over time.
	 */
	private int dt = 0;
	
	/**
	 * Go ahead and ignore this method if you're not into graphics.
	 * We use "dt" as a trick to make the fish change directions every second or so; this makes them feel a little more alive.
	 */
	@Override
	public void draw(Graphics2D g) {
		dt += 1;
		if (dt > 100) {
			dt = 0;
		}
		Shape circle = new Ellipse2D.Double(-0.6, -0.6, 1.2, 1.2);
		Shape body = new Ellipse2D.Double(-.40, -.2, .8, .4);
		Shape tail = new Ellipse2D.Double(+.2, -.3, .2, .6);
		Shape eye = new Ellipse2D.Double(-.25, -.1, .1, .1);
		
		Color color = getColor();
		Color tailColor = color.darker();

		
		Graphics2D flipped = (Graphics2D) g.create();
		if (dt < 50) {
			flipped.scale(-1, 1);
		}
		
		if (this.player) {
			flipped.setColor(new Color(1f,1f,1f,0.5f));
			flipped.fill(circle);
		}

		// Draw the fish of size (1x1, roughly, at 0,0).
		flipped.setColor(color);
		flipped.fill(body);

		flipped.setColor(Color.black);
		flipped.fill(eye);

		// draw tail:
		flipped.setColor(tailColor);
		flipped.fill(tail);
		
		flipped.dispose();
	}
	
	/**
	 * Increases fish's boredomness.
	 */
	public void increaseBoredom() {
		this.stepsTillBored -= 1;
	}
	
	/**
	 * Check if fish is bored.
	 * @return true if stepsTillBored <= 0.
	 */
	public boolean isBored() {
		if (this.stepsTillBored <= 0) return true;
		return false;
	}
	
	/**
	 * Reset its stepsTillBoredom variable. Doesn't have to reset to original number.
	 */
	public void resetBoredom() {
		this.stepsTillBored = 20 + rand.nextInt(5);
	}
	
	@Override
	public void step() {
		// Fish are controlled at a higher level; see FishGame.
	}
}
