package com.aizistral.trustlessauth;

import java.util.List;

import com.aizistral.trustlessauth.network.ClientHandshakeManager;
import com.google.common.collect.ImmutableList;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

@Environment(EnvType.CLIENT)
public class TrustlessAuthenticationClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		TrustlessAuthentication.LOGGER.info("Client initialization...");
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			client.execute(ClientHandshakeManager::terminateIfActive);
		});
	}

}
