package net.runelite.api.ggbot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;

@RequiredArgsConstructor
@Slf4j
public class BotProfile {
    private final String username;

    @Getter
    private String password = "";
    private String randomDat = null;

    @Getter
    private boolean blocked = false;
    @Getter
    private boolean locked = false;
    @Getter
    private LocalDateTime lastLogin = LocalDateTime.now();
    @Getter
    private LocalDateTime lastLogout = LocalDateTime.now();
    @Getter
    private final ArrayList<LocalDateTime[]> sessions = new ArrayList<>();

    @Getter
    private int world = -1;

    public byte[] getRandomDat() {
        if(this.randomDat == null)
            return null;

        return Base64.getDecoder().decode(randomDat);
    }

    public BotProfile setRandomDat(byte[] dat) {
        this.randomDat = Base64.getEncoder().encodeToString(dat);
        return this;
    }

    public BotProfile setBlocked(boolean blocked) {
        this.blocked = blocked;
        return this;
    }
    
    public BotProfile setLocked(boolean locked) {
        this.locked = locked;
        return this;
    }

    public BotProfile setLastLogin(LocalDateTime loggedIn) {
        this.lastLogin = loggedIn;
        return this;
    }

    public BotProfile setLastLogout(LocalDateTime loggedOut) {
        this.lastLogout = loggedOut;
        return this;
    }

    public void setPassword(String password) {
        this.password = password;
        this.save();
    }

    public void startSession(int world) {
        this.world = world;
        this.setLastLogin(LocalDateTime.now()).save();
    }

    public Duration endSession() {
        LocalDateTime start = getLastLogin();
        LocalDateTime end = LocalDateTime.now();

        this.sessions.add(new LocalDateTime[] { start, end });
        this.setLastLogout(end).save();

        return Duration.between(start, end);
    }

    public BotProfile save() {
        final String escaped = username.replace('.', '_');
        File profilesDir = new File(System.getProperty("user.dir"), "bot_profiles");
        File profileFile = new File(profilesDir, escaped + ".json");

        try {
            Files.writeString(Path.of(profileFile.getPath()), GSON.toJson(this));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return this;
    }

    private static final Gson GSON;

    static {
        GSON = new GsonBuilder()
//                .registerTypeAdapter(LocalDateTime.class,
//                        (JsonDeserializer<LocalDateTime>) (json, type, jsonDeserializationContext) ->
//                                LocalDateTime.parse(
//                                        json.getAsJsonPrimitive().getAsString(),
//                                        DateTimeFormatter.ISO_LOCAL_DATE_TIME)
//                )
//                .registerTypeAdapter(LocalDateTime.class,
//                        (JsonSerializer<LocalDateTime>) (value, type, jsonDeserializationContext) ->
//                                new JsonPrimitive(value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
//                )
                .setPrettyPrinting()
                .create();
    }

    public static BotProfile get(String username) {
        final String escaped = username.replace('.', '_');
        File profilesDir = new File(System.getProperty("user.dir"), "bot_profiles");
        if(!profilesDir.exists()) {
            log.info("Directory for bot_profiles is not found, creating it...");

            try {
                Files.createDirectories(Path.of(profilesDir.getPath()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        File profileFile = new File(profilesDir, escaped + ".json");

        if(!profileFile.exists()) {
            log.info("File for user {} does not exist, creating it...", username);
            BotProfile profile = new BotProfile(username);

            try {
                Path path = Path.of(profileFile.getPath());

                Files.createFile(path);
                Files.writeString(path, GSON.toJson(profile));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try
        {
            String profileJson = Files.readString(Path.of(profileFile.getPath()));
            return GSON.fromJson(profileJson, BotProfile.class);
        }
        catch (JsonSyntaxException | IOException e)
        {
            e.printStackTrace();

            return null;
        }
    }
}
