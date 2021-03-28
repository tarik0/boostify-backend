package com.hichigo.utils;

import xyz.gianlu.librespot.core.Session;
import xyz.gianlu.librespot.player.Player;

/**
 * Machine:
 *  Class that includes session and the
 *  players inside it. It will used in machien lists.
 *
 * @author cool guy
 * @version 1.0
 */
public class Machine {
    public final Session session;
    public final Player player;

    /**
     * Initializes the class.
     * @param session Spotify session
     * @param player Player of the given session
     */
    public Machine(Session session, Player player) {
        this.session = session;
        this.player = player;
    }
}
