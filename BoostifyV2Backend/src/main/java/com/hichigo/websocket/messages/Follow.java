package com.hichigo.websocket.messages;

import com.google.gson.annotations.SerializedName;

public class Follow extends JsonMessage{
    /** Json key fields. */
    @SerializedName("command")
    public String command;
    @SerializedName("machine_id")
    public String machineId;
    @SerializedName("spotify_uri")
    public String spotifyUri;
}
