package com.hichigo.websocket.messages;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Followed:
 *  This will be sent after the "FOLLOW" message.
 *
 * @author cool guy
 * @version 1.0
 */
public class Followed extends JsonMessage {
    /** Json key fields. */
    @SerializedName("command")
    public String command;
    @SerializedName("machine_id")
    public String machineId;
    @SerializedName("spotify_uri")
    public String spotifyUri;
    @SerializedName("is_ok")
    public boolean isOk;
    @SerializedName("error")
    public String error;

    /**
     * Initializes a blank message
     * for the builder.
     */
    private Followed() {
        command = "FOLLOWED";
        isOk = true;
    }

    /**
     * Builder:
     *  Creates a builder for the message
     */
    public static class Builder {
        private final Followed message;

        /**
         * Initialize the builder.
         */
        public Builder() {
            message = new Followed();
        }

        /**
         * Return the built message.
         */
        public Followed build() {
            return this.message;
        }

        /**
         * Set online machine id list.
         */
        public Followed.Builder setMachineId(String machineId) {
            this.message.machineId = machineId;
            return this;
        }

        /**
         * Set spotify uri.
         */
        public Followed.Builder setSpotifyUri(String uri) {
            this.message.spotifyUri = uri;
            return this;
        }

        /**
         * Set error.
         */
        public Followed.Builder setError(String error) {
            this.message.error = error;
            this.message.isOk = false;
            return this;
        }
    }
}
