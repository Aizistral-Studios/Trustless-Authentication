package com.aizistral.trustlessauth.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import com.mojang.authlib.minecraft.BanDetails;

import net.minecraft.client.Minecraft;

@Mixin(Minecraft.class)
public class MixinMinecraft {

	@Overwrite
	public BanDetails multiplayerBan() {
		return null;
	}

}
