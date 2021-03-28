package com.hichigo.websocket.messages;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AddToQueue:
 *  Add Spotify URI to the player queue.
 *  Backend will send "ADDED_TO_QUEUE" after this.
 *
 * @author cool guy
 * @version 1.0
 */
public class AddToQueue extends JsonMessage {
    /** Json key fields. */
    @SerializedName("command")
    public String command;
    @SerializedName("spotify_uri")
    public String spotifyUri;
    @SerializedName("machine_id")
    public String machineId;
}
