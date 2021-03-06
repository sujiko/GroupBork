/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GroupBork;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * this keeps track of the players gamestate in bork, it also handles reading in
 * and out save sates
 *
 * @author qureshi225
 */
public class GameState {

    private static GameState onlyInstance;
    private Room currentRoom;
    private Dungeon currentDungeon;
    private boolean initState = true;
    private ArrayList<Item> inventory = new ArrayList<Item>();
    private int score;
    private int health;
    private int maxHealth;
    private int mana;
    private int maxMana;
    private int playersZennys;
    private int strength;
    public boolean running = true;
    private boolean danger = true;
    private boolean verbose = true;
    private ArrayList<Item> outOfGame = new ArrayList<Item>();
    private Monster[] monsters = new Monster[5];

    private GameState() {
    }

    /**
     * this creates and stores only one instance of the gamestate class
     */
    public static synchronized GameState Instance() {
        if (onlyInstance == null) {
            onlyInstance = new GameState();
        }
        return onlyInstance;
    }

    /**
     * creates the dungeon
     *
     * @param dungeon creates the dungeon that is to be stored int he gamestate
     */
    public void initialize(Dungeon dungeon) {
        currentDungeon = dungeon;
        currentRoom = currentDungeon.getEntry();
        genInitialHealth();
        genStrength();
        genMana();
        monInitialize();

    }

    /**
     * this gets the current room an adventurer is in.
     *
     * @return the room that the adventurer is linked to standing in.
     */
    public Room getAdvenurersCurrentRoom() {
        return currentRoom;
    }

    /**
     * sets which room the adventurer is in
     *
     * @param room give this command the room the adventurere is moving into or
     * currently in.
     */
    public void setAdventurersCurrentRoom(Room room) {
        this.currentRoom = room;
    }

    /**
     * this returns the dungeon object.
     *
     * @return the dungeon
     */
    public Dungeon getDungeon() {
        return currentDungeon;
    }

    /**
     * gets a list of all the the items in the players inventory.
     *
     * @return returns the string names of the items int he players invetory.
     */
    public ArrayList<String> getInventoryNames() {
        ArrayList<String> toReturn = new ArrayList<String>();
        for (Item i : inventory) {
            toReturn.add(i.getPrimaryName());
        }
        return toReturn;
    }

    /**
     * this adds an item to the players inventory
     *
     * @param i the item to add to the inventory
     */
    public void addToInventory(Item i) {
        inventory.add(i);
        this.currentRoom.remove(i);
    }

    /**
     * this removes an item to the players inventory
     *
     * @param i the item to remove to the inventory
     */
    public void removeFrominventory(Item i) {
        inventory.remove(i);
        this.currentRoom.add(i);
    }

    /**
     * this gets an item to the players inventory or from a players surroundings
     *
     * @param name the item to find
     * @return the item, if it is found.
     */
    public Item getItemInVicinityNamed(String name) {
        if (this.getItemFromInventoryNamed(name) != null) {
            return this.getItemFromInventoryNamed(name);
        }
        return this.currentRoom.getItemNamed(name);
    }

    /**
     * this gets an item to the players inventory
     *
     * @param name the item to find
     * @return the item, if it is found.
     */
    public Item getItemFromInventoryNamed(String name) {
        for (Item i : inventory) {
            if (i.getPrimaryName().equals(name)) {
                return i;
            }
        }
        return null;
    }

    /**
     * creates the savestate for a player
     *
     * @param saveName takes a name for the players file to be saved as and
     * creates a save from it.
     */
    public void store(String saveName) {
        BufferedWriter writer = null;
        try {
            String fileName = "";
            if (saveName.endsWith(".sav")) {
                fileName = saveName;
            } else {
                fileName = saveName + ".sav";
            }
            FileWriter write = new FileWriter(fileName);
            writer = new BufferedWriter(write);
        } catch (Exception e) {
        }
        try {
            writer.write("Bork V3.0\n");
            currentDungeon.storeState(writer);
            writer.write("Adventurer: \n");
            writer.write("Initial Health:" + this.maxHealth + "\n");
            writer.write("CurrentHealth:" + this.getHealth() + "\n");
            writer.write("Players Zennys:" + this.getZennys() + "\n");
            writer.write("Current Room: " + onlyInstance.getAdvenurersCurrentRoom().getTitle() + "\n");
            String itemsInInventory = "";
            if(!inventory.isEmpty()){
            for (Item i : inventory) {
                itemsInInventory += i.getPrimaryName() + ",";
            }
            itemsInInventory = itemsInInventory.substring(0, itemsInInventory.length() - 1);
            }
            writer.write("Inventory: " + itemsInInventory + "\n");
            String itemsOutOfGame = "";
            if (!outOfGame.isEmpty()) {
                for (Item j : outOfGame) {
                    itemsOutOfGame += j.getPrimaryName() + ",";
                }
                itemsOutOfGame = itemsOutOfGame.substring(0, itemsOutOfGame.length() - 1);
            }
            writer.write("Items that no longer exist: " + itemsOutOfGame + "\n");
            writer.write("Shopkeepers inventory: ");
            String shopsell = "";
            if (!Shopkeeper.Instance().getInventory().isEmpty()) {
                for (Item i : Shopkeeper.Instance().getInventory()) {
                    shopsell += i.getPrimaryName() + ",";
                }
                shopsell = shopsell.substring(0, shopsell.length() - 1) + "\n";
            }
            writer.write(shopsell);
            writer.close();
        } catch (Exception e) {

        }

    }

    /**
     * restores the gamestate from a save file for the player
     *
     * @param fileName takes a file name and reads the files to create a bork
     * game back to how the player had it.
     */
    public void restore(String fileName) {
        BufferedReader buffer = null;
        try {
            FileReader fileReader = new FileReader(fileName);
            buffer = new BufferedReader(fileReader);
        } catch (Exception e) {
            System.out.println("this isn't a proper .sav file.");
            System.exit(54);
        }
        try {
            buffer.readLine();
            buffer.readLine();
            String[] split = buffer.readLine().split(":");
            String line = split[1].trim();
            initState = false;
            currentDungeon = new Dungeon(line, initState);
            line = buffer.readLine();
            //while(!line.equals("===")){
            currentDungeon.restoreState(buffer);
            buffer.readLine();
            line = buffer.readLine();
            split = line.split(":");
            this.maxHealth = Integer.valueOf(split[1]);
            line = buffer.readLine();
            split = line.split(":");
            this.health = Integer.valueOf(split[1]);
            line = buffer.readLine();
            split = line.split(":");
            this.playersZennys = Integer.valueOf(split[1]);
            line = buffer.readLine();
            split = line.split(":");
            line = split[1].trim();
            currentRoom = currentDungeon.getRoom(line);
            line = buffer.readLine();
            split = line.split(":");
            line = split[1].trim();
            String[] loadItems = line.split(",");
            if(!loadItems[0].equals("")){
            for (String i : loadItems) {
                inventory.add(currentDungeon.getItem(i));
            }
            }
            line = buffer.readLine();
            split = line.split(":");
            line = split[1].trim();
            String[] destroyItems = line.split(",");
            if(!destroyItems[0].equals("")){
            for (String i : destroyItems) {
                Event getRid = new DisappearEvent(this.getDungeon().getItem(i));
                getRid.execute();
            }
            }
            line = buffer.readLine();
            split = line.split(":");
            line = split[1].trim();
            String[] isselling = line.split(",");
            if(!isselling[0].equals("")){
            ArrayList<String> selling = new ArrayList<String>(Arrays.asList(isselling));
            ArrayList<Item> inven= Shopkeeper.Instance().getInventory();
            for (Item i :inven) {
                String checkshopkeep= i.getPrimaryName();
                if (selling.contains(i.getPrimaryName())) {

                } else {
                    Shopkeeper.Instance().removeItem(i);
                }
            }
            }
        } catch (Exception e) {

        }

    }

    /* 
    * this gives the current score of the player
    *@return  the integer of the players score
     */
    public int getScore() {
        return this.score;
    }

    /* 
    * this gives the current health of the player
    *@return  the integer of the players health
     */
    public int getHealth() {
        return this.health;
    }

    /*
    * this returns the players initial health
    *@return the integer of the players inital health
     */
    public int getMaxHealth() {
        return this.maxHealth;
    }

    /*
    * this generates the characters initial health.
    *
     */
    public void genInitialHealth() {
        int Health = (int) (Math.random() * 20) + 10;
        this.maxHealth = Health;
        this.health = Health;
    }

    /**
     * this generates the characters initial strength.
     */
    public void genStrength() {
        this.strength = (int) (Math.random() * 20) + 10;
    }

    /* 
    * this adds to the players score
    *@param i
    *               the interget to be subtracted from the players score
     */
    public void addScore(int i) {
        this.score += i;
    }

    /* 
    * thissubtracts from the score of the player
    *@param i
     *              the interger to be subtacted from the players score
     */
    public void minusScore(int i) {
        this.score -= i;
    }

    /* 
    * this adds to the players health
    *@param i
    *               the interget to be subtracted from the players health
     */
    public void addHealth(int i) {
        if ((this.health += i) <= this.maxHealth) {
            this.health += i;
        } else {
            this.health = this.maxHealth;
        }

    }

    /* 
    * thissubtracts from the health of the player
    *@param i
     *              the interger to be subtacted from the players health
     */
    public void minusHealth(int i) {
        this.health -= i;
        if (this.health <= 0) {
            this.running = false;
            System.out.println("It would seem this action killed you.");
        }
    }

    public int getStrength() {
        return this.strength;
    }

    /* 
    * this method adds items that have been taken out of the game to the out of game arraylist
    *@param i
    *               The item to add
     */
    public void isGone(Item i) {
        this.outOfGame.add(i);
    }

    /**
     * this helps get the monsters for a room.
     *
     * @return monster array
     */
    public Monster[] getMon() {
        return this.monsters;
    }

    /**
     * this initializes the monsters in the dungeon.
     *
     */
    public void monInitialize() {
        Monster hydra = new Monster("Hydra");
        Monster dragon = Dragon.Instance();
        Monster skeleton = new Monster("Skeleton");
        Monster basalisk = new Monster("Basalisk");
        Monster shopkeeper = Shopkeeper.Instance();
        Monster Chimera = new Monster("Chimera");
        this.monsters = new Monster[6];
        this.monsters[0] = hydra;
        this.monsters[1] = Chimera;
        this.monsters[2] = skeleton;
        this.monsters[3] = basalisk;
        this.monsters[4] = shopkeeper;
        this.monsters[5] = dragon;
    }

    /**
     * this can change the danger for monster creations
     */
    public void setDanger() {
        if (this.danger == true) {
            this.danger = false;
        } else {
            this.danger = true;
        }
    }

    /**
     * evaluates danger and returns it
     *
     * @return danger
     */
    public boolean getDanger() {
        return this.danger;
    }

    public int getZennys() {
        return this.playersZennys;
    }

    public void addZennys(int zen) {
        this.playersZennys += zen;
    }

    public void removeZenny(int zen) {
        this.playersZennys -= zen;
    }

    /**
     * generates the users mana for magic commands
     */
    public void genMana() {
        int Mana = (int) (Math.random() * 20) + 10;
        this.mana = Mana;
        this.maxMana = Mana;
    }

    /**
     * @return max mana a person has
     */
    public int getMaxMana() {
        return this.maxMana;
    }

    /**
     * @return current manna
     */
    public int getMana() {
        return this.mana;
    }

    /**
     * recovers 25 of your total mana
     */
    public void recoverMana() {
        int recover = (int) (this.maxMana * .25);
        this.mana += recover;
        if (this.mana > this.maxMana) {
            this.mana = this.maxMana;
        }
    }

    /**
     * takes away mana when you use magic
     *
     * @param use
     */
    public void useMana(int use) {
        this.mana = this.mana - use;
    }

    /**
     * allows you to change verbose mode
     */
    public void changeVerbose() {
        if (this.verbose) {
            this.verbose = false;
        } else {
            this.verbose = true;
        }
    }

    /**
     * allows you to get the boolean for verbose
     *
     * @return
     */
    public boolean getVerbose() {
        return this.verbose;
    }
}
