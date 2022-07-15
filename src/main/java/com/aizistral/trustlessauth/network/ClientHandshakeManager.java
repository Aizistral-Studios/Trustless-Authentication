package com.aizistral.trustlessauth.network;

import com.aizistral.trustlessauth.core.HandshakeData;

import net.fabricmc.api.EnvType;

public final class ClientHandshakeManager extends AbstractHandshakeManager {
	private static ClientHandshakeManager current;

	private ClientHandshakeManager(HandshakeData data) {
		super(data);
	}

	public static ClientHandshakeManager getCurrent() {
		return current;
	}

	public static ClientHandshakeManager startHandshake(HandshakeData data) {
		return current = new ClientHandshakeManager(data);
	}

	@Override
	public void terminate() {
		if (current == this) {
			current = null;
		}
	}

	public static boolean terminateIfActive() {
		if (current != null) {
			current.terminate();
			return true;
		} else
			return false;
	}

	@Override
	public EnvType getEnvironment() {
		return EnvType.CLIENT;
	}

}
