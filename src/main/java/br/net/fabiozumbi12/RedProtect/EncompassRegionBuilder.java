package br.net.fabiozumbi12.RedProtect;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

class EncompassRegionBuilder extends RegionBuilder{

    public EncompassRegionBuilder(SignChangeEvent e) {
        super();
        String owner1 = RPUtil.PlayerToUUID(e.getLine(2));
        String owner2 = RPUtil.PlayerToUUID(e.getLine(3));
        Block b = e.getBlock();
        World w = b.getWorld();
        Player p = e.getPlayer();
        String pName = RPUtil.PlayerToUUID(p.getName());
        Block last = b;
        Block current = b;
        Block next = null;
        Block first = null;
        String regionName = e.getLine(1);
        List<Integer> px = new LinkedList<Integer>();
        List<Integer> pz = new LinkedList<Integer>();
        Block bFirst1 = null;
        Block bFirst2 = null;
        List<Block> blocks = new LinkedList<Block>();
        int oldFacing = 0;
        int curFacing = 0;
        
        if (!RPConfig.isAllowedWorld(p)){
        	this.setErrorSign(e, RPLang.get("regionbuilder.region.worldnotallowed"));
            return;
        }
        
        int claimLimit = RedProtect.ph.getPlayerClaimLimit(p);
        if (RedProtect.rm.getRegions(RPUtil.PlayerToUUID(p.getName()),w).size() >= claimLimit && claimLimit != -1  && !p.hasPermission("redprotect.claimunlimited")) {
            this.setErrorSign(e, RPLang.get("regionbuilder.claim.limit"));
            return;
        }
        
        if (regionName == null || regionName.equals("")) {
        	regionName = RPUtil.nameGen(p);
        	if (regionName.length() > 16) {
                this.setErrorSign(e, RPLang.get("regionbuilder.autoname.error"));
                return;
            }
        }
        
        if (RedProtect.rm.getRegion(regionName, w) != null) {
            this.setErrorSign(e, RPLang.get("regionbuilder.regionname.existis"));
            return;
        }
        if (regionName.length() < 2 || regionName.length() > 16) {
            this.setErrorSign(e, RPLang.get("regionbuilder.regionname.invalid"));
            return;
        }
        if (regionName.contains(" ")) {
            this.setErrorSign(e, RPLang.get("regionbuilder.regionname.spaces"));
            return;
        }
            	
        for (int i = 0; i < RPConfig.getInt("region-settings.max-scan"); ++i) {
            int nearbyCount = 0;
            int x = current.getX();
            int y = current.getY();
            int z = current.getZ();
            int blockSize = 6;
            Block[] block;
            if (RPConfig.getString("region-settings.block-id").equalsIgnoreCase("REDSTONE") ) {
                block = new Block[12];
                blockSize = 12;
                block[0] = w.getBlockAt(x + 1, y, z);
                block[1] = w.getBlockAt(x - 1, y, z);
                block[2] = w.getBlockAt(x, y, z + 1);
                block[3] = w.getBlockAt(x, y, z - 1);
                block[4] = w.getBlockAt(x + 1, y + 1, z);
                block[5] = w.getBlockAt(x - 1, y + 1, z);
                block[6] = w.getBlockAt(x, y + 1, z + 1);
                block[7] = w.getBlockAt(x, y + 1, z - 1);
                block[8] = w.getBlockAt(x + 1, y - 1, z);
                block[9] = w.getBlockAt(x - 1, y - 1, z);
                block[10] = w.getBlockAt(x, y - 1, z + 1);
                block[11] = w.getBlockAt(x, y - 1, z - 1);
            }
            else if (RPConfig.getString("region-settings.block-id").equalsIgnoreCase("FENCE")) {
                block = new Block[6];
                blockSize = 6;
                block[0] = w.getBlockAt(x + 1, y, z);
                block[1] = w.getBlockAt(x - 1, y, z);
                block[2] = w.getBlockAt(x, y, z + 1);
                block[3] = w.getBlockAt(x, y, z - 1);
                block[4] = w.getBlockAt(x, y - 1, z);
                block[5] = w.getBlockAt(x, y + 1, z);
            }
            else {
                block = new Block[6];
                blockSize = 6;
                block[0] = w.getBlockAt(x + 1, y, z);
                block[1] = w.getBlockAt(x - 1, y, z);
                block[2] = w.getBlockAt(x, y, z + 1);
                block[3] = w.getBlockAt(x, y, z - 1);
                block[4] = w.getBlockAt(x, y - 1, z);
                block[5] = w.getBlockAt(x, y + 1, z);
            }
            for (int bi = 0; bi < blockSize; ++bi) {
                boolean validBlock = false;            	
                
                validBlock = (block[bi].getType().name().contains(RPConfig.getString("region-settings.block-id")));               
                
                if (validBlock && !block[bi].getLocation().equals((Object)last.getLocation())) {
                    ++nearbyCount;
                    next = block[bi];
                    curFacing = bi % 4;
                    if (i == 1) {
                        if (nearbyCount == 1) {
                            bFirst1 = block[bi];
                        }
                        if (nearbyCount == 2) {
                            bFirst2 = block[bi];
                        }
                    }
                }
            }
            if (nearbyCount == 1) {
                if (i != 0) {
                    blocks.add(current);
                    if (current.equals(first)) {
                        List<String> owners = new LinkedList<String>();
                        owners.add(pName);
                            if (owner1 == null) {
                                e.setLine(2, "--");
                                
                            } else if (pName.equals(owner1)) {
                            	e.setLine(2, "--");
                            	p.sendMessage(RPLang.get("regionbuilder.sign.dontneed.name"));
                            	
                            } else {
                                owners.add(owner1);
                            } 
                                    
                            
                            if (owner2 == null) {
                            	e.setLine(3, "--");
                            } else if (pName.equals(owner2)) {
                            	e.setLine(3, "--");
                            	p.sendMessage(RPLang.get("regionbuilder.sign.dontneed.name"));
                                
                            } else {
                            	owners.add(owner2);                                
                            }
                                                        
                        
                        int[] rx = new int[px.size()];
                        int[] rz = new int[pz.size()];
                        int bl = 0;
                        for (int bx : px) {
                            rx[bl] = bx;
                            ++bl;
                        }
                        bl = 0;
                        for (int bz : pz) {
                            rz[bl] = bz;
                            ++bl;
                        }
                        
                        List<String> othersName = new ArrayList<String>();
                        for (Block ib : blocks) {
                            Map<Integer,Region> otherg = RedProtect.rm.getGroupRegion(w, ib.getX(), ib.getZ());
                            for (Region rname : otherg.values()){
                            	if (!rname.isOwner(pName) || !p.hasPermission("redprotect.admin")){
                            		this.setError(p, RPLang.get("regionbuilder.region.overlapping").replace("{player}", RPUtil.UUIDtoPlayer(rname.getCreator())));
                                    return;
                            	}
                        		if (!othersName.contains(rname.getName())){
                            		othersName.add(rname.getName());
                            	}
                        	} 
                        }
                        
                        Region region = new Region(regionName, owners, new ArrayList<String>(), owners.get(0), rx, rz, 0, w.getName(), RPUtil.DateNow(), RPConfig.getDefFlagsValues(), "");
                        
                        int regionarea = region.getArea();                        
                        Region topRegion = RedProtect.rm.getTopRegion(RedProtect.serv.getWorld(region.getWorld()), region.getCenterX(), region.getCenterZ());
                        Region lowRegion = RedProtect.rm.getLowRegion(RedProtect.serv.getWorld(region.getWorld()), region.getCenterX(), region.getCenterZ());
                        
                        if (lowRegion != null){
                        	if (regionarea > lowRegion.getArea()){
                        		region.setPrior(lowRegion.getPrior() - 1);
                        	} else if (regionarea < lowRegion.getArea() && regionarea < topRegion.getArea() ){
                        		region.setPrior(topRegion.getPrior() + 1);
                        	} else if (regionarea < topRegion.getArea()){
                        		region.setPrior(topRegion.getPrior() + 1);
                        	} 
                        }
                        
                        String uuid = p.getUniqueId().toString();
                    	if (!RedProtect.OnlineMode){
                    		uuid = p.getName().toLowerCase();
                    	}
                    	
                        for (Region reg:RedProtect.rm.getPossibleIntersectingRegions(region, w)){                        	
                        	if (!reg.isOwner(uuid) || !p.hasPermission("redprotect.admin")){
                        		this.setError(p, RPLang.get("regionbuilder.region.overlapping").replace("{player}", RPUtil.UUIDtoPlayer(reg.getCreator())));
                                return;
                        	}
                        }                        
                        
                        int pLimit = RedProtect.ph.getPlayerLimit(p);
                        boolean areaUnlimited = RedProtect.ph.hasPerm(p, "redprotect.unlimited");
                        int totalArea = RedProtect.rm.getTotalRegionSize(pName);
                        if (pLimit >= 0 && totalArea + regionarea > pLimit && !areaUnlimited) {
                            this.setErrorSign(e, RPLang.get("regionbuilder.reach.limit"));
                            return;
                        }
                        p.sendMessage(RPLang.get("general.color") + "------------------------------------");
                        p.sendMessage(RPLang.get("regionbuilder.area.used") + " " + (totalArea + regionarea) + "\n" + 
                        RPLang.get("regionbuilder.area.left") + " " + (areaUnlimited ? RPLang.get("regionbuilder.area.unlimited") : (pLimit - (totalArea + regionarea))));
                        p.sendMessage(RPLang.get("cmdmanager.region.priority.set").replace("{region}", region.getName()) + " " + region.getPrior());
                        
                        if (othersName.size() > 0){
                        	p.sendMessage(RPLang.get("general.color") + "------------------------------------");
                        	p.sendMessage(RPLang.get("regionbuilder.overlapping"));
                        	p.sendMessage(RPLang.get("region.regions") + " " + othersName);
                        	p.sendMessage(RPLang.get("general.color") + "------------------------------------");
                        }
                        
                        if (RPConfig.getDropType("region-settings.drop-type").equals(RedProtect.DROP_TYPE.drop)) {
                            b.breakNaturally();
                            for (Block rb : blocks) {
                                rb.breakNaturally();
                            }
                        }
                        else if (RPConfig.getDropType("region-settings.drop-type").equals(RedProtect.DROP_TYPE.remove)) {
                            b.breakNaturally();
                            for (Block rb : blocks) {
                                rb.setType(Material.AIR);
                            }
                        }
                        super.r = region;
                        return;
                    }
                }
            }
            else if (i == 1 && nearbyCount == 2) {
                blocks.add(current);
                first = current;
                int x2 = bFirst1.getX();
                int z2 = bFirst1.getZ();
                int x3 = bFirst2.getX();
                int z3 = bFirst2.getZ();
                int distx = Math.abs(x2 - x3);
                int distz = Math.abs(z2 - z3);
                if ((distx != 2 || distz != 0) && (distz != 2 || distx != 0)) {
                    px.add(current.getX());
                    pz.add(current.getZ());
                }
            }
            else if (i != 0) {
                this.setErrorSign(e, RPLang.get("regionbuilder.area.error").replace("{area}", "(x: " + current.getX() + ", y: " + current.getY() + ", z: " + current.getZ() + ")"));
                return;
            }
            if (oldFacing != curFacing && i > 1) {
                px.add(current.getX());
                pz.add(current.getZ());
            }
            last = current;
            if (next == null) {
                this.setErrorSign(e, RPLang.get("regionbuilder.area.next"));
                return;
            }
            current = next;
            oldFacing = curFacing;
        }
        this.setErrorSign(e, RPLang.get("regionbuilder.area.toobig"));
    }
}
