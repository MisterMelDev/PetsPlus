package tech.mistermel.petsplus.pet;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Player;

import tech.mistermel.petsplus.PetsPlus;
import tech.mistermel.petsplus.protocol.Reflection;

public class Pet {
	
	private Player owner;
	private PetType type;
	private Creature entity;
	
	private boolean isBaby;
	
	@SuppressWarnings("deprecation")
	protected Pet(Player owner, PetType type) {
		this.owner = owner;
		this.type = type;
		
		this.entity = (Creature) owner.getWorld().spawnEntity(owner.getLocation(), type.getEntityType());
		entity.setSilent(PetsPlus.getInstance().getConfigManager().getSetting("isSilent"));
		
		if(entity instanceof Ageable && PetsPlus.getInstance().getConfigManager().getSetting("isBabyDefault")) {
			Ageable ageable = (Ageable) entity;
			ageable.setBaby();
			ageable.setAgeLock(true);
		}
		
		if(PetsPlus.getInstance().getConfigManager().getSetting("hasNametag")) {
			entity.setCustomName(ChatColor.GOLD + owner.getName() + "'s " + type.getName());
			entity.setCustomNameVisible(true);
		}
	}
	
	@SuppressWarnings("deprecation")
	public void setBaby(boolean isBaby) {
		this.isBaby = isBaby;
		
		if(entity instanceof Ageable) {
			Ageable ageable = (Ageable) entity;
			ageable.setAgeLock(true);
			
			if(isBaby) ageable.setBaby();
			else ageable.setAdult();
		}
	}
	
	public boolean isBaby() {
		return isBaby;
	}
	
	public boolean hasBabyOption() {
		return entity instanceof Ageable;
	}
	
	@SuppressWarnings("deprecation")
	public void tick() {
		if(entity == null || owner == null || entity.isDead()) {
			return;
		}
		
		double maxHealth = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
		if(entity.getHealth() < maxHealth) {
			entity.setHealth(maxHealth);
		}
		
		if(entity.getTarget() != null) {
			entity.setTarget(null);
		}
		
		if(entity.getPassengers().contains(owner))
			return;
		
		double distance = entity.getWorld() != owner.getWorld() ? Double.MAX_VALUE : entity.getLocation().distanceSquared(owner.getLocation());
		if(distance > 510.0 && owner.isOnGround()) {
			entity.teleport(owner.getLocation().add(1, 0, 0));
		} else if(distance > 10.0) {
			walkTo(owner.getLocation().add(1, 0, 0), 1.3);
		}
	}
	
	private void walkTo(Location targetLocation, double speed) {
		Object c = Reflection.getMethod("{obc}.entity.CraftLivingEntity", "getHandle").invoke(entity);
		Object nav = Reflection.getMethod("{nms}.EntityInsentient", "getNavigation").invoke(c);
		Reflection.getMethod("{nms}.NavigationAbstract", "a", double.class, double.class, double.class, double.class).invoke(nav, targetLocation.getX(), targetLocation.getY(), targetLocation.getZ(), speed);
	}
	
	public void despawn() {
		entity.remove();
		this.owner = null;
		this.entity = null;
	}
	
	protected Creature getEntity() {
		return entity;
	}
	
	public void addPassenger() {
		entity.addPassenger(owner);
	}
	
	public Location getLocation() {
		return entity.getLocation();
	}
	
	public Player getOwner() {
		return owner;
	}
	
	public PetType getType() {
		return type;
	}
	
}
