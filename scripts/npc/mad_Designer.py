# ObjectID: 1000000
# Character field ID when accessed: 932200005
# ParentID: 9000233
# Object Position Y: 2428
# Object Position X: 485
chr = sm.getChr()
field = chr.field

party = chr.getParty()

if field.id == 932200002: # daylight exit
    sm.warp(932200001, 0)
elif field.id == 932200004: # sunset exit
    sm.warp(932200003, 0)
elif field.id == 932200006: # night exit
    sm.warp(932200005, 0)
else:
    if party is None:
        response = sm.sendAskYesNo("Start the race?")
        if response:
            targetField = 932200300 # night
            targetPortal = 0
            if field.id == 932200001: # daylight
                targetField = 932200100
            elif field.id == 932200003: # sunset
                targetField = 932200200
                targetPortal = 0
            elif field.id == 942000000: # new morning
                targetField = 942000500
            elif field.id == 942001000: # new sunset
                targetField = 942001500
            elif field.id == 942002000: # new night
                targetField = 942002500
            sm.warpFlag(targetField, targetPortal)
    elif not party.hasCharAsLeader(chr):
        sm.sendSayOkay("You need to be the party leader to start the race")
    else:
        response = sm.sendAskYesNo("Start the race?")
        if response:
            targetField = 932200300 # night
            targetPortal = 0
            if field.id == 932200001: # daylight
                targetField = 932200100
            elif field.id == 932200003: # sunset
                targetField = 932200200
                targetPortal = 0
            elif field.id == 942000000: # new morning
                targetField = 942000500
            elif field.id == 942001000: # new sunset
                targetField = 942001500
            elif field.id == 942002000: # new night
                targetField = 942002500
            sm.warpInstanceIn(targetField, targetPortal, True)