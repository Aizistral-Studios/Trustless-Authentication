package com.aizistral.trustlessauth.mixins;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.UUID;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.aizistral.trustlessauth.TrustlessAuthentication;
import com.aizistral.trustlessauth.core.AuthenticationHelper;
import com.aizistral.trustlessauth.core.HandshakeData;
import com.aizistral.trustlessauth.network.ClientHandshakeManager;
import com.aizistral.trustlessauth.network.ServerHandshakeManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.HttpAuthenticationService;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.minecraft.HttpMinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;

@Mixin(value = YggdrasilMinecraftSessionService.class, remap = false)
public abstract class MixinMinecraftSessionService extends HttpMinecraftSessionService {
	@Shadow @Final
	private Gson gson;

	protected MixinMinecraftSessionService() {
		super(null);
		throw new IllegalStateException("Can't touch this");
	}

	@Inject(method = "hasJoinedServer", at = @At("HEAD"), cancellable = true)
	private void onHasJoinedServer(GameProfile user, String serverId, InetAddress address, CallbackInfoReturnable<GameProfile> info) throws AuthenticationException {
		ServerHandshakeManager.verifyAndTerminate(user).ifPresent(info::setReturnValue);
	}

	@Inject(method = "joinServer", at = @At("HEAD"), cancellable = true)
	private void onJoinServer(GameProfile profile, String authenticationToken, String serverId, CallbackInfo info) {
		if (ClientHandshakeManager.getCurrent() != null) {
			TrustlessAuthentication.LOGGER.info("Authenticating with Trustless Authentication...");
			info.cancel();
		} else {
			TrustlessAuthentication.LOGGER.warn("Server does not support Trustless Authentication, will use vanilla method.");
		}
	}

}
