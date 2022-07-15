package com.aizistral.trustlessauth.mixins;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.mojang.authlib.GameProfile;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;

@Mixin(ServerLoginPacketListenerImpl.class)
public interface AccessorServerLoginPacketListener {

	@Accessor @Nullable
	public GameProfile getGameProfile();

	@Accessor
	public void setGameProfile(GameProfile profile);

	@Accessor @Nullable
	public MinecraftServer getServer();

}
