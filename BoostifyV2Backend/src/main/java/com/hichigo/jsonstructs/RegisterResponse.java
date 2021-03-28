package com.hichigo.jsonstructs;

import com.google.gson.annotations.SerializedName;

public class RegisterResponse {
    public int status;
    public String country;
    @SerializedName("dmca-radio")
    public boolean dmcaRadio;
    @SerializedName("shuffle-restricted")
    public boolean shuffleRestricted;
    public String username;
    public boolean can_accept_licenses_in_one_step;
    public boolean requires_marketing_opt_in;
    public boolean requires_marketing_opt_in_text;
    public int minimum_age;
    public String country_group;
    public boolean specific_licenses;
    public boolean pretick_eula;
    public boolean show_collect_personal_info;
    public boolean use_all_genders;
    public int date_endianness;
    public boolean is_country_launched;
}
