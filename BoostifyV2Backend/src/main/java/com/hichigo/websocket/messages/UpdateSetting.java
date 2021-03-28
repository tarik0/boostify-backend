package com.hichigo.websocket.messages;

import com.google.gson.annotations.SerializedName;

/**
 * UpdateSetting:
 *  Updates a setting in the settings list
 *  when this packet arrives. After that server
 *  will respond with the "SETTING_UPDATED" message.
 *
 * @author cool guy
 * @version 1.0
 */
public class UpdateSetting extends JsonMessage{
    /** Json key fields. */
    @SerializedName("command")
    public String command;
    @SerializedName("setting_name")
    public String settingName;
    @SerializedName("setting_value")
    public String settingValue;
    @SerializedName("req_id")
    public String requestId;
}
