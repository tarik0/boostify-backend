package com.hichigo.websocket;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.hichigo.jsonstructs.RegisterResponse;
import com.hichigo.utils.Machine;
import com.hichigo.websocket.messages.*;
import com.spotify.connectstate.Connect;
import okhttp3.*;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import xyz.gianlu.librespot.core.Session;
import xyz.gianlu.librespot.metadata.PlayableId;
import xyz.gianlu.librespot.player.AudioOutput;
import xyz.gianlu.librespot.player.Player;
import xyz.gianlu.librespot.player.PlayerConfiguration;
import xyz.gianlu.librespot.player.TrackOrEpisode;
import xyz.gianlu.librespot.player.codecs.AudioQuality;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * MessageParser:
 *  Parses the messages that
 *  coming from the frontend.
 *
 * @author cool guy
 * @version 1.0
 */
public class MessageParser {
    /** Global variables */
    public final static Map<String, Machine> onlineMachines = new HashMap<>();
    public final static Map<String, Object>  settings = new HashMap<>();
    public final static Map<String, String> machineCountries = new HashMap<>();
    public final static Map<String, String> proxyAuthMap = new HashMap<>();
    public final static Map<String, String> machineContexts = new HashMap<>();

    /** Properties */
    private static final Logger logger = Logger.getLogger(MessageParser.class.getName());
    private static final Random random = new Random();
    private static final String userAgent = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Spotify/1.1.42.622 Safari/537.36";
    private final Gson gson = new Gson();

    /** Initialize the message parser */
    public MessageParser() {
        setSettings();

        // Set proxy authenticator for registering
        Authenticator.setDefault(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return proxyAuthenticator(getRequestingHost(), getRequestingPort());
            }
        });
    }

    /** Set custom settings */
    private void setSettings() {
        settings.put("discord_webhook", "");
        settings.put("discord_updates_enabled", false);
        settings.put("endless_play", false);
        settings.put("stream_count", 0);
        settings.put("follow_count", 0);
        settings.put("enable_following", false);
        settings.put("max_stream_duration_ms", 5000);
        settings.put("min_stream_duration_ms", 3000);
    }

    /** Proxy authenticator */
    private PasswordAuthentication proxyAuthenticator(String host, Integer port) {
        if (proxyAuthMap.containsKey(String.format("%s:%d", host.toLowerCase(), port))) {
            // Get username and password from the map
            String tmpStr = proxyAuthMap.get(String.format("%s:%d", host.toLowerCase(), port));
            String username = tmpStr.split(":")[0];
            String password = tmpStr.split(":")[1];

            return new PasswordAuthentication(username, password.toCharArray());
        }
        return null;
    }

    /**
     * Increase follow count in the settings.
     * @param i
     */
    public static synchronized void IncreaseFollowCount(int i) {
        int follows = (int)settings.get("follow_count");
        settings.remove("follow_count");
        settings.put("follow_count", follows + i);
    }

    /**
     * Increase stream count in the settings.
     * @param i
     */
    public static synchronized void IncreaseStreamCount(int i) {
        int follows = (int)settings.get("stream_count");
        settings.remove("stream_count");
        settings.put("stream_count", follows + i);
    }

    /**
     * Parse the messages and process them.
     */
    public void parseMessage(BackendWebSocket webSocket, String message) {
        try {
            // Deserialize the Json string
            Type type = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> tmpMap = gson.fromJson(message, type);

            // Check command type
            if (!tmpMap.containsKey("command")) {
                logger.error(String.format("Unknown json message: %s", message));
                return;
            }

            // Get command type
            String command = ((String)tmpMap.get("command")).toUpperCase();
            logger.info(String.format("New %s command: %s", command, message));

            switch (command) {
                case "PONG":
                    // Silent
                    break;
                case "REGISTER":
                    onRegister(webSocket, gson.fromJson(message, Register.class));
                    break;
                case "NEW_MACHINE":
                    onNewMachine(webSocket, gson.fromJson(message, NewMachine.class));
                    break;
                case "ADD_TO_QUEUE":
                    onAddToQueue(webSocket, gson.fromJson(message, AddToQueue.class));
                    break;
                case "UPDATE_SETTING":
                    onUpdateSetting(webSocket, gson.fromJson(message, UpdateSetting.class));
                    break;
                case "FOLLOW":
                    onFollow(webSocket, gson.fromJson(message, Follow.class));
                    break;
            }

        } catch (JsonSyntaxException e) {
            // Not a valid json
            logger.error(String.format("Unknown non-json message: %s", message));
        }
    }

    /**
     * Triggers on a new follow command
     * coming form the fronend UI.
     */
    public void onFollow(BackendWebSocket webSocket, Follow message) {
        try {
            // Get machine from the list
            Machine machine = MessageParser.onlineMachines.get(message.machineId);
            if (machine == null) throw new Exception(String.format("Machine not found for the id: %s", message.machineId));

            Response res = null;
            Request request = null;
            String token = null;
            OkHttpClient client = machine.session.client();;

            // Get follow token
            token = machine.session.tokens().getToken(
                    "playlist-modify-private",
                    "playlist-modify-public",
                    "playlist-read-collaborative",
                    "playlist-read-private",
                    "user-follow-modify",
                    "user-follow-read",
                    "user-library-modify",
                    "user-library-read",
                    "user-modify-playback-state",
                    "user-read-currently-playing",
                    "user-read-email",
                    "user-read-playback-position",
                    "user-read-playback-state",
                    "user-read-private",
                    "user-read-recently-played",
                    "user-top-read"
            ).accessToken;

            // Parse the uri type
            switch (message.spotifyUri.split(":")[1]){
                case "artist":
                {
                    // Generate request
                    request = new Request.Builder()
                            .url(String.format("https://api.spotify.com/v1/me/following?type=artist&ids=%s", message.spotifyUri.split(":")[2]))
                            .put(new FormBody.Builder().build())
                            .addHeader("Accept", "application/json")
                            .addHeader("Accept-Accept-Language", "en")
                            .addHeader("Authorization", String.format("Bearer %s", token))
                            .addHeader("Connection", "close")
                            .addHeader("User-Agent", userAgent)
                            .build();

                    // Send the request
                    Call call = client.newCall(request);
                    res = call.execute();
                    break;
                }
                case "track":
                {
                    // Generate request
                    request = new Request.Builder()
                            .url(String.format("https://api.spotify.com/v1/me/tracks?ids=%s", message.spotifyUri.split(":")[2]))
                            .put(new FormBody.Builder().build())
                            .addHeader("Accept", "application/json")
                            .addHeader("Accept-Accept-Language", "en")
                            .addHeader("Authorization", String.format("Bearer %s", token))
                            .addHeader("Connection", "close")
                            .addHeader("User-Agent", userAgent)
                            .build();

                    // Send the request
                    Call call = client.newCall(request);
                    res = call.execute();
                    break;
                }
                case "playlist":
                {
                    // Generate request
                    request = new Request.Builder()
                            .url(String.format("https://api.spotify.com/v1/playlists/%s/followers", message.spotifyUri.split(":")[2]))
                            .put(new FormBody.Builder().build())
                            .addHeader("Accept", "application/json")
                            .addHeader("Accept-Accept-Language", "en")
                            .addHeader("Authorization", String.format("Bearer %s", token))
                            .addHeader("Connection", "close")
                            .addHeader("User-Agent", userAgent)
                            .build();

                    // Send the request
                    Call call = client.newCall(request);
                    res = call.execute();
                    break;
                }
                case "album":
                {
                    // Generate request
                    request = new Request.Builder()
                            .url(String.format("https://api.spotify.com/v1/me/albums?ids=%s", message.spotifyUri.split(":")[2]))
                            .put(new FormBody.Builder().build())
                            .addHeader("Accept", "application/json")
                            .addHeader("Accept-Accept-Language", "en")
                            .addHeader("Authorization", String.format("Bearer %s", token))
                            .addHeader("Connection", "close")
                            .addHeader("User-Agent", userAgent)
                            .build();

                    // Send the request
                    Call call = client.newCall(request);
                    res = call.execute();
                    break;
                }
                default:
                {
                    throw new Exception(String.format("Unknown uri type to follow: %s", message.spotifyUri.split(":")[1]));
                }
            }

            // Check response status
            if (!res.isSuccessful()) {
                throw new Exception(String.format("Status code is not ok (code: %d)", res.code()));
            }

            // Generate followed response
            Followed followed = new Followed.Builder()
                    .setMachineId(message.machineId)
                    .setSpotifyUri(message.spotifyUri)
                    .build();

            // Increase following stats
            MessageParser.IncreaseFollowCount(1);

            // Send the error response to the frontend
            webSocket.broadcast(followed.toString());
        } catch (Exception ex) {
            // Generate followed response
            Followed followed = new Followed.Builder()
                    .setError(ex.getMessage())
                    .setMachineId(message.machineId)
                    .setSpotifyUri(message.spotifyUri)
                    .build();

            // Send the error response to the frontend
            webSocket.broadcast(followed.toString());
        }
    }

    /**
     * Triggers on a new register command
     * coming from the frontend UI.
     */
    public void onRegister(BackendWebSocket webSocket, Register message) {
        try {
            // Generate new http client
            OkHttpClient client;

            // Use proxy
            if (message.useProxy) {
                Proxy.Type proxyType;
                // Get proxy type
                switch (message.proxyType.toUpperCase()) {
                    case "HTTP":
                    case "HTTPS":
                        proxyType = Proxy.Type.HTTP;
                        break;
                    case "SOCKS4":
                    case "SOCKS5":
                        proxyType = Proxy.Type.SOCKS;
                        break;
                    default:
                        throw new Exception(String.format("Unsupported proxy type: %s", message.proxyType.toUpperCase()));
                }

                // Set the proxy authentication
                if (message.useProxyAuth) {
                    // Add username and password to the authentication list
                    String key = String.format("%s:%s", message.proxyHost.toLowerCase(), message.proxyPort);
                    String val = String.format("%s:%s", message.proxyUsername, message.proxyPassword);
                    proxyAuthMap.put(key, val);
                }

                // Set the proxy
                Proxy proxy = new Proxy(
                        proxyType,
                        new InetSocketAddress(message.proxyHost, Integer.parseInt(message.proxyPort)));

                // Set proxy to the client
                client = new OkHttpClient.Builder()
                        .proxy(proxy)
                        .build();
            } else {
                // Generate client without proxy
                client = new OkHttpClient.Builder()
                        .build();
            }

            // Get random spotify client address
            Request request = new Request.Builder()
                    .url("http://apresolve.spotify.com/?type=spclient")
                    .build();

            // Send the request
            Call call = client.newCall(request);
            Response res = call.execute();

            // Check the status code
            if (!res.isSuccessful()) {
                if (message.useProxy) {
                    throw new Exception(String.format("Status code is not ok (code: %d) with proxy: %s",
                            res.code(),
                            String.format("%s:%s", message.proxyHost.toLowerCase(), message.proxyPort)));
                }
                throw new Exception(String.format("Status code is not ok (code: %d)", res.code()));
            }

            // Deserialize the response body
            Type type = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> tmpMap = gson.fromJson(res.body().string(), type);

            // Validate the json response
            if (!tmpMap.containsKey("spclient")) {
                if (message.useProxy) {
                    throw new Exception(String.format("SPClient response is not valid (res: %s) with proxy: %s",
                            res.body().string(),
                            String.format("%s:%s", message.proxyHost.toLowerCase(), message.proxyPort)));
                }
                throw new Exception(String.format("SPClient response is not valid (res: %s)", res.body().string()));
            }

            // Select random client from the list
            List<String> clientList = (List<String>)tmpMap.get("spclient");
            String spClient = clientList.get(random.nextInt(clientList.size()));

            // Generate form body
            RequestBody formBody = new FormBody.Builder()
                    .addEncoded("displayname", message.displayName)
                    .addEncoded("key", "142b583129b2df829de3656f9eb484e6")
                    .addEncoded("password_repeat", message.password)
                    .addEncoded("birth_day", String.valueOf(Integer.parseInt(message.birthDay)))
                    .addEncoded("email", message.email)
                    .addEncoded("iagree", "true")
                    .addEncoded("platfrom", "desktop")
                    .addEncoded("referrer", "msft_1")
                    .addEncoded("birth_month", String.valueOf(Integer.parseInt(message.birthMonth)))
                    .addEncoded("creation_point", "client_mobile")
                    .addEncoded("platform", "Android-ARM")
                    .addEncoded("birth_year", String.valueOf(Integer.parseInt(message.birthYear)))
                    .addEncoded("password", message.password)
                    .addEncoded("gender", message.gender)
                    .build();

            // Generate register request
            request = new Request.Builder()
                    .url(String.format("https://%s/signup/public/v1/account/", spClient))
                    .addHeader("Connection", "close")
                    .addHeader("Host", spClient.replace("https://", ""))
                    .addHeader("Origin", "https://login.app.spotify.com")
                    .addHeader("Spotify-App-Version", "1.1.42.622.gbd112320")
                    .addHeader("App-Platform", "Android")
                    .addHeader("User-Agent", userAgent)
                    .addHeader("Accept", "*/*")
                    .addHeader("Sec-Fetch-Site", "same-site")
                    .addHeader("Sec-Fetch-Mode", "cors")
                    .addHeader("Sec-Fetch-Dest", "empty")
                    .addHeader("Accept-Language", "en")
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .post(formBody)
                    .build();

            // Send the request
            call = client.newCall(request);
            res = call.execute();

            // Check status code
            if (!res.isSuccessful()) {
                if (message.useProxy) {
                    throw new Exception(String.format("Status code is not ok (code: %d) with proxy: %s",
                            res.code(),
                            String.format("%s:%s", message.proxyHost.toLowerCase(), message.proxyPort)));
                }
                throw new Exception(String.format("Status code is not ok (code: %d)", res.code()));
            }

            // Read the response
            String resText = res.body().string();
            RegisterResponse registerRes = gson.fromJson(resText, RegisterResponse.class);

            // Validate the response
            if (registerRes.status != 1 || registerRes.username.isEmpty()){
                if (message.useProxy) {
                    throw new Exception(String.format("Register response is not valid (res: %s) with proxy: %s",
                            resText,
                            String.format("%s:%s", message.proxyHost.toLowerCase(), message.proxyPort)));
                }
                throw new Exception(String.format("Register response is not valid (res: %s)", res.body().string()));
            }

            // Generate register response
            Registered registeredMessage = new Registered.Builder()
                    .setEmail(message.email)
                    .setPassword(message.password)
                    .setUsername(registerRes.username)
                    .setCountry(registerRes.country)
                    .build();

            // Sent the response to the frontend
            webSocket.broadcast(registeredMessage.toString());
        } catch (Exception ex){
            // Generate register response
            Registered registeredMessage = new Registered.Builder()
                    .setError(ex.getMessage())
                    .setEmail(message.email)
                    .build();

            // Sent the error response to the frontend
            webSocket.broadcast(registeredMessage.toString());
        }
    }

    /**
     * Triggers on a new machine command
     * coming from the frontend UI.
     */
    public synchronized void onNewMachine(BackendWebSocket webSocket, NewMachine message) {
        try {
            // Generate new configuration builder
            Session.Configuration.Builder confBuilder = new Session.Configuration.Builder()
                    .setCacheEnabled(false)
                    .setStoreCredentials(false);

            // Set proxy if there is one
            if (message.useProxy) {
                confBuilder.setProxyEnabled(true)
                        .setProxyAddress(message.proxyHost)
                        .setProxyPort(Integer.parseInt(message.proxyPort));

                // Set proxy types
                if (message.proxyType.toUpperCase().startsWith("HTTPS")) {
                    confBuilder.setProxyType(Proxy.Type.HTTP);
                } else if (message.proxyType.toUpperCase().startsWith("SOCK")) {
                    confBuilder.setProxyType(Proxy.Type.SOCKS);
                } else {
                    throw new Exception(String.format("Unsupported proxy type: %s", message.proxyType.toUpperCase()));
                }

                // Set authentication if there is one
                if (message.useProxyAuth) {
                    confBuilder.setProxyAuth(true)
                            .setProxyUsername(message.proxyUsername)
                            .setProxyPassword(message.proxyPassword);
                }
            }

            // Build the config
            Session.Configuration conf = confBuilder.build();

            // Generate new session builder
            Session.Builder device = new Session.Builder(conf)
                    .setPreferredLocale("en")
                    .setDeviceId(message.machineId);

            // Configure the session profile
            switch (message.machineType) {
                case "MOBILE":
                    device.setDeviceType(Connect.DeviceType.SMARTPHONE).setDeviceName("Android");
                    break;
                case "WINDOWS":
                    device.setDeviceType(Connect.DeviceType.COMPUTER).setDeviceName("Windows 10");
                    break;
                case "LINUX":
                    device.setDeviceType(Connect.DeviceType.COMPUTER).setDeviceName("GNU Linux Ubuntu 18.04");
                    break;
                default:
                    throw new Exception(String.format("Unsupported machine type: %s", message.machineType));
            }

            // Generate the session
            device.userPass(message.username, message.password);
            Session session = device.create();

            // Set session events
            session.addCloseListener(() -> MachineEvents.onMachineClose(session.deviceId(), session, webSocket));

            // Generate the player config
            PlayerConfiguration playerConfig = new PlayerConfiguration.Builder()
                    //.setInitialVolume(0)
                    .setAutoplayEnabled(false)
                    .setInitialVolume(65536)
                    .setPreferredQuality(AudioQuality.NORMAL)
                    .setPreloadEnabled(false)
                    .setOutput(AudioOutput.MIXER)
                    .setVolumeSteps(5)
                    .build();

            // Generate the player
            Player player = new Player(playerConfig, session);

            // Set player events
            player.addEventsListener(new Player.EventsListener() {
                @Override
                public void onContextChanged(@NotNull String s) {
                    MachineEvents.onContextChanged(session.deviceId(), session, player, s, webSocket);
                }

                @Override
                public void onTrackChanged(@NotNull PlayableId playableId, @Nullable TrackOrEpisode trackOrEpisode) {
                    // Silent
                }

                @Override
                public void onPlaybackPaused(long l) {
                    // Silent
                    int t = 1;
                }

                @Override
                public void onPlaybackResumed(long l) {
                    // Silent
                }

                @Override
                public void onTrackSeeked(long l) {
                    // Silent
                }

                @Override
                public void onMetadataAvailable(@NotNull TrackOrEpisode trackOrEpisode) {
                    // Silent
                }

                @Override
                public void onPlaybackHaltStateChanged(boolean b, long l) {
                    // Silent
                    int t = 1;
                }

                @Override
                public void onInactiveSession(boolean b) {
                    // Silent
                    int t = 1;
                }

                @Override
                public void onVolumeChanged(@Range(from = 0L, to = 1L) float v) {
                    // Silent
                }

                @Override
                public void onPanicState() {
                    try {
                        session.close();
                    } catch (IOException e) {
                        // Silent
                    }
                }
            });

            // Wait until country code is set
            long startTime = System.currentTimeMillis();
            while(session.countryCode() == null || session.countryCode().isEmpty()) {
                // Check timeout
                long timePassed = System.currentTimeMillis() - startTime;
                if (timePassed >= 10 * 1000) {
                    // Close the session
                    try {
                        session.close();
                    } catch (IOException e) {
                        // Silent
                    }
                    throw new Exception("Timeout reached while waiting country code!");
                }

                // Sleep the thread
                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    // Silent
                }
            }

            // Append machine to the online machines list
            onlineMachines.put(message.machineId, new Machine(session, player));
            machineCountries.put(message.machineId, session.countryCode());
            machineContexts.put(message.machineId, "");

            // Wait until player initializes
            player.waitReady().get();

            // Generate response
            MachineOpened openedMessage = new MachineOpened.Builder()
                    .setMachineId(message.machineId)
                    .setCountry(session.countryCode())
                    .build();

            // Send the response to the frontend
            webSocket.broadcast(openedMessage.toString());
        } catch (Exception ex){
            // Generate machine opened response
            MachineOpened openedMessage = new MachineOpened.Builder()
                    .setError(ex.getMessage())
                    .setMachineId(message.machineId)
                    .build();

            // Send the error response to the frontend
            webSocket.broadcast(openedMessage.toString());
        }
    }

    /**
     * Triggers on a add to queue command
     * coming from the frontend UI.
     */
    public void onAddToQueue(BackendWebSocket webSocket, AddToQueue message) {
        try {
            // Get machine from the list
            Machine machine = MessageParser.onlineMachines.get(message.machineId);
            if (machine == null) throw new Exception(String.format("Machine not found for the id: %s", message.machineId));

            // Add uri to queue
            try {
                machine.player.currentPlayable();
                machine.player.addToQueue(message.spotifyUri);
            } catch (Exception s) {
                machine.player.load(message.spotifyUri, true);
            }

            // Generate added to queue response
            AddedToQueue addedMessage = new AddedToQueue.Builder()
                    .setMachineId(message.machineId)
                    .setSpotifyUri(message.spotifyUri)
                    .build();

            // Send the error response to the frontend
            webSocket.broadcast(addedMessage.toString());
        } catch (Exception ex) {
            // Generate added to queue response
            AddedToQueue addedMessage = new AddedToQueue.Builder()
                    .setError(ex.getMessage())
                    .setMachineId(message.machineId)
                    .setSpotifyUri(message.spotifyUri)
                    .build();

            // Send the error response to the frontend
            webSocket.broadcast(addedMessage.toString());
        }
    }

    /**
     * Triggers on a update setting
     * coming from the frontend UI.
     */
    public synchronized void onUpdateSetting(BackendWebSocket webSocket, UpdateSetting message) {
        try {
            // Check if setting exists
            if (!MessageParser.settings.containsKey(message.settingName)) {
                throw new Exception(String.format("Setting not found: %s", message.settingName));
            }
            
            // Update the setting
            Object tmpVal = MessageParser.settings.get(message.settingName);

            if (tmpVal instanceof Integer) {
                MessageParser.settings.remove(message.settingName);
                MessageParser.settings.put(message.settingName, Integer.valueOf(message.settingValue));
            } else if (tmpVal instanceof Boolean) {
                MessageParser.settings.remove(message.settingName);
                MessageParser.settings.put(message.settingName, Boolean.valueOf(message.settingValue));
            } else if (tmpVal instanceof String) {
                MessageParser.settings.remove(message.settingName);
                MessageParser.settings.put(message.settingName, message.settingValue);
            } else {
                throw new Error("Unsupported setting type.");
            }

            // Generate added to queue response
            SettingUpdated settingUpdated = new SettingUpdated.Builder()
                    .setRequestId(message.requestId)
                    .build();

            // Send the error response to the frontend
            webSocket.broadcast(settingUpdated.toString());
        } catch (Exception ex) {
            // Generate added to queue response
            SettingUpdated settingUpdated = new SettingUpdated.Builder()
                    .setError(ex.getMessage())
                    .setRequestId(message.requestId)
                    .build();

            // Send the error response to the frontend
            webSocket.broadcast(settingUpdated.toString());
        }
    }
}
