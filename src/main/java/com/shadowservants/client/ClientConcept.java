package com.shadowservants.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = "shadowservants", value = Dist.CLIENT)
public final class ClientConcept {
    private static final KeyMapping OPEN = new KeyMapping(
            "key.shadowservants.menu",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "key.categories.misc"
    );

    private ClientConcept() {}

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(OPEN);
    }

    @SubscribeEvent
    public static void clientTick(ClientTickEvent.Post event) {
        while (OPEN.consumeClick()) {
            Minecraft.getInstance().setScreen(new ServantScreen());
        }
    }

    private static void command(String value) {
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.connection.sendCommand(value);
        }
    }

    private static final class ServantScreen extends Screen {
        protected ServantScreen() {
            super(Component.literal("Shadow Servants"));
        }

        @Override
        protected void init() {
            int x = this.width / 2 - 100;
            int y = 42;
            this.addRenderableWidget(Button.builder(Component.literal("Status"), b -> command("shadowservants status"))
                    .bounds(x, y, 200, 20).build());
            this.addRenderableWidget(Button.builder(Component.literal("Invocar Zombie"), b -> command("shadowservants summon minecraft:zombie"))
                    .bounds(x, y + 26, 200, 20).build());
            this.addRenderableWidget(Button.builder(Component.literal("Invocar Esqueleto"), b -> command("shadowservants summon minecraft:skeleton"))
                    .bounds(x, y + 52, 200, 20).build());
            this.addRenderableWidget(Button.builder(Component.literal("Invocar Aranha"), b -> command("shadowservants summon minecraft:spider"))
                    .bounds(x, y + 78, 200, 20).build());
            this.addRenderableWidget(Button.builder(Component.literal("Guardar todos"), b -> command("shadowservants dismiss_all"))
                    .bounds(x, y + 104, 200, 20).build());
            this.addRenderableWidget(Button.builder(Component.literal("Fechar"), b -> onClose())
                    .bounds(x, this.height - 35, 200, 20).build());
        }

        @Override
        public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            // Deliberately do NOT call super.renderBackground: this avoids Minecraft's background blur.
            graphics.fill(0, 0, this.width, this.height, 0xE80B0912);
            graphics.fill(0, 0, this.width, 30, 0xFF21102E);
            graphics.fill(this.width / 2 - 102, 39, this.width / 2 + 102, this.height - 58, 0xB9141020);
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            renderBackground(graphics, mouseX, mouseY, partialTick);
            graphics.drawCenteredString(this.font, Component.literal("§d§lSHADOW SERVANTS"), this.width / 2, 10, 0xFFFFFFFF);
            graphics.drawCenteredString(this.font, Component.literal("§710 derrotas = 1 servo"), this.width / 2, 27, 0xFFFFFFFF);
            super.render(graphics, mouseX, mouseY, partialTick);
        }

        @Override
        public boolean isPauseScreen() {
            return false;
        }
    }
}
