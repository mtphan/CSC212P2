package edu.smith.cs.csc212.p2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import me.jjfoley.gfx.IntPoint;

/**
 * A World is a 2d grid, represented as a width, a height, and a list of WorldObjects in that world.
 * @author jfoley
 *
 */
public class World {
	/**
	 * The size of the grid (x-tiles).
	 */
	private int width;
	/**
	 * The size of the grid (y-tiles).
	 */
	private int height;
	/**
	 * A list of objects in the world (Fish, Snail, Rock, etc.).
	 */
	private List<WorldObject> items;
	/**
	 * A reference to a random object, so we can randomize placement of objects in this world.
	 */
	private Random rand = ThreadLocalRandom.current();

	/**
	 * Create a new world of a given width and height.
	 * @param w - width of the world.
	 * @param h - height of the world.
	 */
	public World(int w, int h) {
		items = new ArrayList<>();
		width = w;
		height = h;
	}

	/**
	 * What is under this point?
	 * @param x - the tile-x.
	 * @param y - the tile-y.
	 * @return a list of objects!
	 */
	public List<WorldObject> find(int x, int y) {
		List<WorldObject> found = new ArrayList<>();
		
		// Check out every object in the world to find the ones at a particular point.
		for (WorldObject w : this.items) {
			// But only the ones that match are "found".
			if (x == w.getX() && y == w.getY()) {
				found.add(w);
			}
		}
		
		// Give back the list, even if empty.
		return found;
	}
	
	
	/**
	 * This is used by PlayGame to draw all our items!
	 * @return the list of items.
	 */
	public List<WorldObject> viewItems() {
		// Don't let anybody add to this list!
		// Make them use "register" and "remove".

		// This is kind of an advanced-Java trick to return a list where add/remove crash instead of working.
		return Collections.unmodifiableList(items);
	}

	/**
	 * Add an item to this World.
	 * @param item - the Fish, Rock, Snail, or other WorldObject.
	 */
	public void register(WorldObject item) {
		// Print out what we've added, for our sanity.
		System.out.println("register: "+item.getClass().getSimpleName());
		items.add(item);
	}
	
	/**
	 * This is the opposite of register. It removes an item (like a fish) from the World.
	 * @param item - the item to remove.
	 */
	public void remove(WorldObject item) {
		// Print out what we've removed, for our sanity.
		System.out.println("remove: "+item.getClass().getSimpleName());
		items.remove(item);
	}
	
	/**
	 * How big is the world we model?
	 * @return the width.
	 */
	public int getWidth() {
		return width;
	}
	/**
	 * How big is the world we model?
	 * @return the height.
	 */
	public int getHeight() {
		return height;
	}
	
	/**
	 * Try to find an unused part of the World for a new object!
	 * @return a point (x,y) that has nothing else in the grid.
	 */
	public IntPoint pickUnusedSpace() {
		int tries = width * height;
		for (int i=0; i<tries; i++) {
			int x = rand.nextInt(width);
			int y = rand.nextInt(height);
			if (this.find(x, y).size() != 0) {
				continue;
			}
			return new IntPoint(x,y);
		}
		// If we get here, we tried a lot of times and couldn't find a random point.
		// Let's crash our Java program!
		throw new IllegalStateException("Tried to pickUnusedSpace "+tries+" times and it failed! Maybe your grid is too small!");
	}
	
	/**
	 * Insert an item randomly into the grid.
	 * @param item - the rock, fish, snail or other WorldObject.
	 */
	public void insertRandomly(WorldObject item) {
		item.setPosition(pickUnusedSpace());
		this.register(item);
		item.checkFindMyself();
	}
	
	/**
	 * Insert a new Rock into the world at random.
	 * @return the Rock.
	 */
	public Rock insertRockRandomly() {
		Rock r = new Rock(this);
		insertRandomly(r);
		return r;
	}
	
	/**
	 * Insert a falling rock randomly. Could use chances but I prefer being able to control the number of it.
	 * @return FallingRock, the rock that falls.
	 */
	public FallingRock insertFallingRockRandomly() {
		FallingRock r = new FallingRock(this);
		insertRandomly(r);
		return r;
	}
	
	/**
	 * Insert a new Fish into the world at random of a specific color.
	 * @param color - the color of the fish.
	 * @return the new fish itself.
	 */
	public Fish insertFishRandomly(int color) {
		Fish f = new Fish(color, this);
		insertRandomly(f);
		return f;
	}
	
	public FishHome insertFishHome() {
		FishHome home = new FishHome(this);
		insertRandomly(home);
		return home;
	}
	
	public FishFood insertFishFood() {
		FishFood food = new FishFood(this);
		insertRandomly(food);
		return food;
	}
	
	/**
	 * Insert a new Snail at random into the world.
	 * @return the snail!
	 */
	public Snail insertSnailRandomly() {
		Snail snail = new Snail(this);
		insertRandomly(snail);
		return snail;
	}
	
	/**
	 * Determine if a WorldObject can swim to a particular point.
	 * 
	 * @param whoIsAsking - the object (not just the player!)
	 * @param x - the x-tile.
	 * @param y - the y-tile.
	 * @return true if they can move there.
	 */
	public boolean canSwim(WorldObject whoIsAsking, int x, int y) {
		if (x < 0 || x >= width || y < 0 || y >= height) {
			return false;
		}
		
		// This will be important.
		boolean isPlayer = whoIsAsking.isPlayer();
		
		// We will need to look at who all is in the spot to determine if we can move there.
		List<WorldObject> inSpot = this.find(x, y);
		
		for (WorldObject it : inSpot) {
			// The other fish shouldn't step "on" the player, the player should step on the other fish.
			if (isPlayer && it.isFish()) {
				return true;
			}
			// Gish can go on fish food.
			if (whoIsAsking instanceof Fish && it instanceof FishFood) {
				return true;
			}
			
			// When fishfood is visible nothing other than fish can step on it.
			if (it instanceof FishFood) {
				if (((FishFood) it).isVisible()) {
					return false;
				}
			}
			
			if (it instanceof Snail || it instanceof Rock || it instanceof Fish) {
				// This if-statement doesn't let anyone step on the Snail or Rock.
				// Also won't let fish step on each other or step on the player.
				return false;
			}
		}
		
		// If we didn't see an obstacle, we can move there!
		return true;
	}
	
	/**
	 * This is how objects may move. Only Snails do right now.
	 */
	public void stepAll() {
		for (WorldObject it : this.items) {
			it.step();
		}
	}
	
	/**
	 * This signature is a little scary, but we need to support any subclass of WorldObject.
	 * We don't know followers is a {@code List<Fish>} but it should work no matter what!
	 * @param target the leader.
	 * @param followers a set of objects to follow the leader.
	 */
	public static void objectsFollow(WorldObject target, List<? extends WorldObject> followers) {
		// recentPosition = List of position IntPoint that the target has passed through.
		// 					Is used to decide the points for the followers to go to.
		// followers = A list of things that is a subclass of WorldObject (Fish in this case).
		//			   Follows the target.
		// target = Leaders of the follower (player in this case).
		//			The follower follows the path the leader travels.
		// Why is past = putWhere[i+1]? Why not putWhere[i]?
		// putWhere[0] stores current position of the target.
		// => All followers has to use position at putWhere[i+1] or else one of them will step on the target.
		List<IntPoint> putWhere = new ArrayList<>(target.recentPositions);
		for (int i=0; i<followers.size(); i++) {
			IntPoint past = putWhere.get(i+1);
			followers.get(i).setPosition(past.x, past.y);
		}
	}
}
