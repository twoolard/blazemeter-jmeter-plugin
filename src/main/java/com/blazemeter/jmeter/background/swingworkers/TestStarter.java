package com.blazemeter.jmeter.background.swingworkers;

import com.blazemeter.jmeter.constants.Constants;
import com.blazemeter.jmeter.controllers.TestInfoController;
import com.blazemeter.jmeter.entities.TestInfo;
import com.blazemeter.jmeter.entities.TestStatus;
import com.blazemeter.jmeter.testexecutor.BmTestManager;
import com.blazemeter.jmeter.testexecutor.panels.components.CloudPanel;
import com.blazemeter.jmeter.utils.GuiUtils;

import javax.swing.*;

/**
 * Created by dzmitrykashlach on 4/1/14.
 */
public class TestStarter extends SwingWorker {
    private CloudPanel cloudPanel;

    public TestStarter(CloudPanel cloudPanel) {
        this.cloudPanel = cloudPanel;
    }

    @Override
    protected Object doInBackground() throws Exception {
        JSlider numberOfUsersSlider = cloudPanel.getNumberOfUsersSlider();
        JSpinner durationSpinner = cloudPanel.getDurationSpinner();
        JSpinner iterationsSpinner = cloudPanel.getIterationsSpinner();
        JSpinner rampupSpinner = cloudPanel.getRampupSpinner();

        GuiUtils.saveCloudTest(numberOfUsersSlider,
                durationSpinner,
                iterationsSpinner,
                rampupSpinner);
        BmTestManager bmTestManager = BmTestManager.getInstance();
        TestInfoController.stop();
        bmTestManager.runInTheCloud();
        this.firePropertyChange(Constants.BUTTON_ACTION, StateValue.STARTED,
                StateValue.DONE);
        TestInfo testInfo = bmTestManager.getTestInfo();
        if (testInfo.getError() == null & testInfo.getStatus() == TestStatus.Running) {
            String url = bmTestManager.getTestUrl();
            if (url != null)
                url = url.substring(0, url.length() - 5);
            GuiUtils.navigate(url);
        }
        return null;
    }
}