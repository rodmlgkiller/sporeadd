package com.sporeadds.sporeaddsmod.capabilities;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Function;

/**
 * Player data that used to be a Forge capability, now stored as a NeoForge data attachment.
 * Keeps the old "capability.get(entity) -> LazyOptional" access style so call sites barely change.
 * All of these were only ever attached to players, so other entities always yield an empty optional.
 */
public final class Capability<T> {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, "sporeadd");

    public interface TagSerializer<T> {
        CompoundTag save(T data, HolderLookup.Provider provider);

        void load(T data, CompoundTag tag, HolderLookup.Provider provider);
    }

    private final DeferredHolder<AttachmentType<?>, AttachmentType<T>> type;

    private Capability(DeferredHolder<AttachmentType<?>, AttachmentType<T>> type) {
        this.type = type;
    }

    public static <T> Capability<T> of(String id, Function<IAttachmentHolder, T> factory, TagSerializer<T> serializer) {
        return of(id, factory, serializer, true);
    }

    /** copyOnDeath=false for data that must not survive respawn (it is still copied when returning from the End). */
    public static <T> Capability<T> of(String id, Function<IAttachmentHolder, T> factory, TagSerializer<T> serializer, boolean copyOnDeath) {
        DeferredHolder<AttachmentType<?>, AttachmentType<T>> holder = ATTACHMENTS.register(id, () -> {
            var builder = AttachmentType.builder(factory)
                        .serialize(new IAttachmentSerializer<CompoundTag, T>() {
                            @Override
                            public T read(IAttachmentHolder h, CompoundTag tag, HolderLookup.Provider provider) {
                                T data = factory.apply(h);
                                serializer.load(data, tag, provider);
                                return data;
                            }

                            @Override
                            public CompoundTag write(T data, HolderLookup.Provider provider) {
                                return serializer.save(data, provider);
                            }
                        });
            if (copyOnDeath) {
                builder.copyOnDeath();
            }
            return builder.build();
        });
        return new Capability<>(holder);
    }

    /** For data classes with plain saveNBTData/loadNBTData methods that need no registry access. */
    public static <T> Capability<T> ofSimple(String id, Function<IAttachmentHolder, T> factory,
                                             java.util.function.BiConsumer<T, CompoundTag> save,
                                             java.util.function.BiConsumer<T, CompoundTag> load) {
        return of(id, factory, new TagSerializer<T>() {
            @Override
            public CompoundTag save(T data, HolderLookup.Provider provider) {
                CompoundTag tag = new CompoundTag();
                save.accept(data, tag);
                return tag;
            }

            @Override
            public void load(T data, CompoundTag tag, HolderLookup.Provider provider) {
                load.accept(data, tag);
            }
        });
    }

    public LazyOptional<T> get(Entity entity) {
        if (!(entity instanceof Player)) {
            return LazyOptional.empty();
        }
        return LazyOptional.of(() -> entity.getData(type.get()));
    }

    /** Direct access for code that already knows the holder is a player. */
    public T getOrCreate(Player player) {
        return player.getData(type.get());
    }
}
