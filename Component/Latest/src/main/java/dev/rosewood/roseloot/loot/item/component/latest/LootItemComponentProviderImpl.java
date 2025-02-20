package dev.rosewood.roseloot.loot.item.component.latest;

import dev.rosewood.roseloot.loot.item.component.LootItemComponent;
import dev.rosewood.roseloot.loot.item.component.LootItemComponentProvider;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class LootItemComponentProviderImpl implements LootItemComponentProvider {

    @Override
    public Map<String, Function<ConfigurationSection, LootItemComponent>> provideLootItemComponentConstructors() {
        return Map.ofEntries(
                Map.entry("custom-name", CustomNameComponent::new),
                Map.entry("damage", DamageComponent::new),
                Map.entry("enchantments", EnchantmentsComponent::new),
                Map.entry("item-model", ItemModelComponent::new),
                Map.entry("item-name", ItemNameComponent::new),
                Map.entry("lore", LoreComponent::new),
                Map.entry("max-damage", MaxDamageComponent::new),
                Map.entry("max-stack-size", MaxStackSizeComponent::new),
                Map.entry("rarity", RarityComponent::new),
                Map.entry("unbreakable", UnbreakableComponent::new)
        );
    }

    @Override
    public Map<String, BiConsumer<ItemStack, StringBuilder>> provideLootItemComponentPropertyApplicators() {
        return Map.ofEntries(
                Map.entry("custom-name", CustomNameComponent::applyProperties),
                Map.entry("damage", DamageComponent::applyProperties),
                Map.entry("enchantments", EnchantmentsComponent::applyProperties),
                Map.entry("item-model", ItemModelComponent::applyProperties),
                Map.entry("item-name", ItemNameComponent::applyProperties),
                Map.entry("lore", LoreComponent::applyProperties),
                Map.entry("max-damage", MaxDamageComponent::applyProperties),
                Map.entry("max-stack-size", MaxStackSizeComponent::applyProperties),
                Map.entry("rarity", RarityComponent::applyProperties),
                Map.entry("unbreakable", UnbreakableComponent::applyProperties)
        );
    }

}
