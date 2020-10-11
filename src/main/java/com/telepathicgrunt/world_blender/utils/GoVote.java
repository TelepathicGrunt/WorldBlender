package com.telepathicgrunt.world_blender.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmChatLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;

public class GoVote {
    private static final String BRAND = "TelepathicGrunt";
    private static final String MARKER_PATH = ".vote2020_marker";
    private static final LocalDate ELECTION_DAY = LocalDate.of(2020, Month.NOVEMBER, 3);
    private static final String LINK = "https://vote.gov/";
    private static boolean shownThisSession = false;

    private static volatile boolean markerAlreadyExists = false;
    private static volatile String countryCode = "";

    public static void init() {

        try {
            Path path = Paths.get(MARKER_PATH);

            /* NB: This is atomic. Meaning that if the file does not exist,
             * And multiple mods run this call concurrently, only one will succeed,
             * the rest will receive FileAlreadyExistsException
             */
            Files.createFile(path);

            // Set it to hidden on windows to avoid clutter
            if (Util.getOperatingSystem() == Util.OperatingSystem.WINDOWS) {
                Files.setAttribute(path, "dos:hidden", true);
            }
        } catch (Exception ex) {
            markerAlreadyExists = true;
            return;
        }


        if (isAfterElectionDay()) {
            return;
        }

        new Thread(() -> {
            try {
                URL url = new URL("http://ip-api.com/json/");
                URLConnection conn = url.openConnection();
                conn.setConnectTimeout(4000);
                conn.setReadTimeout(4000);
                try (InputStreamReader reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)) {
                    Type typeToken = new TypeToken<Map<String, String>>() {}.getType();
                    Map<String, String> map = new Gson().fromJson(reader, typeToken);
                    countryCode = map.get("countryCode");
                }
            } catch (Exception ignored) {}
        }, "Go Vote Country Check").start();
    }

    private static boolean isAfterElectionDay() {
        return LocalDate.now().isAfter(ELECTION_DAY);
    }

    public static boolean show(MinecraftClient client, Screen parent) {
        if ((parent instanceof SelectWorldScreen || parent instanceof MultiplayerScreen) && shouldShow(client)) {
            client.openScreen(new GoVoteScreen(parent));
            shownThisSession = true;

            return true;
        }

        return false;
    }

    private static boolean shouldShow(MinecraftClient mc) {
        if (shownThisSession || isAfterElectionDay() || markerAlreadyExists) {
            return false;
        }

        return "US".equals(countryCode);
    }

    private static class GoVoteScreen extends Screen {
        private static final int TICKS_PER_GROUP = 50;
        private final Screen parent;
        private int ticksElapsed = 0;
        private final List<List<Text>> message = new ArrayList<>();

        protected GoVoteScreen(Screen parent) {
            super(new LiteralText(""));
            this.parent = parent;
            addGroup(s("Hey there, I'm " + BRAND + " and I have something to tell you."));
            addGroup(s("If you are of voting age in the United States like I am,"));
            addGroup(s("then please consider voting. I say this because voting is"),
                    s("extremely important and in fact, last presidential election"));
            addGroup(s("had a voter turnout rate of only 55.5%!!! Like holy moley!"));
            addGroup(LiteralText.EMPTY, s("So yeah, please vote. Take your time to research the candidates."));
            addGroup(s("Make sure you register to vote too!"),
                    s("(there are deadlines so register now before it's too late!)"));
            addGroup(s("Don't let anyone stand in your way as you cast your powerful vote!"));
            addGroup(LiteralText.EMPTY, s("Click anywhere to check if you are registered to vote."),
                    s("The website is an official government site, unaffiliated with " + BRAND + "."));
            addGroup(s("Press ESC to exit. (This screen will not show up again. EVER)"));
            addGroup(s("Thank you! - TelepathicGrunt, World Blender mod"));
        }

        // Each group appears at the same time
        private void addGroup(Text... lines) {
            message.add(Arrays.asList(lines));
        }

        private static LiteralText s(String txt) {
            return new LiteralText(txt);
        }

        @Override
        public void tick() {
            super.tick();
            ticksElapsed++;
        }

        @Override
        public void render(MatrixStack mstack, int mx, int my, float pticks) {
            super.render(mstack, mx, my, pticks);

            fill(mstack, 0, 0, width, (int) (height*0.16), 0xFF591818);
            fill(mstack, 0, (int) (height*0.16), width, height, 0xFF171D56);
            int middle = width / 2;
            int dist = 12;

            Text note1 = s("Note: If you can't vote in the United States,").formatted(Formatting.ITALIC);
            Text note2 = s("Please press ESC and let World Blender dev know").formatted(Formatting.ITALIC);
            Text note3 = s("if you can see this message while outside USA.").formatted(Formatting.ITALIC);
            drawCenteredText(mstack, this.textRenderer, note1, middle, 5, 0xFFFFFF);
            drawCenteredText(mstack, this.textRenderer, note2, middle, 15, 0xFFFFFF);
            drawCenteredText(mstack, this.textRenderer, note3, middle, 25, 0xFFFFFF);

            int y = 46;
            for (int groupIdx = 0; groupIdx < message.size(); groupIdx++) {
                List<Text> group = message.get(groupIdx);
                if ((ticksElapsed - 20) > groupIdx * TICKS_PER_GROUP) {
                    for (Text line : group) {
                        drawCenteredText(mstack, this.textRenderer, line, middle, y, 0xFFFFFF);
                        y += dist;
                    }
                }
            }
        }

        @Override
        public String getNarrationMessage() {
            StringBuilder builder = new StringBuilder();
            for (List<Text> group : message) {
                for (Text line : group) {
                    builder.append(line.getString());
                }
            }
            return builder.toString();
        }

        @Override
        public boolean keyPressed(int keycode, int scanCode, int modifiers) {
            if (keycode == GLFW.GLFW_KEY_ESCAPE) {
                this.client.openScreen(parent);
            }

            return super.keyPressed(keycode, scanCode, modifiers);
        }

        @Override
        public boolean mouseClicked(double x, double y, int modifiers) {
            if (ticksElapsed < 80) {
                return false;
            }

            if (modifiers == 0) {
                this.client.openScreen(new ConfirmChatLinkScreen(this::consume, LINK, true));
                return true;
            }

            return super.mouseClicked(x, y, modifiers);
        }

        private void consume(boolean doIt) {
            this.client.openScreen(this);
            if (doIt) {
                Util.getOperatingSystem().open(LINK);
            }
        }

    }
}
