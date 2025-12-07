<td style="text-align: center;">

<img src="https://nicholasxdavis.github.io/BN-db1/img/PhotoshopExtension_Image.png" width="600" height="300" alt="PetPlugin Banner" />

PetPlugin 1.0.0 

Private Release by PandoraPlugins by Blacnova

<br />  

<a href="#"> <img src="https://img.shields.io/badge/Version-1.0.0-skyblue?style=for-the-badge" alt="Current Version"> </a>

<br /><hr />

About PetPlugin

PetPlugin is a feature-rich pet management system for Spigot/Paper 1.21 servers. It offers a user-friendly GUI shop where players can purchase various pets with unique abilities, stats, and requirements. Pets act as loyal companions, providing buffs, combat support, and utility features. The plugin integrates with Vault for economy and supports LevelPlugin for level-based unlocking.

Key Features

GUI Pet Shop: A clean, navigable interface (/pet) to browse and purchase pets.

Unique Pet Types: Includes Horses (Mounts), Dogs (Guardians), Cats (Extra Lives), Wolves (Combat), Parrots (Utility), and Foxes (Stealth).

Special Abilities: Pets provide passive buffs to their owners:

Cat: Speed boost and saves the owner from death (Extra Lives).

Parrot: Grants permanent Night Vision.

Fox: Grants Invisibility when the owner sneaks.

Smart AI & Mechanics:

Auto-Heal: Pets regenerate health over time.

Safety: Pets flee when low on health to avoid death.

Teleportation: Pets automatically teleport to catch up with their owner.

Revive System: If a pet dies, players have a configurable window (default 6 hours) to revive them for a fee.

Progression: Lock powerful pets behind player levels (requires LevelPlugin).

</td>

<br /><hr />

# Commands

/pet,/pets | Opens the Pet Shop GUI to buy new pets.

/pet list | "View a list of your active, stored, and dead pets."

/pet spawn <type> | Spawns a specific pet type you own.

/pet despawn | Despawns your currently active pet.

/pet rename <old> <new> | Renames a pet (uses the generated name).

/pet release <name> | Permanently releases a pet.

/pet revive <name> | Revives a dead pet (within time limit).

# Dependencies

To run PetPlugin, your server requires the following:

Java 21 or higher.

Spigot/Paper 1.21 or compatible fork.

Vault (Required for purchasing and reviving pets).

LevelPlugin (Optional, for level-based purchase requirements).

Configuration

The plugin is highly configurable via config.yml. You can adjust pet stats, abilities, prices, and system mechanics.

# Example Pet Configuration
pet-types:
  cat:
    enabled: true
    name: "&6Lucky Cat"
    price: 20000.0
    required-level: 35
    # Cat stats
    max-health: 20.0
    speed-boost-level: 1
    extra-lives: 1
    
  fox:
    enabled: true
    name: "&6Fox"
    price: 8000.0
    # Fox stats
    max-health: 20.0
    invisibility-on-sneak: true


# License

Copyright © 2025 Blacnova Development

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program.  If not, see https://www.gnu.org/licenses/.

<br /><hr />

<td style="text-align: center;">

Support & Links

Report Bugs • View Source

</td>
