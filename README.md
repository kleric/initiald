# Initial D v203.4 flag simulator

A Java Maplestory server emulator modified to allow practicing and racing for Maplestory flag race. This is based off of the v203.4 source modified by poki, with all parts of flag race fixed/implemented. 

## Notable features
 - Implemented Fballs and cannons
 - Ghost/record management so you can race against the best time
 - Powerup spawns
 - Practice/race mode for all maps (including new flag race maps)

## Note on new maps
You'll need to make some client edits to support the new maps. Notably removing pickup cooldown for stars, and importing the maps themselves.
```
  // Star pickup cooldown removal
  DWORD base = 0x012BA6D0;
	*(BYTE*)(base + 1) = 0x2B;
	*(BYTE*)(base + 2) = 0x81;
	*(BYTE*)(base + 3) = 0x62;
	*(BYTE*)(base + 4) = 0xFF;
	*(BYTE*)(base + 5) = 0x3D;
	*(BYTE*)(base + 6) = 0x80;
	*(BYTE*)(base + 7) = 0xC7;
	*(BYTE*)(base + 8) = 0x25;
	*(BYTE*)(base + 9) = 0x38;
	*(BYTE*)(base + 10) = 0x7E;
	*(BYTE*)(base + 11) = 0x2C;
	*(BYTE*)(base + 12) = 0x7F;
	*(BYTE*)(base + 13) = 0x4A;

  // Portal delay removal (lets you hold up to go through portals more quickly)
	base = 0x026BE312;
	for (int i = 0; i < 17; i++) {
		*(BYTE*)(base + i) = 0x90;
	}
```
## Installation
- Join SwordieMS [Discord](https://discord.gg/qzjWZP7hc5).
- Proceed to server-setup-guide.
- Follow the steps accordingly to the steps given in the discord.
- Download and build the given [AuthHook](https://github.com/pokiuwu/AuthHook-v203.4) in Microsoft Visual Studio.
- Drag the output file from the build (ijl15.dll) into your v203.4 Maplestory directory and run a batch file with the following command `MapleStory.exe WebStart admin 8.31.99.141 8484`
- You should be good to go! :octocat:

## Client Installation

- v203.4 Client Download: https://mega.nz/folder/ZnpliaBI#FHc4hqppv6Ustc3zTtSADQ

                                     ----- OR -----

- Download via [Depot Downloader](https://github.com/SteamRE/DepotDownloader).
  - App 216150 
  - Depot 216151 
  - Manifest 116526942226572538 
- Rest of Steps to be added by Poki.


- Setup guide https://docs.google.com/document/d/1BT0IEIUhEIrS9XWISzKcXiSY89PnACYBHnoNI7gIom8/edit?usp=sharing

## Credits
- Notable Credits: SwordieMS Team, Mechaviv, Poki
