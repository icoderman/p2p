package com.icoderman.p2p.domain;

public enum CommandType {
    CLOSE, SEARCH, UPDATE, DOWNLOAD, NONE;

    public static CommandType getCommandByValue(String command) {
        switch (command) {
            case "close":
                return CLOSE;
            case "search":
                return SEARCH;
            case "update":
                return UPDATE;
            case "download":
                return DOWNLOAD;
            default:
                return NONE;
        }
    }
}
