package com.aizistral.trustlessauth.network;

import com.aizistral.trustlessauth.core.HandshakeData;

import net.fabricmc.api.EnvType;

public sealed abstract class AbstractHandshakeManager permits ServerHandshakeManager, ClientHandshakeManager {
	private final HandshakeData data;

	protected AbstractHandshakeManager(HandshakeData data) {
		this.data = data;
	}

	public HandshakeData getData() {
		return this.data;
	}

	public abstract void terminate();

	public abstract EnvType getEnvironment();

}
