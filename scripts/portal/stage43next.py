from net.swordie.ms.client.character.skills.temp import CharacterTemporaryStat

#sm.removeCTS(CharacterTemporaryStat.ReverseInput)
chr.lapCount += 1
if chr.lapCount == 1: # stage 2 darkness
    sm.teleportToPortal(2)
elif chr.lapCount == 2: # stage 3 reverse
    sm.teleportToPortal(3)
    sm.giveCTS(CharacterTemporaryStat.ReverseInput, 1, 132 | 65536, 3600000)
else:
    sm.removeCTS(CharacterTemporaryStat.ReverseInput)
    sm.teleportToPortal(5)