package com.aizistral.trustlessauth.core;

import java.net.URL;
import java.util.Optional;
import java.util.UUID;

import com.aizistral.trustlessauth.TrustlessAuthentication;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.HttpAuthenticationService;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.ProfilePublicKey;

public class AuthenticationHelper {

	public static GameProfile fetchProfileDetails(GameProfile incomplete, HttpAuthenticationService service) throws AuthenticationException {
		try {
			String id = incomplete.getId() != null ? incomplete.getId().toString() : incomplete.getName();
			URL url = HttpAuthenticationService.constantURL("https://playerdb.co/api/player/minecraft/" + id);
			String response = service.performGetRequest(url);
			JsonObject json = JsonParser.parseString(response).getAsJsonObject();
			JsonObject data = json.getAsJsonObject("data");
			JsonObject player = data.getAsJsonObject("player");

			String username = player.get("username").getAsString();
			UUID uuid = UUID.fromString(player.get("id").getAsString());
			GameProfile complete = new GameProfile(uuid, username);

			TrustlessAuthentication.LOGGER.info("Fetched profile data: {}", complete);

			return complete;
		} catch (Exception ex) {
			throw new AuthenticationUnavailableException("Something gone wrong...", ex);
		}
	}

	@Environment(EnvType.CLIENT)
	public static Optional<ProfilePublicKey.Data> getProfileKeyData() {
		AccessKeyPairManager access = (AccessKeyPairManager) Minecraft.getInstance().getProfileKeyPairManager();
		return access.getProfilePublicKeyData();
	}

	@Environment(EnvType.CLIENT)
	public static UUID getUUID() {
		return Minecraft.getInstance().getUser().getProfileId();
	}

}
