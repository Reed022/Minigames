package com.pauldavdesign.mineauz.minigames.minigame;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.pauldavdesign.mineauz.minigames.MinigameUtils;

public class ScoreboardSortThread extends Thread{
	
	private List<ScoreboardPlayer> players;
	private String minigame;
	private List<ScoreboardPlayer> result = new ArrayList<ScoreboardPlayer>();
	private ScoreboardType type;
	private ScoreboardOrder order;
	private CommandSender requested = null;
	private ScoreboardDisplay display = null;
	private int limit = 8;
	
	public ScoreboardSortThread(List<ScoreboardPlayer> players, ScoreboardType type, 
			ScoreboardOrder order, String minigame, CommandSender requested){
		this.players = players;
		this.type = type;
		this.order = order;
		this.requested = requested;
		this.minigame = minigame;
	}
	
	public ScoreboardSortThread(List<ScoreboardPlayer> players, ScoreboardType type, 
			ScoreboardOrder order, String minigame, CommandSender requested, int resultLimit){
		this.players = players;
		this.type = type;
		this.order = order;
		this.requested = requested;
		this.minigame = minigame;
		limit = resultLimit;
	}
	
	public ScoreboardSortThread(List<ScoreboardPlayer> players, ScoreboardType type, 
			ScoreboardOrder order, ScoreboardDisplay display){
		this.players = players;
		this.type = type;
		this.order = order;
		this.display = display;
	}
	
	public void run(){
		for(ScoreboardPlayer ply : players){
			if(result.isEmpty())
				result.add(ply);
			else{
				List<ScoreboardPlayer> resultCopy = new ArrayList<ScoreboardPlayer>(result);
				boolean added = false;
				for(ScoreboardPlayer ply2 : resultCopy){
					if(type == ScoreboardType.LEAST_TIME || type == ScoreboardType.TOTAL_TIME){
						long plyTime = (Long) ply.getByType(type);
						long ply2Time = (Long) ply2.getByType(type);
						if(order == ScoreboardOrder.DESCENDING){
							if(plyTime > ply2Time){
								result.add(resultCopy.indexOf(ply2), ply);
								added = true;
								break;
							}
						}
						else{
							if(plyTime < ply2Time){
								result.add(resultCopy.indexOf(ply2), ply);
								added = true;
								break;
							}
						}
					}
					else{
						int val = (Integer) ply.getByType(type);
						int val2 = (Integer) ply2.getByType(type);
						if(order == ScoreboardOrder.DESCENDING){
							if(val > val2){
								result.add(resultCopy.indexOf(ply2), ply);
								added = true;
								break;
							}
						}
						else{
							if(val < val2){
								result.add(resultCopy.indexOf(ply2), ply);
								added = true;
								break;
							}
						}
					}
				}
				if(!added)
					result.add(ply);
			}
		}
		
		if(requested != null || (requested instanceof Player && ((Player)requested).isOnline())){
			requested.sendMessage(ChatColor.GREEN + minigame + " Scoreboard: " + type.toString().toLowerCase().replace("_", " ") + " " + order.toString().toLowerCase());
			for(int i = 0; i < limit; i++){
				if(i >= result.size()) break;
				String msg = ChatColor.AQUA + result.get(i).getPlayerName() + ": " + ChatColor.WHITE;
				if(type == ScoreboardType.LEAST_TIME || type == ScoreboardType.TOTAL_TIME){
					int time = (int)((Long)result.get(i).getByType(type) / 1000);
					msg += MinigameUtils.convertTime(time, true);
				}
				else{ 
					msg += (Integer)result.get(i).getByType(type);
				}
				requested.sendMessage(msg);
			}
		}
		else if(display != null){
			display.displayStats(result);
		}
	}
}