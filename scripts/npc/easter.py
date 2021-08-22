sm.setSpeakerID(9200001)
answer = sm.sendNext("Which map would you like to practice on?\r\n#L0#Night#l\r\n#L1#Sunset#l\r\n#L2#Morning#l\r\n#L3#Nevermind#l")

if answer == 0: # night
    sm.warp(932200300)
elif answer == 1: #sunset
    sm.warp(932200200)
elif answer == 2: #morning
    sm.warp(932200100)
