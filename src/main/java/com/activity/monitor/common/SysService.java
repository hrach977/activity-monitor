package com.activity.monitor.common;

public class SysService {

    private final String name;
    private final int processID;
    private final State state;

    public enum State {
        RUNNING, STOPPED, OTHER
    }

    public SysService(String name, int processID, State state) {
        this.name = name;
        this.processID = processID;
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public int getProcessID() {
        return processID;
    }

    public State getState() {
        return state;
    }

    @Override
    public String toString() {
        return "SysService{" +
                "name='" + name + '\'' +
                ", processID=" + processID +
                ", state=" + state +
                '}';
    }
}
