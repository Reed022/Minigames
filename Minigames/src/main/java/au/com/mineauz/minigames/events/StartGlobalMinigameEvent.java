package au.com.mineauz.minigames.events;

import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.MinigamePlayer;

public class StartGlobalMinigameEvent extends AbstractMinigameEvent {

    private final String mechanic;
    private final MinigamePlayer caller;

    public StartGlobalMinigameEvent(Minigame mgm, MinigamePlayer caller) {
        super(mgm);
        mechanic = mgm.getMechanicName();
        this.caller = caller;
    }


    public String getMechanic() {
        return mechanic;
    }

    public MinigamePlayer getCaller() {
        return caller;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public void setCancelled(boolean b) {
        throw new UnsupportedOperationException("Cannot cancel a  Global Minigame Star Event");
    }
}
