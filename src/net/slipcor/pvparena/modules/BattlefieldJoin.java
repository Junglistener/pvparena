package net.slipcor.pvparena.modules;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.classes.PACheckResult;
import net.slipcor.pvparena.classes.PALocation;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaModule;
import net.slipcor.pvparena.runnables.PlayerStateCreateRunnable;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * <pre>Arena Module class "BattlefieldJoin"</pre>
 * 
 * Enables direct joining to the battlefield
 * 
 * @author slipcor
 * 
 * @version v0.9.2
 */

public class BattlefieldJoin extends ArenaModule {
	
	private int priority = 0;

	public BattlefieldJoin() {
		super("BattlefieldJoin");
		db = new Debug(300);
	}
	
	@Override
	public String version() {
		return "0.9.1.0";
	}

	public PACheckResult checkJoin(Arena arena, CommandSender sender, PACheckResult result, boolean join) {
		if (!join)
			return result; // we only care about joining, ignore spectators
		
		if (result.getPriority() > this.priority) {
			return result; // Something already is of higher priority, ignore!
		}
		
		Player p = (Player) sender;
		
		if (arena == null) {
			return result; // arena is null - maybe some other mod wants to handle that? ignore!
		}
		
		result.setModName(this.getName());
		
		if (arena.isLocked() && !p.hasPermission("pvparena.admin") && !(p.hasPermission("pvparena.create") && arena.getOwner().equals(p.getName()))) {
			result.setError(Language.parse(MSG.ERROR_NOPERM, Language.parse(MSG.ERROR_NOPERM_X_JOIN)));
			return result;
		}
		
		ArenaPlayer ap = ArenaPlayer.parsePlayer(sender.getName());
		
		if (ap.getArena() != null) {
			result.setError(Language.parse(MSG.ERROR_ARENA_ALREADY_PART_OF, ap.getArena().getName()));
			return result;
		}

		result.setModName(getName());
		result.setPriority(priority);
		return result;
	}
	
	@Override
	public boolean isActive(Arena arena) {
		return true;
	}

	@Override
	public void parseJoin(PACheckResult res, Arena arena, CommandSender sender, ArenaTeam team) {
		if (res == null || !res.getModName().equals(getName())) {
			return;
		}
		
		// standard join --> lounge
		ArenaPlayer ap = ArenaPlayer.parsePlayer(sender.getName());
		Bukkit.getScheduler().scheduleAsyncDelayedTask(PVPArena.instance, new PlayerStateCreateRunnable(ap, ap.get()), 2L);
		//ArenaPlayer.prepareInventory(arena, ap.get());
		ap.setLocation(new PALocation(ap.get().getLocation()));
		ap.setArena(arena);
		team.add(ap);
		if (arena.isFreeForAll()) {
			arena.tpPlayerToCoordName(ap.get(), "spawn");
		} else {
			arena.tpPlayerToCoordName(ap.get(), team.getName() + "spawn");
		}
		arena.broadcast(Language.parse(MSG.FIGHT_BEGINS));
	}
}