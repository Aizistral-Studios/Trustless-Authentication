package com.aizistral.trustlessauth.mixins;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.aizistral.trustlessauth.core.AccessKeyPairManager;

import net.minecraft.client.multiplayer.ProfileKeyPairManager;
import net.minecraft.util.Signer;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.minecraft.world.entity.player.ProfilePublicKey.Data;

@Mixin(ProfileKeyPairManager.class)
public class MixinProfileKeyPairManager implements AccessKeyPairManager {
	@Shadow @Final
	private CompletableFuture<Optional<ProfilePublicKey>> publicKey;
	@Shadow @Final
	private CompletableFuture<Optional<Signer>> signer;

	@Override
	public Signer getSigner() {
		return this.signer.join().orElse(null);
	}

	@Override
	public Optional<ProfilePublicKey> getProfilePublicKey() {
		return this.publicKey.join();
	}

	@Override
	public Optional<Data> getProfilePublicKeyData() {
		return this.getProfilePublicKey().map(ProfilePublicKey::data);
	}

}
