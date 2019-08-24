package net.programmer.igoodie.twitchspawn.tslanguage.action;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.programmer.igoodie.twitchspawn.tslanguage.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLParser;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;

import java.util.List;

public class SummonAction extends TSLAction {

    private EntityType<?> entityType;
    private String rawCoordX, rawCoordY, rawCoordZ;
    private CompoundNBT nbt;

    public SummonAction(List<String> words) throws TSLSyntaxError {
        this.message = TSLParser.parseMessage(words);
        List<String> actionWords = actionPart(words);

        if (actionWords.size() != 1 && actionWords.size() != 4 && actionWords.size() != 5)
            throw new TSLSyntaxError("Invalid length of words (expected 1, 4 or 5): " + actionWords);

        // Fetch TSL input
        String entityName = actionWords.get(0);
        String rawCoordX = actionWords.size() < 4 ? "~" : validateCoordinateExpression(actionWords.get(1));
        String rawCoordY = actionWords.size() < 4 ? "~" : validateCoordinateExpression(actionWords.get(2));
        String rawCoordZ = actionWords.size() < 4 ? "~" : validateCoordinateExpression(actionWords.get(3));
        String nbtRaw = actionWords.size() != 5 ? null : actionWords.get(4);

        // Fetch entity type
        EntityType<?> entityType = EntityType.byKey(entityName).orElse(null);

        // Entity with given key not found
        if (entityType == null)
            throw new TSLSyntaxError("Invalid entity name -> " + entityName);

        // Save parsed words
        try {
            this.nbt = nbtRaw == null ? null : new JsonToNBT(new StringReader(nbtRaw)).readStruct();
            this.rawCoordX = rawCoordX;
            this.rawCoordY = rawCoordY;
            this.rawCoordZ = rawCoordZ;
            this.entityType = entityType;

        } catch (CommandSyntaxException e) {
            throw new TSLSyntaxError("Malformed NBT json -> " + nbtRaw);
        }
    }

    private String validateCoordinateExpression(String expression) throws TSLSyntaxError {
        if (expression.equals("~"))
            return expression;

        try {
            if (expression.startsWith("~"))
                Double.parseDouble(expression.substring(1));
            else
                Double.parseDouble(expression);

        } catch (NumberFormatException e) {
            throw new TSLSyntaxError("Malformed position expression -> " + expression);
        }

        return expression;
    }

    @Override
    protected void performAction(ServerPlayerEntity player, EventArguments args) {
        String command = String.format("/summon %s %s %s %s %s",
                entityType.getRegistryName(),
                rawCoordX,
                rawCoordY,
                rawCoordZ,
                (nbt == null ? "{}" : nbt.toString()));

        System.out.printf("Running %s\n", command);

        player.getServer().getCommandManager().handleCommand(player.getCommandSource()
                .withPermissionLevel(9999).withFeedbackDisabled(), command);
    }

    @Override
    protected String subtitleEvaluator(String expression, EventArguments args) {
        if (expression.equals("mobName"))
            return entityType.getName().getString();
        return null;
    }

}
