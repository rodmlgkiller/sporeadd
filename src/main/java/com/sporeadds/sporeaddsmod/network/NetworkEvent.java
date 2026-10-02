package com.sporeadds.sporeaddsmod.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.concurrent.CompletableFuture;

/** Compatibility shim: keeps the old "NetworkEvent.Context" handler API on top of NeoForge payload contexts. */
public final class NetworkEvent {

    private NetworkEvent() {
    }

    public static final class Context {
        private final IPayloadContext delegate;

        public Context(IPayloadContext delegate) {
            this.delegate = delegate;
        }

        public ServerPlayer getSender() {
            return delegate.flow().isServerbound() && delegate.player() instanceof ServerPlayer sp ? sp : null;
        }

        public CompletableFuture<Void> enqueueWork(Runnable work) {
            return delegate.enqueueWork(work);
        }

        public void setPacketHandled(boolean handled) {
        }

        public NetworkDirection getDirection() {
            return delegate.flow().isServerbound() ? NetworkDirection.PLAY_TO_SERVER : NetworkDirection.PLAY_TO_CLIENT;
        }

        public IPayloadContext payloadContext() {
            return delegate;
        }
    }
}
