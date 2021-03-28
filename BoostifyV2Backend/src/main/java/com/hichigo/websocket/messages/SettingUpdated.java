package com.hichigo.websocket.messages;

import com.google.gson.annotations.SerializedName;

public class SettingUpdated extends JsonMessage {
    @SerializedName("command")
    public String command;
    @SerializedName("req_id")
    public String requestId;
    @SerializedName("is_ok")
    public boolean isOk;
    @SerializedName("error")
    public String error;

    /**
     * Initializes a blank message
     * for the builder.
     */
    private SettingUpdated() {
        command = "SETTING_UPDATED";
        isOk = true;
    }

    /**
     * Builder:
     *  Creates a builder for the message
     */
    public static class Builder {
        private final SettingUpdated message;

        /**
         * Initialize the builder.
         */
        public Builder() {
            message = new SettingUpdated();
        }

        /**
         * Return the built message.
         */
        public SettingUpdated build() {
            return this.message;
        }

        /**
         * Set username.
         */
        public SettingUpdated.Builder setRequestId(String id) {
            this.message.requestId = id;
            return this;
        }

        /**
         * Set error.
         */
        public SettingUpdated.Builder setError(String error) {
            this.message.error = error;
            this.message.isOk = false;
            return this;
        }
    }
}
