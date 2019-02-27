package edu.smith.cs.csc212.p2;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

public class FishFood extends WorldObject {

	/**
	 * Counter, count until food appear.
	 */
	private int timer = 0;
	
	/**
	 * When to eat.
	 */
	private int eatTime = 100 + rand.nextInt(200);
	
	/**
	 * Create a food.
	 * @param world - where the fish food lives.
	 */
	public FishFood(World world) {
		super(world);
		
	}
	
	@Override
	public void draw(Graphics2D g) {
		// Draw the food dot.
		Shape circle = new Ellipse2D.Double(-.1, -.1, .3, .3);
		if (timer >= eatTime) {
			g.setColor(Color.yellow);
			g.fill(circle);
		} else {
			// Put it in an if just in case it got too big and overflow.
			timer += 1;
		}
	}
	
	/**
	 * Return if food is on screen or not.
	 * @return true if food is on screen, false if not.
	 */
	public boolean isVisible() {
		if (timer >= eatTime) return true;
		else return false;
	}
	
	public void resetTimer() {
		this.timer = 0;
		// New time to eat.
		this.eatTime = 100 + rand.nextInt(200);
	}

	@Override
	public void step() {
		// Nothing, does food even move?
	}

}
