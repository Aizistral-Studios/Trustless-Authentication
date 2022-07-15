package com.aizistral.trustlessauth.mixins;

import java.util.Base64;
import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.aizistral.trustlessauth.TrustlessAuthentication;
import com.aizistral.trustlessauth.core.AuthenticationHelper;
import com.aizistral.trustlessauth.core.HandshakeData;
import com.aizistral.trustlessauth.network.ClientHandshakeManager;
import com.aizistral.trustlessauth.network.ServerHandshakeManager;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.HttpMinecraftSessionService;
import com.mojang.authlib.minecraft.InsecurePublicKeyException;

import net.minecraft.client.multiplayer.ProfileKeyPairManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.login.ServerLoginPacketListener;
import net.minecraft.network.protocol.login.ServerboundKeyPacket;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.minecraft.util.CryptException;
import net.minecraft.util.SignatureValidator;
import net.minecraft.world.entity.player.ProfilePublicKey;

@Mixin(ServerboundKeyPacket.class)
public class MixinServerboundKeyPacket {
	private HandshakeData superSecretData;
	private ProfilePublicKey.Data superSecretPublicKey;
	private byte[] superSecretSignature;
	private UUID superSecretUUID;

	@Inject(method = "write", at = @At("RETURN"))
	private void onWrite(FriendlyByteBuf buf, CallbackInfo info) {
		ClientHandshakeManager manager = ClientHandshakeManager.getCurrent();

		if (manager != null) {
			manager.getData().write(buf);
			buf.writeUUID(AuthenticationHelper.getUUID());
			AuthenticationHelper.getProfileKeyData().get().write(buf);
			buf.writeByteArray(manager.getData().sign());
		}
	}

	@Inject(method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At("RETURN"))
	private void onRead(FriendlyByteBuf buf, CallbackInfo info) {
		HandshakeData.read(buf).ifPresent(data -> {
			this.superSecretData = data;
			this.superSecretUUID = buf.readUUID();
			this.superSecretPublicKey = new ProfilePublicKey.Data(buf);
			this.superSecretSignature = buf.readByteArray();
		});
	}

	@Inject(method = "handle", at = @At("HEAD"))
	private void onHandle(ServerLoginPacketListener listener, CallbackInfo info) {
		var accessor = (AccessorServerLoginPacketListener) listener;

		if (this.superSecretData != null && this.superSecretPublicKey != null && this.superSecretSignature != null && this.superSecretUUID != null) {
			try {
				ServerHandshakeManager manager = ServerHandshakeManager.find(this.superSecretData).orElseThrow(
						() -> new AuthenticationException("Received invalid handshake data from client!"));

				ProfilePublicKey key = ProfilePublicKey.createValidated(accessor.getServer().getServiceSignatureValidator(),
						this.superSecretUUID, this.superSecretPublicKey);

				if (this.superSecretData.verifyAgainst(key, this.superSecretSignature)) {
					GameProfile complete = new GameProfile(this.superSecretUUID, accessor.getGameProfile().getName());

					TrustlessAuthentication.LOGGER.info("Successfully verified signature of {}.", complete);
					TrustlessAuthentication.LOGGER.info("Signature was: {}", Base64.getEncoder().encodeToString(this.superSecretSignature));
					TrustlessAuthentication.LOGGER.info("Will authenticate with Trustless Authentication.");

					accessor.setGameProfile(complete);
					manager.setVerifiedProfile(complete);
				} else
					throw new AuthenticationException("Failed to verify handshake signature!");

			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		} else {
			TrustlessAuthentication.LOGGER.warn("Client {} does not seem to support Trustless Authentication. Won't let them in.", accessor.getGameProfile());
			((ServerLoginPacketListenerImpl)listener).disconnect(Component.literal("You need to install Trustless Authentication to join this server!"));
		}
	}


}
