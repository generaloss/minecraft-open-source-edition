package jpize.tests.mcose.launcher;

import jpize.tests.mcose.launcher.swing.JHintTextField;
import jpize.tests.mcose.launcher.swing.JPanelWithBackground;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class McOseLauncherSwing extends JFrame{

    private final McOseClient client;

    public McOseLauncherSwing(){
        this.client = new McOseClient();

        final JPanelWithBackground layout = new JPanelWithBackground("/background.png");
        layout.setLayout(new BorderLayout());
        layout.setDoubleBuffered(true);
        super.add(layout);

        final JPanel panel = new JPanel();
        panel.setSize(800, 600);
        layout.add(panel, BorderLayout.CENTER);

        final JHintTextField username = new JHintTextField("username");
        username.setColumns(10);
        panel.add(username);

        final JButton launch = new JButton("Launch");
        launch.addActionListener((ActionEvent e) -> {
            super.hide();
            client.username = username.getText();
            client.launch();
            super.show();
        });
        panel.add(launch);
    }

}
