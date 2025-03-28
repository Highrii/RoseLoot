package dev.rosewood.roseloot.loot.item.component.latest;

import dev.rosewood.roseloot.RoseLoot;
import dev.rosewood.roseloot.loot.context.LootContext;
import dev.rosewood.roseloot.loot.item.ItemGenerativeLootItem;
import dev.rosewood.roseloot.loot.item.LootItem;
import dev.rosewood.roseloot.loot.item.component.LootItemComponent;
import dev.rosewood.roseloot.loot.item.meta.ItemLootMeta;
import dev.rosewood.roseloot.manager.LootTableManager;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.BundleContents;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

class BundleContentsComponent implements LootItemComponent {

    private final List<ItemGenerativeLootItem> contents;

    public BundleContentsComponent(ConfigurationSection section) {
        ConfigurationSection bundleContentsSection = section.getConfigurationSection("bundle-contents");
        if (bundleContentsSection != null) {
            this.contents = new ArrayList<>();
            for (String key : bundleContentsSection.getKeys(false)) {
                ConfigurationSection contentSection = bundleContentsSection.getConfigurationSection(key);
                if (contentSection != null) {
                    LootItem lootItem = RoseLoot.getInstance().getManager(LootTableManager.class).parseLootItem("$internal", "none", "none", "bundle-contents", contentSection);
                    if (lootItem instanceof ItemGenerativeLootItem itemGenerativeLootItem) {
                        this.contents.add(itemGenerativeLootItem);
                    } else {
                        RoseLoot.getInstance().getLogger().warning("Ignoring bundle-contents entry because it does not generate an ItemStack");
                    }
                }
            }
        } else {
            this.contents = null;
        }
    }

    @Override
    public void apply(ItemStack itemStack, LootContext context) {
        BundleContents.Builder builder = BundleContents.bundleContents();

        if (this.contents != null)
            for (ItemGenerativeLootItem content : this.contents)
                builder.addAll(content.generate(context));

        itemStack.setData(DataComponentTypes.BUNDLE_CONTENTS, builder.build());
    }

    public static void applyProperties(ItemStack itemStack, StringBuilder stringBuilder) {
        if (!itemStack.hasData(DataComponentTypes.BUNDLE_CONTENTS))
            return;

        BundleContents bundleContents = itemStack.getData(DataComponentTypes.BUNDLE_CONTENTS);
        if (bundleContents.contents().isEmpty())
            return;
            
        stringBuilder.append("bundle-contents:\n");
        
        for (int i = 0; i < bundleContents.contents().size(); i++) {
            stringBuilder.append("  ").append(i).append(":\n");
            StringBuilder subBuilder = new StringBuilder();
            ItemLootMeta.applyProperties(bundleContents.contents().get(i), subBuilder);
            stringBuilder.append(subBuilder.toString().indent(4));
        }
    }

} 
