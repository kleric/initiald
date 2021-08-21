# Finish portal in Flag Race
# 932200100
chr = sm.getChr()
chr.lapCount = 0
sm.showActionBar(22)
if not chr.field.isChannelField():
    sm.startNewRace()
    if field.id == 942001500:
        chr.sjumps = 1
        chr.ddashes = 1
    elif field.id == 942002500:
        chr.sjumps = 1
        chr.ddashes = 1
else:
    sm.removeClock()

sm.refreshFlagActionBar()
sm.dispose()
#sm.startNewRace(chr)
