sm.setSpeakerID(9000232)
response = sm.sendAskYesNo("Go to the flag area?")

if response:
    sm.warp(820000000)