package com.hichigo.websocket.messages;

import com.google.gson.annotations.SerializedName;

/**
 * NewMachine
 *  After this command new Spotify session
 *  will be generated and "MACHINE_OPENED" response
 *  will be sent to the web sockets.
 *
 * @author cool guy
 * @version 1.0
 */
public class NewMachine extends JsonMessage {
    /** Json key fields. */
    @SerializedName("command")
    public String command;
    @SerializedName("machine_type") // "LINUX" or "WINDOWS" or "MOBILE"
    public String machineType;
    @SerializedName("username")
    public String username;
    @SerializedName("password")
    public String password;
    @SerializedName("machine_id")
    public String machineId;
    @SerializedName("use_proxy")
    public boolean useProxy;
    @SerializedName("use_proxy_auth")
    public boolean useProxyAuth;
    @SerializedName("proxy_type")
    public String proxyType;
    @SerializedName("proxy_host")
    public String proxyHost;
    @SerializedName("proxy_port")
    public String proxyPort;
    @SerializedName("proxy_username")
    public String proxyUsername;
    @SerializedName("proxy_password")
    public String proxyPassword;
}
