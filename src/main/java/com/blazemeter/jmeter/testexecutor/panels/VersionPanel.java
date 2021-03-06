package com.blazemeter.jmeter.testexecutor.panels;

import com.blazemeter.jmeter.api.BmUrlManager;
import com.blazemeter.jmeter.constants.Constants;
import com.blazemeter.jmeter.controllers.ServerStatusController;
import com.blazemeter.jmeter.entities.PluginUpdate;
import com.blazemeter.jmeter.testexecutor.BmTestManager;
import com.blazemeter.jmeter.testexecutor.listeners.VersionMouseListener;
import com.blazemeter.jmeter.testexecutor.notifications.IPluginUpdateNotification;
import com.blazemeter.jmeter.testexecutor.notifications.IServerStatusChangedNotification;
import com.blazemeter.jmeter.testexecutor.notificationsImpl.serverstatus.ServerStatusChangedNotificationVP;
import com.blazemeter.jmeter.utils.PluginInstaller;
import com.blazemeter.jmeter.utils.URIOpener;
import com.blazemeter.jmeter.utils.Utils;

import javax.swing.*;
import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: dzmitrykashlach
 * Date: 11/21/13
 * Time: 12:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class VersionPanel extends JPanel implements IPluginUpdateNotification {


    private static VersionPanel versionPanel;
    private static JLabel connectionStatus = new JLabel();
    private JPanel panelLink;

    public VersionPanel() {
        super();

        JLabel icon = new JLabel();

        icon.setIcon(
                new ImageIcon(
                        Utils.class.getResource("/com/blazemeter/jmeter/images/BlazemeterLogoB.png")));
        icon.setCursor(new Cursor(Cursor.HAND_CURSOR));
        icon.addMouseListener(new URIOpener(BmUrlManager.getServerUrl()));

        JLabel version = new JLabel("Version:" + Utils.getPluginVersion().toString());

        version.setFont(version.getFont().deriveFont(Font.PLAIN).deriveFont(14F));
        version.setForeground(Color.GRAY);


        this.setBackground(new Color(47, 41, 43));
        this.setForeground(Color.GRAY);
        panelLink = new JPanel(new GridBagLayout());
        panelLink.setBackground(new Color(47, 41, 43));

        JLabel reportBug = new JLabel();
        reportBug.setText("<html><u>Report a bug</u></html>");
        reportBug.setToolTipText("Click here to report a bug");
        reportBug.setForeground(Color.WHITE);
        reportBug.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel requestFeature = new JLabel();
        requestFeature.setText("<html><u>Request a feature</u></html>");
        requestFeature.setToolTipText("Click here to request a feature");
        requestFeature.setForeground(Color.WHITE);
        requestFeature.setCursor(new Cursor(Cursor.HAND_CURSOR));


        GridBagConstraints gridBagConstraints;

        gridBagConstraints = new GridBagConstraints();

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(0, 1, 0, 0);
        gridBagConstraints.gridheight = 2;
        panelLink.add(icon, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 0.95;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        panelLink.add(connectionStatus, gridBagConstraints);


        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 0.02;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = new Insets(2, 0, 0, 10);
        panelLink.add(reportBug, gridBagConstraints);
        reportBug.addMouseListener(new URIOpener(Constants.REQUEST_FEATURE_REPORT_BUG_URL));


        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 0.03;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = new Insets(2, 0, 0, 10);
        panelLink.add(requestFeature, gridBagConstraints);
        requestFeature.addMouseListener(new URIOpener(Constants.REQUEST_FEATURE_REPORT_BUG_URL));


        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = new Insets(2, 0, 0, 4);
        panelLink.add(version, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.CENTER;
        gridBagConstraints.insets = new Insets(0, 0, 0, 4);
        panelLink.add(this, gridBagConstraints);

        connectionStatus.setText("SERVER IS AVAILABLE");
        connectionStatus.setForeground(Color.GREEN);

        ServerStatusController serverStatusController = ServerStatusController.getServerStatusController();
        IServerStatusChangedNotification serverStatusChangedNotification = new ServerStatusChangedNotificationVP(connectionStatus);
        serverStatusController.serverStatusChangedNotificationListeners.add(serverStatusChangedNotification);

        BmTestManager bmTestManager = BmTestManager.getInstance();
        bmTestManager.pluginUpdateNotificationListeners.add(this);

    }

    public static VersionPanel getVersionPanel() {
        if (versionPanel == null) {
            versionPanel = new VersionPanel();
        }
        return versionPanel;
    }

    public JPanel getPanelLink() {
        return panelLink;
    }

    @Override
    public void onPluginUpdate(final PluginUpdate update) {
        if (update == null)
            return;

        versionPanel.removeAll();

        JLabel newVersion = new JLabel(String.format("New version - %s, is available", update.getVersion().toString()));
        newVersion.setForeground(Color.WHITE);
        versionPanel.add(newVersion);
        JLabel moreInfo = new JLabel();
        moreInfo.setText("<html><u>More info</u></html>");
        moreInfo.setToolTipText("Click here to see changes in new version");
        moreInfo.setForeground(Color.WHITE);
        moreInfo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        VersionMouseListener mouseListener = new VersionMouseListener(update);
        moreInfo.addMouseListener(mouseListener);
        moreInfo.setVisible(true);
        versionPanel.add(moreInfo);
        JLabel download = new JLabel("<html><u>Open plugin page</u></html>");
        download.setForeground(Color.WHITE);
        download.setCursor(new Cursor(Cursor.HAND_CURSOR));
        download.setToolTipText("Click here to open plugin page");
        PluginInstaller pluginInstaller = new PluginInstaller();
        download.addMouseListener(pluginInstaller);
        versionPanel.add(download);


    }

}
