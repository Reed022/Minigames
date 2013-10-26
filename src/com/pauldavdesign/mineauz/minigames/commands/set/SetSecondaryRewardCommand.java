package com.pauldavdesign.mineauz.minigames.commands.set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.pauldavdesign.mineauz.minigames.MinigameUtils;
import com.pauldavdesign.mineauz.minigames.commands.ICommand;
import com.pauldavdesign.mineauz.minigames.minigame.Minigame;
import com.pauldavdesign.mineauz.minigames.minigame.reward.RewardRarity;

public class SetSecondaryRewardCommand implements ICommand{
	
	@Override
	public String getName() {
		return "reward2";
	}

	@Override
	public String[] getAliases() {
		return new String[] {"secondaryreward", "sreward"};
	}

	@Override
	public boolean canBeConsole() {
		return true;
	}

	@Override
	public String getDescription() {
		return "Sets the players secondary reward for completing the Minigame after the first time.";
	}

	@Override
	public String[] getParameters() {
		return null;
	}

	@Override
	public String[] getUsage() {
		return new String[] {"/minigame set <Minigame> reward2 <Item Name> [Quantity] [Rarity]",
				"/minigame set <Minigame> reward2 $<Money Amount> [Rarity]"
		};
	}

	@Override
	public String getPermissionMessage() {
		return "You do not have permission to set a Minigames secondary reward!";
	}

	@Override
	public String getPermission() {
		return "minigame.set.reward2";
	}

	@Override
	public boolean onCommand(CommandSender sender, Minigame minigame,
			String label, String[] args) {
		if(args != null){
			int quantity = 1;
			double money = -1;
			if(args.length >= 2 && args[1].matches("[0-9]+")){
				quantity = Integer.parseInt(args[1]);
			}
			ItemStack item = null;
			if(args[0].startsWith("$")){
				try{
					money = Double.parseDouble(args[0].replace("$", ""));
				}
				catch(NumberFormatException e){}
			}
			else{
				item = MinigameUtils.stringToItemStack(args[0], quantity);
			}
			
			if(item != null && item.getType() != Material.AIR){
				RewardRarity rarity = RewardRarity.NORMAL;
				if(args.length == 3){
					rarity = RewardRarity.valueOf(args[2].toUpperCase());
				}
				minigame.getSecondaryRewardItems().addItem(item, rarity);
				
				sender.sendMessage(ChatColor.GRAY + "Added " + item.getAmount() + " of " + MinigameUtils.getItemStackName(item) + " to secondary rewards of \"" + minigame.getName() + "\" "
						+ "with a rarity of " + rarity.toString().toLowerCase().replace("_", " "));
				return true;
			}
			else if(sender instanceof Player && args[0].equals("SLOT")){
				item = ((Player)sender).getItemInHand();
				RewardRarity rarity = RewardRarity.NORMAL;
				if(args.length == 2){
					rarity = RewardRarity.valueOf(args[1].toUpperCase());
				}
				minigame.getSecondaryRewardItems().addItem(item, rarity);
				sender.sendMessage(ChatColor.GRAY + "Added " + item.getAmount() + " of " + MinigameUtils.getItemStackName(item) + " to secondary rewards of \"" + minigame.getName() + "\" "
						+ "with a rarity of " + rarity.toString().toLowerCase().replace("_", " "));
				return true;
			}
			else if(item != null && item.getType() == Material.AIR){
				sender.sendMessage(ChatColor.RED + "Secondary reward for \"" + minigame.getName() + "\" cannot be Air!");
				return true;
			}
			else if(money != -1 && plugin.hasEconomy()){
				RewardRarity rarity = RewardRarity.NORMAL;
				if(args.length == 2){
					rarity = RewardRarity.valueOf(args[1].toUpperCase());
				}
				minigame.getSecondaryRewardItems().addMoney(money, rarity);
				sender.sendMessage(ChatColor.GRAY + "Added $" + money + " to secondary rewards of \"" + minigame.getName() + "\" "
						+ "with a rarity of " + rarity.toString().toLowerCase().replace("_", " "));
				return true;
			}
			else if(!plugin.hasEconomy()){
				sender.sendMessage(ChatColor.RED + "Vault required to set a money reward! Download from dev.bukkit.org");
				return true;
			}
		}
		return false;
	}
}
