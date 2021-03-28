package com.hichigo.websocket.messages;

import com.google.gson.annotations.SerializedName;

import java.util.*;

/**
 * Ping:
 *  The ping request that will
 *  update the machine status every n time.
 *
 * @author cool guy
 * @version 1.0
 */
public class Ping extends JsonMessage {
    /** Json key fields. */
    @SerializedName("command")
    public String command;
    @SerializedName("settings")
    public Map<String, Object> settings;
    @SerializedName("online_machine_ids")
    public List<String> onlineMachineIds;
    @SerializedName("machine_countries")
    public Map<String, String> machineCountries;
    @SerializedName("machine_contexts")
    public Map<String, String> machineContexts;

    /**
     * Initializes a blank message
     * for the builder.
     */
    private Ping() {
        command = "PING";
        onlineMachineIds = new ArrayList<String>();
        settings = new HashMap<String, Object>();
        machineCountries = new HashMap<String, String>();
    }

    /**
     * Builder:
     *  Creates a builder for the message
     */
    public static class Builder {
        private final Ping message;

        /**
         * Initialize the builder.
         */
        public Builder() {
            message = new Ping();
        }

        /**
         * Return the built message.
         */
        public Ping build() {
            return this.message;
        }

        /**
         * Set online machine id list.
         */
        public Builder setOnlineMachineIds(List<String> onlineMachineIds) {
            this.message.onlineMachineIds = onlineMachineIds;
            return this;
        }

        /**
         * Set settings property.
         */
        public Builder setSettings(Map<String, Object> settings) {
            this.message.settings = settings;
            return this;
        }

        /**
         * Set machine contexts.
         */
        public Builder setMachineContexts(Map<String, String> contexts) {
            this.message.machineContexts = contexts;
            return this;
        }

        /**
         * Set machine countries property.
         */
        public Builder setMachineCountries(Map<String, String> machineCountries) {
            this.message.machineCountries = machineCountries;
            return this;
        }
    }
}
