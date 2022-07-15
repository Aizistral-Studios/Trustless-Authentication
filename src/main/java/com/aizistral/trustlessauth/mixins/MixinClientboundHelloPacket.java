package com.aizistral.trustlessauth.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.aizistral.trustlessauth.core.HandshakeData;
import com.aizistral.trustlessauth.network.ClientHandshakeManager;
import com.aizistral.trustlessauth.network.ServerHandshakeManager;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.login.ClientboundHelloPacket;

@Mixin(ClientboundHelloPacket.class)
public class MixinClientboundHelloPacket {

	@Inject(method = "write", at = @At("RETURN"))
	public void onWrite(FriendlyByteBuf buf, CallbackInfo info) {
		ServerHandshakeManager.startHandshake().getData().write(buf);
	}

	@Inject(method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At("RETURN"))
	public void onRead(FriendlyByteBuf buf, CallbackInfo info) {
		HandshakeData.read(buf).ifPresentOrElse(ClientHandshakeManager::startHandshake,
				ClientHandshakeManager::terminateIfActive);
	}

}
