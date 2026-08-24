package jpize.tests.mcose.launcher;

import javax.swing.*;
import java.awt.*;

public class Main{

    private static final int width = 720;
    private static final int height = 480;
    private static final String title = "MCOSE Launcher";

    public static void main(String[] args){
        // runJPize();
        // runCli();
        runJFrame();
    }

    private static void runCli(){
        new McOseLauncherCli();
    }

    private static void runJFrame(){
        final McOseLauncherSwing jFrame = new McOseLauncherSwing();

        final Toolkit toolkit = Toolkit.getDefaultToolkit();
        final Dimension screenSize = toolkit.getScreenSize();
        jFrame.setLocation(screenSize.width / 2 - width / 2, screenSize.height / 2 - height / 2);

        jFrame.setUndecorated(false);
        jFrame.setSize(width, height);
        jFrame.setTitle(title);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setVisible(true);
    }

}
