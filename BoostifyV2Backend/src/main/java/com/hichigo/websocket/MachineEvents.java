package com.hichigo.websocket;

import xyz.gianlu.librespot.core.Session;
import xyz.gianlu.librespot.player.Player;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class MachineEvents {
    /**
     * Triggers when a session is closing.
     * @param deviceId
     * @param device
     * @param ws
     */
    public synchronized static void onMachineClose(String deviceId, Session device, BackendWebSocket ws) {
        // Remove machine from online machines list
        MessageParser.onlineMachines.remove(deviceId);
        MessageParser.machineCountries.remove(deviceId);
        MessageParser.machineContexts.remove(deviceId);

        /*
            Frontend will understand this when a machine is no longer in the "PING" request's
            "online_machine_ids" list. (and also the "machine_countries" list)
         */
    }

    /**
     * Triggers when a player's context is changed.
     * @param deviceId
     * @param session
     * @param player
     * @param context
     * @param ws
     */
    public synchronized static void onContextChanged(String deviceId, Session session, Player player, String context, BackendWebSocket ws) {
        // Update the context list
        if (MessageParser.machineContexts.get(deviceId) != null) MessageParser.machineContexts.remove(deviceId);
        MessageParser.machineContexts.put(deviceId, context);

        // Increase the steaming count
        MessageParser.IncreaseStreamCount(1);

        // Generate random streaming duration
        int streamingDuration = ThreadLocalRandom.current().nextInt(
                (int)MessageParser.settings.get("min_stream_duration_ms"),
                (int)MessageParser.settings.get("max_stream_duration_ms") + 1
        );

        // TODO: Find a way to check if there is a playable at nextPlayable() cuz https://github.com/librespot-org/librespot-java/discussions/300
        // Play the context for the certain amount
        CompletableFuture.delayedExecutor(streamingDuration, TimeUnit.MILLISECONDS).execute(() -> {
            if (!player.isActive()) return;
            if (player.nextPlayable() != null && !player.currentPlayable().toSpotifyUri().equals(player.nextPlayable().toSpotifyUri())) {
                // Skip to the next context
                player.next();
            } else {
                player.close();
                try {
                    session.close();
                } catch (IOException e) {
                    // Silent
                }
                // Update the context list to blank
                if (MessageParser.machineContexts.get(session.deviceId()) != null)
                    MessageParser.machineContexts.remove(session.deviceId());
            }
        });

        /*
            Frontend will understand this from the "PING" request's "machine_contexts" lists.
         */
    }

}
