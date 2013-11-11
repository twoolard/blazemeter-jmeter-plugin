package com.blazemeter.jmeter.testinfo;

import com.blazemeter.jmeter.utils.Utils;

/**
 * Created by IntelliJ IDEA.
 * User: Vitali
 * Date: 3/28/12
 * Time: 15:43
 */
public class TestInfo {
    private String id;
    private String name;
    private TestStatus status = null;
    private String error = null;
    private int numberOfUsers;
    private String location;
    private Overrides overrides;
    private String type;


    private TestInfo(String testName, String testId) {
        this.name = testName;
        this.id = testId;
    }

    public TestInfo() {
        this("", "");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TestStatus getStatus() {
        return status;
    }

    public void setStatus(TestStatus status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public void setNumberOfUsers(int numberOfUsers) {
        this.numberOfUsers = numberOfUsers;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Overrides getOverrides() {
        return overrides;
    }

    public void setOverrides(Overrides overrides) {
        this.overrides = overrides;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {

        String ret = String.format("%s - %s", id, name);
        if (error != null)
            ret += "error: " + error;
        return ret;
    }

    public boolean equals(TestInfo testInfo) {
        boolean equals;

        equals = (this == null & testInfo == null);
        if (!equals | (this == null & testInfo == null)) {
            return equals;
        }
        return testInfo.name.equals(this.name) &&
                testInfo.id.equals(this.id)
                && testInfo.status.equals(this.status);
    }

    public boolean isEmpty() {
        return (id == null) && (name == null);
    }

    public boolean isValid() {
        return Utils.isInteger(id) && error == null;
    }

    public int getNumberOfUsers() {
        return numberOfUsers;
    }

    public String getLocation() {
        return location;
    }
}
