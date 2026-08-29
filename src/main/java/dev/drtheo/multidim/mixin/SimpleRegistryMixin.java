package dev.drtheo.multidim.mixin;

import dev.drtheo.multidim.api.MutableRegistry;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryInfo;

@Mixin(SimpleRegistry.class)
public abstract class SimpleRegistryMixin<T> implements MutableRegistry<T> {

    @Shadow @Final private Map<T, RegistryEntry.Reference<T>> valueToEntry;

    @Shadow @Final private Reference2IntMap<T> entryToRawId;

    @Shadow @Final private ObjectList<RegistryEntry.Reference<T>> rawIdToEntry;

    @Shadow @Final private Map<RegistryKey<T>, RegistryEntry.Reference<T>> keyToEntry;

    @Shadow @Final private Map<T, RegistryEntryInfo> keyToEntryInfo;

    @Shadow private boolean frozen;

    @Shadow public abstract RegistryKey<? extends Registry<T>> getKey();

    @Shadow public abstract RegistryEntry.Reference<T> add(RegistryKey<T> key, T value, RegistryEntryInfo info);

    @Shadow public abstract boolean contains(RegistryKey<T> key);

    @Override
    public boolean multidim$remove(T entry) {
        RegistryEntry.Reference<T> registryEntry = this.valueToEntry.get(entry);
        if (registryEntry == null)
            return false;

        int rawId = this.entryToRawId.removeInt(entry);
        if (rawId == -1)
            return false;

        try {
            this.rawIdToEntry.set(rawId, null);

            this.keyToEntry.remove(registryEntry.registryKey());
            this.keyToEntryInfo.remove(entry);
            this.valueToEntry.remove(entry);

            return true;
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean multidim$remove(Identifier key) {
        RegistryKey<T> registryKey = RegistryKey.of(this.getKey(), key);
        RegistryEntry.Reference<T> entry = this.keyToEntry.get(registryKey);

        return entry != null && entry.hasKeyAndValue() && this.multidim$remove(entry.value());
    }

    @Override
    public void multidim$freeze() {
        this.frozen = true;
    }

    @Override
    public void multidim$unfreeze() {
        this.frozen = false;
    }

    @Override
    public boolean multidim$isFrozen() {
        return this.frozen;
    }

    @Override
    public boolean multidim$contains(RegistryKey<T> key) {
        return this.contains(key);
    }

    @Override
    public RegistryEntry.Reference<T> multidim$add(RegistryKey<T> key, T value, RegistryEntryInfo info) {
        return this.add(key, value, info);
    }
}