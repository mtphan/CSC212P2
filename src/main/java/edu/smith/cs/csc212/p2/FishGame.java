package edu.smith.cs.csc212.p2;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This class manages our model of gameplay: missing and found fish, etc.
 * @author jfoley
 *
 */
public class FishGame {
	/**
	 * This is the world in which the fish are missing. (It's mostly a List!).
	 */
	World world;
	/**
	 * The player (a Fish.COLORS[0]-colored fish) goes seeking their friends.
	 */
	Fish player;
	/**
	 * The home location.
	 */
	FishHome home;
	
	/**
	 * The food location.
	 */
	FishFood food;
	
	/**
	 * These are the missing fish!
	 */
	List<Fish> missing;
	
	/**
	 * These are fish we've found!
	 */
	List<Fish> found;
	
	/**
	 * Number of steps!
	 */
	int stepsTaken;
	
	/**
	 * Score!
	 */
	int score;

	/**
	 * Number of rocks!
	 */
	public static final int NUM_ROCKS = 20;
	
	/**
	 * Number of rocks that fall!
	 */
	public static final int NUM_FALLING_ROCKS = 20;
	
	/**
	 * Create a FishGame of a particular size.
	 * @param w how wide is the grid?
	 * @param h how tall is the grid?
	 */
	public FishGame(int w, int h) {
		world = new World(w, h);
		
		missing = new ArrayList<Fish>();
		found = new ArrayList<Fish>();
		
		// Add a home!
		home = world.insertFishHome();
		
		// Generate some more rocks!
		for (int i=0; i<NUM_ROCKS; i++) {
			world.insertRockRandomly();
		}
		
		// Generate falling rocks!
		for (int i=0; i<NUM_FALLING_ROCKS; i++) {
			world.insertFallingRockRandomly();
		}
		
		// Make the snail!
		world.insertSnailRandomly();
		
		// Add fish food in?
		food = world.insertFishFood();
		
		// Make the player out of the 0th fish color.
		player = new Fish(0, world);
		// Start the player at "home".
		player.setPosition(home.getX(), home.getY());
		player.markAsPlayer();
		world.register(player);
		
		// Generate fish of all the colors but the first into the "missing" List.
		for (int ft = 1; ft < Fish.COLORS.length; ft++) {
			Fish friend = world.insertFishRandomly(ft);
			missing.add(friend);
		}
	}
	
	
	/**
	 * How we tell if the game is over: if missingFishLeft() == 0.
	 * @return the size of the missing list.
	 */
	public int missingFishLeft() {
		return missing.size();
	}
	
	/**
	 * Check if the player has got home. Remove all following fish if it did get home.
	 * Also check if other fish got home by accident.
	 * @return true if player reaches home.
	 */
	public boolean reachHome() {
		// Variable to check if player reaches home
		boolean playerReachesHome = false;
		
		// List of things that are currently at home.
		List<WorldObject> thingsAtHome = this.home.findSameCell();
		thingsAtHome.remove(home);
		
		// If player is at home
		if (thingsAtHome.remove(player)) {
			playerReachesHome = true;
			// Remove fish in found from the world
			eatFish(found);
		}
		
		// Remove fish at home from the world.
		eatFish(thingsAtHome);
		
		return playerReachesHome;
		
	}
	
	/**
	 * Remove a bunch of fish. Actually only use when the fish reaches home.
	 * @param fishToEat - List of fish to remove from absolutely everything.
	 */
	private void eatFish(List<? extends WorldObject> fishToEat) {
		// Make an array to remove stuff in case of ModifiedConcurrentError something.
		List<Fish> home = new ArrayList<>();
		for (WorldObject it : fishToEat) {
			// Only eat if it's a fish.
			if (it instanceof Fish) {
				home.add((Fish) it);
				world.remove(it);
			}
		}
		missing.removeAll(home);
		found.removeAll(home);
	}
	
	/**
	 * This method is how the PlayFish app tells whether we're done.
	 * @return true if the player has won (or maybe lost?).
	 */
	public boolean gameOver() {
		// If player hasn't reach home, it's always a false.
		if (!reachHome()) {
			return false;
		}
		return missing.isEmpty();
	}

	/**
	 * Update positions of everything (the user has just pressed a button).
	 */
	public void step() {
		// Keep track of how long the game has run.
		this.stepsTaken += 1;
		
		// These are all the objects in the world in the same cell as the player.
		List<WorldObject> overlap = this.player.findSameCell();
		// The player is there, too, let's skip them.
		overlap.remove(this.player);
		
		// If we find a fish, remove it from missing.
		for (WorldObject wo : overlap) {
			// It is missing if it's in our missing list.
			if (missing.contains(wo)) {
				// Remove this fish from the missing list.
				missing.remove(wo);
				
				// Add to found!
				found.add((Fish) wo);
				
				// Increase score when you find a fish according to the fish value!
				score += ((Fish) wo).value;
			}
		}
		
		List<WorldObject> potentialEater = this.food.findSameCell();
		potentialEater.remove(food);
		
		for (WorldObject eater : potentialEater) {
			if (eater.isFish()) {
				food.setPosition(world.pickUnusedSpace());
				food.resetTimer();
				if (eater.isPlayer()) {
					score += 10;
				}
			}
			// Only 1 fish is enough to be eaten.
			break;
		}
		
		// Make fish in found get bored.
		makeFishBored();
		
		// Make sure missing fish *do* something.
		wanderMissingFish();
		// When fish get added to "found" they will follow the player around.
		World.objectsFollow(player, found);
		// Step any world-objects that run themselves.
		world.stepAll();
	}
	
	/**
	 * Call moveRandomly() on all of the missing fish to make them seem alive.
	 */
	private void wanderMissingFish() {
		Random rand = ThreadLocalRandom.current();
		for (Fish lost : missing) {
			// Chance of fish being scared is determined by the variable scareChance that Fish has.
			// Is either 0.8 or 0.3 but could make it randomly.
			if (rand.nextDouble() < lost.scareChance) {
				lost.moveRandomly();
			}
		}
	}
	
	/**
	 * Increase the boredom of fish (decrease its stepsTillBored variable).
	 * Remove it from found and add it to missing if it got bored.
	 */
	private void makeFishBored() {
		// Loop backwards.
		for (int i = found.size()-1; i>1; i--) {
			found.get(i).increaseBoredom();
			// If fish is bored. Fish at position 0 and 1 never gets bored because it's not "too far back".
			if (found.get(i).isBored()) {
				missing.add(found.get(i));
				found.get(i).resetBoredom();
				found.remove(i);
			}
		}
	}

	/**
	 * This gets a click on the grid. We want it to destroy rocks that ruin the game.
	 * @param x - the x-tile.
	 * @param y - the y-tile.
	 */
	public void click(int x, int y) {
		System.out.println("Clicked on: "+x+","+y+ " world.canSwim(player,...)="+world.canSwim(player, x, y));
		List<WorldObject> atPoint = world.find(x, y);
		// Loop through all found objects.
		for (WorldObject it : atPoint) {
			// Remove if an instance of Rock.
			if (it instanceof Rock) {
				world.remove(it);
			}
		}

	}
	
}
