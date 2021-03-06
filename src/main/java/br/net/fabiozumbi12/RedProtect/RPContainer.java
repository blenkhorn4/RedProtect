package br.net.fabiozumbi12.RedProtect;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

class RPContainer {

	public boolean canOpen(Block b, Player p) {
    	if (!RPConfig.getBool("private.use")){
    		return true;
    	}
    	
        Boolean Final = true;
    	
        if (RPConfig.getStringList("private.allowed-blocks").contains(b.getType().name())){
        	int x = b.getX();
            int y = b.getY();
            int z = b.getZ();
            World w = p.getWorld();        
            Block[] blocks = new Block[4];        
            blocks[0] = w.getBlockAt(x-1, y, z);
            blocks[1] = w.getBlockAt(x+1, y, z);
            blocks[2] = w.getBlockAt(x, y, z-1);
            blocks[3] = w.getBlockAt(x, y, z+1);     	        

        	for (Block signb:blocks){
            	if (signb.getType().equals(Material.WALL_SIGN)){
            		Sign s = (Sign) signb.getState();
            		if (s.getLine(0).equalsIgnoreCase("[private]") || s.getLine(0).equalsIgnoreCase("private") || s.getLine(0).equalsIgnoreCase(RPLang.get("blocklistener.container.signline")) || s.getLine(0).equalsIgnoreCase("["+RPLang.get("blocklistener.container.signline")+"]")){
            			for (String line:s.getLines()){
            				if (line.equals(p.getName())){
                				Final = true;
                				break;
                			} else {
                				Final = false;
                			}        				
            			}
            		}
            	}
            	            	
            	if (RPConfig.getStringList("private.allowed-blocks").contains(signb.getType().name())){
            		x = signb.getX();
                    y = signb.getY();
                    z = signb.getZ();                
                    blocks[0] = w.getBlockAt(x-1, y, z);
                    blocks[1] = w.getBlockAt(x+1, y, z);
                    blocks[2] = w.getBlockAt(x, y, z-1);
                    blocks[3] = w.getBlockAt(x, y, z+1);  
            		for (Block signc:blocks){
                    	if (signc.getType().equals(Material.WALL_SIGN)){
                    		Sign s = (Sign) signc.getState();
                    		if (s.getLine(0).equalsIgnoreCase("[private]") || s.getLine(0).equalsIgnoreCase("private") || s.getLine(0).equalsIgnoreCase(RPLang.get("blocklistener.container.signline")) || s.getLine(0).equalsIgnoreCase("["+RPLang.get("blocklistener.container.signline")+"]")){ 
                    			for (String line:s.getLines()){
                					if (line.equals(p.getName())){
                        				Final = true;
                        				break;
                    				} else {
                        				Final = false;
                    				}
                				}
                    		}
                    	}               		
            		}
            	}        	
            }
        }
		return Final;        
    }

	public boolean canBreak(Player p, Block b){
    	if (!RPConfig.getBool("private.use")){
    		return true;
    	}
    	Region reg = RedProtect.rm.getTopRegion(b.getLocation());
    	if (reg == null && !RPConfig.getBool("private.allow-outside")){
    		return true;
    	}
    	int x = b.getX();
        int y = b.getY();
        int z = b.getZ();
        World w = p.getWorld();
        
        Block[] blocks = new Block[6];        
        blocks[0] = w.getBlockAt(x-1, y, z);
        blocks[1] = w.getBlockAt(x+1, y, z);
        blocks[2] = w.getBlockAt(x, y, z-1);
        blocks[3] = w.getBlockAt(x, y, z+1); 
        blocks[4] = w.getBlockAt(x, y-1, z);
        blocks[5] = w.getBlockAt(x, y+1, z); 

        if (b.getType().equals(Material.WALL_SIGN)){
    		Sign s = (Sign) b.getState();
    		if ((s.getLine(0).equalsIgnoreCase("[private]") || s.getLine(0).equalsIgnoreCase("private") || s.getLine(0).equalsIgnoreCase(RPLang.get("blocklistener.container.signline")) || s.getLine(0).equalsIgnoreCase("["+RPLang.get("blocklistener.container.signline")+"]")) && 
    			!s.getLine(1).equals(p.getName())){
    		    return false;
    		}
    	}   		
           		
        if (RPConfig.getStringList("private.allowed-blocks").contains(b.getType().name())){
            for (Block signb:blocks){ 
            	if (signb.getType().equals(Material.WALL_SIGN)){
            		Sign s = (Sign) signb.getState();
            		if ((s.getLine(0).equalsIgnoreCase("[private]") || s.getLine(0).equalsIgnoreCase("private") || s.getLine(0).equalsIgnoreCase(RPLang.get("blocklistener.container.signline")) || s.getLine(0).equalsIgnoreCase("["+RPLang.get("blocklistener.container.signline")+"]")) && 
            			!s.getLine(1).equals(p.getName())){
            		    return false;
            		} 
            	}            	
            }        
        } 
        return true;
    }
    
    @SuppressWarnings("deprecation")
	public boolean isContainer(Block b){
    	Block container = null;
    	int face = b.getData() & 0x7;
	    if (face == 3) {
	    	container = b.getRelative(BlockFace.NORTH);
	    }
	    if (face == 4) {
	    	container = b.getRelative(BlockFace.EAST);
	    }
	    if (face == 2) {
	    	container = b.getRelative(BlockFace.SOUTH);
	    }
	    if (face == 5) {
	    	container = b.getRelative(BlockFace.WEST);
	    }    	    
	    
	    if (RPConfig.getStringList("private.allowed-blocks").contains(container.getType().name())){
	    	return true;
	    }
	    return false;
    }  
    
}
