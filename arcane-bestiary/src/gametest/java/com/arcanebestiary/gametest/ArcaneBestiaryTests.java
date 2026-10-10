package com.arcanebestiary.gametest;

import com.arcanebestiary.block.SolarPanelBlock;
import com.arcanebestiary.block.SolarPanelBlockEntity;
import com.arcanebestiary.entity.BossEntity;
import com.arcanebestiary.registry.ModBlocks;
import com.arcanebestiary.registry.ModEntities;
import com.arcanebestiary.registry.ModItems;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

public class ArcaneBestiaryTests {
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 40)
	public void entitiesSpawn(TestContext context) {
		BlockPos pos = new BlockPos(2, 1, 2);
		for (EntityType<? extends MobEntity> type : java.util.List.of(ModEntities.FROST_WRAITH, ModEntities.EMBER_IMP,
			ModEntities.CRYSTAL_CRAWLER, ModEntities.INFERNAL_WARDEN, ModEntities.GLACIAL_SOVEREIGN, ModEntities.CRYSTAL_BEHEMOTH)) {
			context.spawnMob(type, pos);
		}
		context.runAtTick(20, () -> {
			context.expectEntity(ModEntities.INFERNAL_WARDEN);
			context.expectEntity(ModEntities.CRYSTAL_BEHEMOTH);
			context.complete();
		});
	}

	private void bossFight(TestContext context, EntityType<? extends BossEntity> bossType, EntityType<?> expectedProjectileOrMinion, int ticks) {
		PlayerEntity player = context.createMockPlayer(GameMode.SURVIVAL);
		player.setPosition(context.getAbsolute(new net.minecraft.util.math.Vec3d(6.5, 1, 6.5)));
		BossEntity boss = context.spawnMob(bossType, new BlockPos(2, 1, 2));
		boss.setHealth(boss.getMaxHealth() * 0.4f);
		boolean[] sawAbility = {false};
		for (int t = 1; t < ticks; t += 5) {
			context.runAtTick(t, () -> {
				boss.setTarget(player);
				player.setHealth(player.getMaxHealth());
				if (!context.getWorld().getEntitiesByType(expectedProjectileOrMinion, boss.getBoundingBox().expand(40), e -> true).isEmpty()) {
					sawAbility[0] = true;
				}
			});
		}
		context.runAtTick(ticks, () -> {
			context.assertTrue(boss.isEnraged(), "boss should be enraged below half health");
			context.assertTrue(sawAbility[0], "boss never produced " + expectedProjectileOrMinion);
			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 700)
	public void infernalWardenUsesAbilities(TestContext context) {
		bossFight(context, ModEntities.INFERNAL_WARDEN, EntityType.SMALL_FIREBALL, 600);
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 700)
	public void glacialSovereignUsesAbilities(TestContext context) {
		bossFight(context, ModEntities.GLACIAL_SOVEREIGN, ModEntities.FROST_BOLT, 600);
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 700)
	public void crystalBehemothUsesAbilities(TestContext context) {
		bossFight(context, ModEntities.CRYSTAL_BEHEMOTH, EntityType.EVOKER_FANGS, 600);
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 400)
	public void frostWraithShootsBolts(TestContext context) {
		PlayerEntity player = context.createMockPlayer(GameMode.SURVIVAL);
		player.setPosition(context.getAbsolute(new net.minecraft.util.math.Vec3d(8.5, 1, 4.5)));
		MobEntity wraith = context.spawnMob(ModEntities.FROST_WRAITH, new BlockPos(2, 1, 4));
		boolean[] seen = {false};
		for (int t = 1; t < 300; t += 5) {
			context.runAtTick(t, () -> {
				wraith.setTarget(player);
				player.setHealth(player.getMaxHealth());
				if (!context.getWorld().getEntitiesByType(ModEntities.FROST_BOLT, wraith.getBoundingBox().expand(30), e -> true).isEmpty()) {
					seen[0] = true;
				}
			});
		}
		context.runAtTick(300, () -> {
			context.assertTrue(seen[0], "frost wraith never fired a bolt");
			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
	public void sigilSummonsBoss(TestContext context) {
		PlayerEntity player = context.createMockPlayer(GameMode.SURVIVAL);
		context.setBlockState(3, 0, 3, net.minecraft.block.Blocks.STONE);
		ItemStack sigil = new ItemStack(ModItems.INFERNAL_SIGIL);
		player.setStackInHand(net.minecraft.util.Hand.MAIN_HAND, sigil);
		context.useStackOnBlock(player, sigil, new BlockPos(3, 0, 3), net.minecraft.util.math.Direction.UP);
		context.runAtTick(10, () -> {
			context.expectEntity(ModEntities.INFERNAL_WARDEN);
			context.assertTrue(sigil.isEmpty(), "sigil should be consumed");
			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
	public void weaponsWork(TestContext context) {
		PlayerEntity player = context.createMockPlayer(GameMode.SURVIVAL);
		player.setPosition(context.getAbsolute(new net.minecraft.util.math.Vec3d(2.5, 1, 2.5)));
		MobEntity zombie = context.spawnMob(EntityType.ZOMBIE, new BlockPos(3, 1, 2));
		zombie.setAiDisabled(true);

		ItemStack brand = new ItemStack(ModItems.EMBERBRAND);
		brand.getItem().postHit(brand, zombie, player);
		context.assertTrue(zombie.isOnFire(), "Emberbrand should ignite");

		ItemStack fang = new ItemStack(ModItems.FROSTFANG);
		fang.getItem().postHit(fang, zombie, player);
		context.assertTrue(zombie.hasStatusEffect(net.minecraft.entity.effect.StatusEffects.SLOWNESS), "Frostfang should slow");

		player.setStackInHand(net.minecraft.util.Hand.MAIN_HAND, new ItemStack(ModItems.TEMPEST_STAFF));
		ItemStack staff = player.getMainHandStack();
		player.setPitch(90.0f);
		var result = staff.getItem().use(context.getWorld(), player, net.minecraft.util.Hand.MAIN_HAND);
		context.assertTrue(result.getResult().isAccepted(), "staff use should be accepted");
		context.runAtTick(1, () -> {
			var bolts = context.getWorld().getEntitiesByType(EntityType.LIGHTNING_BOLT, player.getBoundingBox().expand(200), e -> true);
			context.assertTrue(!bolts.isEmpty(), "Tempest Staff should call lightning");
			context.complete();
		});
	}

	private SolarPanelBlockEntity placePanel(TestContext context, BlockPos relative) {
		context.setBlockState(relative, ModBlocks.SOLAR_PANEL);
		return (SolarPanelBlockEntity) context.getBlockEntity(relative);
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 120)
	public void solarPanelRepairsAndPowersInDaylight(TestContext context) {
		context.getWorld().setTimeOfDay(6000);
		context.getWorld().setWeather(100000, 0, false, false);
		BlockPos rel = new BlockPos(2, 1, 2);
		SolarPanelBlockEntity panel = placePanel(context, rel);
		PlayerEntity player = context.createMockPlayer(GameMode.SURVIVAL);
		ItemStack pick = new ItemStack(ModItems.CRYSTALITE_PICKAXE);
		pick.setDamage(500);
		player.setStackInHand(net.minecraft.util.Hand.MAIN_HAND, pick);
		context.useBlock(rel, player);
		context.assertTrue(panel.getStoredStack() == pick || !panel.getStoredStack().isEmpty(), "panel should accept the damaged item");
		context.assertTrue(player.getMainHandStack().isEmpty(), "item should leave the player's hand");
		context.runAtTick(60, () -> {
			context.assertTrue(panel.getStoredStack().getDamage() < 500, "stored item should have been repaired, damage=" + panel.getStoredStack().getDamage());
			int power = context.getBlockState(rel).get(SolarPanelBlock.POWER);
			context.assertTrue(power > 0, "panel should emit redstone power in daylight, power=" + power);
			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 120)
	public void solarPanelIdleWhenCovered(TestContext context) {
		context.getWorld().setTimeOfDay(6000);
		BlockPos rel = new BlockPos(2, 1, 2);
		placePanel(context, rel);
		for (int y = 2; y <= 6; y++) {
			context.setBlockState(new BlockPos(2, y, 2), net.minecraft.block.Blocks.STONE);
		}
		context.runAtTick(60, () -> {
			int power = context.getBlockState(rel).get(SolarPanelBlock.POWER);
			context.assertTrue(power == 0, "covered panel should not emit power, power=" + power);
			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 40)
	public void solarPanelDropsItemAndPersists(TestContext context) {
		BlockPos rel = new BlockPos(2, 1, 2);
		SolarPanelBlockEntity panel = placePanel(context, rel);
		ItemStack sword = new ItemStack(ModItems.EMBERBRAND);
		sword.setDamage(100);
		panel.setStoredStack(sword);

		var registries = context.getWorld().getRegistryManager();
		var nbt = panel.createNbt(registries);
		SolarPanelBlockEntity copy = new SolarPanelBlockEntity(panel.getPos(), panel.getCachedState());
		copy.read(nbt, registries);
		context.assertTrue(copy.getStoredStack().isOf(ModItems.EMBERBRAND) && copy.getStoredStack().getDamage() == 100,
			"stored item should survive an NBT round trip");

		context.setBlockState(rel, net.minecraft.block.Blocks.AIR);
		context.runAtTick(5, () -> {
			context.expectEntity(EntityType.ITEM);
			context.complete();
		});
	}
}
