package com.aizistral.trustlessauth.core;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

import com.aizistral.trustlessauth.TrustlessAuthentication;
import com.mojang.authlib.GameProfile;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.ProfilePublicKey;

public class HandshakeData {
	private static final SecureRandom THEY_SEE_ME_ROLLIN = new SecureRandom();
	public static final int HELLO_CONSTANT = 7361744;

	private final long nonce;
	private final long timestamp;

	private HandshakeData() {
		this.nonce = THEY_SEE_ME_ROLLIN.nextLong();
		this.timestamp = Instant.now().getEpochSecond();
	}

	private HandshakeData(FriendlyByteBuf buf) {
		this.nonce = buf.readLong();
		this.timestamp = buf.readLong();
	}

	public void write(FriendlyByteBuf buf) {
		buf.writeInt(HELLO_CONSTANT);
		buf.writeLong(this.nonce);
		buf.writeLong(this.timestamp);
	}

	@Environment(EnvType.CLIENT)
	public byte[] sign() {
		return Minecraft.getInstance().getProfileKeyPairManager().signer().sign(this.toBytes());
	}

	public boolean verifyAgainst(ProfilePublicKey key, byte[] signature) {
		return key.createSignatureValidator().validate(this.toBytes(), signature);
	}

	public byte[] toBytes() {
		byte[] bs = new byte[16];

		ByteBuffer byteBuffer = ByteBuffer.wrap(bs).order(ByteOrder.BIG_ENDIAN);
		byteBuffer.putLong(this.nonce);
		byteBuffer.putLong(this.timestamp);

		return bs;
	}

	public static Optional<HandshakeData> read(FriendlyByteBuf buf) {
		if (buf.readableBytes() >= 4) {
			int hello = buf.readInt();

			if (hello == HELLO_CONSTANT)
				return Optional.of(new HandshakeData(buf));
		}

		return Optional.empty();
	}

	public static HandshakeData create() {
		return new HandshakeData();
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.nonce, this.timestamp);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		HandshakeData other = (HandshakeData) obj;
		return this.nonce == other.nonce && this.timestamp == other.timestamp;
	}

}
