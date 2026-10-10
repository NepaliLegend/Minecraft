package com.arcanebestiary.gametest;

import com.arcanebestiary.entity.BossEntity;
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
}
