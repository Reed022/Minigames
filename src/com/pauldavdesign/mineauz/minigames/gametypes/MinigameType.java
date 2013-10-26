package com.pauldavdesign.mineauz.minigames.gametypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import com.pauldavdesign.mineauz.minigames.MinigameData;
import com.pauldavdesign.mineauz.minigames.MinigamePlayer;
import com.pauldavdesign.mineauz.minigames.MinigameUtils;
import com.pauldavdesign.mineauz.minigames.Minigames;
import com.pauldavdesign.mineauz.minigames.MultiplayerTimer;
import com.pauldavdesign.mineauz.minigames.PlayerData;
import com.pauldavdesign.mineauz.minigames.minigame.Minigame;
import com.pauldavdesign.mineauz.minigames.minigame.reward.RewardItem;

public abstract class MinigameType implements Listener{
	private static Minigames plugin;
	private PlayerData pdata;
	private MinigameData mdata;
	
	protected MinigameType(){
		plugin = Minigames.plugin;
		pdata = plugin.pdata;
		mdata = plugin.mdata;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	public String typeLabel;
	
	public void setLabel(String label){
		typeLabel = label;
	}
	
	public String getLabel(){
		return typeLabel;
	}
	
	public abstract boolean joinMinigame(MinigamePlayer player, Minigame mgm);
	
	public abstract void quitMinigame(MinigamePlayer player, Minigame mgm, boolean forced);
	
	public abstract void endMinigame(MinigamePlayer player, Minigame mgm);
	
	public void callGeneralQuit(final MinigamePlayer player, final Minigame minigame){
		
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			
			@Override
			public void run() {
				if(!player.getPlayer().isDead()){
					pdata.minigameTeleport(player, minigame.getQuitPosition());
				}
				else{
					player.setQuitPos(minigame.getQuitPosition());
					player.setRequiredQuit(true);
				}
			}
		});
	}
	
	public boolean callLMSJoin(MinigamePlayer player, Minigame mgm){
		if(mgm.getQuitPosition() != null && mgm.isEnabled() && mgm.getEndPosition() != null && mgm.getLobbyPosition() != null){
			
			String gametype = mgm.getType();
			if(gametype.equals("dm"))
				gametype = "deathmatch";
			if(mgm.getScoreType().equals("ctf"))
				gametype += " CTF";
			
			Location lobby = mgm.getLobbyPosition();
			if(mdata.getMinigame(mgm.getName()).getPlayers().size() < mgm.getMaxPlayers()){
				if((mgm.canLateJoin() && mgm.getMpTimer() != null && mgm.getMpTimer().getStartWaitTimeLeft() == 0) 
						|| mgm.getMpTimer() == null || mgm.getMpTimer().getPlayerWaitTimeLeft() != 0){
					player.storePlayerData();
					player.setMinigame(mgm);
					
					mgm.addPlayer(player);
					if(mgm.getMpTimer() == null || mgm.getMpTimer().getStartWaitTimeLeft() != 0){
						pdata.minigameTeleport(player, lobby);
						if(mgm.getMpTimer() == null && mgm.getPlayers().size() == mgm.getMaxPlayers()){
							mgm.setMpTimer(new MultiplayerTimer(mgm));
							mgm.getMpTimer().startTimer();
							mgm.getMpTimer().setPlayerWaitTime(0);
							mdata.sendMinigameMessage(mgm, MinigameUtils.getLang("minigame.skipWaitTime"), "info", null);
						}
					}
					else{
						player.sendMessage(MinigameUtils.formStr("minigame.lateJoin", 5));
						pdata.minigameTeleport(player, lobby);
						final MinigamePlayer fply = player;
						final Minigame fmgm = mgm;
						Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
							
							@Override
							public void run() {
								if(fply.isInMinigame()){
									List<Location> locs = new ArrayList<Location>();
									locs.addAll(fmgm.getStartLocations());
									Collections.shuffle(locs);
									pdata.minigameTeleport(fply, locs.get(0));
									fply.getLoadout().equiptLoadout(fply);
								}
							}
						}, 100);

						player.getPlayer().setScoreboard(mgm.getScoreboardManager());
						mgm.setScore(player, 1);
						mgm.setScore(player, 0);
					}
					player.sendMessage(MinigameUtils.formStr("player.join.plyInfo", mgm.getType()), "win");
				
					if(mgm.getMpTimer() == null && mgm.getPlayers().size() == mgm.getMinPlayers()){
						mgm.setMpTimer(new MultiplayerTimer(mgm));
						mgm.getMpTimer().startTimer();
						if(mgm.getPlayers().size() == mgm.getMaxPlayers()){
							mgm.getMpTimer().setPlayerWaitTime(0);
							mdata.sendMinigameMessage(mgm, MinigameUtils.getLang("minigame.skipWaitTime"), "info", null);
						}
					}
					else{
						int neededPlayers = mgm.getMinPlayers() - mgm.getPlayers().size();
						if(neededPlayers == 1){
							player.sendMessage(MinigameUtils.formStr("minigame.waitingForPlayers", 1), null);
						}
						else if(neededPlayers > 1){
							player.sendMessage(MinigameUtils.formStr("minigame.waitingForPlayers", neededPlayers), null);
						}
					}
					return true;
				}
				else if((mgm.canLateJoin() && mgm.getMpTimer() != null && mgm.getMpTimer().getStartWaitTimeLeft() != 0)){
					player.sendMessage(MinigameUtils.formStr("minigame.lateJoinWait", mgm.getMpTimer().getStartWaitTimeLeft()), null);
					return false;
				}
				else if(mgm.getMpTimer().getPlayerWaitTimeLeft() == 0){
					player.sendMessage(MinigameUtils.getLang("minigame.started"), "error");
					return false;
				}
			}
			else if(mgm.getPlayers().size() == mgm.getMaxPlayers()){
				player.sendMessage(MinigameUtils.getLang("minigame.full"), "error");
			}
		}
		else if(mgm.getQuitPosition() == null){
			player.sendMessage(MinigameUtils.getLang("minigame.error.noQuit"), "error");
		}
		else if(mgm.getEndPosition() == null){
			player.sendMessage(MinigameUtils.getLang("minigame.error.noEnd"), "error");
		}
		else if(mgm.getLobbyPosition() == null){
			player.sendMessage(MinigameUtils.getLang("minigame.error.noLobby"), "error");
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public void issuePlayerRewards(MinigamePlayer player, Minigame save, boolean hascompleted){
		RewardItem reward = save.getRewardItem();
		RewardItem sreward = save.getSecondaryRewardItem();
		if(!hascompleted){
			if(reward != null){
				if(reward.getItem() != null){
					player.getPlayer().getInventory().addItem(reward.getItem());
					player.sendMessage(MinigameUtils.formStr("player.end.awardItem", reward.getItem().getAmount(), MinigameUtils.getItemStackName(reward.getItem())), "win");
				}
				else{
					if(Minigames.plugin.hasEconomy() && reward.getMoney() != 0){
						Minigames.plugin.getEconomy().depositPlayer(player.getName(), reward.getMoney());
						player.sendMessage(MinigameUtils.formStr("player.end.awardMoney", reward.getMoney()), "win");
					}
				}
			}
		}
		else if(hascompleted){
			if(sreward != null){
				if(sreward.getItem() != null){
					player.getPlayer().getInventory().addItem(sreward.getItem());
					player.sendMessage(MinigameUtils.formStr("player.end.awardItem", sreward.getItem().getAmount(), MinigameUtils.getItemStackName(sreward.getItem())), "win");
				}
				else{
					if(Minigames.plugin.hasEconomy() && sreward.getMoney() != 0){
						Minigames.plugin.getEconomy().depositPlayer(player.getName(), sreward.getMoney());
						player.sendMessage(MinigameUtils.formStr("player.end.awardMoney", sreward.getMoney()), "win");
					}
				}
			}
		}
		player.getPlayer().updateInventory();
	}
}
