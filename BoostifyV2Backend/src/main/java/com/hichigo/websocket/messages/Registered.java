package com.hichigo.websocket.messages;

import com.google.gson.annotations.SerializedName;

/**
 * Registered:
 *  After the "REGISTER" command
 *  backend will register to the Spotify and
 *  it will return this as a response.
 *
 * @author cool guy
 * @version 1.0
 */
public class Registered extends JsonMessage {
    /** Json key fields. */
    @SerializedName("command")
    public String command;
    @SerializedName("is_ok")
    public boolean isOk;
    @SerializedName("error")
    public String error;
    @SerializedName("username")
    public String username;
    @SerializedName("password")
    public String password;
    @SerializedName("email")
    public String email;
    @SerializedName("country")
    public String country;

    /**
     * Initializes a blank message
     * for the builder.
     */
    private Registered() {
        command = "REGISTERED";
        isOk = true;
    }

    /**
     * Builder:
     *  Creates a builder for the message
     */
    public static class Builder {
        private final Registered message;

        /**
         * Initialize the builder.
         */
        public Builder() {
            message = new Registered();
        }

        /**
         * Return the built message.
         */
        public Registered build() {
            return this.message;
        }

        /**
         * Set username.
         */
        public Builder setUsername(String username) {
            this.message.username = username;
            return this;
        }

        /**
         * Set country.
         */
        public Builder setCountry(String country) {
            this.message.country = country;
            return this;
        }

        /**
         * Set email.
         */
        public Builder setEmail(String email) {
            this.message.email = email;
            return this;
        }

        /**
         * Set password.
         */
        public Builder setPassword(String password) {
            this.message.password = password;
            return this;
        }

        /**
         * Set error message.
         */
        public Builder setError(String error) {
            this.message.error = error;
            this.message.isOk = false;
            return this;
        }
    }
}
