# Finish portal in Flag Race
# 932200100
chr = sm.getChr()
chr.lapCount = 0
sm.showActionBar(22)
if not chr.field.isChannelField():
    sm.startNewRace()
else:
    sm.removeClock()

sm.refreshFlagActionBar()
sm.dispose()
#sm.startNewRace(chr)
