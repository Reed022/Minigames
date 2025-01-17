package au.com.mineauz.minigames.blockRecorder;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.minigame.Minigame;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.List;

/**
 * advanced recorder if a minigame has a regen area
 */
public class RegenRecorder implements Listener {
    private final Minigame minigame;
    private final RecorderData recorderData;

    public RegenRecorder(Minigame minigame) {
        this.minigame = minigame;
        this.recorderData = minigame.getRecorderData();
    }

    //kill placed vehicles like minecarts
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void vehicleCreate(VehicleCreateEvent event) {
        if (minigame.hasPlayers() && minigame.isInRegenArea(event.getVehicle().getLocation())) {
            recorderData.addEntity(event.getVehicle(), null, true);
        }
    }

    //revive killed vehicles like minecarts
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void vehicleDestroy(VehicleDestroyEvent event) {
        if (event.getAttacker() == null) {
            if (minigame.hasPlayers() && minigame.isInRegenArea(event.getVehicle().getLocation())) {
                recorderData.addEntity(event.getVehicle(), null, false);
            }
        }
    }

    //give a hurt animal it's health back
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void animalDeath(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Animals animal) {
            if (minigame.hasPlayers() && !(event.getDamager() instanceof Player)) {
                Location entityLoc = event.getEntity().getLocation();

                if (minigame.isInRegenArea(entityLoc)) {
                    if (animal.getHealth() <= event.getDamage()) {
                        recorderData.addEntity(event.getEntity(), null, false);
                    }
                }
            }
        }
    }

    //kill spawned creatures (living entities)
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void mobSpawnEvent(CreatureSpawnEvent event) {
        if (minigame.hasPlayers() && minigame.isInRegenArea(event.getLocation())) {
            recorderData.addEntity(event.getEntity(), null, true);
        }
    }

    //regen exploded blocks (and don't allow block removal of not white or blacklisted blocks) <-- todo move this mode testing into a better fitting class
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void entityExplode(EntityExplodeEvent event) {
        if (minigame.hasPlayers()) {
            Location block = event.getLocation().getBlock().getLocation();
            if (minigame.isInRegenArea(block)) {
                List<Block> blocks = new ArrayList<>(event.blockList());

                for (Block bl : blocks) {
                    if ((recorderData.getWhitelistMode() && recorderData.getWBBlocks().contains(bl.getType())) ||
                            (!recorderData.getWhitelistMode() && !recorderData.getWBBlocks().contains(bl.getType()))) {
                        recorderData.addBlock(bl, null);
                    } else {
                        // don't allow exploding blocks that anre not on
                        event.blockList().remove(bl);
                    }
                }
            }
            //don't allow explosions while regenerating
        } else if (minigame.isRegenerating() && minigame.isInRegenArea(event.getLocation())) {
            event.setCancelled(true);
        }
    }

    // remove dropped items
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void itemDrop(ItemSpawnEvent event) {
        if (minigame.hasPlayers()) {
            Location ent = event.getLocation();
            if (minigame.isInRegenArea(ent)) {
                recorderData.addEntity(event.getEntity(), null, true);
            }
        }
    }

    // reset block changes like gravity blocks falling
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void physicalBlock(EntityChangeBlockEvent event) {
        if (minigame.isInRegenArea(event.getBlock().getLocation())) {
            if (minigame.isRegenerating()) {
                event.setCancelled(true);
                return;
            }
            if (event.getTo().hasGravity()) {
                if (minigame.hasPlayers() || event.getEntity().hasMetadata("FellInMinigame")) {
                    recorderData.addEntity(event.getEntity(), null, true);
                }
            } else if (event.getEntityType() == EntityType.FALLING_BLOCK && minigame.hasPlayers()) {
                event.getEntity().setMetadata("FellInMinigame", new FixedMetadataValue(Minigames.getPlugin(), true));
                recorderData.addEntity(event.getEntity(), null, true);
            }
        }
    }

    // reset inventories of hopper minecarts picking up items from the world
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void cartHopperPickup(InventoryPickupItemEvent event) {
        if (minigame.hasPlayers() && event.getInventory().getHolder() instanceof HopperMinecart) {
            Location loc = ((HopperMinecart) event.getInventory().getHolder()).getLocation();
            if (minigame.isInRegenArea(loc)) {
                recorderData.addEntity((HopperMinecart) event.getInventory().getHolder(), null, false);
            }
        }
    }

    // reset inventorys changed by hopper minecarts
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void cartMoveItem(InventoryMoveItemEvent event) {
        if (!minigame.hasPlayers()) return;

        Location loc;
        if (event.getInitiator().getHolder() instanceof HopperMinecart hopperMinecart) {
            loc = hopperMinecart.getLocation().clone();
            if (minigame.isInRegenArea(loc))
                recorderData.addEntity((Entity) event.getInitiator().getHolder(), null, false);
        }

        if (event.getDestination().getHolder() instanceof HopperMinecart hopperMinecart) {
            loc = hopperMinecart.getLocation().clone();
            if (minigame.isInRegenArea(loc))
                recorderData.addEntity((Entity) event.getInitiator().getHolder(), null, false);
        }
    }

    // don't allow block physics (like gravity blocks falling) to happen while the minigame is regenerating
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void physEvent(BlockPhysicsEvent event) {
        if (minigame.isRegenerating() && minigame.isInRegenArea(event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    // don't allow water / lava to flow while the minigame is regenerating
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void liquidFlow(BlockFromToEvent event) {
        if (minigame.isRegenerating() && minigame.isInRegenArea(event.getBlock().getLocation()))
            event.setCancelled(true);
    }

    // don't allow fire to spread while the minigame is regenerating
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void fireSpread(BlockSpreadEvent event) {
        if (minigame.isRegenerating() && minigame.isInRegenArea(event.getBlock().getLocation()))
            event.setCancelled(true);
    }

    // don't allow interacting with inventories while the minigame is regenerating
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void interact(PlayerInteractEvent event) {
        if (minigame.isRegenerating() && minigame.isInRegenArea(event.getClickedBlock().getLocation())) {
            event.setCancelled(true);
        }
    }
}
