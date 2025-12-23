package Tanks;

public class Tank {
    private char tank;
    private int xaxis;
    private float radian = 0.0f;
    private int power = 50;
    private int health = 100;
    private int fuel = 250;
    private int parachute = 3;

    private boolean fall = false;
    private boolean noParachute = false;
    private int yFallinital;
    private int yFallCoordinate;
    private int yFallRate;
    private char shotBy;

    public Tank(char t, int x){
        tank = t;
        xaxis = x;
    }

    public int getPower(){
        return power;
    }
    public int getFuel(){
        return fuel;
    }
    public int getParachute(){
        return parachute;
    }
    public int getXaxis(){
        return xaxis;
    }
    public int getHealth(){
        return health;
    }
    public boolean getFall(){
        return fall;
    }

    /**
     * Assigning new value to health and checking if it is destroyed or not
     */
    public void setHealth(App app, int hl){
        health = hl;
        if (health < 1){
            modifyPlayerMap(app);
        }
        if (power > health) {
            power = health;
        }
    }

    /**
     * Assigning values for executing tank falling operations
     */
    public void tankFall(int ycoord, char sB){
        if (parachute > 0){
            parachute -= 1;
            yFallRate = 2;
        } else{
            noParachute = true;
            yFallRate = 4;
            shotBy = sB;
        }
        fall = true;
        yFallinital = ycoord;
        yFallCoordinate = yFallinital;
    }


    /**
     * Drawing tank and parachute
     */
    public void drawTank(App app) {

        int[] tankColour = app.playerColourMap.get(tank);
        int yaxis;

        //normal yaxis
        if (!fall) {
            yaxis = app.terrain.get(xaxis);
        }
        //tank starts falling with parachute
        else if (fall && !noParachute){
            yFallCoordinate += yFallRate;
            yaxis = yFallCoordinate;
            app.image(app.pimgParachuteTank, xaxis-30, yaxis -68);
        }
        //tank starts falling without parachute
        else{
            yFallCoordinate += yFallRate;
            yaxis = yFallCoordinate;
        }

        //drawing tank
        app.noStroke();
        app.fill(tankColour[0], tankColour[1], tankColour[2]);

        //body (body size with height 15 width 30)
        app.rect(xaxis - 10, yaxis - 10, 20, 10);
        app.ellipse(xaxis - 10, yaxis - 5, 10, 10);
        app.ellipse(xaxis + 10, yaxis - 5, 10, 10);//12

        //turret
        app.rect(xaxis - 8, yaxis - 15, 16, 5);
        app.ellipse(xaxis - 8, yaxis - 12.5f, 5, 5);//4
        app.ellipse(xaxis + 8, yaxis - 12.5f, 5, 5);

        //design
        //line(xaxis - 6, yaxis - 11, xa + 6, yaxis - 11);
        app.stroke(0, 0, 0);
        app.strokeWeight(1);
        app.line(xaxis - 6, yaxis - 11, xaxis - 2, yaxis - 11);
        app.line(xaxis + 2, yaxis - 11, xaxis + 6, yaxis - 11);
        app.noStroke();
        //rect(xa - 6, yaxis - 11, 12, 1);


        //turret gun
        app.fill(0, 0, 0);
        app.translate(xaxis, yaxis - 10);
        app.rotate(radian);
        //system.out.println(  (t*3*(PI/180))  )//sometimes angle and sometimes radian

        app.rect(-2, -16, 4, 11);//drawing opposite of y-axis
        app.ellipse(0, -16, 4, 4);

        app.rotate(-radian);
        app.translate(-xaxis, -(yaxis - 10));

        //pointing line above tank
        if (app.currentPlayer == tank) {
            app.stroke(0, 0, 0);
            app.strokeWeight(3);
            app.line(xaxis, yaxis - 150, xaxis, yaxis - 75);
            app.line(xaxis - 15, yaxis - 100, xaxis, yaxis - 75);
            app.line(xaxis + 15, yaxis - 100, xaxis, yaxis - 75);
        }


        //tank placement operations
        //tank under bottom of map
        if (app.terrain.get(xaxis) >= app.height) {
            int[] blast = {xaxis, yaxis, 2, 1};
            app.blastList.add(blast);
            modifyPlayerMap(app);
            return;
        }

        //fall damage
        if (noParachute && yaxis >= app.terrain.get(xaxis)) {
            int damage = app.terrain.get(xaxis) - yFallinital;
            if (damage > health){
                damage = health;
            }
            health -= damage;
            if (shotBy != tank) {
                app.playerScoreMap.put(shotBy, app.playerScoreMap.get(shotBy) + damage);
            }

            if (health < 1) {
                int[] blast = {xaxis, yaxis, 1, 1};
                app.blastList.add(blast);
                modifyPlayerMap(app);
                return;
            }
        }

        //changing fall settings
        if (yaxis >= app.terrain.get(xaxis) && fall) {
            fall = false;
        }
    }

    /**
     * deleting tank from playerMap
     */
    public void modifyPlayerMap(App app){
        if (app.currentPlayer == tank){
            app.nextPlayer(app);
        }
        app.tankMap.remove(tank);
        for (int i = 0; i < app.tankList.size(); i++){
            char temp = app.tankList.get(i);
            if (temp == tank){
                app.tankList.remove(i);
                return;
            }
        }
    }

    /**
     * Assigning and verifying if the user input can be accepted.
     */
    public void keyAction(App app, int key){
        if (key == 38) {                 //Up Arrow - Tank turret moves left
            if (radian > -(3.14f/2)){
                radian -= 0.1f;
            } else{
                radian = -(3.14f/2);
            }
        }
        else if (key == 40) {          //Down Arrow - Tank turret moves right
            if (radian < 3.14f/2){
                radian += 0.1f;
            } else{
                radian = 3.14f/2;
            }
        }

        else if (key == 37) {          //Left Arrow - Tank moves left across terrain
            if (fuel > 0 && xaxis - 2 >= 0) {
                xaxis -= 2;
                fuel -= 1;
            }
        }
        else if (key == 39) {          //Right Arrow - Tank moves right across terrain
            if (fuel > 0 && xaxis - 2 < 864) {
                xaxis += 2;
                fuel -= 1;
            }
        }

        else if (key == 87) {          //W - Turret power increases
            power += 36/30;
            if (power > 100){
                power = 100;
            }
            if (power > health){
                power = health;
            }
        }
        else if (key == 83) {          //S - Turret power decreases
            power -= 36/30;
            if (power < 0){
                power = 0;
            }
        }

        else if (key == 32) {          //Spacebar - Fire a projectile (in these block of code)
            if (!fall) {
                Projectile p = new Projectile(tank, radian, power, xaxis, app.terrain.get(xaxis));
                app.projectileList.add(p);
                app.nextPlayer(app);

                app.wind += app.random.nextInt(11) - 5;
            }
        }

        else if (key == 82) {          //R - Repair kit (in these block of code)
            int score = app.playerScoreMap.get(tank);
            if (score >= 20) {
                if (health > 80) {
                    health = 100;
                } else {
                    health += 20;
                }
                app.playerScoreMap.put(tank,score - 20);
            }
        }

        else if (key == 70) {          //F - Additional fuel
            int score = app.playerScoreMap.get(tank);
            if (score >= 10) {
                fuel += 200;
                app.playerScoreMap.put(tank,score - 10);
            }
        }
    }
}