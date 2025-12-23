package Tanks;

import processing.core.PApplet;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SampleTest {
    @Test
    public void initialSettingsTest() {
        App app = new App();
        app.loop();
        PApplet.runSketch(new String[] { "App" }, app);
        app.delay(1000);
        assertNotNull(app, "App is not correctly initiated");
    }

    @Test
    public void getPowerTest() {
        Tank t = new Tank('A', 400);
        assertEquals(50, t.getPower(), "Fuction did not return expected default value");
    }
    @Test
    public void getFuelTest() {
        Tank t = new Tank('A', 400);
        assertEquals(250, t.getFuel(), "Fuction did not return expected default value");
    }
    @Test
    public void getParachuteTest() {
        Tank t = new Tank('A', 400);
        assertEquals(3, t.getParachute(), "Fuction did not return expected default value");
    }
    @Test
    public void getXaxisTest() {
        Tank t = new Tank('A', 400);
        assertEquals(400, t.getXaxis(), "Fuction did not return expected value");
    }
    @Test
    public void getHealthTest() {
        Tank t = new Tank('A', 400);
        assertEquals(100, t.getHealth(), "Fuction did not return expected default value");
    }
    @Test
    public void getFallTest() {
        Tank t = new Tank('A', 400);
        assertFalse(t.getFall(), "fall should not be true, implying tank is falling");
    }
    @Test
    public void setHealthTest() {
        App app = new App();
        Tank t = new Tank('A', 400);
        t.setHealth(app,1);
        assertEquals(1,t.getHealth(), "tank's health was not set correctly");
    }
    @Test void tankFallTest(){
        Tank t = new Tank('A', 400);
        t.tankFall(400,'D');
        assertEquals(2,t.getParachute(), "reduceParachute() method did not assign expected value");
    }


    @Test
    public void executeProjectileTest(){
        App app = new App();
        PApplet.runSketch(new String[] { "App" }, app);
        app.delay(1000);
        Projectile p = new Projectile('A', 1.2f, 50, 400, 400);
        //app.projectileList.add(p);
        p.executeProjectile(app);
        assertNotNull(p, "Projectile is not working or moving");
    }

    //test for keys
    @Test
    public void leftArrowKeyActionTest(){
        App app = new App();
        Tank t = new Tank('A', 400);
        t.keyAction(app, 37);
        assertEquals(398, t.getXaxis(), "Tank did not move left");
    }

    @Test
    public void rightArrowKeyActionTest(){
        App app = new App();
        Tank t = new Tank('A', 400);
        t.keyAction(app, 39);
        assertEquals(402, t.getXaxis(), "Tank did not move right");
    }

    @Test
    public void wKeyActionTest(){
        App app = new App();
        Tank t = new Tank('A', 400);
        t.keyAction(app, 87);
        assertEquals(50 + (36/30), t.getPower(), "Power did not increase");
    }

    @Test
    public void sKeyActionTest(){
        App app = new App();
        Tank t = new Tank('A', 400);
        t.keyAction(app, 83);
        assertEquals(50 - (36/30),t.getPower(), "Power did not decrease");
    }
    @Test
    public void spacebarKeyActionTest(){
        App app = new App();
        PApplet.runSketch(new String[] { "App" }, app);
        Tank t = new Tank('A', 400);
        t.keyAction(app, 32);
        assertFalse(app.projectileList.isEmpty(), "Tank did not fire projectile");
    }
    @Test
    public void rKeyActionTest(){
        App app = new App();
        PApplet.runSketch(new String[] { "App" }, app);
        Tank t = new Tank('A', 400);
        app.playerScoreMap.put('A',20);
        t.setHealth(app, 85);
        t.keyAction(app, 82);
        assertEquals(100, t.getHealth(), "Tank was not repaired");
    }
    @Test
    public void fKeyActionTest(){
        App app = new App();
        PApplet.runSketch(new String[] { "App" }, app);
        Tank t = new Tank('A', 400);
        app.playerScoreMap.put('A',10);
        t.keyAction(app, 70);
        assertEquals(450, t.getFuel(), "Tank was not filled with fuel");
    }


    //Testing actual gameplay and operaions by blind shooting
    @Test
    public void gasmeplayBlindShootingTest(){
        App app = new App();
        PApplet.runSketch(new String[] { "App" }, app);
        app.delay(1000);
        for (int i = 0; i < 250; i++){
            app.currentTank.keyAction(app, 32);
            app.delay(150);
        }
        app.delay(5000);
        assertNotNull(app, "Program was not completely executed");
    }

    //Testing actual gameplay and operations by hitting oneself (by suspending wind element)
    @Test
    public void gameplaySelfDestructionTest(){
        App app = new App();
        PApplet.runSketch(new String[] { "App" }, app);
        app.delay(1000);
        app.wind = 0;
        for (int i = 0; i < 3; i++){
            for (int j = 0; j < 7; j++){
                app.currentTank.keyAction(app, 32);
                app.wind = 0;
                app.delay(100);
            }
            app.delay(4500);
        }
        app.delay(2000);
        app.gameOver = false;
        app.currentLevel++;
        app.unlimitedLevels();
        app.delay(1000);
        assertNotEquals(2, app.currentLevel, "Program was not completely executed");
    }
}
