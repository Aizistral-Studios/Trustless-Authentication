package com.aizistral.trustlessauth.core;

import java.util.Optional;

import net.minecraft.util.Signer;
import net.minecraft.world.entity.player.ProfilePublicKey;

public interface AccessKeyPairManager {

	public Signer getSigner();

	public Optional<ProfilePublicKey> getProfilePublicKey();

	public Optional<ProfilePublicKey.Data> getProfilePublicKeyData();

}
