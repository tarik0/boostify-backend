package com.hichigo.websocket.messages;

import com.google.gson.annotations.SerializedName;

/**
 * Register:
 *  Registering command for the frontend.
 *  After that command backend will register to Spotify
 *  and it will broadcast an "REGISTERED" packet to all web sockets.
 *
 * @author cool guy
 * @version 1.0
 */
public class Register extends JsonMessage{
    /** Json key fields. */
    @SerializedName("command")
    public String command;
    @SerializedName("email")
    public String email;
    @SerializedName("password")
    public String password;
    @SerializedName("display_name")
    public String displayName;
    @SerializedName("gender")
    public String gender;
    @SerializedName("birth_day")
    public String birthDay;
    @SerializedName("birth_month")
    public String birthMonth;
    @SerializedName("birth_year")
    public String birthYear;
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
