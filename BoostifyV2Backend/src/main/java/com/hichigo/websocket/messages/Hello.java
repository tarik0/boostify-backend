package com.hichigo.websocket.messages;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Hello:
 *  The first message that will
 *  be sent to the frontend socket
 *  after connection is established.
 *
 * @author cool guy
 * @version 1.0
 */
public class Hello extends JsonMessage {
    /** Json key fields. */
    @SerializedName("command")
    public String command;
    @SerializedName("online_machine_ids")
    public List<String> onlineMachineIds;

    // TODO: Add settings so frontend can get saved settings.

    /**
     * Initializes a blank message
     * for the builder.
     */
    private Hello() {
        command = "HELLO";
        onlineMachineIds = new ArrayList<String>();
    }

    /**
     * Builder:
     *  Creates a builder for the message
     */
    public static class Builder {
        private Hello message;

        /**
         * Initialize the builder.
         */
        public Builder() {
            message = new Hello();
        }

        /**
         * Return the built message.
         */
        public Hello build() {
            return this.message;
        }

        /**
         * Set online machine id list.
         */
        public Builder setOnlineMachineIds(List<String> onlineMachineIds) {
            this.message.onlineMachineIds = onlineMachineIds;
            return this;
        }
    }
}
