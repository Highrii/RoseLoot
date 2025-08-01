package dev.rosewood.roseloot.listener;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.roseloot.config.SettingKey;
import dev.rosewood.roseloot.hook.RoseStackerHook;
import dev.rosewood.roseloot.listener.helper.LazyLootTableListener;
import dev.rosewood.roseloot.loot.ExplosionType;
import dev.rosewood.roseloot.loot.LootContents;
import dev.rosewood.roseloot.loot.LootResult;
import dev.rosewood.roseloot.loot.OverwriteExisting;
import dev.rosewood.roseloot.loot.context.LootContext;
import dev.rosewood.roseloot.loot.context.LootContextParams;
import dev.rosewood.roseloot.loot.table.LootTableTypes;
import dev.rosewood.roseloot.manager.LootTableManager;
import dev.rosewood.roseloot.util.LootUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;

public class EntityListener extends LazyLootTableListener {

    public EntityListener(RosePlugin rosePlugin) {
        super(rosePlugin, LootTableTypes.ENTITY);
    }

    @EventHandler(priority = EventPriority.LOW) // changing to LOW so worldguard flags detect changes properly
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (this.rosePlugin.getRoseConfig().get(SettingKey.DISABLED_WORLDS).stream().anyMatch(x -> x.equalsIgnoreCase(entity.getWorld().getName())))
            return;

        if (RoseStackerHook.shouldIgnoreNormalDeathEvent(event))
            return;

        Entity looter = null;
        if (entity.getLastDamageCause() instanceof EntityDamageByEntityEvent)
            looter = ((EntityDamageByEntityEvent) entity.getLastDamageCause()).getDamager();

        LootContext lootContext = LootContext.builder(LootUtils.getEntityLuck(looter))
                .put(LootContextParams.ORIGIN, entity.getLocation())
                .put(LootContextParams.LOOTER, looter)
                .put(LootContextParams.LOOTED_ENTITY, entity)
                .put(LootContextParams.EXPLOSION_TYPE, ExplosionType.getDeathExplosionType(entity))
                .put(LootContextParams.HAS_EXISTING_ITEMS, !event.getDrops().isEmpty())
                .build();
        LootResult lootResult = this.rosePlugin.getManager(LootTableManager.class).getLoot(LootTableTypes.ENTITY, lootContext);
        if (lootResult.isEmpty())
            return;

        LootContents lootContents = lootResult.getLootContents();

        // Overwrite existing drops if applicable
        if (lootResult.doesOverwriteExisting(OverwriteExisting.ITEMS))
            event.getDrops().clear();

        if (lootResult.doesOverwriteExisting(OverwriteExisting.EXPERIENCE))
            event.setDroppedExp(0);

        Runnable task;
        if (this.rosePlugin.getRoseConfig().get(SettingKey.MODIFY_ENTITYDEATHEVENT_DROPS)) {
            // Add items to drops and adjust experience
            event.getDrops().addAll(lootContents.getItems());
            event.setDroppedExp(event.getDroppedExp() + lootContents.getExperience());
            task = () -> lootContents.triggerExtras(entity.getLocation());
        } else {
            task = () -> lootContents.dropAtLocation(entity.getLocation());
        }

        if (!Bukkit.isPrimaryThread()) {
            this.rosePlugin.getScheduler().runTask(task);
        } else {
            task.run();
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntitySpawn(CreatureSpawnEvent event) {
        // Tag all spawned entities with the spawn reason
        LootUtils.setEntitySpawnReason(event.getEntity(), event.getSpawnReason());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntitySpawnFromSpawner(SpawnerSpawnEvent event) {
        // Tag all spawned entities with the spawn reason, separate event listener for spawners for custom spawner plugins
        Entity entity = event.getEntity();
        if (entity instanceof LivingEntity)
            LootUtils.setEntitySpawnReason((LivingEntity) entity, CreatureSpawnEvent.SpawnReason.SPAWNER);
    }

}
