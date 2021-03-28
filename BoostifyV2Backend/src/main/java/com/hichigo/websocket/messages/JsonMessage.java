package com.hichigo.websocket.messages;

import com.google.gson.Gson;

/**
 * JsonMessage:
 *  Class to convert structures to JSON strings.
 *
 * @author cool guy
 * @version 1.0
 */
public class JsonMessage {
    private static Gson gson = new Gson();
    /**
     * Return serialized JSON string
     * @return
     */
    @Override
    public String toString() {
        return gson.toJson(this);
    }
}
