package com.playpandora.petplugin.listeners;

import com.playpandora.petplugin.PetPlugin;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

public class PetListener implements Listener {
    
    private final PetPlugin plugin;
    
    public PetListener(PetPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        
        String title = event.getView().getTitle();
        String shopTitle = org.bukkit.ChatColor.translateAlternateColorCodes('&',
            plugin.getConfig().getString("shop.title", "&8Pet Shop"));
        
        if (!title.equals(shopTitle)) {
            return;
        }
        
        event.setCancelled(true);
        
        if (event.getCurrentItem() == null) {
            return;
        }
        
        int slot = event.getSlot();
        
        // Check if close button was clicked
        int inventorySize = event.getInventory().getSize();
        if (slot == inventorySize - 1) {
            player.closeInventory();
            return;
        }
        
        String petType = plugin.getShopGUI().getPetTypeBySlot(player, slot);
        
        if (petType != null) {
            boolean success = plugin.getPurchaseManager().purchasePet(player, petType);
            // Refresh GUI after purchase attempt
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                plugin.getShopGUI().openShop(player);
            });
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        
        String title = event.getView().getTitle();
        String shopTitle = org.bukkit.ChatColor.translateAlternateColorCodes('&',
            plugin.getConfig().getString("shop.title", "&8Pet Shop"));
        
        if (title.equals(shopTitle)) {
            // Clear slot mapping when shop closes
            plugin.getShopGUI().clearPlayerMapping(player);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }
        
        // Check if this is a pet
        UUID petOwner = plugin.getPetManager().getPetOwner(entity.getUniqueId());
        if (petOwner == null) {
            return; // Not a pet
        }
        
        // Check if damage is from an entity (player or mob)
        Player damagerPlayer = null;
        org.bukkit.entity.Entity damagerEntity = null;
        
        if (event instanceof org.bukkit.event.entity.EntityDamageByEntityEvent) {
            org.bukkit.event.entity.EntityDamageByEntityEvent byEntityEvent = 
                (org.bukkit.event.entity.EntityDamageByEntityEvent) event;
            org.bukkit.entity.Entity directDamager = byEntityEvent.getDamager();
            
            // Check if it's a player
            if (directDamager instanceof Player) {
                damagerPlayer = (Player) directDamager;
            }
            // Check if it's a projectile
            else if (directDamager instanceof org.bukkit.entity.Projectile) {
                org.bukkit.entity.Projectile projectile = (org.bukkit.entity.Projectile) directDamager;
                if (projectile.getShooter() instanceof Player) {
                    damagerPlayer = (Player) projectile.getShooter();
                } else if (projectile.getShooter() instanceof org.bukkit.entity.LivingEntity) {
                    damagerEntity = (org.bukkit.entity.LivingEntity) projectile.getShooter();
                }
            }
            // Check if it's a living entity (mob)
            else if (directDamager instanceof org.bukkit.entity.LivingEntity) {
                damagerEntity = (org.bukkit.entity.LivingEntity) directDamager;
            }
        }
        
        // Prevent owners from hurting their own pets
        if (damagerPlayer != null && damagerPlayer.getUniqueId().equals(petOwner)) {
            event.setCancelled(true);
            damagerPlayer.sendMessage(plugin.formatMessage("messages.cannot-hurt-own-pet",
                "{prefix} &7You cannot hurt your own pet!"));
            return;
        }
        
        // Allow damage from other players (PvP)
        if (damagerPlayer != null && !damagerPlayer.getUniqueId().equals(petOwner)) {
            // Other player damage is allowed, don't cancel
            return;
        }
        
        // Check if damage is from a hostile mob
        if (damagerEntity != null && damagerEntity instanceof org.bukkit.entity.LivingEntity) {
            org.bukkit.entity.LivingEntity livingDamager = (org.bukkit.entity.LivingEntity) damagerEntity;
            org.bukkit.entity.EntityType damagerType = livingDamager.getType();
            
            // Check if it's a hostile mob - check all hostile mob types explicitly
            if (damagerType == org.bukkit.entity.EntityType.ZOMBIE ||
                damagerType == org.bukkit.entity.EntityType.SKELETON ||
                damagerType == org.bukkit.entity.EntityType.CREEPER ||
                damagerType == org.bukkit.entity.EntityType.SPIDER ||
                damagerType == org.bukkit.entity.EntityType.ENDERMAN ||
                damagerType == org.bukkit.entity.EntityType.WITCH ||
                damagerType == org.bukkit.entity.EntityType.BLAZE ||
                damagerType == org.bukkit.entity.EntityType.SLIME ||
                damagerType == org.bukkit.entity.EntityType.MAGMA_CUBE ||
                damagerType == org.bukkit.entity.EntityType.PHANTOM ||
                damagerType == org.bukkit.entity.EntityType.GHAST ||
                damagerType == org.bukkit.entity.EntityType.SHULKER ||
                damagerType == org.bukkit.entity.EntityType.ENDER_DRAGON ||
                damagerType == org.bukkit.entity.EntityType.HOGLIN ||
                damagerType == org.bukkit.entity.EntityType.ZOGLIN ||
                damagerType == org.bukkit.entity.EntityType.PIGLIN_BRUTE ||
                damagerType == org.bukkit.entity.EntityType.VEX ||
                damagerType == org.bukkit.entity.EntityType.EVOKER ||
                damagerType == org.bukkit.entity.EntityType.VINDICATOR ||
                damagerType == org.bukkit.entity.EntityType.PILLAGER ||
                damagerType == org.bukkit.entity.EntityType.RAVAGER ||
                damagerType == org.bukkit.entity.EntityType.WITHER ||
                damagerType == org.bukkit.entity.EntityType.WITHER_SKELETON) {
                // Hostile mob damage is allowed, don't cancel
                return;
            }
        }
        
        // Prevent ALL other damage sources (environmental, fall, fire, lava, drowning, suffocation, void, etc.)
        // This includes:
        // - FALL, FIRE, FIRE_TICK, LAVA, DROWNING, SUFFOCATION, VOID, STARVATION
        // - CONTACT, ENTITY_ATTACK from non-hostile mobs, ENTITY_SWEEP_ATTACK
        // - MAGIC, POISON, WITHER, LIGHTNING, HOT_FLOOR, CRAMMING, FLY_INTO_WALL
        // - DRAGON_BREATH, FREEZE, SONIC_BOOM, etc.
        event.setCancelled(true);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        
        // Check if this is a pet
        UUID petOwner = plugin.getPetManager().getPetOwner(entity.getUniqueId());
        if (petOwner == null) {
            return; // Not a pet
        }
        
        // Prevent death drops and experience
        event.setDroppedExp(0);
        event.getDrops().clear();
        
        // The pet manager will handle death in the health monitor
        // We don't cancel the event here to allow the health monitor to detect it
        // But we ensure no drops are created
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        // Teleport pet if too far
        plugin.getPetManager().teleportPetToPlayer(player);
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Despawn pet on quit
        plugin.getPetManager().despawnPet(player.getUniqueId());
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        
        // Check if player has a cat pet with extra lives
        if (plugin.getPetManager().hasCatPet(player.getUniqueId())) {
            int extraLives = plugin.getPetManager().getCatExtraLives(player.getUniqueId());
            
            // Store extra lives in player metadata if not already set
            if (!player.hasMetadata("petplugin_extra_lives")) {
                player.setMetadata("petplugin_extra_lives", 
                    new org.bukkit.metadata.FixedMetadataValue(plugin, extraLives));
            }
            
            int currentLives = player.getMetadata("petplugin_extra_lives").get(0).asInt();
            if (currentLives > 0) {
                // Use an extra life
                player.setMetadata("petplugin_extra_lives", 
                    new org.bukkit.metadata.FixedMetadataValue(plugin, currentLives - 1));
                
                // Cancel death and revive player
                event.setCancelled(true);
                event.setKeepInventory(true);
                event.setKeepLevel(true);
                
                // Schedule revival for next tick
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    player.setHealth(player.getMaxHealth());
                    player.setFoodLevel(20);
                    player.setSaturation(20);
                    player.setFireTicks(0);
                    
                    // Teleport player to safe location (slightly above)
                    org.bukkit.Location loc = player.getLocation();
                    loc.setY(loc.getY() + 1);
                    player.teleport(loc);
                    
                    // Send message
                    String message = plugin.formatMessage("messages.cat-extra-life-used",
                        "{prefix}Your cat saved you! &6{remaining} &7extra lives remaining.",
                        "remaining", String.valueOf(currentLives - 1));
                    player.sendMessage(message);
                    
                    // Play effects
                    player.getWorld().playSound(player.getLocation(), 
                        org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                    player.getWorld().spawnParticle(org.bukkit.Particle.HEART, 
                        player.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.1);
                });
            }
        }
    }
    
    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        
        // Check if player has a fox pet
        var activePet = plugin.getPetManager().getActivePet(player.getUniqueId());
        if (activePet != null && activePet.getPetType().equalsIgnoreCase("fox")) {
            var petConfig = plugin.getConfig().getConfigurationSection("pet-types.fox");
            if (petConfig != null && petConfig.getBoolean("invisibility-on-sneak", true)) {
                if (event.isSneaking()) {
                    // Apply invisibility
                    player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 
                        Integer.MAX_VALUE, 0, true, false));
                } else {
                    // Remove invisibility
                    player.removePotionEffect(PotionEffectType.INVISIBILITY);
                }
            }
        }
    }
}

