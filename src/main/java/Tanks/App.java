package Tanks;

import processing.core.PApplet;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONObject;
import processing.event.KeyEvent;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class App extends PApplet {

    public static final int CELLSIZE = 32; //8;
    public static final int CELLHEIGHT = 32;

    public static int WIDTH = 864; //CELLSIZE*BOARD_WIDTH;
    public static int HEIGHT = 640; //BOARD_HEIGHT*CELLSIZE+TOPBAR;

    public static final int FPS = 30;
    public String configPath;
    public static Random random = new Random();


    public static char currentPlayer = 'A';
    public static char[] playerArray = {'A', 'B', 'C', 'D'};

    public static Tank currentTank;
    public static ArrayList<Character> tankList = new ArrayList<>();
    public static Map<Character, Tank> tankMap = new HashMap<>();

    public static ArrayList<Projectile> projectileList = new ArrayList<>();
    public static ArrayList<int[]> blastList = new ArrayList<>();

    public static Map<Character, Integer> playerScoreMap = new HashMap<>();
    public static ArrayList<Character> sortedScoreBoardList = new ArrayList<>();


    public static Map<Integer, Integer> terrain = new HashMap<>();
    public static ArrayList<Integer> treeList = new ArrayList<>();
    public static Map<Character, int[]> playerColourMap = new HashMap<>();

    public static String[] layout = new String[3];
    public static String[] background = new String[3];
    public static int[] foregroundColour = new int[3];
    public static int[][] foregroundColourArray = new int[3][3];
    public static String[] trees = new String[3];;


    public static PImage pimgBackground;
    public static PImage pimgTree;

    public static PImage pimgFuel;
    public static PImage pimgParachute;
    public static PImage pimgParachuteTank;
    public static PImage pimgWindRight;
    public static PImage pimgWindLeft;


    public static int currentLevel = 0;
    public static int wind;
    public static int endTime = 0;
    public static boolean gameOver = false;


    public App() {
        this.configPath = "config.json";
    }

    /**
     * Initialise the setting of the window size.
     */
    @Override
    public void settings() {
        size(WIDTH, HEIGHT);
    }

    /**
     * Load all resources such as images. Initialise the elements such as the player and map elements.
     */
    @Override
    public void setup() {
        frameRate(FPS);

        JSONObject config = loadJSONObject(configPath);
        JSONArray jsonLevels = config.getJSONArray("levels");

        //Getting and saving address for unlimited levels
        for (int i = 0; i < 3; i++){
            JSONObject jsonLvl = jsonLevels.getJSONObject(i);

            layout[i] = jsonLvl.getString("layout");
            background [i] = jsonLvl.getString("background");

            //foreground colour
            String tempFGC = jsonLvl.getString("foreground-colour");
            String[] fgc = tempFGC.split(",");
            for (int j = 0; j < 3; j++) {
                int k = Integer.parseInt(fgc[j]);
                foregroundColourArray[i][j] = k;
            }

            //trees
            try {
                trees[i] = jsonLvl.getString("trees");
            } catch (Exception e) {
                trees[i] = null;
            }
        }

        //Getting player details from json file for tank colour
        JSONObject jsonplr = config.getJSONObject("player_colours");
        for (char i : playerArray) {
            String playerCl = jsonplr.getString(String.valueOf(i));
            String[] playerColo = playerCl.split(",");

            int[] playerColour = new int[3];
            int kk = 0;
            for (String ii : playerColo) {
                int j = Integer.parseInt(ii);
                playerColour[kk] = j;
                kk++;
            }
            playerColourMap.put(i, playerColour);
        }

        //Setting initial score
        for (char i : playerArray) {
            playerScoreMap.put(i, 0);
        }

        //icons
        pimgFuel = loadImage("Tanks/fuel.png");
        pimgFuel.resize(0, 26);
        pimgParachute = loadImage("Tanks/parachute.png");
        pimgParachute.resize(0, 26);
        pimgParachuteTank = loadImage("Tanks/parachute.png");
        pimgParachuteTank.resize(0, 60);
        pimgWindRight = loadImage("Tanks/wind.png");
        pimgWindRight.resize(0, 55);
        pimgWindLeft = loadImage("Tanks/wind-1.png");
        pimgWindLeft.resize(0, 55);

        definedLevels();
    }

    /**
     * Load and initialise the elements for first 3 levels.
     */
    public void definedLevels(){
        pimgBackground = loadImage("Tanks/" + background[currentLevel]);

        foregroundColour = foregroundColourArray[currentLevel];

        if (trees[currentLevel] != null) {
            pimgTree = loadImage("Tanks/" + trees[currentLevel]);
            pimgTree.resize(0, 36);
        } else{
            pimgTree = null;
        }

        //getting all coordinates from txt file
        try {
            BufferedReader br = new BufferedReader(new FileReader(layout[currentLevel]));

            String pos;
            int xaxis = 0;
            int yaxis = 0;
            treeList = new ArrayList<>();

            while ((pos = br.readLine()) != null) {
                char[] p = pos.toCharArray();
                xaxis = 0;

                for (char j : p) {
                    if (j == ' ') {
                    }
                    else if (j == 'X') {              //terrain coordinates
                        for (int i = 0; i < CELLSIZE; i++) {
                            terrain.put(xaxis * CELLSIZE + i, yaxis * CELLHEIGHT);
                        }
                    }
                    else if (j == 'T') {             //tree coordinates
                        treeList.add(xaxis * CELLSIZE);
                    }
                    else {                           //tank coordinates
                        for (char i : playerArray) {
                            if (i == j) {
                                Tank t = new Tank(j, xaxis * 32);
                                tankMap.put(j, t);
                                if (j == 'A') {
                                    currentTank = t;
                                }
                            }
                        }
                    }
                    xaxis++;
                }
                yaxis++;
            }
        } catch (Exception e) {
        }

        levelInterface();
    }

    /**
     * Resetting values and calling common operations (like moving average) after every level.
     */
    public void levelInterface() {
        //Setting up and smoothing terrain
        movingAverageTerrain();
        movingAverageTerrain();

        //player list of only char
        tankList = new ArrayList<>();
        for (char i : playerArray) {
            tankList.add(i);
        }

        //Resetting values after every level
        currentPlayer = 'A';
        projectileList = new ArrayList<>();
        wind = random.nextInt(71) - 35;
        endTime = 0;
    }

    /**
     * Calculating moving average for smoothening terrain.
     */
    public void movingAverageTerrain(){
        for (int i = 0; i < WIDTH; i++) {
            int sum = 0;
            for (int j = i; j < i + 32; j++) {
                sum += terrain.get(j);
            }
            terrain.put(i, sum / 32);
        }
    }

    /**
     * Extension: Unlimited levels
     * Loading images and positions of with random numbers
     */
    public void unlimitedLevels(){
        pimgBackground = loadImage("Tanks/" + background[random.nextInt(3)]);

        foregroundColour = foregroundColourArray[random.nextInt(3)];

        int no = random.nextInt(3);
        if (trees[no] != null) {
            pimgTree = loadImage("Tanks/" + trees[no]);
            pimgTree.resize(0, 36);
        } else{
            pimgTree = null;
        }

        //setting players
        ArrayList<Integer> tempNumList = new ArrayList<>();
        for (int i=0; i < 4; i++){
            char j = playerArray[i];
            int num = 0;
            boolean similar = true;
            while (similar) {
                num = random.nextInt(WIDTH);
                similar = false;
                for (int k : tempNumList) {
                    if (abs(num - k) < 100) { //Each tank is atleast 100 pixel away from the center of tanks
                        similar = true;
                    }
                }
            }

            tempNumList.add(num);
            Tank t = new Tank(j, num);
            tankMap.put(j, t);

            if (i == 0) {
                currentTank = t;
            }
        }

        //Setting tree
        if (pimgTree != null){
            treeList = new ArrayList<>();
            tempNumList = new ArrayList<>();
            tempNumList.add(random.nextInt(WIDTH));

            for (int i=1; i < 10; i++){
                boolean similar = true;
                int num = 0;

                while (similar) {
                    num = random.nextInt(WIDTH);
                    similar = false;
                    for (int k : tempNumList) {
                        if (abs(num - k) < 50) {   //Each tree is atleast 50 pixel away
                            similar = true;
                        }
                    }
                }

                tempNumList.add(num);
                treeList.add(num);
            }
        }

        //Setting terrain in blocks of 32 units
        int currentnum = 0;
        tempNumList = new ArrayList<>();
        int num = 0;

        //Setting initial terrain (x-axis at 0 block) which starts from 5*CELLSIZE to 13*CELLSIZE on y axis
        num = random.nextInt(8) + 6;
        tempNumList.add(num);
        currentnum = num;

        //Setting terrain for rest of the blocks
        for (int i=1; i < 28; i++){
            boolean outOfRange = true;
            while (outOfRange) {
                num = random.nextInt(7) -3;   //Changes by maximum 3*CELLSIZE
                if (currentnum + num > 3 && currentnum + num < 15){
                    outOfRange = false;
                }
            }
            tempNumList.add(currentnum + num);
        }

        //Setting terrain for each unit
        int xaxis = 0;
        for (int i: tempNumList){
            for (int j = 0; j < CELLSIZE; j++) {
                terrain.put(xaxis * CELLSIZE + j, i * CELLHEIGHT);
            }
            xaxis++;
        }

        levelInterface();
    }


    /**
     * Receive key pressed signal from the keyboard.
     */
    @Override
    public void keyPressed(KeyEvent event) {
        if (endTime == 0) {
            currentTank.keyAction(this, keyCode);
        }
        else if (keyCode == 32 && !gameOver) {             //spacebar - next level is loaded
            changeLevel();
        }
        else if (keyCode == 82 && gameOver && endTime > 21*3) {   //R - restart game
            playerScoreMap = new HashMap<>();
            for (char i : playerArray) {
                playerScoreMap.put(i, 0);
            }
            gameOver = false;
            currentLevel = 0;
            definedLevels();
        }
        else if(keyCode == 67 && gameOver && endTime > 21*3){     //C - continue for unlimited levels with c
            gameOver = false;
            currentLevel++;
            unlimitedLevels();
        }
    }


    /**
     * Draw all elements in the game by current frame.
     */
    @Override
    public void draw() {
        //background
        image(pimgBackground, 0, 0);


        //foreground
        for (int x = 0; x < WIDTH; x++) {
            noStroke();
            fill(foregroundColour[0], foregroundColour[1], foregroundColour[2]);
            rect(x, terrain.get(x), 1, 800 - terrain.get(x));
        }

        //trees
        if (pimgTree != null) {
            for (int x : treeList) {
                image(pimgTree, x - 17, terrain.get(x) - 36);
            }
        }

        //tanks
        for (int i = tankList.size() - 1; i >= 0; i--) {
            char tank = tankList.get(i);
            tankMap.get(tank).drawTank(this);
        }

        //projectiles
        for (int i = projectileList.size() - 1; i >= 0; i--) {
            projectileList.get(i).executeProjectile(this);
        }

        //tank explosion
        if (blastList.size() > 0) {
            exploding();
        }

        //HUD
        headsUpDisplay();


        if (!gameOver) {
            //scoreboard
            stroke(0, 0, 0);
            strokeWeight(4);
            noFill();
            rect(707, 60, 143, 115);
            fill(0, 0, 0);
            line(707, 85, 850, 85);
            text("Scores", 715, 78);
            int jj = 0;
            for (char i : playerArray) {
                int[] colr = playerColourMap.get(i);
                fill(colr[0], colr[1], colr[2]);
                text("Player " + i, 715, 105 + jj);
                fill(0, 0, 0);
                text(playerScoreMap.get(i), 820, 105 + jj);
                jj += 20;
            }
        } else {
            //End game Final scores
            //text above box
            char j = sortedScoreBoardList.get(0);
            int[] colorT = playerColourMap.get(j);
            fill(colorT[0], colorT[1], colorT[2]);
            textSize(22);
            text("Player "+sortedScoreBoardList.get(0)+" wins!",300, 125);

            //drawing box and line
            stroke(0, 0, 0);
            strokeWeight(4);
            fill(colorT[0], colorT[1], colorT[2], 128);
            rect(275, 150, 350, 225);
            fill(0, 0, 0);
            line(275, 195, 625, 195);
            textSize(32);

            fill(0, 0, 0);
            text("Final Scores", 300, 185);
            int k = 0;
            for (int i = 0; i < 4; i++){
                if(21*i <= endTime){
                    j = sortedScoreBoardList.get(i);
                    int[] colorS = playerColourMap.get(j);
                    fill(colorS[0], colorS[1], colorS[2]);

                    text("Player " + j, 300, 235 + k);
                    fill(0, 0, 0);
                    text(playerScoreMap.get(j), 555, 235 + k);
                    k += 40;
                }
            }
            if (endTime >= 21*3){
                textSize(22);
                fill(0,0,0);
                text("Press 'C' to continue", 315, 400);
            }
        }


        //level end
        if(tankList.size() < 2){
            endTime++;
            if (endTime >= 30 && !gameOver) {   //Waiting before going to next level - 1 sec
                changeLevel();
            }
        }
    }

    /**
     * HUD which contains tank status like fuel, parachute, healthbar, power.
     */
    public void headsUpDisplay(){
        //current player, fuel, parachute
        fill(0, 0, 0);
        textSize(16);
        text("Player " + currentPlayer + "'s turn", 20, 29);
        text(currentTank.getFuel(), 210, 25);
        text(currentTank.getParachute(), 210, 55);

        image(pimgFuel, 175, 5);
        image(pimgParachute, 175, 35);


        //healthbar and power
        noStroke();
        int h = currentTank.getHealth();
        int p = currentTank.getPower();
        text("Health:", 350, 30);
        text(h, 630, 30);
        text("Power:   " + p, 350, 60);

        int[] col = playerColourMap.get(currentPlayer);
        fill(col[0], col[1], col[2]);

        //delete this code
        if (h > 0) {
            rect(420, 10, h * 2, 26);   //health
        }

        fill(255, 255, 255);
        if (h >= 0) {
            rect(420 + (h * 2), 10, 200 - (h * 2), 26);   //zerohealth
        } else {
            rect(420 + (0), 10, 200, 26);   //zerohealth
        }
        stroke(255, 0, 0);


        stroke(0, 0, 0);
        strokeWeight(4);
        noFill();
        rect(420, 10, 200, 26);   //health border

        stroke(128, 128, 128);
        strokeWeight(4);
        noFill();
        rect(420, 10, p * 2, 26);   //power border
        stroke(255, 0, 0);
        strokeWeight(0);
        line(420 + (p * 2) + 2, 4, 420 + (p * 2) + 2, 42);   //power line (redline)

        //wind
        fill(0, 0, 0);
        text(Math.abs(wind), 823, 35);
        if (wind < 0) {
            image(pimgWindLeft, 750, 2);
        } else {
            image(pimgWindRight, 750, 2);
        }
    }

    /**
     * Displaing explosion for both tank and projectile
     */
    public void exploding(){
        for (int i = blastList.size() - 1; i >= 0; i--) {
            int[] blast = blastList.get(i);

            float aa = 30 * blast[2];
            float bb = 15 * blast[2];
            float cc = 6 * blast[2];

            float divisor = (float) (5f * blast[3]) / 30;
            aa *= divisor;
            bb *= divisor;
            cc *= divisor;

            noStroke();
            fill(255, 0, 0);
            ellipse(blast[0], blast[1], aa, aa);   //biggest outer circle
            strokeWeight(bb - cc);                 //middle circle
            fill(255, 255, 0);
            ellipse(blast[0], blast[1], cc, cc);   //smallest inner circle
            stroke(255, 165, 0);
            blast[3]++;

            if (blast[3] == 6) {
                blastList.remove(i);
            }
        }
    }

    /**
     * Switching between players.
     */
    public void nextPlayer(App app){
        for (int i = 0; i < app.tankList.size(); i++){
            char temp = app.tankList.get(i);

            if (temp == app.currentPlayer){
                char nextPlayer;

                if (i != app.tankList.size() - 1) {
                    nextPlayer = app.tankList.get(i + 1);
                }
                else{
                    nextPlayer = app.tankList.get(0);
                }
                app.currentPlayer = nextPlayer;
                app.currentTank = app.tankMap.get(nextPlayer);
                break;

            }
        }
    }

    /**
     * Switching between levels.
     */
    public void changeLevel() {
        if (currentLevel < 2) {                  //change level for levels upto 2
            currentLevel++;
            definedLevels();
        }
        else if (currentLevel > 2) {             //change level for unlimited levels
            unlimitedLevels();
        }
        else if (!gameOver) {                    //End of 3rd level
            gameOver = true;
            sortScoreBoard();
            endTime = 0;
        }
    }

    /**
     * Sorting scoreboard in decending order of their scores and storing the order in sortedScoreBoardList.
     */
    public void sortScoreBoard(){
        //Resetting values in tankList to store the players temporarily
        ArrayList<Character> tankList = new ArrayList<>();
        for (char i: playerArray){
            tankList.add(i);
        }
        //Checking for smallest value
        for (int i = 0; i < 4; i++){
            int maxScore = playerScoreMap.get(tankList.get(0));
            int maxSTankno = 0;
            for (int j = 0; j < tankList.size(); j++){
                int k = playerScoreMap.get(tankList.get(j));
                if (maxScore < k){
                    maxScore = k;
                    maxSTankno = j;
                }
            }
            char maxSTank = tankList.get(maxSTankno);
            sortedScoreBoardList.add(maxSTank);
            tankList.remove(maxSTankno);
        }
    }

    public static void main(String[] args) {
        PApplet.main("Tanks.App");
    }

}