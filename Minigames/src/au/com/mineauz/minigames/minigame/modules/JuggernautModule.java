package au.com.mineauz.minigames.minigame.modules;

import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;

import au.com.mineauz.minigames.MinigamePlayer;
import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.config.Flag;
import au.com.mineauz.minigames.minigame.Minigame;

public class JuggernautModule extends MinigameModule{
	
	private MinigamePlayer juggernaut = null;

	public JuggernautModule(Minigame mgm) {
		super(mgm);
	}

	@Override
	public String getName() {
		return "Juggernaut";
	}

	@Override
	public Map<String, Flag<?>> getFlags() {
		return null;
	}

	@Override
	public boolean useSeparateConfig() {
		return false;
	}

	@Override
	public void save(FileConfiguration config) {
	}

	@Override
	public void load(FileConfiguration config) {
	}

	public static JuggernautModule getMinigameModule(Minigame minigame){
		return (JuggernautModule) minigame.getModule("Juggernaut");
	}
	
	public void setJuggernaut(MinigamePlayer player){
		if(juggernaut != null){
			juggernaut.setLoadout(null);
			juggernaut.getMinigame().getScoreboardManager().getTeam("juggernaut").removePlayer(juggernaut.getPlayer().getPlayer());
		}
		juggernaut = player;
		
		if(juggernaut != null){
			player.getMinigame().getScoreboardManager().getTeam("juggernaut").addPlayer(player.getPlayer().getPlayer());
			
			juggernaut.sendMessage(MinigameUtils.getLang("player.juggernaut.plyMsg"), null);
			Minigames.plugin.mdata.sendMinigameMessage(getMinigame(), 
					MinigameUtils.formStr("player.juggernaut.gameMsg", juggernaut.getDisplayName()), null, juggernaut);
			
			LoadoutModule lm =LoadoutModule.getMinigameModule(getMinigame());
			if(lm.hasLoadout("juggernaut")){
				player.setLoadout(lm.getLoadout("juggernaut"));
				player.getLoadout().equiptLoadout(player);
			}
		}
	}
	
	public MinigamePlayer getJuggernaut(){
		return juggernaut;
	}

}