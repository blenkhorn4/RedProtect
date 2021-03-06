package br.net.fabiozumbi12.RedProtect;

import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

abstract class RegionBuilder{
	
    Region r;
    
    public RegionBuilder() {
        this.r = null;
    }
    
    public boolean ready() {
        return this.r != null;
    }
    
    public Region build() {
        return this.r;
    }
    
    void setErrorSign(SignChangeEvent e, String error) {
        e.setLine(0, RPLang.get("regionbuilder.signerror"));
        this.setError(e.getPlayer(), error);
    }
    
    void setError(Player p, String error) {
        p.sendMessage(RPLang.get("regionbuilder.error") + " (" + error + ")");
    }
}
