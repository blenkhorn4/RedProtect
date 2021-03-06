package br.net.fabiozumbi12.RedProtect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.mgone.bossbarapi.BossbarAPI;
import net.digiex.magiccarpet.MagicCarpet;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Fish;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.Snowball;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import br.net.fabiozumbi12.RedProtect.events.EnterExitRegionEvent;

@SuppressWarnings("deprecation")
class RPPlayerListener implements Listener{
	
	static RPContainer cont = new RPContainer();
	private HashMap<Player,String> Ownerslist = new HashMap<Player,String>();
	static HashMap<Player,Region> LastDeath = new HashMap<Player,Region>();
	private HashMap<Player, String> PlayerCmd = new HashMap<Player, String>();
	private HashMap<String, String> PlayertaskID = new HashMap<String, String>();
    RedProtect plugin;
    
    public RPPlayerListener(RedProtect plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onConsume(PlayerItemConsumeEvent e){
        if(e.getItem() == null){
            return;
        }
        
    	Player p = e.getPlayer();
        //deny potion
        List<String> Pots = RPConfig.getStringList("server-protection.deny-potions");
        if(e.getItem().getType().equals(Material.POTION) && Pots.size() > 0){
        	Potion pot = Potion.fromItemStack(e.getItem());        	
        	for (String potion:Pots){
        		potion = potion.toUpperCase();
        		PotionType ptype = PotionType.valueOf(potion);
        		try{
        			if (pot.getType().equals(ptype) && !p.hasPermission("redprotect.bypass")){
            			e.setCancelled(true);
            			RPLang.sendMessage(p, RPLang.get("playerlistener.denypotion"));
            		}
        		} catch(IllegalArgumentException ex){
        			RPLang.sendMessage(p, "The config 'deny-potions' have a unknow potion type. Change to a valid potion type to really deny the usage.");
        			RedProtect.logger.severe("The config 'deny-potions' have a unknow potion type. Change to a valid potion type to really deny the usage.");
        		}        		
        	}                    
        }
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
    	RedProtect.logger.debug("RPPlayerListener - PlayerInteractEvent canceled? " + event.isCancelled());
    	
        Player p = event.getPlayer();
        Block b = event.getClickedBlock();
        
        if (b == null) {
            return;
        }
        
        RedProtect.logger.debug("RPPlayerListener - Is PlayerInteractEvent event. The block is " + b.getType().name());
        
        Location l = b.getLocation();
        Region r = RedProtect.rm.getTopRegion(l);
        Material itemInHand = p.getItemInHand().getType(); 
        
        if (p.getItemInHand().getTypeId() == RPConfig.getInt("wands.adminWandID") && p.hasPermission("redprotect.magicwand")) {
            if (event.getAction().equals((Object)Action.RIGHT_CLICK_BLOCK)) {
            	RedProtect.secondLocationSelections.put(p, b.getLocation());
                p.sendMessage(RPLang.get("playerlistener.wand2") + RPLang.get("general.color") + " (" + ChatColor.GOLD + b.getLocation().getBlockX() + RPLang.get("general.color") + ", " + ChatColor.GOLD + b.getLocation().getBlockY() + RPLang.get("general.color") + ", " + ChatColor.GOLD + b.getLocation().getBlockZ() + RPLang.get("general.color") + ").");
                event.setCancelled(true);
                return;                
            }
            else if (event.getAction().equals((Object)Action.LEFT_CLICK_BLOCK)) {
                RedProtect.firstLocationSelections.put(p, b.getLocation());
                p.sendMessage(RPLang.get("playerlistener.wand1") + RPLang.get("general.color") + " (" + ChatColor.GOLD + b.getLocation().getBlockX() + RPLang.get("general.color") + ", " + ChatColor.GOLD + b.getLocation().getBlockY() + RPLang.get("general.color") + ", " + ChatColor.GOLD + b.getLocation().getBlockZ() + RPLang.get("general.color") + ").");
                event.setCancelled(true);
                return;
            }
        }
        if (p.getItemInHand().getTypeId() == RPConfig.getInt("wands.infoWandID")) {
            if (event.getAction().equals((Object)Action.RIGHT_CLICK_AIR)) {
            	Location lp = p.getLocation();
                r = RedProtect.rm.getTopRegion(lp);
            }
            else if (event.getAction().equals((Object)Action.RIGHT_CLICK_BLOCK)) {
            	Location lb = b.getLocation();
                r = RedProtect.rm.getTopRegion(lb);
            }
            if (p.hasPermission("redprotect.infowand")) {
                if (r == null) {
                    p.sendMessage(RPLang.get("playerlistener.noregion.atblock"));
                }
                else if (r.canBuild(p)) {
                    p.sendMessage(RPLang.get("general.color") + "--------------- [" + ChatColor.GOLD + r.getName() + RPLang.get("general.color") + "] ---------------");
                    p.sendMessage(r.info());
                    p.sendMessage(RPLang.get("general.color") + "-----------------------------------------");
                } else {
                	p.sendMessage(RPLang.get("playerlistener.region.entered").replace("{region}", r.getName()).replace("{owners}", r.getCreator()));
                }
                event.setCancelled(true);
                return;
            }
        } 

        
        if (b.getType().equals(Material.CHEST) || 
        		b.getType().equals(Material.ANVIL) ||
        		b.getType().equals(Material.ENCHANTMENT_TABLE) ||
        		b.getType().equals(Material.BED) ||
        		b.getType().equals(Material.BED_BLOCK) ||
        		b.getType().equals(Material.NOTE_BLOCK) ||
        		b.getType().equals(Material.JUKEBOX) ||
        		b.getType().equals(Material.WORKBENCH) ||
        		b.getType().equals(Material.BREWING_STAND) ||
        		b.getType().equals(Material.CAULDRON) ||
        		b.getType().equals(Material.BEACON) ||
        		b.getType().equals(Material.DROPPER) ||
        		b.getType().equals(Material.ENDER_CHEST) || 
        		b.getType().equals(Material.DISPENSER) || 
        		b.getType().equals(Material.FURNACE) ||
        		b.getType().equals(Material.BURNING_FURNACE) ||
        		b.getType().equals(Material.TRAPPED_CHEST) || 
        		b.getType().equals(Material.HOPPER)){   
        	
            Boolean out = RPConfig.getBool("allow-private-outside");
        	if (r != null && (!r.canChest(p) || (r.canChest(p) && !cont.canOpen(b, p)))) {
                    if (!RedProtect.ph.hasPerm(p, "redprotect.bypass")) {
                        p.sendMessage(RPLang.get("playerlistener.region.cantopen"));
                        event.setCancelled(true);
                        return;
                    }
                    else {
                        p.sendMessage(RPLang.get("playerlistener.region.opened").replace("{region}", RPUtil.UUIDtoPlayer(r.getCreator())));
                    }
        	} else {
        		if (r == null && out && !cont.canOpen(b, p)) {
        			if (!RedProtect.ph.hasPerm(p, "redprotect.bypass")) {
                        p.sendMessage(RPLang.get("playerlistener.region.cantopen"));
                        event.setCancelled(true);
                        return;
                    } else {
                    	int x = b.getX();
                    	int y = b.getY();
                    	int z = b.getZ();
                        p.sendMessage(RPLang.get("playerlistener.region.opened").replace("{region}", "X:"+x+" Y:"+y+" Z:"+z));
                    }
                    
                }
        	}
        }               
        
        else if (b.getType().equals((Object)Material.LEVER)) {
            if (r != null && !r.canLever(p)) {
                if (!RedProtect.ph.hasPerm(p, "redprotect.bypass")) {
                    p.sendMessage(RPLang.get("playerlistener.region.cantlever"));
                    event.setCancelled(true);
                }
                else {
                    p.sendMessage(RPLang.get("playerlistener.region.levertoggled").replace("{region}", RPUtil.UUIDtoPlayer(r.getCreator())));
                }
            }
        }
        else if (b.getType().equals((Object)Material.STONE_BUTTON) || b.getType().equals((Object)Material.WOOD_BUTTON)) {
            if (r != null && !r.canButton(p)) {
                if (!RedProtect.ph.hasPerm(p, "redprotect.bypass")) {
                    p.sendMessage(RPLang.get("playerlistener.region.cantbutton"));
                    event.setCancelled(true);
                }
                else {
                    p.sendMessage(RPLang.get("playerlistener.region.buttonactivated").replace("{region}", RPUtil.UUIDtoPlayer(r.getCreator())));
                }
            }
        }
        else if (b.getType().name().contains("_DOOR") || b.getType().name().contains("_GATE")) {
            if (r != null && (!r.canDoor(p) || (r.canDoor(p) && !cont.canOpen(b, p)))) {
                if (!RedProtect.ph.hasPerm(p, "redprotect.bypass")) {
                    p.sendMessage(RPLang.get("playerlistener.region.cantdoor"));
                    event.setCancelled(true);
                }
                else {
                    p.sendMessage(RPLang.get("playerlistener.region.opendoor"));
                }
            }            
        } 
        else if (b.getType().name().contains("RAIL")){
            if (r != null && !r.canMinecart(p)){
        		p.sendMessage(RPLang.get("blocklistener.region.cantplace"));
        		event.setUseItemInHand(Event.Result.DENY);
        		event.setCancelled(true);
    			return;		
        	}
        } 
        else if ((event.getAction().equals(Action.LEFT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) && 
        	      b.getType().name().contains("SIGN") && (r != null && !r.canSign(p))){
        	      p.sendMessage(RPLang.get("playerlistener.region.cantinteract"));
        	      event.setUseItemInHand(Event.Result.DENY);
        	      event.setCancelled(true);
        	      return;
        } 
        else if ((itemInHand.equals(Material.FLINT_AND_STEEL) || 
        		itemInHand.equals(Material.WATER_BUCKET) || 
        		itemInHand.equals(Material.BUCKET) || 
        		itemInHand.equals(Material.LAVA_BUCKET) || 
        		itemInHand.equals(Material.ITEM_FRAME) || 
        		itemInHand.equals(Material.PAINTING) ||
        		itemInHand.name().contains("POTION") ||
        		itemInHand.name().contains("EGG")) && r != null && !r.canBuild(p)) {
            p.sendMessage(RPLang.get("playerlistener.region.cantuse"));
            event.setUseItemInHand(Event.Result.DENY);
            event.setCancelled(true);
            return;
        }            
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
    	if (event.isCancelled()) {
            return;
        }
        Entity e = event.getRightClicked();
        Player p = event.getPlayer();
        
        RedProtect.logger.debug("Is PlayerInteractEntityEvent event.");
        
        Location l = e.getLocation();
        Region r = RedProtect.rm.getTopRegion(l);
        if (r == null){
        	return;
        }
        if (e instanceof ItemFrame) {        	
            if (!r.canBuild(p)) {
                p.sendMessage(RPLang.get("playerlistener.region.cantedit"));
                event.setCancelled(true);
                return;
            }
        }
        if ((e.getType().name().contains("MINECART") || e.getType().name().contains("BOAT")) && !r.canMinecart(p)) {
        	p.sendMessage(RPLang.get("blocklistener.region.cantenter"));
            event.setCancelled(true);
            return;
        }

    }
    
    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent e) { 
    	if (!(e.getEntity() instanceof Player)){
    		return;
    	}

        //deny damagecauses
        List<String> Causes = RPConfig.getStringList("server-protection.deny-playerdeath-by");
        if(Causes.size() > 0){
        	for (String cause:Causes){
        		cause = cause.toUpperCase();
        		try{
        			if (e.getCause().equals(DamageCause.valueOf(cause))){
            			e.setCancelled(true);
            		}
        		} catch(IllegalArgumentException ex){
        			RedProtect.logger.severe("The config 'deny-playerdeath-by' have a unknow damage cause type. Change to a valid damage cause type.");
        		}        		
        	}                    
        }        
    }
    
    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent e) {                
        Player p = null;        
        if (e.getDamager() instanceof Player){
        	p = (Player)e.getDamager();
        } else if (e.getDamager() instanceof Arrow){
        	Arrow proj = (Arrow)e.getDamager();
        	if (proj.getShooter() instanceof Player){
        		p = (Player) proj.getShooter();
        	}        	
        } else if (e.getDamager() instanceof Fish){
        	Fish fish = (Fish)e.getDamager();
        	if (fish.getShooter() instanceof Player){
        		p = (Player) fish.getShooter();
        	} 
        } else if (e.getDamager() instanceof Egg){
        	Egg Egg = (Egg)e.getDamager();
        	if (Egg.getShooter() instanceof Player){
        		p = (Player) Egg.getShooter();
        	} 
        } else if (e.getDamager() instanceof Snowball){
        	Snowball Snowball = (Snowball)e.getDamager();
        	if (Snowball.getShooter() instanceof Player){
        		p = (Player) Snowball.getShooter();
        	} 
        } else if (e.getDamager() instanceof Fireball){
        	Fireball Fireball = (Fireball)e.getDamager();
        	if (Fireball.getShooter() instanceof Player){
        		p = (Player) Fireball.getShooter();
        	} 
        } else if (e.getDamager() instanceof SmallFireball){
        	SmallFireball SmallFireball = (SmallFireball)e.getDamager();
        	if (SmallFireball.getShooter() instanceof Player){
        		p = (Player) SmallFireball.getShooter();
        	} 
        } else {
        	e.isCancelled();
        	return;
        }

        RedProtect.logger.debug("Is EntityDamageByEntityEvent event.");
        
        Location l = e.getEntity().getLocation();
        Region r = RedProtect.rm.getTopRegion(l);
        if (r == null || p == null){
        	return;
        }
        
        if (e.getEntityType().equals(EntityType.PLAYER) && r.flagExists("pvp") && !r.canPVP(p)){
        	p.sendMessage(RPLang.get("entitylistener.region.cantpvp"));
            e.setCancelled(true);
            return;
        }
        
        if (e.getEntityType().equals(EntityType.ITEM_FRAME) && !r.canBuild(p)){
        	p.sendMessage(RPLang.get("playerlistener.region.cantremove"));
            e.setCancelled(true);
            return;
        }   

        if (e.getEntityType().name().contains("MINECART") && !r.canMinecart(p)){
        	p.sendMessage(RPLang.get("blocklistener.region.cantbreak"));
            e.setCancelled(true);
        	return;
        }
        
    }
    
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent e){
    	if (e.isCancelled()) {
            return;
        }
    	final Player p = e.getPlayer();
    	Location lfrom = e.getFrom();
    	Location lto = e.getTo();
    	final Region rfrom = RedProtect.rm.getTopRegion(lfrom);
    	final Region rto = RedProtect.rm.getTopRegion(lto);
    	   	
    	Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
			@Override
			public void run() {
				if (rto != null && rfrom != null){
		    		RegionFlags(rto, rfrom, p);    		
		    	}
		    	
		    	if (rto == null && rfrom != null){
		    		noRegionFlags(rfrom, p);
		    	}
		    	
		    	if (rfrom == null && rto != null){
		    		noRegionFlags(rto, p);
		    	}				
			}    		
    	}, 40L);
    	
    	
    	if (rto != null && !rto.canEnter(p)){
    		p.sendMessage(RPLang.get("playerlistener.region.cantregionenter"));
    		e.setCancelled(true); 
    	}
    	
    	if (PlayerCmd.containsKey(p)){
    		if (rto != null && !rto.canDeathBack(p) && PlayerCmd.get(p).startsWith("/back") && LastDeath.get(p) != null && LastDeath.get(p).equals(rto)){
        		p.sendMessage(RPLang.get("playerlistener.region.cantback"));
        		LastDeath.remove(p);
        		e.setCancelled(true);
        	}
    		if (rto != null && !rto.AllowHome(p) && PlayerCmd.get(p).startsWith("/home")){
        		p.sendMessage(RPLang.get("playerlistener.region.canthome"));
        		e.setCancelled(true);
        	}
    		PlayerCmd.remove(p);    		
    	}
    	
    	//teleport player to coord/world if playerup 128 y
    	int NetherY = RPConfig.getInt("netherProtection.maxYsize");
    	if (lto.getWorld().getEnvironment().equals(World.Environment.NETHER) && NetherY != -1 && lto.getBlockY() >= NetherY && !p.hasPermission("redprotect.bypass")){
    		RPLang.sendMessage(p, RPLang.get("playerlistener.upnethery").replace("{location}", NetherY+""));
    		e.setCancelled(true); 
    	}
    	
    	if (e.getCause().equals(PlayerTeleportEvent.TeleportCause.ENDER_PEARL)){
    		if (rfrom != null && !rfrom.canEnderPearl(p)){
        		p.sendMessage(RPLang.get("playerlistener.region.cantuse"));
                e.setCancelled(true);    		
        	}
        	if (rto != null && !rto.canEnderPearl(p)){
        		p.sendMessage(RPLang.get("playerlistener.region.cantuse"));
                e.setCancelled(true);    		
        	}
    	}    	
    }
    
    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent e){
    	Player p = e.getPlayer();
    	String msg = e.getMessage();
    	Region r = RedProtect.rm.getTopRegion(p.getLocation());
    	    	
    	if (msg.startsWith("/back") || msg.startsWith("/home")){
    		PlayerCmd.put(p, msg);
    	}
    	
    	if (msg.startsWith("/sethome") && r != null && !r.AllowHome(p)){
    		p.sendMessage(RPLang.get("playerlistener.region.canthome"));
    		e.setCancelled(true);
    	}    	
    	
    	if (RedProtect.Mc && r != null && r.flagExists("allow-magiccarpet") && !r.getFlagBool("allow-magiccarpet") && !r.isOwner(RPUtil.PlayerToUUID(p.getName()))){
    		if (msg.startsWith("/magiccarpet")){
    			e.setCancelled(true);
    			RPLang.sendMessage(p, RPLang.get("playerlistener.region.cantmc"));
    		} else {
    			for (String cmd:MagicCarpet.getPlugin(MagicCarpet.class).getCommand("MagicCarpet").getAliases()){
        			if (msg.startsWith("/"+cmd)){
        				e.setCancelled(true);
        				RPLang.sendMessage(p, RPLang.get("playerlistener.region.cantmc"));
        			}
        		}
    		}      	
        }
    }
     
    
    @EventHandler
    public void onPlayerDie(PlayerDeathEvent e){
    	Player p = e.getEntity();
    	Region r = RedProtect.rm.getTopRegion(p.getLocation());    	
    	if (r != null){
    		LastDeath.put(p, r);
    	}
    }
    
    @EventHandler
    public void onPlayerMovement(PlayerMoveEvent e){
    	if (e.isCancelled() || RPConfig.getBool("performance.disable-onPlayerMoveEvent-handler")) {
            return;
        }
    	
    	Player p = e.getPlayer();
    	Location lfrom = e.getFrom();
    	Location lto = e.getTo();
    	
    	//teleport player to coord/world if playerup 128 y
    	int NetherY = RPConfig.getInt("netherProtection.maxYsize");
    	if (lto.getWorld().getEnvironment().equals(World.Environment.NETHER) && NetherY != -1 && lto.getBlockY() >= NetherY && !p.hasPermission("redprotect.bypass")){
    		for (String cmd:RPConfig.getStringList("netherProtection.execute-cmd")){
        		RedProtect.serv.dispatchCommand(RedProtect.serv.getConsoleSender(), cmd.replace("{player}", p.getName()));
    		}
    		p.sendMessage(RPLang.get("playerlistener.upnethery").replace("{location}", NetherY+""));
    	}
    	
    	//Enter flag
        Region r = RedProtect.rm.getTopRegion(lto);
        World w = lfrom.getWorld();
        if (r != null && !r.canEnter(p)){
    		for (int i = 0; i < 100; i++){
        		Region r1 = RedProtect.rm.getTopRegion(w, lfrom.getBlockX()+i, lfrom.getBlockZ());
        		Region r2 = RedProtect.rm.getTopRegion(w, lfrom.getBlockX()-i, lfrom.getBlockZ());
        		Region r3 = RedProtect.rm.getTopRegion(w, lfrom.getBlockX(), lfrom.getBlockZ()+i);
        		Region r4 = RedProtect.rm.getTopRegion(w, lfrom.getBlockX(), lfrom.getBlockZ()-i);
        		Region r5 = RedProtect.rm.getTopRegion(w, lfrom.getBlockX()+i, lfrom.getBlockZ()+i);
        		Region r6 = RedProtect.rm.getTopRegion(w, lfrom.getBlockX()-i, lfrom.getBlockZ()-i);
        		if (r1 != r){
        			e.setTo(lfrom.add(+i, 0, 0));
        			break;
        		} 
        		if (r2 != r){
        			e.setTo(lfrom.add(-i, 0, 0));
        			break;
        		} 
        		if (r3 != r){
        			e.setTo(lfrom.add(0, 0, +i));
        			break;
        		} 
        		if (r4 != r){
        			e.setTo(lfrom.add(0, 0, -i));
        			break;
        		} 
        		if (r5 != r){
        			e.setTo(lfrom.add(+i, 0, +i));
        			break;
        		} 
        		if (r6 != r){
        			e.setTo(lfrom.add(-i, 0, -i));
        			break;
        		} 
    		}
    		p.sendMessage(RPLang.get("playerlistener.region.cantregionenter"));			
    	}
        
        //update region owner or member visit
        if (RPConfig.getString("region-settings.record-player-visit-method").equalsIgnoreCase("ON-REGION-ENTER")){
    		String uuid = p.getUniqueId().toString();        	
    		if (!RedProtect.OnlineMode){
    			uuid = p.getName().toLowerCase();
    		}
    		if (r != null && (r.isMember(uuid) || r.isOwner(uuid))){
            	if (r.getDate() == null || (r.getDate() != RPUtil.DateNow())){
            		r.setDate(RPUtil.DateNow());
            	}        	
    		}
    	}
        
        
        if (r != null && Ownerslist.get(p) != r.getName()){ 
			Region er = RedProtect.rm.getRegion(Ownerslist.get(p), p.getWorld());			
			Ownerslist.put(p, r.getName());
			
			//Execute listener:
			EnterExitRegionEvent event = new EnterExitRegionEvent(er, r, p);
			Bukkit.getPluginManager().callEvent(event);
			if (event.isCancelled()){
				return;
			}
			//--
			RegionFlags(r, er, p);	
			if (!r.getWelcome().equalsIgnoreCase("hide ")){
				EnterExitNotify(r, p);
			}		
    		
    	} else {
    		if (r == null && (Ownerslist.get(p) != null)) {    			
    			Region er = RedProtect.rm.getRegion(Ownerslist.get(p), p.getWorld());    
    			if (Ownerslist.containsKey(p)){
            		Ownerslist.remove(p);
            	}
    			
    			//Execute listener:
    			EnterExitRegionEvent event = new EnterExitRegionEvent(er, r, p);
    			Bukkit.getPluginManager().callEvent(event);    			
    			if (event.isCancelled()){
    				return;
    			}
    			//---
    			noRegionFlags(er, p);    	
    			if (er != null && !er.getWelcome().equalsIgnoreCase("hide ") && RPConfig.getBool("notify.region-exit")){
    				SendNotifyMsg(p, RPLang.get("playerlistener.region.wilderness"));
    			}    			
        	}
    	}   	
    }
    
    @EventHandler
    public void onPlayerLogout(PlayerQuitEvent e){
    	stopTaskPlayer(e.getPlayer());
    }
    
    @EventHandler
    public void PlayerLogin(PlayerJoinEvent e){
    	Player p = e.getPlayer();
    	
    	if (p.hasPermission("redprotect.update") && RedProtect.Update && !RPConfig.getBool("update-check.auto-update")){
    		RPLang.sendMessage(p, ChatColor.AQUA + "An update is available for RedProtect: " + RedProtect.UptVersion + " - on " + RedProtect.UptLink);
    		RPLang.sendMessage(p, ChatColor.AQUA + "Use /rp update to download and automatically install this update.");
    	}
    	
    	if (RPConfig.getString("region-settings.record-player-visit-method").equalsIgnoreCase("ON-LOGIN")){    		
        	String uuid = p.getUniqueId().toString();
        	if (!RedProtect.OnlineMode){
        		uuid = p.getName().toLowerCase();
        	}
        	for (Region r:RedProtect.rm.getMemberRegions(uuid)){
        		if (r.getDate() == null || !r.getDate().equals(RPUtil.DateNow())){
        			r.setDate(RPUtil.DateNow());
        		}
        	}
    	}    	
    }
    
    @EventHandler
    public void PlayerTrownEgg(PlayerEggThrowEvent e){
    	Location l = e.getEgg().getLocation();
    	Player p = e.getPlayer();
    	Region r = RedProtect.rm.getTopRegion(l);
    	
    	if (r != null && !r.canBuild(p)){
    		e.setHatching(false);
    		p.sendMessage(RPLang.get("playerlistener.region.canthatch"));
    	}
    }
    
    @EventHandler
    public void PlayerTrownPotion(PotionSplashEvent e){    	
    	//deny potion
        List<String> Pots = RPConfig.getStringList("server-protection.deny-potions");
        if(Pots.size() > 0){
        	Potion pot = Potion.fromItemStack(e.getPotion().getItem());
        	for (String potion:Pots){
        		try{
        			if (pot.getType().equals(PotionType.valueOf(potion))){
            			e.setCancelled(true);
            			if (e.getPotion().getShooter() instanceof Player){
            				RPLang.sendMessage((Player)e.getPotion().getShooter(), RPLang.get("playerlistener.denypotion"));
            			}            			
            		}
        		} catch(IllegalArgumentException ex){
        			RedProtect.logger.severe("The config 'deny-potions' have a unknow potion type. Change to a valid potion type to really deny the usage.");
        		}
        	}                    
        }
        
    	if (!(e.getPotion().getShooter() instanceof Player)){
    		return;
    	}
    	
    	Player p = (Player)e.getPotion().getShooter();
    	Entity ent = e.getEntity();
    	
    	RedProtect.logger.debug("Is PotionSplashEvent event.");
    	
    	
        
    	Region r = RedProtect.rm.getTopRegion(ent.getLocation());
    	if (r != null && !r.canBuild(p)){
    		p.sendMessage(RPLang.get("playerlistener.region.cantuse"));
    		e.setCancelled(true);
    		return;
    	}    	
    }
            
    public void SendNotifyMsg(Player p, String notify){
    	if (!notify.equals("")){
    		if (RPConfig.getString("notify.region-enter-mode").equalsIgnoreCase("BOSSBAR")){
    			if (RedProtect.BossBar){
    				BossbarAPI.setMessage(p,notify);
    			} else {
    				p.sendMessage(notify);
    			}
    		} 
    		if (RPConfig.getString("notify.region-enter-mode").equalsIgnoreCase("CHAT")){
    			p.sendMessage(notify);
    		}
    	}
    }

    public void SendWelcomeMsg(Player p, String wel){
		if (RPConfig.getString("notify.welcome-mode").equalsIgnoreCase("BOSSBAR")){
			if (RedProtect.BossBar){
				BossbarAPI.setMessage(p,wel);
			} else {
				p.sendMessage(wel);
			}
		} 
		if (RPConfig.getString("notify.welcome-mode").equalsIgnoreCase("CHAT")){
			p.sendMessage(wel);
		}
    }
    
    private void stopTaskPlayer(Player p){
    	List<String> toremove = new ArrayList<String>();
    	for (String taskId:PlayertaskID.keySet()){
    		if (PlayertaskID.get(taskId).equals(p.getName())){
    			Bukkit.getScheduler().cancelTask(Integer.parseInt(taskId.split("_")[0]));  
    			toremove.add(taskId);    			
    		}    		  			
    	}
    	for (String remove:toremove){
    		PlayertaskID.remove(remove);
    		RedProtect.logger.debug("Removed task ID: " + remove + " for player " + p.getName());
    	}
    	toremove.clear();
    }
    
    private void RegionFlags(Region r, Region er, final Player p){  
		//Enter command as player
        if (r.flagExists("player-enter-command")){
        	String[] cmds = r.getFlagString("player-enter-command").split(",");
        	for (String cmd:cmds){
        		if (cmd.startsWith("/")){
            		cmd = cmd.substring(1);
            	}
            	p.getServer().dispatchCommand(p.getPlayer(), cmd.replace("{player}", p.getName()).replace("{region}", r.getName()));
        	}                	
        }
        
        //Enter command as console
        if (r.flagExists("server-enter-command")){
        	String[] cmds = r.getFlagString("server-enter-command").split(",");
        	for (String cmd:cmds){
        		if (cmd.startsWith("/")){
            		cmd = cmd.substring(1);
            	}
            	RedProtect.serv.dispatchCommand(RedProtect.serv.getConsoleSender(), cmd.replace("{player}", p.getName()).replace("{region}", r.getName()));
        	}                	
        }
        
        //Enter MagicCarpet
        if (r.flagExists("allow-magiccarpet") && !r.getFlagBool("allow-magiccarpet") && RedProtect.Mc){
        	if (MagicCarpet.getCarpets().getCarpet(p) != null){
        		MagicCarpet.getCarpets().remove(p);
        		RPLang.sendMessage(p, RPLang.get("playerlistener.region.cantmc"));
        	}        	
        }
        
        if (er != null){                	
        	//Exit effect
			if (er.flagExists("effects")){
				String[] effects = er.getFlagString("effects").split(",");
				for (String effect:effects){
					if (PlayertaskID.containsValue(p.getName())){						
						String eff = effect.split(" ")[0];
						String amplifier = effect.split(" ")[1];
						PotionEffect fulleffect = new PotionEffect(PotionEffectType.getByName(eff), 60, Integer.parseInt(amplifier));
						p.removePotionEffect(fulleffect.getType());	
						List<String> removeTasks = new ArrayList<String>();
						for (String taskId:PlayertaskID.keySet()){
							int id = Integer.parseInt(taskId.split("_")[0]);
							String ideff = id+"_"+eff+er.getName();
							if (PlayertaskID.containsKey(ideff) && PlayertaskID.get(ideff).equals(p.getName())){
								Bukkit.getScheduler().cancelTask(id);
								removeTasks.add(taskId);
								RedProtect.logger.debug("Removed task ID: " + taskId + " for player " + p.getName());
							}
						}
						for (String key:removeTasks){
							PlayertaskID.remove(key);
						}
						removeTasks.clear();
					}					
				}
			} else {
				stopTaskPlayer(p);
			}
			
        	//Exit command as player
            if (er.flagExists("player-exit-command")){
            	String[] cmds = er.getFlagString("player-exit-command").split(",");
            	for (String cmd:cmds){
            		if (cmd.startsWith("/")){
                		cmd = cmd.substring(1);
                	}
                	p.getServer().dispatchCommand(p.getPlayer(), cmd.replace("{player}", p.getName()).replace("{region}", er.getName()));
            	}                	
            }
            
            //Exit command as console
            if (er.flagExists("server-exit-command")){
            	String[] cmds = er.getFlagString("server-exit-command").split(",");
            	for (String cmd:cmds){
            		if (cmd.startsWith("/")){
                		cmd = cmd.substring(1);
                	}
                	RedProtect.serv.dispatchCommand(RedProtect.serv.getConsoleSender(), cmd.replace("{player}", p.getName()).replace("{region}", er.getName()));
            	}                	
            }
        }
        
        //Enter effect
        if (r.flagExists("effects")){
  			int TaskId = 0;
  			String[] effects = r.getFlagString("effects").split(",");
  			for (String effect:effects){
  				String eff = effect.split(" ")[0];
  				String amplifier = effect.split(" ")[1];
  				final PotionEffect fulleffect = new PotionEffect(PotionEffectType.getByName(eff), 60, Integer.parseInt(amplifier));
  				TaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, new Runnable() { 
  					public void run() {
  						p.addPotionEffect(fulleffect, true); 
  						} 
  					},0, 20);	
  				PlayertaskID.put(TaskId+"_"+eff+r.getName(), p.getName());
  				RedProtect.logger.debug("Added task ID: " + TaskId+"_"+eff + " for player " + p.getName());
  			}
  		}
    }
    
    private void EnterExitNotify(Region r, Player p){
    	if (!RPConfig.getBool("notify.region-enter")){
    		return;
    	}
    	
    	String ownerstring = "";
    	String m = "";
    	//Enter-Exit notifications    
        if (r.getWelcome().equals("")){
			if (RPConfig.getString("notify.region-enter-mode").equalsIgnoreCase("BOSSBAR")
	    			|| RPConfig.getString("notify.region-enter-mode").equalsIgnoreCase("CHAT")){
				for (int i = 0; i < r.getOwners().size(); ++i) {
    				ownerstring = ownerstring + ", " + RPUtil.UUIDtoPlayer(r.getOwners().get(i)); 
    	        }
				
				if (r.getOwners().size() > 0) {
		            ownerstring = ownerstring.substring(2);
		        }
		        else {
		            ownerstring = "None";
		        }
    			m = RPLang.get("playerlistener.region.entered"); 
        		m = m.replace("{owners}", ownerstring);
        		m = m.replace("{region}", r.getName());
			} 
			SendNotifyMsg(p, m);
		} else {
			SendWelcomeMsg(p, ChatColor.GOLD + r.getName() + ": "+ ChatColor.RESET + r.getWelcome().replaceAll("(?i)&([a-f0-9k-or])", "�$1"));
    		return;        			
		}
    }
    
    private void noRegionFlags(Region er, Player p){
    	if (er != null){			
			//Exit effect
			if (er.flagExists("effects")){
				String[] effects = er.getFlagString("effects").split(",");
				for (String effect:effects){
					if (PlayertaskID.containsValue(p.getName())){						
						String eff = effect.split(" ")[0];
						String amplifier = effect.split(" ")[1];
						PotionEffect fulleffect = new PotionEffect(PotionEffectType.getByName(eff), 60, Integer.parseInt(amplifier));
						p.removePotionEffect(fulleffect.getType());
						List<String> removeTasks = new ArrayList<String>();
						for (String taskId:PlayertaskID.keySet()){
							int id = Integer.parseInt(taskId.split("_")[0]);
							String ideff = id+"_"+eff+er.getName();
							if (PlayertaskID.containsKey(ideff) && PlayertaskID.get(ideff).equals(p.getName())){
								Bukkit.getScheduler().cancelTask(id);
								removeTasks.add(taskId);
								RedProtect.logger.debug("Removed task ID: " + taskId + " for effect " + effect);
							}
						}
						for (String key:removeTasks){
							PlayertaskID.remove(key);
						}
						removeTasks.clear();
					}
				}
			} else {
				stopTaskPlayer(p);
			}
			
			//Exit command as player
            if (er.flagExists("player-exit-command")){
            	String[] cmds = er.getFlagString("player-exit-command").split(",");
            	for (String cmd:cmds){
            		if (cmd.startsWith("/")){
                		cmd = cmd.substring(1);
                	}
                	RedProtect.serv.dispatchCommand(p, cmd.replace("{player}", p.getName()));
            	}                	
            }
            
            //Exit command as console
            if (er.flagExists("server-exit-command")){
            	String[] cmds = er.getFlagString("server-exit-command").split(",");
            	for (String cmd:cmds){
            		if (cmd.startsWith("/")){
                		cmd = cmd.substring(1);
                	}
                	RedProtect.serv.dispatchCommand(RedProtect.serv.getConsoleSender(), cmd.replace("{player}", p.getName()));
            	}                	
            }
		}
    }
}
