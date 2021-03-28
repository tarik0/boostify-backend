package com.hichigo.websocket.messages;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MachineOpened:
 *  This will be sent after a new machine
 *  is connected or failed. It will be sent
 *  after "NEW_MACHINE" command.
 *
 * @author cool guy
 * @version 1.0
 */
public class MachineOpened extends JsonMessage {
    /** Json key fields. */
    @SerializedName("command")
    public String command;
    @SerializedName("is_ok")
    public boolean isOk;
    @SerializedName("error")
    public String error;
    @SerializedName("machine_id")
    public String machineId;
    @SerializedName("country")
    public String country;

    /**
     * Initializes a blank message
     * for the builder.
     */
    private MachineOpened() {
        command = "MACHINE_OPENED";
        isOk = true;
    }

    /**
     * Builder:
     *  Creates a builder for the message
     */
    public static class Builder {
        private final MachineOpened message;

        /**
         * Initialize the builder.
         */
        public Builder() {
            message = new MachineOpened();
        }

        /**
         * Return the built message.
         */
        public MachineOpened build() {
            return this.message;
        }

        /**
         * Set error.
         */
        public MachineOpened.Builder setError(String error) {
            this.message.error = error;
            this.message.isOk = false;
            return this;
        }

        /**
         * Set machine id.
         */
        public MachineOpened.Builder setMachineId(String id) {
            this.message.machineId = id;
            return this;
        }

        /**
         * Set country.
         */
        public MachineOpened.Builder setCountry(String country) {
            this.message.country = country;
            return this;
        }
    }
}
