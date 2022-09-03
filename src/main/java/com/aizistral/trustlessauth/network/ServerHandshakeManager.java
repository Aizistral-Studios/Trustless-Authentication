package com.aizistral.trustlessauth.network;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;

import com.aizistral.trustlessauth.TrustlessAuthentication;
import com.aizistral.trustlessauth.core.HandshakeData;
import com.google.common.base.Preconditions;
import com.mojang.authlib.GameProfile;

import net.fabricmc.api.EnvType;

public final class ServerHandshakeManager extends AbstractHandshakeManager {
	private static final List<ServerHandshakeManager> MANAGERS = new ArrayList<>();
	private GameProfile verifiedProfile = null;

	private ServerHandshakeManager() {
		super(HandshakeData.create());
	}

	public static ServerHandshakeManager startHandshake() {
		if (MANAGERS.size() > 400) {
			MANAGERS.clear();
		}

		ServerHandshakeManager manager = new ServerHandshakeManager();
		MANAGERS.add(manager);
		return manager;
	}

	public static Optional<ServerHandshakeManager> find(HandshakeData data) {
		return MANAGERS.stream().filter(manager -> manager.getData().equals(data)).findAny();
	}

	public void setVerifiedProfile(@Nonnull GameProfile verifiedProfile) {
		this.verifiedProfile = Preconditions.checkNotNull(verifiedProfile);
	}

	public Optional<GameProfile> getVerifiedProfile() {
		return Optional.ofNullable(this.verifiedProfile);
	}

	public boolean isVerifiedProfile(@Nonnull GameProfile profile) {
		Preconditions.checkNotNull(profile);

		if (this.verifiedProfile != null)
			return Objects.equals(profile.getName(), this.verifiedProfile.getName()) ||
					Objects.equals(profile.getId(), this.verifiedProfile.getId());
		else
			return false;
	}

	public static Optional<GameProfile> verifyAndTerminate(@Nonnull GameProfile profile) {
		Preconditions.checkNotNull(profile);
		var optional = MANAGERS.stream().filter(manager -> manager.isVerifiedProfile(profile)).findAny();
		optional.ifPresent(ServerHandshakeManager::terminate);
		TrustlessAuthentication.LOGGER.info("Found verified profile " + profile + " :" + optional.isPresent());
		return optional.isPresent() ? optional.get().getVerifiedProfile() : Optional.empty();
	}

	@Override
	public void terminate() {
		MANAGERS.remove(this);
	}

	@Override
	public EnvType getEnvironment() {
		return EnvType.SERVER;
	}

}
