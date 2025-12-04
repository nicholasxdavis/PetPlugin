package com.playpandora.petplugin.storage;

import com.playpandora.petplugin.PetPlugin;
import com.playpandora.petplugin.models.Pet;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DataManager {
    
    private final PetPlugin plugin;
    private File dataFile;
    private FileConfiguration dataConfig;
    private final Map<UUID, List<Pet>> playerPets = new HashMap<>();
    private final Map<UUID, List<Pet>> deadPets = new HashMap<>(); // Player UUID -> Dead Pets
    
    public DataManager(PetPlugin plugin) {
        this.plugin = plugin;
        loadData();
    }
    
    public void loadData() {
        dataFile = new File(plugin.getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create data.yml: " + e.getMessage());
                return;
            }
        }
        
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        
        // Load player pets
        if (dataConfig.contains("pets")) {
            for (String uuidString : dataConfig.getConfigurationSection("pets").getKeys(false)) {
                UUID uuid = UUID.fromString(uuidString);
                List<Pet> pets = new ArrayList<>();
                
                for (String petKey : dataConfig.getConfigurationSection("pets." + uuidString).getKeys(false)) {
                    String path = "pets." + uuidString + "." + petKey;
                    String petType = dataConfig.getString(path + ".type");
                    String generatedName = dataConfig.getString(path + ".generatedName");
                    String customName = dataConfig.getString(path + ".customName");
                    Long deathTimestamp = dataConfig.contains(path + ".deathTimestamp") ? 
                        dataConfig.getLong(path + ".deathTimestamp") : null;
                    
                    Pet pet = new Pet(uuid, petType, generatedName);
                    if (customName != null) {
                        pet.setCustomName(customName);
                    }
                    if (deathTimestamp != null) {
                        pet.setDeathTimestamp(deathTimestamp);
                        deadPets.computeIfAbsent(uuid, k -> new ArrayList<>()).add(pet);
                    } else {
                        pets.add(pet);
                    }
                }
                
                playerPets.put(uuid, pets);
            }
        }
    }
    
    public void saveData() {
        if (dataConfig == null || dataFile == null) {
            return;
        }
        
        // Clear existing data
        dataConfig.set("pets", null);
        
        // Save player pets (alive and dead)
        for (Map.Entry<UUID, List<Pet>> entry : playerPets.entrySet()) {
            UUID uuid = entry.getKey();
            List<Pet> pets = entry.getValue();
            
            int index = 0;
            for (Pet pet : pets) {
                String path = "pets." + uuid.toString() + ".pet" + index;
                dataConfig.set(path + ".type", pet.getPetType());
                dataConfig.set(path + ".generatedName", pet.getGeneratedName());
                if (pet.getCustomName() != null) {
                    dataConfig.set(path + ".customName", pet.getCustomName());
                }
                if (pet.getDeathTimestamp() != null) {
                    dataConfig.set(path + ".deathTimestamp", pet.getDeathTimestamp());
                }
                index++;
            }
        }
        
        // Save dead pets
        for (Map.Entry<UUID, List<Pet>> entry : deadPets.entrySet()) {
            UUID uuid = entry.getKey();
            List<Pet> pets = entry.getValue();
            
            int startIndex = playerPets.getOrDefault(uuid, new ArrayList<>()).size();
            for (int i = 0; i < pets.size(); i++) {
                Pet pet = pets.get(i);
                String path = "pets." + uuid.toString() + ".pet" + (startIndex + i);
                dataConfig.set(path + ".type", pet.getPetType());
                dataConfig.set(path + ".generatedName", pet.getGeneratedName());
                if (pet.getCustomName() != null) {
                    dataConfig.set(path + ".customName", pet.getCustomName());
                }
                if (pet.getDeathTimestamp() != null) {
                    dataConfig.set(path + ".deathTimestamp", pet.getDeathTimestamp());
                }
            }
        }
        
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save data.yml: " + e.getMessage());
        }
    }
    
    public void addPet(UUID uuid, Pet pet) {
        playerPets.computeIfAbsent(uuid, k -> new ArrayList<>()).add(pet);
        saveData();
    }
    
    public List<Pet> getPlayerPets(UUID uuid) {
        return playerPets.getOrDefault(uuid, new ArrayList<>());
    }
    
    public boolean hasPetType(UUID uuid, String petType) {
        List<Pet> pets = getPlayerPets(uuid);
        return pets.stream().anyMatch(pet -> pet.getPetType().equalsIgnoreCase(petType));
    }
    
    public Pet getPetByGeneratedName(UUID uuid, String generatedName) {
        List<Pet> pets = getPlayerPets(uuid);
        return pets.stream()
                .filter(pet -> pet.getGeneratedName().equalsIgnoreCase(generatedName))
                .findFirst()
                .orElse(null);
    }
    
    public void removePet(UUID uuid, Pet pet) {
        List<Pet> pets = getPlayerPets(uuid);
        pets.remove(pet);
        
        // Also remove from dead pets if it's there
        List<Pet> dead = deadPets.get(uuid);
        if (dead != null) {
            dead.remove(pet);
            if (dead.isEmpty()) {
                deadPets.remove(uuid);
            }
        }
        
        if (pets.isEmpty() && (dead == null || dead.isEmpty())) {
            playerPets.remove(uuid);
            deadPets.remove(uuid);
        }
        saveData();
    }
    
    public void markPetAsDead(UUID uuid, Pet pet) {
        // Remove from alive pets
        List<Pet> pets = getPlayerPets(uuid);
        pets.remove(pet);
        
        // Add to dead pets with timestamp
        pet.setDeathTimestamp(System.currentTimeMillis());
        deadPets.computeIfAbsent(uuid, k -> new ArrayList<>()).add(pet);
        
        saveData();
    }
    
    public List<Pet> getDeadPets(UUID uuid) {
        return deadPets.getOrDefault(uuid, new ArrayList<>());
    }
    
    public Pet getDeadPetByName(UUID uuid, String petName) {
        List<Pet> dead = getDeadPets(uuid);
        return dead.stream()
                .filter(pet -> pet.getDisplayName().equalsIgnoreCase(petName) || 
                             pet.getGeneratedName().equalsIgnoreCase(petName))
                .findFirst()
                .orElse(null);
    }
    
    public void revivePet(UUID uuid, Pet pet) {
        // Remove from dead pets
        List<Pet> dead = deadPets.get(uuid);
        if (dead != null) {
            dead.remove(pet);
            if (dead.isEmpty()) {
                deadPets.remove(uuid);
            }
        }
        
        // Revive and add back to alive pets
        pet.revive();
        playerPets.computeIfAbsent(uuid, k -> new ArrayList<>()).add(pet);
        
        saveData();
    }
}

