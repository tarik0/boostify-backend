package com.hichigo.websocket.messages;

import com.google.gson.annotations.SerializedName;

/**
 * AddedToQueue:
 *  This will be sent after "ADD_TO_QUEUE" command.
 *
 * @author cool guy
 * @version 1.0
 */
public class AddedToQueue extends JsonMessage {
    /** Json key fields. */
    @SerializedName("command")
    public String command;
    @SerializedName("spotify_uri")
    public String spotifyUri;
    @SerializedName("machine_id")
    public String machineId;
    @SerializedName("is_ok")
    public boolean isOk;
    @SerializedName("error")
    public String error;

    /**
     * Initializes a blank message
     * for the builder.
     */
    private AddedToQueue() {
        command = "ADDED_TO_QUEUE";
        isOk = true;
    }

    /**
     * Builder:
     *  Creates a builder for the message
     */
    public static class Builder {
        private final AddedToQueue message;

        /**
         * Initialize the builder.
         */
        public Builder() {
            message = new AddedToQueue();
        }

        /**
         * Return the built message.
         */
        public AddedToQueue build() {
            return this.message;
        }

        /**
         * Set error.
         */
        public AddedToQueue.Builder setError(String error) {
            this.message.error = error;
            this.message.isOk = false;
            return this;
        }

        /**
         * Set Spotify URI
         */
        public AddedToQueue.Builder setSpotifyUri(String uri) {
            this.message.spotifyUri = uri;
            return this;
        }

        /**
         * Set machine id
         */
        public AddedToQueue.Builder setMachineId(String id) {
            this.message.machineId = id;
            return this;
        }
    }
}
