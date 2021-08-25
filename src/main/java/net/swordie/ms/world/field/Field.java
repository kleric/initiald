package net.swordie.ms.world.field;

import io.netty.channel.ChannelHandlerContext;
import net.swordie.ms.client.Client;
import net.swordie.ms.client.character.Char;
import net.swordie.ms.client.character.items.Item;
import net.swordie.ms.client.character.runestones.RuneStone;
import net.swordie.ms.client.character.skills.TownPortal;
import net.swordie.ms.client.character.skills.info.SkillInfo;
import net.swordie.ms.client.character.skills.temp.TemporaryStatManager;
import net.swordie.ms.client.jobs.adventurer.Archer;
import net.swordie.ms.client.jobs.resistance.OpenGate;
import net.swordie.ms.client.party.Party;
import net.swordie.ms.client.party.PartyMember;
import net.swordie.ms.connection.OutPacket;
import net.swordie.ms.connection.packet.*;
import net.swordie.ms.constants.FlagConstants;
import net.swordie.ms.constants.GameConstants;
import net.swordie.ms.constants.ItemConstants;
import net.swordie.ms.enums.*;
import net.swordie.ms.flag.Ghost;
import net.swordie.ms.flag.GhostManager;
import net.swordie.ms.handlers.EventManager;
import net.swordie.ms.life.*;
import net.swordie.ms.life.drop.Drop;
import net.swordie.ms.life.drop.DropInfo;
import net.swordie.ms.life.mob.Mob;
import net.swordie.ms.life.movement.MovementInfo;
import net.swordie.ms.life.npc.Npc;
import net.swordie.ms.loaders.ItemData;
import net.swordie.ms.loaders.containerclasses.ItemInfo;
import net.swordie.ms.loaders.MobData;
import net.swordie.ms.loaders.SkillData;
import net.swordie.ms.scripts.ScriptManager;
import net.swordie.ms.scripts.ScriptManagerImpl;
import net.swordie.ms.scripts.ScriptType;
import net.swordie.ms.util.*;
import org.apache.log4j.Logger;
import org.kleric.proximity.DiscordConnector;
import org.kleric.proximity.DiscordLobbyManager;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static net.swordie.ms.client.character.skills.SkillStat.t;
import static net.swordie.ms.client.character.skills.SkillStat.time;

/**
 * Created on 12/14/2017.
 */
public class Field {
    private static final Logger log = Logger.getLogger(Field.class);
    private Rect rect;
    private int vrTop, vrLeft, vrBottom, vrRight;
    private double mobRate;
    private int id;
    private FieldType fieldType;
    private long fieldLimit;
    private int returnMap, forcedReturn, createMobInterval, timeOut, timeLimit, lvLimit, lvForceMove;
    private int consumeItemCoolTime, link;
    private boolean town, swim, fly, reactorShuffle, expeditionOnly, partyOnly, needSkillForFly;
    private Set<Portal> portals;
    private Set<Foothold> footholds;
    private Map<Integer, Life> lifes;
    private List<Char> chars;
    private List<Char> ghostChars;
    private Map<Life, Char> lifeToControllers;
    private Map<Life, ScheduledFuture> lifeSchedules;
    private String onFirstUserEnter = "", onUserEnter = "";
    private int fixedMobCapacity;
    private int objectIDCounter = 1000000;
    private boolean userFirstEnter = false;
    private String fieldScript = "";
    private ScriptManagerImpl scriptManagerImpl = new ScriptManagerImpl(this);
    private RuneStone runeStone;
    private ScheduledFuture runeStoneHordesTimer;
    private ScheduledFuture spawnItemsTimer;
    private int burningFieldLevel;
    private long nextEliteSpawnTime = System.currentTimeMillis();
    private int killedElites;
    private EliteState eliteState;
    private int bossMobID;
    private boolean kishin;
    private List<OpenGate> openGateList = new ArrayList<>();
    private List<TownPortal> townPortalList = new ArrayList<>();
    private boolean isChannelField;
    private Map<Integer, List<String>> directionInfo;
    private Clock clock;
    private int channel;

    private boolean soloGhostRace;
    private boolean ghostRace;
    private int ghostId;

    public String voiceLobby;

    public Field(int fieldID) {
        this.id = fieldID;
        this.rect = new Rect();
        this.portals = new HashSet<>();
        this.footholds = new HashSet<>();
        this.lifes = new ConcurrentHashMap<>();
        this.chars = new CopyOnWriteArrayList<>();
        this.ghostChars = new CopyOnWriteArrayList<>();
        this.lifeToControllers = new HashMap<>();
        this.lifeSchedules = new HashMap<>();
        this.directionInfo = new HashMap<>();
        this.fixedMobCapacity = GameConstants.DEFAULT_FIELD_MOB_CAPACITY; // default
    }

    public void startFieldScript() {
        String script = getFieldScript();
        if(!"".equalsIgnoreCase(script)) {
            log.debug(String.format("Starting field script %s.", script));
            scriptManagerImpl.startScript(getId(), script, ScriptType.Field);
        }
    }

    public Rect getRect() {
        return rect;
    }

    public void setRect(Rect rect) {
        this.rect = rect;
    }

    public int getVrTop() {
        return vrTop;
    }

    public void setVrTop(int vrTop) {
        this.vrTop = vrTop;
    }

    public int getVrLeft() {
        return vrLeft;
    }

    public void setVrLeft(int vrLeft) {
        this.vrLeft = vrLeft;
    }

    public int getVrBottom() {
        return vrBottom;
    }

    public void setVrBottom(int vrBottom) {
        this.vrBottom = vrBottom;
    }

    public int getVrRight() {
        return vrRight;
    }

    public void setVrRight(int vrRight) {
        this.vrRight = vrRight;
    }

    public int getHeight() {return getVrTop() - getVrBottom();}

    public int getWidth() {return getVrRight() - getVrLeft();}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setVoiceLobby(String secret) {
        this.voiceLobby = secret;
    }

    public String getVoiceLobby() {
        return voiceLobby;
    }

    public FieldType getFieldType() { return fieldType; }

    public void setFieldType(FieldType fieldType) { this.fieldType = fieldType; }

    public long getFieldLimit() { return fieldLimit; }

    public void setFieldLimit(long fieldLimit) { this.fieldLimit = fieldLimit; }
    
    public Set<Portal> getPortals() {
        return portals;
    }

    public void setPortals(Set<Portal> portals) {
        this.portals = portals;
    }

    public void addPortal(Portal portal) {
        getPortals().add(portal);
    }

    public int getReturnMap() {
        return returnMap;
    }

    public void setReturnMap(int returnMap) {
        this.returnMap = returnMap;
    }

    public int getForcedReturn() {
        return forcedReturn;
    }

    public void setForcedReturn(int forcedReturn) {
        this.forcedReturn = forcedReturn;
    }

    public double getMobRate() {
        return mobRate;
    }

    public void setMobRate(double mobRate) {
        this.mobRate = mobRate;
    }

    public int getCreateMobInterval() {
        return createMobInterval;
    }

    public void setCreateMobInterval(int createMobInterval) {
        this.createMobInterval = createMobInterval;
    }

    public int getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public int getLvLimit() {
        return lvLimit;
    }

    public void setLvLimit(int lvLimit) {
        this.lvLimit = lvLimit;
    }

    public int getLvForceMove() {
        return lvForceMove;
    }

    public void setLvForceMove(int lvForceMove) {
        this.lvForceMove = lvForceMove;
    }

    public int getConsumeItemCoolTime() {
        return consumeItemCoolTime;
    }

    public void setConsumeItemCoolTime(int consumeItemCoolTime) {
        this.consumeItemCoolTime = consumeItemCoolTime;
    }

    public int getLink() {
        return link;
    }

    public void setLink(int link) {
        this.link = link;
    }

    public boolean isTown() {
        return town;
    }

    public void setTown(boolean town) {
        this.town = town;
    }

    public boolean isSwim() {
        return swim;
    }

    public void setSwim(boolean swim) {
        this.swim = swim;
    }

    public boolean isFly() {
        return fly;
    }

    public void setFly(boolean fly) {
        this.fly = fly;
    }

    public boolean isReactorShuffle() {
        return reactorShuffle;
    }

    public void setReactorShuffle(boolean reactorShuffle) {
        this.reactorShuffle = reactorShuffle;
    }

    public boolean isExpeditionOnly() {
        return expeditionOnly;
    }

    public void setExpeditionOnly(boolean expeditionONly) {
        this.expeditionOnly = expeditionONly;
    }

    public boolean isPartyOnly() {
        return partyOnly;
    }

    public void setPartyOnly(boolean partyOnly) {
        this.partyOnly = partyOnly;
    }

    public boolean isNeedSkillForFly() {
        return needSkillForFly;
    }

    public void setNeedSkillForFly(boolean needSkillForFly) {
        this.needSkillForFly = needSkillForFly;
    }

    public String getOnFirstUserEnter() {
        return onFirstUserEnter;
    }

    public void setOnFirstUserEnter(String onFirstUserEnter) {
        this.onFirstUserEnter = onFirstUserEnter;
    }

    public String getOnUserEnter() {
        return onUserEnter;
    }

    public void setOnUserEnter(String onUserEnter) {
        this.onUserEnter = onUserEnter;
    }

    public Portal getPortalByName(String name) {
        return Util.findWithPred(getPortals(), portal -> portal.getName().equals(name));
    }

    public Portal getPortalByID(int id) {
        return Util.findWithPred(getPortals(), portal -> portal.getId() == id);
    }

    public RuneStone getRuneStone() {
        return runeStone;
    }

    public void setRuneStone(RuneStone runeStone) {
        this.runeStone = runeStone;
    }

    public int getBurningFieldLevel() {
        return burningFieldLevel;
    }

    public void setBurningFieldLevel(int burningFieldLevel) {
        this.burningFieldLevel = burningFieldLevel;
    }

    public Foothold findFootHoldBelow(Position pos) {
        Set<Foothold> footholds = getFootholds().stream().filter(fh -> fh.getX1() <= pos.getX() && fh.getX2() >= pos.getX()).collect(Collectors.toSet());
        Foothold res = null;
        int lastY = Integer.MAX_VALUE;
        for (Foothold fh : footholds) {
            int y = fh.getYFromX(pos.getX());
            if (res == null && y >= pos.getY()) {
                res = fh;
                lastY = y;
            } else {
                if (y < lastY && y >= pos.getY()) {
                    res = fh;
                    lastY = y;
                }
            }
        }
        return res;
    }

    public Set<Foothold> getFootholds() {
        return footholds;
    }

    public void setFootholds(Set<Foothold> footholds) {
        this.footholds = footholds;
    }

    public void addFoothold(Foothold foothold) {
        getFootholds().add(foothold);
    }

    public void setFixedMobCapacity(int fixedMobCapacity) {
        this.fixedMobCapacity = fixedMobCapacity;
    }

    public int getFixedMobCapacity() {
        return fixedMobCapacity;
    }

    public Map<Integer, Life> getLifes() {
        return lifes;
    }

    public void addLife(Life life) {
        if (life.getObjectId() < 0) {
            life.setObjectId(getNewObjectID());
        }
        if (!getLifes().values().contains(life)) {
            getLifes().put(life.getObjectId(), life);
            life.setField(this);
            if (life instanceof Mob) {
                if (getScriptManager() != null) {
                    life.addObserver(getScriptManager());
                }
                for (Char chr : getChars()) {
                    life.addObserver(chr.getScriptManager());
                }
            }
        }
    }

    public void removeLife(int id) {
        Life life = getLifeByObjectID(id);
        if (life == null) {
            return;
        }
        getLifes().remove(life.getObjectId());
    }

    public void spawnSummon(Summon summon) {
        Summon oldSummon = (Summon) getLifes().values().stream()
                .filter(s -> s instanceof Summon &&
                        ((Summon) s).getChr() == summon.getChr() &&
                        ((Summon) s).getSkillID() == summon.getSkillID())
                .findFirst().orElse(null);
        if (oldSummon != null) {
            removeLife(oldSummon.getObjectId(), false);
        }
        spawnLife(summon, null);
    }

    public void spawnAddSummon(Summon summon) { //Test
        spawnLife(summon, null);
    }

    public void removeSummon(int skillID, int chrID) {
        Summon summon = (Summon) getLifes().values().stream()
                .filter(s -> s instanceof Summon &&
                        ((Summon) s).getChr().getId() == chrID &&
                        ((Summon) s).getSkillID() == skillID)
                .findFirst().orElse(null);
        if (summon != null) {
            removeLife(summon.getObjectId(), false);
        }
    }

    public void spawnLife(Life life, Char onlyChar) {
        addLife(life);
        if (getChars().size() > 0) {
            Char controller = null;
            if (getLifeToControllers().containsKey(life)) {
                controller = getLifeToControllers().get(life);
            }
            if (controller == null) {
                setRandomController(life);
            }
            life.broadcastSpawnPacket(onlyChar);
        }
    }

    private void setRandomController(Life life) {
        // No chars -> set controller to null, so a controller will be assigned next time someone enters this field
        Char controller = null;
        if (getChars().size() > 0) {
            controller = Util.getRandomFromCollection(getChars());
            life.notifyControllerChange(controller);
        }
        putLifeController(life, controller);
    }

    public void removeLife(Life life) {
        removeLife(life.getObjectId(), false);
    }

    public Foothold getFootholdById(int fh) {
        return getFootholds().stream().filter(f -> f.getId() == fh).findFirst().orElse(null);
    }

    public void clear() {
        List<Char> chrs = getChars();
        for(Char chr : chrs) {
            if (raceGhosts.containsValue(chr)) continue;
            if (chr == ghost) continue;
            chr.setFieldInstanceType(FieldInstanceType.CHANNEL);
            int returnMap = getForcedReturn();
            if (id == FlagConstants.MAP_NEW_NIGHT) {
                Field field = chr.getOrCreateFieldByCurrentInstanceType(FlagConstants.MAP_NEW_NIGHT_LOBBY);
                chr.warp(field);
            } else if (id == FlagConstants.MAP_NEW_SUNSET) {
                Field field = chr.getOrCreateFieldByCurrentInstanceType(FlagConstants.MAP_NEW_SUNSET_LOBBY);
                chr.warp(field);
            } else if (returnMap != GameConstants.NO_MAP_ID) {
                Field field = chr.getOrCreateFieldByCurrentInstanceType(returnMap);
                chr.warp(field);
            }
        }
        chrs.clear();
        ghostChars.clear();
        scriptManagerImpl.stopEvents();

        if (voiceLobby != null) {
            DiscordLobbyManager.deleteLobby(voiceLobby);
            voiceLobby = null;
        }
        if (updateGhostFuture != null) {
            updateGhostFuture.cancel(true);
        }
        if (raceNumber != null && race != null) {
            synchronized (ghosts) {
                for (Integer id : ghosts.keySet()) {
                    Ghost g = new Ghost();
                    g.time = null;
                    g.id = id;
                    if (ghosts.get(g.id) == null) return;
                    g.history = new ArrayList<>(ghosts.get(g.id));
                    race.ghosts.add(g);
                }
                ghosts.clear();
            }
            GhostManager.getInstance().saveRace(raceNumber, race);
            raceGhosts.clear();
        }
    }

    public List<Char> getChars() {
        return chars;
    }

    public Char getCharByID(int id) {
        return getChars().stream().filter(c -> c.getId() == id).findFirst().orElse(null);
    }

    public void addGhostChar(Char chr) {
        if (!getChars().contains(chr)) {
            getChars().add(chr);
        }
        if (!ghostChars.contains(chr)) {
            ghostChars.add(chr);
        }
        if (!FlagConstants.CAMERA_NAME.equalsIgnoreCase(chr.getName())) {
            broadcastPacket(UserPool.userEnterField(chr), chr);
        }
        for (Char c : getChars()) {
            if (c.isCamera()) {
                c.refreshCameraField();
            }
        }
    }

    public void addChar(Char chr) {
        if (isRaceLobby()) {
            if (voiceLobby == null) {
                voiceLobby = DiscordLobbyManager.getOrCreateLobby(DiscordLobbyManager.RACE_LOBBY_CAPACITY);
            }
        }
        if (!getChars().contains(chr)) {
            getChars().add(chr);
            if (voiceLobby != null) {
                DiscordConnector.getInstance().onAddedToLobby(chr, voiceLobby);
            }
            if (!isUserFirstEnter()) {
                if (hasUserFirstEnterScript()) {
                    chr.chatMessage("First enter script!");
                    chr.getScriptManager().startScript(getId(), getOnFirstUserEnter(), ScriptType.FirstEnterField);
                    setUserFirstEnter(true);
                } else if (CustomFUEFieldScripts.getByVal(getId()) != null) {
                    chr.chatMessage("Custom First enter script!");
                    String feFieldScript = CustomFUEFieldScripts.getByVal(getId()).toString();
                    chr.getScriptManager().startScript(getId(), feFieldScript, ScriptType.FirstEnterField);
                    setUserFirstEnter(true);
                }
            }
        }
        if (!FlagConstants.CAMERA_NAME.equalsIgnoreCase(chr.getName())) {
            broadcastPacket(UserPool.userEnterField(chr), chr);
        }
        DiscordConnector.getInstance().updateAccountCharMap(chr);
        for (Char c : getChars()) {
            if (c.isCamera()) {
                c.refreshCameraField();
            }
        }
    }

    private boolean hasUserFirstEnterScript() {
        return getOnFirstUserEnter() != null && !getOnFirstUserEnter().equalsIgnoreCase("");
    }



    public void broadcastPacket(OutPacket outPacket, Char exceptChr) {
        getChars().stream().filter(chr -> !chr.equals(exceptChr)).forEach(
                chr -> chr.write(outPacket)
        );
    }

    public HashMap<Integer, Integer> charScores = new HashMap<>();

    // start -2204
    // goal -2665
    // right side 2532
    // y 1778 , 1408 higher up
    // 2174

    private void sendUpdatedScores() {
        broadcastPacket(CField.updateRanking(charScores, finishedRanking));
    }

    private int bestScore;

    private void updateScore(Char chr) {
        if (!charScores.containsKey(chr.getId())) {
            return;
        }
        Position pos = chr.getPosition();
        if (chr.lapCount >= 3) {
            if (charScores.get(chr.getId()) != 730) {
                bestScore = Math.max(730, bestScore);
                charScores.put(chr.getId(), 730);
                sendUpdatedScores();
            }
            return;
        }
        int score = 0;
        if (chr.lapCount == 1) {
            score = 200;
        } else if (chr.lapCount == 2) {
            score = 400;
        }
        int x = pos.getX();
        int y = pos.getY();
        boolean top;
        if (x < 100) {
            top = pos.getY() < 2150;
        } else {
            top = pos.getY() < 1800;
        }
        if (top) {
            if (x < -2050) { // near goal
                score += 120;
            } else if (x < -1400) {
                score += 110;
            } else if (x < -850) {
                score += 100;
            } else if (x < 100) {
                score += 90;
            } else if (x < 920) {
                score += 80;
            } else if (x < 1400) {
                score += 70;
            } else {
                score += 60;
            }
        } else {
            if (x > 2100) {
                score += 50;
            } else if (x > 400) {
                score += 40;
            } else if (x > -400) {
                score += 30;
            } else if (x > -1250) {
                score += 20;
            } else if (x > -1750) {
                score += 10;
            }
        }

        if (charScores.get(chr.getId()) != score) {
            charScores.put(chr.getId(), score);
            bestScore = Math.max(score, bestScore);
            sendUpdatedScores();
        }
        if (!chr.pity) {
            if (bestScore - score >= 200) {
                chr.pity = true;
                FieldAttackObj fao = new FieldAttackObj(3, 0, chr.getPosition().deepCopy(), false);
                spawnLife(fao, chr);
            }
        }
    }

    public void onMove(Char chr, MovementInfo movementInfo) {
        if (voiceLobby != null) {
            refreshLocation();
        }
        if (!isRace()) {
            return;
        }
        updateScore(chr);
        long time = System.currentTimeMillis() - startTime;
        if (flagFinished > 0) {
            return;
        }
        synchronized (ghosts) {
            List<MovementHistory> moves = ghosts.computeIfAbsent(chr.getId(), k -> new ArrayList<>());
            moves.add(new MovementHistory(time, movementInfo));
        }
    }

    private long lastProximityRefresh;

    public void refreshLocation() {
        if (voiceLobby == null) {
            return;
        }
        if (System.currentTimeMillis() - lastProximityRefresh < 100) {
            return;
        }

        lastProximityRefresh = System.currentTimeMillis();

        for (Char c : chars) {
            OutPacket outPacket = new OutPacket(2);
            outPacket.encodeInt(chars.size() - 1);

            for (Char otherRacer : chars) {
                if (otherRacer != c) {
                    Long discordId = DiscordConnector.getInstance().getDiscordId(otherRacer.getAccId());
                    if (discordId == null) continue;
                    outPacket.encodeLong(discordId);
                    outPacket.encodeByte(calculateVolume(computeDist(c, otherRacer)));
                }
            }
            Long discordId = DiscordConnector.getInstance().getDiscordId(c.getAccId());
            if (discordId == null) continue;
            ChannelHandlerContext context = DiscordConnector.getInstance().getContext(discordId);
            if (context == null) {
                continue;
            }
            context.channel().writeAndFlush(outPacket);
        }
    }

    private double computeDist(Char c1, Char c2) {
        Position p1 = c1.getPosition();
        Position p2 = c2.getPosition();
        int dX = p1.getX() - p2.getX();
        int dY = p1.getY() - p2.getY();

        return Math.sqrt(dX * dX + dY * dY);
    }

    // TODO
    private int cutoff = 1000;
    private double ratio = (100.0 / Math.log10(cutoff));
    private byte calculateVolume(double dist) {
        if (dist > cutoff) return 0;
        if (dist < 20) return 100;
        int volume = (int)(100.0 * (90 / dist));

        return (byte) Math.min(Math.max(0, volume), 100);
    }

    private final HashMap<Integer, List<MovementHistory>> ghosts = new HashMap<>();

    public boolean isRaceLobby() {
        return FlagConstants.isRaceLobby(id);
    }

    public boolean isNewRace() {
        switch (id) {
            case FlagConstants.MAP_NEW_NIGHT:
            case FlagConstants.MAP_NEW_SUNSET:
                return !isChannelField;
        }
        return false;
    }

    public boolean isRace() {
        switch (id) {
            case FlagConstants.MAP_SUNSET:
            case FlagConstants.MAP_DAY:
            case FlagConstants.MAP_NIGHT:
            case FlagConstants.MAP_NEW_NIGHT:
            case FlagConstants.MAP_NEW_SUNSET:
                return !isChannelField;
        }
        return false;
    }

    private boolean isSoloRace() {
        return isRace() && chars != null && chars.size() == 1;
    }

    public void removeChar(Char chr) {
        getChars().remove(chr);
        ghostChars.remove(chr);
        if (!FlagConstants.CAMERA_NAME.equalsIgnoreCase(chr.getName())) {
            broadcastPacket(UserPool.userLeaveField(chr), chr);
            for (Char c : getChars()) {
                if (c.isCamera()) {
                    c.refreshCameraField();
                }
            }
        }
        // change controllers for which the chr was the controller of
        for (Map.Entry<Life, Char> entry : getLifeToControllers().entrySet()) {
            if (entry.getValue() != null && entry.getValue().equals(chr)) {
                setRandomController(entry.getKey());
            }
        }
        // remove summons of that char & remove field attacks of that char
        List<Integer> removedList = new ArrayList<>();
        for (Life life : getLifes().values()) {
            if (life instanceof Summon && ((Summon) life).getChr() == chr) {
                removedList.add(life.getObjectId());
            } else if (life instanceof FieldAttackObj) {
                FieldAttackObj fao = (FieldAttackObj) life;
                if (fao.getOwnerID() == chr.getId() && fao.getTemplateId() == Archer.ARROW_PLATTER) {
                    removedList.add(life.getObjectId());
                }
            }
        }
        for (int id : removedList) {
            removeLife(id, false);
        }
        if (getChars().size() == ghostChars.size()) {
            if (voiceLobby != null) {
                String voice = voiceLobby;
                voiceLobby = null;
                DiscordLobbyManager.deleteLobby(voice);
            }
            clear();
        }
    }

    public Map<Life, Char> getLifeToControllers() {
        return lifeToControllers;
    }

    public void setLifeToControllers(Map<Life, Char> lifeToControllers) {
        this.lifeToControllers = lifeToControllers;
    }

    public void putLifeController(Life life, Char chr) {
        getLifeToControllers().put(life, chr);
    }

    public Life getLifeByObjectID(int objectId) {
        return getLifes().getOrDefault(objectId, null);
    }

    public Life getLifeByTemplateId(int templateId) {
        return getLifes().values().stream().filter(l -> l.getTemplateId() == templateId).findFirst().orElse(null);
    }

    public void spawnLifesForChar(Char chr) {
        for (Life life : getLifes().values()) {
            spawnLife(life, chr);
        }
        if (getRuneStone() != null && getMobs().size() > 0 && getBossMobID() == 0 && isChannelField() && !isTown()) {
            chr.write(CField.runeStoneAppear(getRuneStone()));
        }
        if (getOpenGates() != null && getOpenGates().size() > 0) {
            for (OpenGate openGate : getOpenGates()) {
                openGate.showOpenGate(this);
            }
        }
        if (getTownPortalList() != null && getTownPortalList().size() > 0) {
            for (TownPortal townPortal : getTownPortalList()) {
                townPortal.showTownPortal(this);
            }
        }
        if (getClock() != null) {
            getClock().showClock(chr);
        }
        boolean isCamera = chr.isCamera();
        if (isCamera) {
            chr.refreshCameraField();
        }
        for (Char c : getChars()) {
            if (!c.equals(chr) && !c.isCamera()) {
                chr.write(UserPool.userEnterField(c));
                Dragon dragon = c.getDragon();
                if (dragon != null) {
                    chr.write(CField.createDragon(dragon));
                }
            }
        }
    }

    @Override
    public String toString() {
        return "" + getId();
    }

    public void respawn(Mob mob) {
        mob.setHp(mob.getMaxHp());
        mob.setMp(mob.getMaxMp());
        mob.setPosition(mob.getHomePosition().deepCopy());
        spawnLife(mob, null);
    }

    public void broadcastPacket(OutPacket outPacket) {
        for (Char c : getChars()) {
            if (c.getClient() != null) {
                c.getClient().write(outPacket);
            }
        }
    }

    private void broadcastPacket(OutPacket outPacket, Predicate<? super Char> predicate) {
        getChars().stream().filter(predicate).forEach(chr -> chr.write(outPacket));
    }

    public void spawnAffectedArea(AffectedArea aa) {
        addLife(aa);
        SkillInfo si = SkillData.getSkillInfoById(aa.getSkillID());
        if (si != null) {
            int duration = aa.getDuration() == 0 ? si.getValue(time, aa.getSlv()) * 1000 : aa.getDuration();
            if (duration > 0) {
                ScheduledFuture sf = EventManager.addEvent(() -> removeLife(aa.getObjectId(), true), duration);
                addLifeSchedule(aa, sf);
            }
        }
        broadcastPacket(CField.affectedAreaCreated(aa));
        getChars().forEach(chr -> aa.getField().checkCharInAffectedAreas(chr));
        getMobs().forEach(mob -> aa.getField().checkMobInAffectedAreas(mob));
    }

    public void spawnAffectedAreaAndRemoveOld(AffectedArea aa) {
        AffectedArea oldAA = (AffectedArea) getLifes().values().stream()
                .filter(l -> l instanceof AffectedArea &&
                        ((AffectedArea) l).getCharID() == aa.getCharID() &&
                        ((AffectedArea) l).getSkillID() == aa.getSkillID())
                .findFirst().orElse(null);
        if (oldAA != null) {
            removeLife(oldAA.getObjectId(), false);
        }
        spawnAffectedArea(aa);
    }

    private <T> Set<T> getLifesByClass(Class clazz) {
        return (Set<T>) getLifes().values().stream()
                .filter(l -> l.getClass().equals(clazz))
                .collect(Collectors.toSet());
    }

    public Set<Mob> getMobs() {
        return getLifesByClass(Mob.class);
    }

    public Set<Summon> getSummons() {
        return getLifesByClass(Summon.class);
    }

    public Set<Drop> getDrops() {
        return getLifesByClass(Drop.class);
    }

    public Set<MobGen> getMobGens() {
        return getLifesByClass(MobGen.class);
    }

    public Set<AffectedArea> getAffectedAreas() {
        return getLifesByClass(AffectedArea.class);
    }

    public Set<Reactor> getReactors() {
        return getLifesByClass(Reactor.class);
    }

    public Set<Npc> getNpcs() {
        return getLifesByClass(Npc.class);
    }

    public Set<FieldAttackObj> getFieldAttackObjects() {
        return getLifesByClass(FieldAttackObj.class);
    }

    public void setObjectIDCounter(int idCounter) {
        objectIDCounter = idCounter;
    }

    public int getNewObjectID() {
        return objectIDCounter++;
    }

    public List<Life> getLifesInRect(Rect rect) {
        List<Life> lifes = new ArrayList<>();
        for (Life life : getLifes().values()) {
            Position position = life.getPosition();
            int x = position.getX();
            int y = position.getY();
            if (x >= rect.getLeft() && y >= rect.getTop()
                    && x <= rect.getRight() && y <= rect.getBottom()) {
                lifes.add(life);
            }
        }
        return lifes;
    }

    public List<Char> getCharsInRect(Rect rect) {
        List<Char> chars = new ArrayList<>();
        for (Char chr : getChars()) {
            Position position = chr.getPosition();
            int x = position.getX();
            int y = position.getY();
            if (x >= rect.getLeft() && y >= rect.getTop()
                    && x <= rect.getRight() && y <= rect.getBottom()) {
                chars.add(chr);
            }
        }
        return chars;
    }

    public List<PartyMember> getPartyMembersInRect(Char chr, Rect rect) {
        Party party = chr.getParty();
        List<PartyMember> partyMembers = new ArrayList<>();
        for (PartyMember partyMember : party.getOnlineMembers()) {
            Position position = partyMember.getChr().getPosition();
            int x = position.getX();
            int y = position.getY();
            if (x >= rect.getLeft() && y >= rect.getTop()
                    && x <= rect.getRight() && y <= rect.getBottom()) {
                partyMembers.add(partyMember);
            }
        }
        return partyMembers;
    }

    public List<Mob> getMobsInRect(Rect rect) {
        List<Mob> mobs = new ArrayList<>();
        for (Mob mob : getMobs()) {
            Position position = mob.getPosition();
            int x = position.getX();
            int y = position.getY();
            if (x >= rect.getLeft() && y >= rect.getTop()
                    && x <= rect.getRight() && y <= rect.getBottom()) {
                mobs.add(mob);
            }
        }
        return mobs;
    }

    public List<Mob> getBossMobsInRect(Rect rect) {
        List<Mob> mobs = new ArrayList<>();
        for (Mob mob : getMobs()) {
            if(mob.isBoss()) {
                Position position = mob.getPosition();
                int x = position.getX();
                int y = position.getY();
                if (x >= rect.getLeft() && y >= rect.getTop()
                        && x <= rect.getRight() && y <= rect.getBottom()) {
                    mobs.add(mob);
                }
            }
        }
        return mobs;
    }

    public List<Drop> getDropsInRect(Rect rect) {
        List<Drop> drops = new ArrayList<>();
        for (Drop drop : getDrops()) {
            Position position = drop.getPosition();
            int x = position.getX();
            int y = position.getY();
            if (x >= rect.getLeft() && y >= rect.getTop()
                    && x <= rect.getRight() && y <= rect.getBottom()) {
                drops.add(drop);
            }
        }
        return drops;
    }

    public synchronized void removeLife(int id, boolean fromSchedule) {
        Life life = getLifeByObjectID(id);
        if (life == null) {
            return;
        }
        removeLife(id);
        removeSchedule(life, fromSchedule);
        life.broadcastLeavePacket();
    }

    public synchronized void removeDrop(int dropID, int pickupUserID, boolean fromSchedule, int petID) {
        Life life = getLifeByObjectID(dropID);
        if (life instanceof Drop) {
            if (petID >= 0) {
                broadcastPacket(DropPool.dropLeaveField(DropLeaveType.PetPickup, pickupUserID, life.getObjectId(),
                        (short) 0, petID, 0));
            } else if (pickupUserID != 0) {
                broadcastPacket(DropPool.dropLeaveField(dropID, pickupUserID));
            } else {
                broadcastPacket(DropPool.dropLeaveField(DropLeaveType.Fade, pickupUserID, life.getObjectId(),
                        (short) 0, 0, 0));
            }
            removeLife(dropID, fromSchedule);
        }
    }

    public Map<Life, ScheduledFuture> getLifeSchedules() {
        return lifeSchedules;
    }

    public void addLifeSchedule(Life life, ScheduledFuture scheduledFuture) {
        getLifeSchedules().put(life, scheduledFuture);
    }

    public void removeSchedule(Life life, boolean fromSchedule) {
        if (!getLifeSchedules().containsKey(life)) {
            return;
        }
        if (!fromSchedule) {
            getLifeSchedules().get(life).cancel(false);
        }
        getLifeSchedules().remove(life);
    }

    public void checkMobInAffectedAreas(Mob mob) {
        for (AffectedArea aa : getAffectedAreas()) {
            if (aa.getRect().hasPositionInside(mob.getPosition())) {
                aa.handleMobInside(mob);
            }
        }
    }

    public void checkCharInAffectedAreas(Char chr) {
        TemporaryStatManager tsm = chr.getTemporaryStatManager();
        for (AffectedArea aa : getAffectedAreas()) {
            boolean isInsideAA = aa.getRect().hasPositionInside(chr.getPosition());
            if (isInsideAA) {
                aa.handleCharInside(chr);
            } else if (tsm.hasAffectedArea(aa) && !isInsideAA) {
                tsm.removeAffectedArea(aa);
            }
        }
    }
    public void drop(Drop drop, Position posFrom, Position posTo) {
        drop(drop, posFrom, posTo, false);
    }
    
    /**
     * Drops an item to this map, given a {@link Drop}, a starting Position and an ending Position.
     * Immediately broadcasts the drop packet.
     *
     * @param drop    The Drop to drop.
     * @param posFrom The Position that the drop starts off from.
     * @param posTo   The Position where the drop lands.
     * @param ignoreTradability if the drop should ignore tradability (i.e., untradable items won't disappear)
     */
    public void drop(Drop drop, Position posFrom, Position posTo, boolean ignoreTradability) {
        boolean isTradable = true;
        Item item = drop.getItem();
        if (item != null) {
            ItemInfo itemInfo = ItemData.getItemInfoByID(item.getItemId());
            // must be tradable, and if not an equip, not a quest item
            isTradable = ignoreTradability ||
                    (item.isTradable() && (ItemConstants.isEquip(item.getItemId()) || itemInfo != null
                    && !itemInfo.isQuest()));
        }
        drop.setPosition(posTo);
        if (isTradable) {
            addLife(drop);
            getLifeSchedules().put(drop,
                    EventManager.addEvent(() -> removeDrop(drop.getObjectId(), 0, true, -1),
                            GameConstants.DROP_REMAIN_ON_GROUND_TIME, TimeUnit.SECONDS));
        } else {
            drop.setObjectId(getNewObjectID()); // just so the client sees the drop
        }
        // Check for collision items such as exp orbs from combo kills
        if (!isTradable) {
            broadcastPacket(DropPool.dropEnterField(drop, posFrom, 0, DropEnterType.FadeAway));
        } else if(drop.getItem() != null && ItemConstants.isCollisionLootItem(drop.getItem().getItemId())) {
            broadcastPacket(DropPool.dropEnterFieldCollisionPickUp(drop, posFrom, 0));
        } else {
            for (Char chr : getChars()) {
                if (!chr.getClient().getWorld().isReboot() || drop.canBePickedUpBy(chr)) {
                    broadcastPacket(DropPool.dropEnterField(drop, posFrom, posTo, 0, drop.canBePickedUpBy(chr)));
                }
            }
        }

    }

    /**
     * Drops a {@link Drop} according to a given {@link DropInfo DropInfo}'s specification.
     *
     * @param dropInfo The
     * @param posFrom  The Position that hte drop starts off from.
     * @param posTo    The Position where the drop lands.
     * @param ownerID  The owner's character ID. Will not be able to be picked up by Chars that are not the owner.
     */
    public void drop(DropInfo dropInfo, Position posFrom, Position posTo, int ownerID) {
        int itemID = dropInfo.getItemID();
        Item item;
        Drop drop = new Drop(-1);
        drop.setPosition(posTo);
        drop.setOwnerID(ownerID);
        Set<Integer> quests = new HashSet<>();
        if (itemID != 0) {
            item = ItemData.getItemDeepCopy(itemID, true);
            if (item != null) {
                item.setQuantity(dropInfo.getQuantity());
                drop.setItem(item);
                ItemInfo ii = ItemData.getItemInfoByID(itemID);
                if (ii != null && ii.isQuest()) {
                    quests = ii.getQuestIDs();
                }
            } else {
                log.error("Was not able to find the item to drop! id = " + itemID);
                return;
            }
        } else {
            drop.setMoney(dropInfo.getMoney());
        }
        addLife(drop);
        drop.setExpireTime(FileTime.fromDate(LocalDateTime.now().plusSeconds(GameConstants.DROP_REMOVE_OWNERSHIP_TIME)));
        getLifeSchedules().put(drop,
                EventManager.addEvent(() -> removeDrop(drop.getObjectId(), 0, true, -1),
                        GameConstants.DROP_REMAIN_ON_GROUND_TIME, TimeUnit.SECONDS));
        EventManager.addEvent(() -> drop.setOwnerID(0), GameConstants.DROP_REMOVE_OWNERSHIP_TIME, TimeUnit.SECONDS);
        for (Char chr : getChars()) {
            if (chr.hasAnyQuestsInProgress(quests)) {
                broadcastPacket(DropPool.dropEnterField(drop, posFrom, posTo, ownerID, drop.canBePickedUpBy(chr)));
            }
        }
    }

    /**
     * Drops a Set of {@link DropInfo}s from a base Position.
     *
     * @param dropInfos The Set of DropInfos.
     * @param position  The Position the initial Drop comes from.
     * @param ownerID   The owner's character ID.
     */
    public void drop(Set<DropInfo> dropInfos, Position position, int ownerID, boolean isElite) {
        drop(dropInfos, findFootHoldBelow(position), position, ownerID, 0, 0, isElite);
    }

    public void drop(Drop drop, Position position) {
        drop(drop, position, false);
    }
    
    /**
     * Drops a {@link Drop} at a given Position. Calculates the Position that the Drop should land at.
     *
     * @param drop     The Drop that should be dropped.
     * @param position The Position it is dropped from.
     * @param fromReactor if it quest item the item will disapear
     */
    public void drop(Drop drop, Position position, boolean fromReactor) {
        int x = position.getX();
        Position posTo = new Position(x, findFootHoldBelow(position).getYFromX(x));
        drop(drop, position, posTo, fromReactor);
    }

    /**
     * Drops a Set of {@link DropInfo}s, locked to a specific {@link Foothold}.
     * Not all drops are guaranteed to be dropped, as this method calculates whether or not a Drop should drop, according
     * to the DropInfo's prop chance.
     *
     * @param dropInfos The Set of DropInfos that should be dropped.
     * @param fh        The Foothold this Set of DropInfos is bound to.
     * @param position  The Position the Drops originate from.
     * @param ownerID   The ID of the owner of all drops.
     * @param mesoRate  The added meso rate of the character.
     * @param dropRate  The added drop rate of the character.
     */
    public void drop(Set<DropInfo> dropInfos, Foothold fh, Position position, int ownerID, int mesoRate, int dropRate, boolean isElite) {
        int x = position.getX();
        int minX = fh == null ? position.getX() : fh.getX1();
        int maxX = fh == null ? position.getX() : fh.getX2();
        int diff = 0;
        for (DropInfo dropInfo : dropInfos) {
            if (dropInfo.willDrop(dropRate)) {
                if (!isElite && dropInfo.getItemID() / 10000 == 271) continue;
                x = (x + diff) > maxX ? maxX - 10 : (x + diff) < minX ? minX + 10 : x + diff;
                Position posTo;
                if (fh == null) {
                    posTo = position.deepCopy();
                } else {
                    posTo = new Position(x, fh.getYFromX(x));
                }
                // Copy the drop info for money, as we chance the amount that's in there.
                // Not copying -> original dropinfo will keep increasing in mesos
                DropInfo copy = null;
                if (dropInfo.isMoney()) {
                    copy = dropInfo.deepCopy();
                    copy.setMoney((int) (dropInfo.getMoney() * ((100 + mesoRate) / 100D)));
                }
                drop(copy != null ? copy : dropInfo, position, posTo, ownerID);
                diff = diff < 0 ? Math.abs(diff - GameConstants.DROP_DIFF) : -(diff + GameConstants.DROP_DIFF);
                dropInfo.generateNextDrop();
            }
        }
    }

    public List<Portal> getClosestPortal(Rect rect) {
        List<Portal> portals = new ArrayList<>();
        for (Portal portals2 : getPortals()) {
            int x = portals2.getX();
            int y = portals2.getY();
            if (x >= rect.getLeft() && y >= rect.getTop()
                    && x <= rect.getRight() && y <= rect.getBottom()) {
                portals.add(portals2);
            }
        }
        return portals;
    }

    public Char getCharByName(String name) {
        return getChars().stream().filter(chr -> chr.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public void execUserEnterScript(Char chr) {
        chr.clearCurrentDirectionNode();
        if(getOnUserEnter() == null) {
            return;
        }
        if (!getOnUserEnter().equalsIgnoreCase("")) {
            String script = getOnUserEnter();
            chr.getScriptManager().startScript(getId(), script, ScriptType.Field);
        } else if (CustomFieldScripts.getByVal(getId()) != null) {
            String customFieldScriptName = CustomFieldScripts.getByVal(getId()).toString();
            chr.chatMessage("Custom Field Script:");
            chr.getScriptManager().startScript(getId(), customFieldScriptName, ScriptType.Field);
        }
    }

    public boolean isUserFirstEnter() {
        return userFirstEnter;
    }

    public void setUserFirstEnter(boolean userFirstEnter) {
        this.userFirstEnter = userFirstEnter;
    }

    public int getAliveMobCount() {
        // not using getMobs() to only have to iterate `lifes' once
        return getLifes().values().stream()
                .filter(life -> life instanceof Mob && ((Mob) life).isAlive())
                .collect(Collectors.toList())
                .size();
    }

    public int getAliveMobCount(int mobID) {
        // not using getMobs() to only have to iterate `lifes' once
        return getLifes().values().stream()
                .filter(life -> life instanceof Mob && life.getTemplateId() == mobID && ((Mob) life).isAlive())
                .collect(Collectors.toList())
                .size();
    }

    public String getFieldScript() {
        return fieldScript;
    }

    public void setFieldScript(String fieldScript) {
        this.fieldScript = fieldScript;
    }

    public Mob spawnMobWithAppearType(int id, int x, int y, int appearType, int option) {
        Mob mob = MobData.getMobDeepCopyById(id);
        Position pos = new Position(x, y);
        mob.setPosition(pos.deepCopy());
        mob.setPrevPos(pos.deepCopy());
        mob.setPosition(pos.deepCopy());
        mob.setNotRespawnable(true);
        mob.setAppearType((byte) appearType);
        mob.setOption(option);
        if (mob.getField() == null) {
            mob.setField(this);
        }
        spawnLife(mob, null);
        return mob;
    }
    
    public Mob spawnMob(int id, int x, int y, boolean respawnable, long hp) {
        Mob mob = MobData.getMobDeepCopyById(id);
        Position pos = new Position(x, y);
        mob.setPosition(pos.deepCopy());
        mob.setPrevPos(pos.deepCopy());
        mob.setPosition(pos.deepCopy());
        mob.setNotRespawnable(!respawnable);
        if (hp > 0) {
            mob.setHp(hp);
            mob.setMaxHp(hp);
        }
        if (mob.getField() == null) {
            mob.setField(this);
        }
        spawnLife(mob, null);
        return mob;
    }

    public void spawnRuneStone() {
        if(getMobs().size() <= 0 || getBossMobID() != 0 || !isChannelField()) {
            return;
        }
        if(getRuneStone() == null) {
            RuneStone runeStone = new RuneStone().getRandomRuneStone(this);
            setRuneStone(runeStone);
            broadcastPacket(CField.runeStoneAppear(runeStone));
        }
    }

    public void useRuneStone(Client c, RuneStone runeStone) {
        broadcastPacket(CField.completeRune(c.getChr()));
        broadcastPacket(CField.runeStoneDisappear());
        c.write(CField.runeStoneSkillAck(runeStone.getRuneType()));

        setRuneStone(null);

        EventManager.addEvent(this::spawnRuneStone, GameConstants.RUNE_RESPAWN_TIME, TimeUnit.MINUTES);
    }

    public void runeStoneHordeEffect(int mobRateMultiplier, int duration) {
        double prevMobRate = getMobRate();
        setMobRate(getMobRate() * mobRateMultiplier); //Temporary increase in mob Spawn
        if(runeStoneHordesTimer != null && !runeStoneHordesTimer.isDone()) {
            runeStoneHordesTimer.cancel(true);
        }
        runeStoneHordesTimer = EventManager.addEvent(() -> setMobRate(prevMobRate), duration, TimeUnit.SECONDS);
    }

    public int getBonusExpByBurningFieldLevel() {
        return burningFieldLevel * GameConstants.BURNING_FIELD_BONUS_EXP_MULTIPLIER_PER_LEVEL; //Burning Field Level * The GameConstant
    }

    public void showBurningLevel() {
        String string = "#fn ExtraBold##fs26#          Burning Field has been destroyed.          ";
        if(getBurningFieldLevel() > 0) {
            string = "#fn ExtraBold##fs26#          Burning Stage " + getBurningFieldLevel() + ": " + getBonusExpByBurningFieldLevel() + "% Bonus EXP!          ";
        }
        Effect effect = Effect.createFieldTextEffect(string, 50, 2000, 4,
                new Position(0, -200), 1, 4 , TextEffectType.BurningField, 0, 0);
        broadcastPacket(User.effect(effect));
    }

    public void increaseBurningLevel() {
        setBurningFieldLevel(getBurningFieldLevel() + 1);
    }

    public void decreaseBurningLevel() {
        setBurningFieldLevel(getBurningFieldLevel() - 1);
    }

    public void startBurningFieldTimer() {
        if(getMobGens().size() > 0
                && getMobs().stream().mapToInt(m -> m.getForcedMobStat().getLevel()).min().orElse(0) >= GameConstants.BURNING_FIELD_MIN_MOB_LEVEL) {
            setBurningFieldLevel(GameConstants.BURNING_FIELD_LEVEL_ON_START);
            EventManager.addFixedRateEvent(this::changeBurningLevel, 0, GameConstants.BURNING_FIELD_TIMER, TimeUnit.MINUTES); //Every X minutes runs 'changeBurningLevel()'
        }
    }

    public void changeBurningLevel() {
        boolean showMessage = true;

        if(getBurningFieldLevel() <= 0) {
            showMessage = false;
        }

        //If there are players on the map,  decrease the level  else  increase the level
        if(getChars().size() > 0 && getBurningFieldLevel() > 0) {
            decreaseBurningLevel();

        } else if(getChars().size() <= 0 && getBurningFieldLevel() < GameConstants.BURNING_FIELD_MAX_LEVEL){
            increaseBurningLevel();
            showMessage = true;
        }

        if(showMessage) {
            showBurningLevel();
        }
    }

    public void setNextEliteSpawnTime(long nextEliteSpawnTime) {
        this.nextEliteSpawnTime = nextEliteSpawnTime;
    }

    public long getNextEliteSpawnTime() {
        return nextEliteSpawnTime;
    }

    public boolean canSpawnElite() {
        return isChannelField()
                && (getEliteState() == null || getEliteState() == EliteState.None)
                && getNextEliteSpawnTime() < System.currentTimeMillis();
    }

    public int getKilledElites() {
        return killedElites;
    }

    public void setKilledElites(int killedElites) {
        this.killedElites = killedElites;
    }

    public void incrementEliteKillCount() {
        setKilledElites(getKilledElites() + 1);
    }

    public void setEliteState(EliteState eliteState) {
        this.eliteState = eliteState;
    }

    public EliteState getEliteState() {
        return eliteState;
    }

    public List<Foothold> getNonWallFootholds() {
        return getFootholds().stream().filter(fh -> !fh.isWall()).collect(Collectors.toList());
    }

    public void setBossMobID(int bossMobID) {
        this.bossMobID = bossMobID;
    }

    public int getBossMobID() {
        return bossMobID;
    }

    public Portal getDefaultPortal() {
        Portal p = getPortalByName("sp");
        return p == null ? getPortalByID(0) : p;
    }

    private ScriptManager getScriptManager() {
        return scriptManagerImpl;
    }

    /**
     * Goes through all MobGens, and spawns a Mob from it if allowed to do so. Only generates when there are Chars
     * on this Field, or if the field is being initialized.
     * @param init if this is the first time that this method is called.
     */
    public void generateMobs(boolean init) {
        if (init || getChars().size() > 0) {
            boolean buffed = !isChannelField()
                    && getChannel() > GameConstants.CHANNELS_PER_WORLD - GameConstants.BUFFED_CHANNELS;
            int currentMobs = getMobs().size();
            for (MobGen mg : getMobGens()) {
                if (mg.canSpawnOnField(this)) {
                    mg.spawnMob(this, buffed);
                    currentMobs++;
                    if ((getFieldLimit() & FieldOption.NoMobCapacityLimit.getVal()) == 0
                            && currentMobs > getFixedMobCapacity()) {
                        break;
                    }
                }
            }
        }
        // No fixed rate to ensure kishin-ness keeps being checked
        double kishinMultiplier = hasKishin() ? GameConstants.KISHIN_MOB_RATE_MULTIPLIER : 1;
        EventManager.addEvent(() -> generateMobs(false),
                (long) (GameConstants.BASE_MOB_RESPAWN_RATE / (getMobRate() * kishinMultiplier)));
    }

    private boolean spawningItems = false;

    private ScheduledFuture timeLimitTimer;

    private long startTime = 0;

    // top level < 1800

    public synchronized void startNewRace() {
        if (isChannelField || startTime != 0) {
            return;
        }
        flagFinished = 0;
        startTime = System.currentTimeMillis();
        bestScore = 0;
        startSpawningItems();
        setTimer(8 * 60);
        ghostRace = isSoloRace();
        synchronized (ghosts) {
            ghosts.clear();
        }
        if (isRace() && FlagConstants.SPAWN_GHOST) {
            spawnGhost();
            //spawnGhosts();
        }
        charScores.clear();
        finishedRanking.clear();
        EventManager.addEvent(this::createRanking, 25_000);
    }

    private void createRanking() {
        List<Char> rankingList = new ArrayList<>();
        for (Char c : chars) {
            if (raceGhosts.containsValue(c)) continue;
            if (c == ghost) continue;
            if (FlagConstants.CAMERA_NAME.equalsIgnoreCase(c.getName())) continue;
            charScores.put(c.getId(), 1);
            rankingList.add(c);
        }
        if (rankingList.isEmpty()) return;
        broadcastPacket(CField.createRanking(rankingList.size()));
        broadcastPacket(CField.setRankingNames(rankingList));
        broadcastPacket(CField.updateRanking(charScores, finishedRanking));
    }

    private HashMap<Integer, Char> raceGhosts = new HashMap<>();
    private Char ghost;

    private ScheduledFuture updateGhostFuture;

    private int getNumGhosts() {
        if (ghostRace) return 1;
        return 3;
    }

    public void updateRanking(int b) {
        HashMap<Integer, Integer> scores = new HashMap<>();
        for (Char c : chars) {
            scores.put(c.getId(), 100);
        }
        broadcastPacket(CField.updateRanking(scores, b));
    }

    private void spawnGhosts() {
        if (!raceGhosts.isEmpty()) return;
        mapGhosts = GhostManager.getInstance().getGhosts(id);
        Portal portal = getDefaultPortal();
        int numGhosts = getNumGhosts();
        NumberFormat formatter = new DecimalFormat("#0.000");
        for (int i = 0; i < numGhosts && i < mapGhosts.size(); i++) {
            Ghost g = mapGhosts.get(i);
            Char ghostChar = Char.getFromDBByName("Ghost" + (i + 1));
            ghostChar.setField(this);
            ghostChar.getAvatarData().getCharacterStat().setPortal(portal.getId());
            ghostChar.setPosition(new Position(portal.getX(), portal.getY()));

            Char real = Char.getFromDBById(g.id);
            boolean inRace = false;
            for (Char c : chars) {
                if (c.getId() == g.id) {
                    inRace = true;
                    break;
                }
            }

            String time = formatter.format(g.time / 1000.0);
            ghostChar.getAvatarData().getCharacterStat().setName(real.getAvatarData().getCharacterStat().getName() + " " + time);
            if (!inRace) {
                ghostChar.getAvatarData().setAvatarLook(real.getAvatarData().getAvatarLook());
            }
            raceGhosts.put(g.id, ghostChar);
            addGhostChar(ghostChar);
        }

        startGhosts();
    }

    private void spawnGhost() {
        if (ghost == null) {
            Portal portal = getDefaultPortal();
            ghost = Char.getFromDBByName("Ghost");
            ghost.setField(this);
            ghost.getAvatarData().getCharacterStat().setPortal(portal.getId());
            ghost.setPosition(new Position(portal.getX(), portal.getY()));
            //mapGhost = GhostManager.getInstance().getGhost(id);
            mapGhosts = GhostManager.getInstance().getGhosts(id);
            if (mapGhosts.isEmpty()) return;
            int ghostIndex = 1;
            for (Char c : chars) {
                if (ghostRace) {
                    ghostIndex = c.ghostSetting;
                    break;
                }
            }
            ghostIndex -= 1;
            if (ghostIndex >= 0 && ghostIndex < mapGhosts.size()) {
                mapGhost = mapGhosts.get(ghostIndex);
            }
            if (mapGhost == null) {
                return;
            }
            Char real = Char.getFromDBById(mapGhost.id);
            boolean inRace = false;
            for (Char c : chars) {
                if (c.getId() == mapGhost.id) {
                    inRace = true;
                    break;
                }
            }

            NumberFormat formatter = new DecimalFormat("#0.000");
            String time = formatter.format(mapGhost.time / 1000.0);
            ghost.getAvatarData().getCharacterStat().setName(real.getAvatarData().getCharacterStat().getName() + " " + time);
            if (!inRace) {
                ghost.getAvatarData().setAvatarLook(real.getAvatarData().getAvatarLook());
            }
            ghostId = ghost.getId();
            addGhostChar(ghost);
            startGhost();
        }
    }

    private List<Ghost> mapGhosts;

    private Ghost mapGhost;

    private HashMap<Integer, ScheduledFuture> ghostFutures = new HashMap<>();
    private HashMap<Integer, Integer> ghostIndexes = new HashMap<>();

    private void startGhosts() {
        for (ScheduledFuture f : ghostFutures.values()) {
            if (!f.isDone() || !f.isCancelled()) {
                f.cancel(true);
            }
        }
        ghostFutures.clear();
        ghostIndexes.clear();

        for (Ghost g : mapGhosts) {
            ghostIndexes.put(g.id, 0);
            ghostFutures.put(g.id,
                    EventManager.addEvent(
                            new Runnable() {
                                @Override
                                public void run() {
                                    Long diff = updateGhosts(g);
                                    if (diff != null) {
                                        ghostFutures.put(g.id, EventManager.addEvent(this, diff));
                                    }
                                }
                            }, 0));
        }
    }

    private void startGhost() {
        if (updateGhostFuture != null && !updateGhostFuture.isDone() && !updateGhostFuture.isCancelled()) {
            updateGhostFuture.cancel(true);
        }
        ghostIndex = 0;
        if (mapGhost != null) {
            updateGhostFuture = EventManager.addEvent(this::updateGhost, 0);
        }
    }

    private int ghostIndex = 0;

    private Long updateGhosts(Ghost g) {
        long offset = System.currentTimeMillis() - startTime;
        int index = ghostIndexes.get(g.id);
        MovementHistory cur = g.history.get(index);
        broadcastPacket(UserRemote.move(raceGhosts.get(g.id), cur.movementInfo));
        index++;
        ghostIndexes.put(g.id, index);
        if (g.history.size() > index) {
            cur = g.history.get(index);
            return cur.timestamp - offset;
        }
        return null;
    }

    private void updateGhost() {
        long offset = System.currentTimeMillis() - startTime;
        MovementHistory cur = mapGhost.history.get(ghostIndex);
        cur.movementInfo.applyTo(ghost);
        broadcastPacket(UserRemote.move(ghost, cur.movementInfo));
        ghostIndex++;
        if (mapGhost.history.size() > ghostIndex) {
            cur = mapGhost.history.get(ghostIndex);
            long diff = cur.timestamp - offset;
            EventManager.addEvent(this::updateGhost, diff);
        } else {
            removeChar(ghost);
            mapGhost = null;
        }
    }

    public long getCreateTime() {
        return startTime;
    }

    private int flagFinished = 0;
    private Integer raceNumber = null;

    public synchronized int incrementAndGetFinish() {
        flagFinished++;
        if (flagFinished == 1) {
            boolean solo = (getChars().size() - ghostChars.size()) == 1;
            setTimer(solo ? 5 : 60);
            raceNumber = GhostManager.getInstance().getAndIncrementRaceCount();
        }
        return flagFinished;
    }

    private ArrayList<Integer> finishedRanking = new ArrayList<>();

    private GhostManager.Race race;

    public synchronized void addGhost(int place, long time, Char chr) {
        if (chr.getId() == ghostId) return;
        Ghost g = new Ghost();
        g.time = time;
        g.id = chr.getId();
        finishedRanking.add(g.id);
        synchronized (ghosts) {
            if (ghosts.get(g.id) == null) return;
            g.history = new ArrayList<>(ghosts.remove(g.id));
        }
        if (race == null) {
            race = new GhostManager.Race();
        }
        race.ghosts.add(g);
        GhostManager.getInstance().updateRecord(id, chr, time);
        if (place == 1) {
            GhostManager.getInstance().updateGhost(id, g);
        }
    }

    public void setTimer(int seconds) {
        if (timeLimitTimer != null && !timeLimitTimer.isDone()) {
            timeLimitTimer.cancel(true);
        }
        new Clock(ClockType.SecondsClock, this, seconds);
        timeLimitTimer = EventManager.addEvent(this::clear, seconds, TimeUnit.SECONDS);
    }

    public synchronized void startSpawningItems() {
        if (!spawningItems) {
            if (isNewRace()) {
                scriptManagerImpl.addEvent(EventManager.addFixedRateEvent(this::spawnStars, 1000, FlagConstants.POWERUP_SPAWN_TIME));
            } else {
                scriptManagerImpl.addEvent(EventManager.addFixedRateEvent(this::spawnItems, FlagConstants.POWERUP_START_TIME, FlagConstants.POWERUP_SPAWN_TIME));
            }
            spawningItems = true;
        }
    }

    private void spawnStars() {
        if (!isNewRace()) {
            return;
        }
        if (id == FlagConstants.MAP_NEW_NIGHT) {
            spawnStar(FlagConstants.N_STAR_1);
            spawnStar(FlagConstants.N_STAR_2);
            spawnStar(FlagConstants.N_STAR_3);
            spawnStar(FlagConstants.N_STAR_4);
            spawnStar(FlagConstants.N_STAR_5);
            spawnStar(FlagConstants.N_STAR_6);
        } else if (id == FlagConstants.MAP_NEW_SUNSET) {
            spawnStar(FlagConstants.S_STAR_1);
            spawnStar(FlagConstants.S_STAR_2);
            spawnStar(FlagConstants.S_STAR_3);
            spawnStar(FlagConstants.S_STAR_4);
            spawnStar(FlagConstants.S_STAR_5);
        }
    }

    private void spawnItems() {
        if (id != FlagConstants.MAP_DAY && id != FlagConstants.MAP_NIGHT) return;
        dropPowerUp(FlagConstants.NIGHT_SPAWN_1_X, FlagConstants.NIGHT_SPAWN_1_Y);
        dropPowerUp(FlagConstants.NIGHT_SPAWN_2_X, FlagConstants.NIGHT_SPAWN_2_Y);
        dropPowerUp(FlagConstants.NIGHT_SPAWN_3_X, FlagConstants.NIGHT_SPAWN_3_Y);
        dropPowerUp(FlagConstants.NIGHT_SPAWN_4_X, FlagConstants.NIGHT_SPAWN_4_Y);
        dropPowerUp(FlagConstants.NIGHT_SPAWN_5_X, FlagConstants.NIGHT_SPAWN_5_Y);
        dropPowerUp(FlagConstants.NIGHT_SPAWN_5_X, FlagConstants.NIGHT_SPAWN_5_Y);
        dropPowerUp(FlagConstants.NIGHT_SPAWN_6_X, FlagConstants.NIGHT_SPAWN_6_Y);
        dropPowerUp(FlagConstants.NIGHT_SPAWN_7_X, FlagConstants.NIGHT_SPAWN_7_Y);
        dropPowerUp(FlagConstants.NIGHT_SPAWN_8_X, FlagConstants.NIGHT_SPAWN_8_Y);

        // Regular
        /*dropPowerUp(FlagConstants.NIGHT_SPAWN_9_X, FlagConstants.NIGHT_SPAWN_9_Y);
        dropPowerUp(FlagConstants.NIGHT_SPAWN_10_X, FlagConstants.NIGHT_SPAWN_10_Y);
        dropPowerUp(FlagConstants.NIGHT_SPAWN_11_X, FlagConstants.NIGHT_SPAWN_11_Y);*/

        if (getChannel() == 3) {
            // FBall
            dropPowerUp(FlagConstants.NIGHT_SPAWN_9_X, FlagConstants.NIGHT_SPAWN_9_Y);
            dropFBall(FlagConstants.NIGHT_SPAWN_10_X, FlagConstants.NIGHT_SPAWN_10_Y);
            dropDDash(FlagConstants.NIGHT_SPAWN_11_X, FlagConstants.NIGHT_SPAWN_11_Y);
        } else {
            dropPowerUp(FlagConstants.NIGHT_SPAWN_9_X, FlagConstants.NIGHT_SPAWN_9_Y);
            dropPowerUp(FlagConstants.NIGHT_SPAWN_10_X, FlagConstants.NIGHT_SPAWN_10_Y);
            dropPowerUp(FlagConstants.NIGHT_SPAWN_11_X, FlagConstants.NIGHT_SPAWN_11_Y);
        }

        // Rigged
        /*dropSJump(FlagConstants.NIGHT_SPAWN_9_X, FlagConstants.NIGHT_SPAWN_9_Y);
        dropDDash(FlagConstants.NIGHT_SPAWN_10_X, FlagConstants.NIGHT_SPAWN_10_Y);
        dropDDash(FlagConstants.NIGHT_SPAWN_11_X, FlagConstants.NIGHT_SPAWN_11_Y);*/

        dropPowerUp(FlagConstants.NIGHT_SPAWN_12_X, FlagConstants.NIGHT_SPAWN_12_Y);
    }

    private void dropFBall(int x, int y) {
        int id = 2023298; // sjump
        Item item = ItemData.getItemDeepCopy(id);
        Drop drop = new Drop(-1, item);
        drop(drop, new Position(x, y));
    }

    private void dropSJump(int x, int y) {
        int id = 2023296; // sjump
        Item item = ItemData.getItemDeepCopy(id);
        Drop drop = new Drop(-1, item);
        drop(drop, new Position(x, y));
    }

    private void dropDDash(int x, int y) {
        int id = 2023297; // ddash
        Item item = ItemData.getItemDeepCopy(id);
        Drop drop = new Drop(-1, item);
        drop(drop, new Position(x, y));
    }

    private void dropPowerUp(int x, int y) {
        //dropSJump(x, y);
        int index = (int) (Math.random() * FlagConstants.POWERUPS.length);
        int id = FlagConstants.POWERUPS[index];
        Item item = ItemData.getItemDeepCopy(id);
        Drop drop = new Drop(-1, item);
        drop(drop, new Position(x, y));
    }

    private void spawnStar(Position position) {
        Item item = ItemData.getItemDeepCopy(FlagConstants.STAR);
        Drop drop = new Drop(-1, item);
        drop(drop, position, position,true);
    }


    public int getMobCapacity() {
        return (int) (getFixedMobCapacity() * (hasKishin() ? GameConstants.KISHIN_MOB_MULTIPLIER : 1));
    }

    public boolean hasKishin() {
        return kishin;
    }

    public void setKishin(boolean kishin) {
        this.kishin = kishin;
    }

    public List<OpenGate> getOpenGates() {
        return openGateList;
    }

    public void setOpenGates(List<OpenGate> openGateList) {
        this.openGateList = openGateList;
    }

    public void addOpenGate(OpenGate openGate) {
        getOpenGates().add(openGate);
    }

    public void removeOpenGate(OpenGate openGate) {
        getOpenGates().remove(openGate);
    }

    public boolean isChannelField() {
        return isChannelField;
    }

    public void setChannelField(boolean channelField) {
        this.isChannelField = channelField;
    }

    public List<TownPortal> getTownPortalList() {
        return townPortalList;
    }

    public void setTownPortalList(List<TownPortal> townPortalList) {
        this.townPortalList = townPortalList;
    }

    public void addTownPortal(TownPortal townPortal) {
        getTownPortalList().add(townPortal);
    }

    public void removeTownPortal(TownPortal townPortal) {
        getTownPortalList().remove(townPortal);
    }

    public TownPortal getTownPortalByChrId(int chrId) {
        return getTownPortalList().stream().filter(tp -> tp.getChr().getId() == chrId).findAny().orElse(null);
    }
    
    public void increaseReactorState(Char chr, int templateId, int stateLength) {
        Life life = getLifeByTemplateId(templateId);
        if (life instanceof Reactor) {
            Reactor reactor = (Reactor) life;
            reactor.increaseState();
            chr.write(ReactorPool.reactorChangeState(reactor, (short) 0, (byte) stateLength));
        }
    }

    public Map<Integer, List<String>> getDirectionInfo() {
        return directionInfo;
    }

    public void setDirectionInfo(Map<Integer, List<String>> directionInfo) {
        this.directionInfo = directionInfo;
    }

    public List<String> getDirectionNode(int node) {
        return directionInfo.getOrDefault(node, null);
    }

    public void addDirectionInfo(int node, List<String> scripts) {
        directionInfo.put(node, scripts);
    }

    public Clock getClock() {
        return clock;
    }

    public void setClock(Clock clock) {
        this.clock = clock;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }
}
