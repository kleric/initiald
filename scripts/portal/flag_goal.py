from net.swordie.ms.enums import WeatherEffNoticeType

chr = sm.getChr()
field = chr.field

chr.lapCount += 1

chr.dispose()

if field.isChannelField() or chr.lapCount < 3:
    sm.showWeatherNoticeToField(chr.getName() + " you're on lap " + str(chr.lapCount + 1), WeatherEffNoticeType.SnowySnowAndSprinkledFlowerAndSoapBubbles)
    if field.id == 932200300: # Night
        sm.teleportInField(-2203, 2558)
    elif field.id == 932200200: # Sunset
        sm.teleportInField(-343, 2196)
    elif field.id == 932200100: # Night
        sm.teleportInField(-2203, 2558)
    elif field.id == 942001500:
        sm.teleportToPortal(1)
        chr.sjumps = 1
        chr.ddashes = 1
        sm.refreshFlagActionBar()
    elif field.id == 942002500:
        sm.teleportToPortal(5)
        chr.sjumps = 1
        chr.ddashes = 1
        sm.refreshFlagActionBar()
else:
    if field.id == 932200200:
        sm.teleportInField(0, 750)
    elif field.id == 942001500:
        sm.teleportToPortal(5)
    elif field.id == 942002500:
        sm.teleportToPortal(3)
    else:
        sm.teleportToPortal(3)
        # sm.teleportInField(-2053, 700)
    sm.flagGoalReached()
sm.dispose()