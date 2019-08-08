package net.programmer.igoodie.twitchspawn.tslanguage.action;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.arguments.NBTCompoundTagArgument;
import net.minecraft.command.impl.SummonCommand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.monster.SkeletonEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.ServerWorld;
import net.minecraftforge.common.util.Constants;
import net.programmer.igoodie.twitchspawn.tslanguage.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLParser;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;

import java.rmi.server.Skeleton;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public class SummonAction extends TSLAction {

    private EntityType<?> entityType;
    private Vec3d offset;
    private CompoundNBT nbt;

    public SummonAction(List<String> words) throws TSLSyntaxError {
        this.message = TSLParser.parseMessage(words);
        List<String> actionWords = actionPart(words);

        if (actionWords.size() != 1 && actionWords.size() != 4 && actionWords.size() != 5)
            throw new TSLSyntaxError("Invalid length of words (expected 1, 4 or 5): " + actionWords);

        // Fetch TSL input
        String entityName = actionWords.get(0);
        double offsetX = actionWords.size() < 4 ? 0 : parsePositionOffset(actionWords.get(1));
        double offsetY = actionWords.size() < 4 ? 0 : parsePositionOffset(actionWords.get(2));
        double offsetZ = actionWords.size() < 4 ? 0 : parsePositionOffset(actionWords.get(3));
        String nbtRaw = actionWords.size() != 5 ? null : actionWords.get(4);

        // Fetch entity type
        EntityType<?> entityType = EntityType.byKey(entityName).orElse(null);

        // Entity with given key not found
        if (entityType == null)
            throw new TSLSyntaxError("Invalid entity name -> " + entityName);

        // Save parsed words
        try {
            this.nbt = nbtRaw != null ? new JsonToNBT(new StringReader(nbtRaw)).readStruct() : null;
            this.offset = new Vec3d(offsetX, offsetY, offsetZ);
            this.entityType = entityType;

        } catch (CommandSyntaxException e) {
            throw new TSLSyntaxError("Malformed NBT json -> " + nbtRaw);
        }
    }

    private double parsePositionOffset(String positionExpression) throws TSLSyntaxError {
        if (positionExpression.equals("~"))
            return 0.0;

        if (!positionExpression.startsWith("~"))
            throw new TSLSyntaxError("Malformed position expression -> " + positionExpression);

        try {
            return Double.parseDouble(positionExpression.substring(1)); // ~ is the first char

        } catch (NumberFormatException e) {
            throw new TSLSyntaxError("Malformed position expression -> " + positionExpression);
        }
    }

    @Override
    protected void performAction(ServerPlayerEntity player, EventArguments args) {
//        Vec3d summonPosition = player.getPositionVector().add(offset);
//        ServerWorld serverWorld = player.getServerWorld();
//
//        System.out.println("NBT -> " + nbt);
//
//        Entity summonedEntity = EntityType.func_220335_a(nbt.copy(), serverWorld, createdEntity -> {
//            createdEntity.setLocationAndAngles(
//                    summonPosition.x, summonPosition.y, summonPosition.y,
//                    -player.cameraYaw, 0.0f);
//            return !serverWorld.summonEntity(createdEntity) ? null : createdEntity;
////            return !serverWorld.summonEntity(createdEntity) ? null : createdEntity;
//        });

        // TODO: fix this ugly (?) hack
        String command = String.format("/summon %s %s %s %s %s",
                entityType.getRegistryName(),
                "~" + offset.getX(),
                "~" + offset.getY(),
                "~" + offset.getZ(),
                (nbt == null ? "" : nbt.toString()));

        player.getServer().getCommandManager().handleCommand(player.getCommandSource()
                .withPermissionLevel(9999).withFeedbackDisabled(), command);

//        Entity summonedEntity = entityType.spawn(
//                serverWorld,
//                null,
//                null,
//                null,
//                new BlockPos(summonPosition),
//                SpawnReason.COMMAND,
//                true, true);
//        summonedEntity.read(nbt);
//        summonedEntity.read(nbt);

//        System.out.println("Summoned " + summonedEntity);

//        player.getServerWorld().summonEntity()
    }

    @Override
    protected String subtitleEvaluator(String expression, EventArguments args) {
        if (expression.equals("mobName"))
            return entityType.getName().getString();
        return null;
    }

}
