package net.programmer.igoodie.twitchspawn.tslanguage.action;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.JsonToNBT;
import net.programmer.igoodie.twitchspawn.tslanguage.event.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLParser;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;

import java.util.List;

public class SummonAction extends TSLAction {

    private EntityType<?> entityType;
    private String rawCoordX, rawCoordY, rawCoordZ;
    private String rawNbt;

    public SummonAction(List<String> words) throws TSLSyntaxError {
        this.message = TSLParser.parseMessage(words);
        List<String> actionWords = actionPart(words);

        if (actionWords.size() != 1 && actionWords.size() != 4 && actionWords.size() != 5)
            throw new TSLSyntaxError("Invalid length of words (expected 1, 4 or 5): %s", actionWords);

        // Fetch TSL input
        this.rawCoordX = actionWords.size() < 4 ? "~" : validateCoordinateExpression(actionWords.get(1));
        this.rawCoordY = actionWords.size() < 4 ? "~" : validateCoordinateExpression(actionWords.get(2));
        this.rawCoordZ = actionWords.size() < 4 ? "~" : validateCoordinateExpression(actionWords.get(3));
        this.rawNbt = actionWords.size() != 5 ? "{}" : actionWords.get(4);

        // Fetch entity type
        String entityName = actionWords.get(0);
        EntityType<?> entityType = EntityType.byKey(entityName).orElse(null);

        // Entity with given key not found
        if (entityType == null)
            throw new TSLSyntaxError("Invalid entity name -> %s", entityName);

        this.entityType = entityType;

        // Save parsed words
        try {
            new JsonToNBT(new StringReader(rawNbt)).readStruct();

        } catch (CommandSyntaxException e) {
            throw new TSLSyntaxError("Malformed NBT json -> %s", rawNbt);
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
            throw new TSLSyntaxError("Malformed position expression -> %s", expression);
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
                replaceExpressions(rawNbt, args));

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
