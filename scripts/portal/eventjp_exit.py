MENU_TEXT = ["Light Path", "Wat"]
answer = sm.sendNext("Which map would you like to race on?\r\n#L0#Night#l\r\n#L1#Sunset#l\r\n#L2#Morning#l\r\n#L3#Nevermind#l")

if answer == 0: # night
    sm.warp(932200005)
elif answer == 1: #sunset
    sm.warp(932200003)
elif answer == 2: #morning
    sm.warp(932200001)
