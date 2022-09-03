# Trustless Authentication

**Current Version:** 1.19.2-v1.0.0

**Minecraft Version:** 1.19.2

This is a proof-of-concept mod which allows the server to authenticate join requests from clients without direct communication with Mojang's authentication servers on either side. It needs to be installed on both client and server to function correctly, but clients can still use default authentication if mod is not present on server.

## Technical basis

Historically, server-client authentication relied on Mojang's authentication services to provide confirmation of legitimacy for every player that tries to join given server. Without their participation there was no way to tell that client actually has access to the account it tries to login under. Although it was not impossible to set up a third-party service that would function as a replacement for Mojang's authentication services, the only way for such service to verify legitimacy of accounts would be to receive access token from the client, and directly check its validity against Mojang's authentication services. The problem of such implementation is that player would have to have complete trust into this external service, as access token is very sensitive data which can provide complete access to Minecraft account in-game.

Since 1.19, Minecraft have implemented cryptographically signed chat, the primary purpose of which was to serve as ground for further implementation of Player Chat Reporting. As part of this system Mojang started issuing private and public keys for every Minecraft account, where private key is used by client to generate message sigatures, and public - by server and other clients to verify them. Mojang's services also provide their own signature for every public key issued by them, which allows server and other clients to verify legitimacy of public key received from client, and it's association with specific account client is logged in in from.

#### Crucial things to understand about private and public keys are:
- Public key is derived from private key;
- Knowing public key does not allow you to know private key;
- Private key can be used to generate unique signature against arbitrary data;
- Public key can be used to verify that signature was generated against specific data using private key that it was derived from.

With those things in mind, we can employ this key system in a way that allows the server to obtain complete proof that the client has access to the account it claims to be logged in from, without requiring that client to expose any sensitive data. First, let's take a look at how client-server authentication is handled by default:

[IMAGE]

Notice that this isn't a perfectly accurate representation of login process. It omits certain steps and details in favor of focusing on what matters for general understanding.

The profile data that gets sent by client in the step 1 is submitted by server to authentication services in step 6, as they need to know which account needs login confirmation. They return some additional data about the account in step 7. Although this is not very relevant, in a system that eliminates authentication services from participation this data will need to be supplied by the client itself.

Such elimination can be achieved in a few relatively simple steps:

1. During initial exchange of information, let the server generate and send to the client additional set of data, which we will call "Handshake Data". It is important that this data is:

    - Issued by server;
    - Unique for every login attempt, ever.

In our case, Handshake Data will include timestamp (exact absolute time on server) and nonce (completely random, long number). Thought this is not the limit of what can be included, such combination should perfectly fulfill the minimal requirement of uniqueness.

2. Once the client received Handshake Data, it will use private key associated with its account to generate a signature against that data. That private key is typically only used to sign chat messages, but nothing stands in the way of signing any arbitrary data with it, which is the opportunity we make use of here.

3. After the signature is generated, client will send to the server following data as part of next handshake packet:

    - Signature itself;
    - Public key associated with its account, as well as Mojang-issued signature for that public key;
    - Unique user ID of the account.
    
4. Upon receving abovementioned data, server will first verify the validity of Mojang-issued signature for client's public key. For that it will use Mojang's public key, which it can obtain from default distribution of authentication library (it is stored as `yggdrasil_session_pubkey.der` file in the root of `authlib` jar). Signature is generated against user's public key + unique user ID, and now that server received both from client - it can ensure that aforementioned public key is indeed part of keypair issued by Mojang, and is associated with the account unique ID of which client have sent.

5. Next - server will verify the validity of signature user generated against login-specific Handshake Data, using public key that was itself verified in previous step. This is the final and most important step, since if verification passes - we reach conclusion that the client must have access to their account's private key, and, by extension, to account itself. Both server and client will then complete the rest of login process, and player will end up logged in and able to play on the server.

This alternative login sequence is represented by a following flowchart:

[IMAGE]

Notice that now Mojang's authentication services have no direct involvement with login process.

## Advantages

There are clear benefits to exluding Mojang's services from login process:

- Mojang cannot collect data about what servers you log into and when;

- Due to one less external service involved, login process proceeds faster;

- Mojang cannot impose any restrictions on login process (for instance, global bans become non-enforceable).

...all the while you maintain certainty that all players joining a server have legitimate accounts.

## Critical assumptions

Current implementation is based on an amount of assumptions, which, while true in general case, may not always be.

### 1. The client will always supply username that is associated with UUID of their account.

For now Trustless Authentication makes no effort to verify usernames, which means players can change their displayed name to anything they want, at any time. This will not allow them to login as different players, since UUID is used to actually determine who is who, but nonetheless we ideally want to send request to Mojang API in order to confirm usernames; as evidenced by my [research on Minecraft bans](https://gist.github.com/Aizistral/39d570738fd1b0866245b23744fcda98), this is entirely doable for banned accounts.

### 2. Owner of the account always has access to their private and public key.

This holds true so far, but cryptographically signed chat was designed in a way that leaves some space for the case where user has no keys and cannot sign their messages. It was [verified](https://gist.github.com/Aizistral/39d570738fd1b0866245b23744fcda98#7-address-httpsapiminecraftservicescomplayercertificates) that banned users can fetch their keypair, but things might change in the future.

### 3. It is safe to inject necessary data at the tail of vanilla login packets.

There is no indication yet that doing so breaks things, but it is not the most compatible way if we assume multiple mods want to do that.

## Fallback mechanisms

Current implementation has no fallback mechanisms. If key-based authentication stops being possible for some reason - there is no other alternative than using default authentication scheme. Such mechanisms are possible, however, at least in theory.

### Skin-based authentication

Since there is a [known API endpoint](https://wiki.vg/Mojang_API#Change_Skin) for uploading new skins, we can make use of it in the login process. It would look as following:

1. Server generates a unique 64x64 `.png` file that will be associated with this login attempt, and sends it to the client;
2. Client uploads that `.png` as skin;
3. Client informs the server that upload is complete;
4. Server uses [another API endpoint](https://wiki.vg/Mojang_API#UUID_to_Profile_and_Skin.2FCape) to fetch user's skin, and confirm client indeed uploaded unique file provided by the server;
5. Server informs the client that it can join;
6. Client uploads old skin back, which it presumably cached before executin step 2;
7. Client joins.

This would be harder to counteract on Mojang's behalf than key-based authentication, so it can serve as a reliable fallback mechanism if key-based authentication is ever taken down.

## Conclusion

While this mod indeed serves as a crude proof of trustless authentication being possible, it is not an ideal solution. Intercompatibility between various mods and plugins that want to implement it would require an agreed-upon protocol of protocols that can be used during login process, and it is yet to be established. Fallback mechanisms are also desired, since Mojang can adjust behavior of their services at any time.

However, it does an important job of proving the point - reliable authentication without direct involvement of Mojang's services is possible, and will very likely remain possible in the future, even if particular mechanisms for achieving it will have to be adjusted. I hope that this will allow third-party servers to strenghten their independence from Mojang and unreasonable, overreaching restrictions on their behalf, putting fate of Minecraft multiplayer back into the hands of community, where it rightfully belongs.