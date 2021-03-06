package com.blazemeter.jmeter.utils;

import com.blazemeter.jmeter.api.BlazemeterApi;
import com.blazemeter.jmeter.constants.Constants;
import com.blazemeter.jmeter.entities.PluginVersion;
import com.blazemeter.jmeter.entities.TestInfo;
import com.blazemeter.jmeter.entities.Users;
import com.blazemeter.jmeter.testexecutor.BmTestManager;
import com.blazemeter.jmeter.testexecutor.RemoteTestRunner;
import com.blazemeter.jmeter.testexecutor.RemoteTestRunnerGui;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.JMeter;
import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.action.ActionNames;
import org.apache.jmeter.gui.action.Load;
import org.apache.jmeter.gui.action.Save;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Vitali
 * Date: 4/2/12
 * Time: 14:05
 */
public class Utils {


    private Utils() {
    }


    public static boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;

    }

    public static String getHostIP() {
        String hostIP = "";
        try {
            hostIP = InetAddress.getLocalHost().getHostAddress();

        } catch (UnknownHostException uhe) {
            BmLog.error("Failed to get host IP: ", uhe);
        }
        return hostIP;
    }

    public static boolean isJMeterServer() {
        return Thread.currentThread().getThreadGroup().getName().equals("RMI Runtime");
    }

    public static String getLocationId(Users users, String locationTitle) {
        JSONArray locations = users.getLocations();
        if (locations.length() > 0) {
            String locationId;
            try {
                for (int i = 0; i < locations.length(); ++i) {
                    JSONObject location = locations.getJSONObject(i);
                    if (location.get("title").equals(locationTitle)) {
                        locationId = (String) location.get("id");
                        return locationId;
                    }
                }
            } catch (JSONException je) {
                BmLog.error("Error during parsing locations JSONArray: " + je.getMessage());
            }

        }
        return "";
    }

    public static String getLocationTitle(Users users, String locationId) {
        JSONArray locations = users.getLocations();
        if (locations.length() > 0) {
            String locationTitle;
            try {
                for (int i = 0; i < locations.length(); ++i) {
                    JSONObject location = locations.getJSONObject(i);
                    if (location.get("id").equals(locationId)) {
                        locationTitle = (String) location.get("title");
                        return locationTitle;
                    }
                }
            } catch (JSONException je) {
                BmLog.error("Error during parsing locations JSONArray: " + je.getMessage());
            }
        }
        return "";
    }


    /*
      This method perform verification whether or not test plan contains
      ThreadGroups. If test-plan does not contain any,
      then "true" is returned, otherwise - "false";

     */
    public static boolean isTestPlanEmpty() {
        boolean isTestPlanEmpty = true;
        @SuppressWarnings("deprecation")
        JMeterTreeModel jMeterTreeModel = new JMeterTreeModel(new Object());// Create non-GUI version to avoid headless problems

        if (JMeter.isNonGUI()) {
            try {
                FileServer fileServer = FileServer.getFileServer();
                String scriptName = fileServer.getBaseDir() + "/" + fileServer.getScriptName();
                FileInputStream reader = new FileInputStream(scriptName);
                HashTree tree = SaveService.loadTree(reader);
                JMeterTreeNode root = (JMeterTreeNode) jMeterTreeModel.getRoot();
                jMeterTreeModel.addSubTree(tree, root);

            } catch (FileNotFoundException fnfe) {
                BmLog.error("Script was not found: " + fnfe);
            } catch (Exception e) {
                BmLog.error("TestScript was not loaded: " + e);
            }
        } else {
            jMeterTreeModel = GuiPackage.getInstance().getTreeModel();
        }

        List<JMeterTreeNode> jMeterTreeNodes = jMeterTreeModel.getNodesOfType(AbstractThreadGroup.class);
        isTestPlanEmpty = jMeterTreeNodes.size() == 0 ? true : false;
        return isTestPlanEmpty;
    }

    public static String getFileContents(String fn) {
        StringBuilder contents = new StringBuilder();
        File aFile = new File(fn);
        try {
            BufferedReader input = new BufferedReader(new FileReader(aFile));
            try {
                String line;
                String newline = System.getProperty("line.separator");
                while ((line = input.readLine()) != null) {
                    contents.append(line);
                    contents.append(newline);
                }
            } finally {
                input.close();
            }
        } catch (IOException ignored) {
        }
        return StringUtils.strip(contents.toString());
    }

    public static void checkChangesInTestPlan() {
        GuiPackage guiPackage = GuiPackage.getInstance();
        if (guiPackage.isDirty()) {
            int chosenOption = JOptionPane.showConfirmDialog(GuiPackage.getInstance().getMainFrame(),
                    "Do you want to save changes in current test-plan?",
                    JMeterUtils.getResString("save?"),
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (chosenOption == JOptionPane.YES_OPTION) {
                Save save = new Save();
                try {
                    save.doAction(new ActionEvent(guiPackage, ActionEvent.ACTION_PERFORMED, "save"));
                    GuiPackage.showInfoMessage("All changes are saved to " + guiPackage.getTestPlanFile(), "File is saved");
                } catch (IllegalUserActionException iuae) {
                    BmLog.error("Can not save file," + iuae);
                }
            }
        }
    }

    public static void checkJMeterVersion() {

        String[] currentVersion = StringUtils.split(Utils.getJmeterVersion(), ".");
        String[] baseVersion = StringUtils.split("2.5", ".");
        if (currentVersion[0].equals(baseVersion[0])) {
            if (Integer.valueOf(currentVersion[1]) < Integer.valueOf(baseVersion[1])) {
                if (JMeter.isNonGUI()) {
                    BmLog.error("Blazemeter Listener won't work with " + Utils.getJmeterVersion() + " version of JMeter. Please, update Jmeter to 2.5 or later.");
                } else {
                    JMeterUtils.reportErrorToUser("Blazemeter Listener won't work with " + Utils.getJmeterVersion() + " version of JMeter. Please, update Jmeter to 2.5 or later.",
                            "Invalid JMeter version");
                }
            }
        }
    }


    public static void downloadJMX() {
        BmTestManager bmTestManager = BmTestManager.getInstance();
        BlazemeterApi blazemeterApi = BlazemeterApi.getInstance();
        TestInfo testInfo = bmTestManager.getTestInfo();
        File file = blazemeterApi.downloadJmx(bmTestManager.getUserKey(), testInfo.getId());
        Utils.openJMX(file);
    }


    public static void saveJMX() {
        Save save = new Save();
        try {
            save.doAction(new ActionEvent(new Object(), ActionEvent.ACTION_PERFORMED, ActionNames.SAVE_ALL_AS));
        } catch (IllegalUserActionException iuae) {
            BmLog.error("Can not save file," + iuae.getMessage());
        }
    }


    public static boolean isWindows() {
        String OS = System.getProperty("os.name").toLowerCase();
        return (OS.indexOf("win") >= 0);
    }

    public static boolean isMac() {
        String OS = System.getProperty("os.name").toLowerCase();
        return (OS.indexOf("mac") >= 0);
    }


    public static boolean isUnix() {
        String OS = System.getProperty("os.name").toLowerCase();
        return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0);
    }

    public static void openJMX(File file) {

        FileInputStream reader = null;
        try {

            BmLog.debug("Loading file: " + file);
            reader = new FileInputStream(file);
            HashTree tree = SaveService.loadTree(reader);
            GuiPackage guiPackage = GuiPackage.getInstance();
            guiPackage.setTestPlanFile(file.getAbsolutePath());
            Load.insertLoadedTree(1, tree);

            JMeterTreeModel model = guiPackage.getTreeModel();
            JMeterTreeNode testPlanNode = model.getNodesOfType(TestPlan.class).get(0);
            List<JMeterTreeNode> nodes = Collections.list(testPlanNode.children());
            boolean containsRemoteTestRunner = false;
            for (JMeterTreeNode node : nodes) {
                if (node.getStaticLabel().equals("BlazeMeter")) {
                    containsRemoteTestRunner = true;
                }
                if (node.getStaticLabel().contains("Thread Group")) {
                    List<JMeterTreeNode> subNodes = Collections.list(node.children());
                    for (JMeterTreeNode subNode : subNodes) {
                        if (subNode.getStaticLabel().equals("BlazeMeter")) {
                            containsRemoteTestRunner = true;
                        }
                    }
                }
            }


            if (!containsRemoteTestRunner) {
                TestElement remoteTestRunner = guiPackage.createTestElement(RemoteTestRunnerGui.class, RemoteTestRunner.class);
                model.addComponent(remoteTestRunner, testPlanNode);
            }

        } catch (FileNotFoundException fnfe) {
            BmLog.error("JMX file " + file.getName() + "was not found ", fnfe);
        } catch (IllegalUserActionException iuae) {
            BmLog.error(iuae);
        } catch (Exception exc) {
            BmLog.error(exc);
        }
    }


    public static JSONObject getJSONObject(SampleEvent evt) {
        SampleResult res = evt.getResult();
        long t = res.getTime();
        long lt = res.getLatency();
        long ts = res.getTimeStamp();
        boolean s = res.isSuccessful();
        String lb = escape(res.getSampleLabel());
        String rc = escape(res.getResponseCode());
        String rm = escape(res.getResponseMessage());
        String tn = escape(res.getThreadName());
        String dt = escape(res.getDataType());
        int by = res.getBytes();
        int sc = res.getSampleCount();
        int ec = res.getErrorCount();
        int ng = res.getGroupThreads();
        int na = res.getAllThreads();
        String hn = XML.escape(JMeterUtils.getLocalHostFullName());
        long in = res.getIdleTime();

        JSONObject httpSample = new JSONObject();
        try {
            httpSample.put("t", t);
            httpSample.put("lt", lt);
            httpSample.put("lt", lt);
            httpSample.put("ts", ts);
            httpSample.put("s", s);
            httpSample.put("lb", lb);
            httpSample.put("rc", rc);
            httpSample.put("rm", rm);
            httpSample.put("tn", tn);
            httpSample.put("dt", dt);
            httpSample.put("by", by);
            httpSample.put("sc", sc);
            httpSample.put("ec", ec);
            httpSample.put("ng", ng);
            httpSample.put("na", na);
            httpSample.put("hn", hn);
            httpSample.put("in", in);

        } catch (JSONException je) {
            BmLog.error("Error while converting sample to JSONObject");
        }
        return httpSample;
    }

    private static String escape(String str) {
        int len = str.length();
        StringWriter writer = new StringWriter((int) (len * 0.1));
        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
            switch (c) {
                case '"':
                    writer.write("&quot;");
                    break;
                case '&':
                    writer.write("&amp;");
                    break;
                case '<':
                    writer.write("&lt;");
                    break;
                case '>':
                    writer.write("&gt;");
                    break;
                case '\'':
                    writer.write("&apos;");
                    break;
                default:
                    if (c > 0x7F) {
                        writer.write("&#");
                        writer.write(Integer.toString(c, 10));
                        writer.write(';');
                    } else {
                        writer.write(c);
                    }
            }
        }
        return writer.toString();
    }

    public static boolean downloadFile(String filename, String url) throws IOException {
        URL updateURL = new URL(url);
        File f = new File(filename);
        FileUtils.copyURLToFile(updateURL, f);
        return true;
    }


    public static String getProjectName() {
        if (GuiPackage.getInstance() == null)
            return null;
        String projectPath = GuiPackage.getInstance().getTestPlanFile();
        String filename = "untitled";
        if (projectPath != null) {
            filename = new File(projectPath).getName();
            if (filename.length() > 4)
                filename = filename.toLowerCase().endsWith(".jmx") ? filename.substring(0, filename.length() - 4) : filename;
        }
        return filename;
    }

    public static String getJmeterVersion() {
        String version = JMeterUtils.getJMeterVersion();

        int hyphenIndex = version.indexOf("-");
        int spaceIndex = version.indexOf(" ");

        return (hyphenIndex != -1 & spaceIndex == -1) ?
                version.substring(0, hyphenIndex) :
                version.substring(0, spaceIndex);
    }

    public static PluginVersion getPluginVersion() {
        return new PluginVersion(2, 2, "0"); //number of patch
        //should be changed before building version for publishing
    }

    public static JSONObject convertToJSON(Properties properties) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        Enumeration e = properties.propertyNames();

        while (e.hasMoreElements()) {

            String key = (String) e.nextElement();
            jsonObject.put(key, properties.getProperty(key));
        }
        return jsonObject;
    }

    public static void enableElements(Container container, boolean shouldBeEnabled) {
        Component[] components = container.getComponents();
        for (Component c : components) {
            c.setEnabled(shouldBeEnabled);
            if (c instanceof Container) {
                enableElements((Container) c, shouldBeEnabled);
            }
        }
    }

    public static String getCurrentTestId() {
        String currentTest = JMeterUtils.getPropDefault(Constants.CURRENT_TEST, "");
        String currentTestId = null;
        if (!currentTest.isEmpty()) {
            currentTestId = currentTest.substring(0, currentTest.indexOf(";"));
        } else {
            currentTestId = "";
        }

        return currentTestId;
    }
}
