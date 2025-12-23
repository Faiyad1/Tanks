package Tanks;

public class Projectile {
    private boolean explode = false;
    private char shotBy;
    private float radian;
    private float power;
    private float xSpeed;
    private float ySpeed;
    private float xaxis = 0;
    private float yaxis = 0;
    private int xcoordinate;
    private int ycoordinate;
    private int tick = 0;

    public Projectile(char shot, float rad, int pwr, int xcoord, int ycoord) {
        shotBy = shot;
        radian = rad;
        power = 2 + pwr * 0.16f;  //(540 - 60) / 100) / 30f
        xaxis = xcoord;
        yaxis = ycoord - 16;   //shot form turret height
        assigningSpeed();
    }


    public void assigningSpeed() {
        xSpeed += power * (float) Math.sin(radian);
        ySpeed -= power * (float) Math.cos(radian);
    }

    /**
     * calling method for drawing projectile
     * If explode is set to true, destroyTerrain() function is called and this instance is removed once it is over.
     */
    public void executeProjectile(App app) {
        if (!explode) {
            projectileDiaplacement(app);
        } else {
            tick++;
            if (tick > 6) {
                destroyTerrain(app);
                app.projectileList.remove(this);
            }
        }
    }

    /**
     * Calculating and drawing Projectile, with scoring and initializing explosion on immediate impact
     */
    public void projectileDiaplacement(App app) {
        float windPixel = App.wind * 0.03f / 30;  //0.03f / 30;
        float gravity = (3.6f / 30) * 2;

        //calculating acceleration
        xSpeed += windPixel;
        ySpeed += gravity;

        //final coordinates
        xaxis += xSpeed;
        yaxis += ySpeed;


        //inside map
        if (xaxis >= 0 && xaxis < 864 && yaxis >= 0) {
            //touching terrain ground
            if ((int) (yaxis + 2) >= App.terrain.get((int) xaxis)) {
                explode = true;
                xcoordinate = (int) xaxis;
                ycoordinate = App.terrain.get((int) xaxis);
                int[] blast = {xcoordinate, ycoordinate, 2, 1};
                app.blastList.add(blast);
                scores(app);
            } else {
                //drawing projectile
                app.stroke(0, 0, 255);
                app.strokeWeight(2);
                app.fill(0, 0, 0);
                app.ellipse(xaxis, yaxis, 2, 2);
            }
        }
        //out of map and point of no return
        else if (yaxis > 640) {
            app.projectileList.remove(this);
        }
    }

    /**
     * Scores on immediate projectile impact
     */
    public void scores(App app) {
        for (int i = app.tankList.size() - 1; i >= 0; i--) {
            char temp = app.tankList.get(i);
            Tank plDetails = app.tankMap.get(temp);

            int xTankCoord = plDetails.getXaxis();
            int yTankCoord = app.terrain.get(xTankCoord);

            //distance formula for calculating distance;
            float distance = (float) Math.sqrt(Math.pow(xcoordinate - xTankCoord, 2) + Math.pow(ycoordinate - yTankCoord, 2));

            if (distance <= 30.0f) {
                int damage = 60 - (int) (distance * 2);

                //updating health
                int health = plDetails.getHealth();
                if (health > damage) {
                    plDetails.setHealth(app, health - damage);
                } else {
                    plDetails.setHealth(app, 0);
                    damage = health;
                }

                //score
                if (temp != shotBy) {
                    app.playerScoreMap.put(shotBy, app.playerScoreMap.get(shotBy) + damage);
                }
            }
        }
    }

    /**
     * Destroying terrain after explosion is over
     */
    public void destroyTerrain(App app) {
        for (int i = -30; i <= 30; i++) {
            if (xcoordinate + i >= 0 && xcoordinate + i < 864) {
                int yco = app.terrain.get(xcoordinate + i);

                //distance formula for calculating y coordinates;
                int distance = (int) Math.sqrt(Math.pow(30, 2) - Math.pow(i, 2));
                boolean terrainDestroyed = false;

                //Setting yaxis of terrain
                if (yco < ycoordinate - distance) {
                    app.terrain.put(xcoordinate + i, yco + (distance * 2));
                    terrainDestroyed = true;
                } else if ((yco < ycoordinate + distance)) {
                    app.terrain.put(xcoordinate + i, ycoordinate + distance);
                    terrainDestroyed = true;
                }

                //Initiating tank to fall
                if (terrainDestroyed) {
                    for (int j = app.tankList.size() - 1; j >= 0; j--) {
                        char temp = app.tankList.get(j);
                        Tank plDetails = app.tankMap.get(temp);
                        if (plDetails.getXaxis() == xcoordinate + i) {
                            if (!plDetails.getFall()) {
                                plDetails.tankFall(yco, shotBy);
                            }
                        }
                    }
                }
            }
        }
    }
}
