package com.krld.bttest;

/**
 * Created by Andrey on 4/26/2014.
 */
public class BTMessage {
    private final String string;
    private final byte[] bytes;
    private final Types type;

    public BTMessage(String string, byte[] bytes, Types type) {
        this.string = string;
        this.bytes = bytes;
        this.type = type;
    }

    public String getLogString(boolean showBytes) {
        String logString = "";
        if (type == Types.SEND) {
            logString = "SEND: ";
        } else if (type == Types.RECEIVED) {
            logString = "RECEIVED: ";
        }

        logString += string;
        if (showBytes) {
            logString += " | ";
            String bytesString = "";
            for (byte curByte : bytes) {
                String binaryString = Integer.toBinaryString(curByte);
                bytesString += ",[" + (binaryString.length() > 8 ? "" : "00000000".substring(binaryString.length())) + binaryString + "(" + curByte + ")]";
            }
            logString += bytesString.substring(1);
        }
        return logString;
    }

    public enum Types {RECEIVED, SEND}
}
