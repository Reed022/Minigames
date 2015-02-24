package au.com.mineauz.minigames.minigame;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.scoreboard.NameTagVisibility;

import au.com.mineauz.minigames.MinigamePlayer;
import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.config.EnumFlag;
import au.com.mineauz.minigames.config.Flag;
import au.com.mineauz.minigames.config.IntegerFlag;
import au.com.mineauz.minigames.config.StringFlag;
import au.com.mineauz.minigames.menu.Callback;
import au.com.mineauz.minigames.minigame.modules.TeamsModule;

public class Team {
	private String displayName = null;
	private TeamColor color;
	private IntegerFlag maxPlayers = new IntegerFlag(0, "maxPlayers");
	private List<Location> startLocations = new ArrayList<Location>();
	private StringFlag assignMsg = new StringFlag(MinigameUtils.getLang("player.team.assign.joinTeam"), "assignMsg");
	private StringFlag gameAssignMsg = new StringFlag(MinigameUtils.getLang("player.team.assign.joinAnnounce"), "gameAssignMsg");
	private StringFlag autobalanceMsg = new StringFlag(MinigameUtils.getLang("player.team.autobalance.plyMsg"), "autobalanceMsg");
	private StringFlag gameAutobalanceMsg = new StringFlag(MinigameUtils.getLang("player.team.autobalance.minigameMsg"), "gameAutobalanceMsg");
	private EnumFlag<NameTagVisibility> nametagVisibility = new EnumFlag<NameTagVisibility>(NameTagVisibility.ALWAYS, "nametagVisibility");
	
	private List<MinigamePlayer> players = new ArrayList<MinigamePlayer>();
	private int score = 0;
	private Minigame mgm;
	
	/**
	 * Creates a team for the use in a specific Minigame
	 * @param color - The unique team color to identify the team by.
	 * @param minigame - The Minigame this team is assigned to.
	 */
	public Team(TeamColor color, Minigame minigame){
		this.color = color;
		displayName = MinigameUtils.capitalize(color.toString()) + " Team";
		mgm = minigame;
	}
	/**
	 * Gets the teams Minigame
	 * @return The Minigame this team is assigned to.
	 */
	public Minigame getMinigame(){
		return mgm;
	}
	
	/**
	 * Changes the color of the team for the Minigame its assigned to.
	 * @param color - The color to change this team to.
	 * @return true if the Minigame doesn't have the team color already available, fails if it already has that team.
	 */
	public boolean setColor(TeamColor color){
		TeamsModule module = mgm.getModule(TeamsModule.class);
		if(!module.hasTeam(color)){
			module.removeTeam(this.color);
			if(displayName.equals(this.color.toString().toLowerCase() + " team"))
				displayName = MinigameUtils.capitalize(color.toString()) + " Team";
			this.color = color;
			module.addTeam(color, this);
			return true;
		}
		return false;
	}
	
	/**
	 * Gets the teams color.
	 * @return The teams color.
	 */
	public TeamColor getColor(){
		return color;
	}
	
	/**
	 * Gets the teams ChatColor alternative.
	 * @return The ChatColor
	 */
	public ChatColor getChatColor(){
		return color.getColor();
	}
	
	/**
	 * Sets the display name for this team. If the name is longer than 32 characters, 
	 * it'll be trimmed to that length (Minecraft limitation).
	 * @param name - The name to change the team to.
	 */
	public void setDisplayName(String name){
		if(name.length() > 32)
			name = name.substring(0, 31);
		displayName = name;
	}
	
	/**
	 * Gets the teams display name. If none is set, it will return the color folowed by "Team".
	 * @return The display name or the color followed by "Team"
	 */
	public String getDisplayName(){
		return displayName;
	}
	
	public Set<Flag<?>> getFlags(){
		Set<Flag<?>> flags = new HashSet<Flag<?>>();
		flags.add(maxPlayers);
		flags.add(assignMsg);
		flags.add(gameAssignMsg);
		flags.add(gameAutobalanceMsg);
		flags.add(autobalanceMsg);
		flags.add(nametagVisibility);
		
		return flags;
	}
	
	public int getMaxPlayers() {
		return maxPlayers.getFlag();
	}
	
	public void setMaxPlayers(int maxPlayers) {
		this.maxPlayers.setFlag(maxPlayers);
	}
	
	public boolean isFull(){
		if(maxPlayers.getFlag() != 0 && players.size() >= maxPlayers.getFlag())
			return true;
		return false;
	}
	
	
	/**
	 * Gets the teams current score
	 * @return The score.
	 */
	public int getScore(){
		return score;
	}
	
	/**
	 * Adds 1 point to the team.
	 */
	public int addScore(){
		return addScore(1);
	}
	
	/**
	 * Adds a specific amount to the teams score.
	 * @param amount - The amount of points to add to the team
	 * @return The new score for the team.
	 */
	public int addScore(int amount){
		score += amount;
		mgm.getScoreboardManager().getObjective(mgm.getName(false)).getScore(getChatColor() + getDisplayName()).setScore(score);
		return score;
	}
	
	/**
	 * Sets the teams score to a specific value.
	 * @param amount The score amount to set for the team.
	 */
	public void setScore(int amount){
		score = amount;
		mgm.getScoreboardManager().getObjective(mgm.getName(false)).getScore(getChatColor() + getDisplayName()).setScore(score);
	}
	
	/**
	 * Sets the teams score back to 0.
	 */
	public void resetScore(){
		score = 0;
		mgm.getScoreboardManager().resetScores(getChatColor() + getDisplayName());
	}
	
	/**
	 * Gets a list of all the players assigned to this team.
	 * @return A list of all players assigned to the team.
	 */
	public List<MinigamePlayer> getPlayers(){
		return players;
	}
	
	/**
	 * Adds a player to the team.
	 * @param player - The player to add.
	 */
	public void addPlayer(MinigamePlayer player){
		players.add(player);
		player.setTeam(this);
		player.getPlayer().setScoreboard(mgm.getScoreboardManager());
		mgm.getScoreboardManager().getTeam(getColor().toString().toLowerCase()).addPlayer(player.getPlayer());
	}
	
	/**
	 * Removes a player from the team.
	 * @param player - The player to remove.
	 */
	public void removePlayer(MinigamePlayer player){
		players.remove(player);
		mgm.getScoreboardManager().getTeam(getColor().toString().toLowerCase()).removePlayer(player.getPlayer());
		player.getPlayer().setScoreboard(Minigames.plugin.getServer().getScoreboardManager().getMainScoreboard());
	}
	
	/**
	 * Adds a starting location for the team to spawn at.
	 * @param loc - The location to add to the team.
	 */
	public void addStartLocation(Location loc){
		startLocations.add(loc);
	}
	
	/**
	 * Replaces a starting location already assigned on the team.
	 * @param loc - The new location
	 * @param number - The number id of the original starting location (Ranging from 1 to the amount of start points [Not 0])
	 */
	public void addStartLocation(Location loc, int number){
		if(startLocations.size() >= number){
			startLocations.set(number - 1, loc);
		}
		else{
			startLocations.add(loc);
		}
	}
	
	/**
	 * Gets all the starting locations for this team.
	 * @return The teams starting locations.
	 */
	public List<Location> getStartLocations(){
		return startLocations;
	}
	
	/**
	 * Gets whether the team has start locations
	 * @return true if the team has start locations
	 */
	public boolean hasStartLocations(){
		return !startLocations.isEmpty();
	}
	
	/**
	 * Removes a specific start location from this team.
	 * @param locNumber - The id of the starting location.
	 * @return true if removal was successful.
	 */
	public boolean removeStartLocation(int locNumber){
		if(startLocations.size() < locNumber){
			startLocations.remove(locNumber);
			return true;
		}
		return false;
	}
	
	public String getAssignMessage(){
		return assignMsg.getFlag();
	}
	
	public void setAssignMessage(String msg){
		assignMsg.setFlag(msg);
	}
	
	public String getGameAssignMessage(){
		return gameAssignMsg.getFlag();
	}
	
	public void setGameAssignMessage(String msg){
		gameAssignMsg.setFlag(msg);
	}
	
	public String getAutobalanceMessage(){
		return autobalanceMsg.getFlag();
	}
	
	public void setAutobalanceMessage(String msg){
		autobalanceMsg.setFlag(msg);
	}
	
	public String getGameAutobalanceMessage(){
		return gameAutobalanceMsg.getFlag();
	}
	
	public void setGameAutobalanceMessage(String msg){
		gameAutobalanceMsg.setFlag(msg);
	}
	
	public NameTagVisibility getNameTagVisibility(){
		return nametagVisibility.getFlag();
	}
	
	public Callback<NameTagVisibility> getNameTagVisibilityCallback() {
		return new Callback<NameTagVisibility>() {

			@Override
			public void setValue(NameTagVisibility value) {
				nametagVisibility.setFlag(value);
				mgm.getScoreboardManager().getTeam(color.toString().toLowerCase()).setNameTagVisibility(value);
			}

			@Override
			public NameTagVisibility getValue() {
				return nametagVisibility.getFlag();
			}
		};
	}
	
	public void setNameTagVisibility(NameTagVisibility vis){
		nametagVisibility.setFlag(vis);
		mgm.getScoreboardManager().getTeam(color.toString().toLowerCase()).setNameTagVisibility(vis);
	}
}
