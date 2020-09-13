package net.programmer.igoodie.twitchspawn.tslanguage.action;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.programmer.igoodie.twitchspawn.tslanguage.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLParser;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;

import java.util.List;

public class SummonAction extends TSLAction {

    private Class<? extends Entity> entityType;
    private String rawCoordX, rawCoordY, rawCoordZ;
    private String rawNbt;

    public SummonAction(List<String> words) throws TSLSyntaxError {
        this.message = TSLParser.parseMessage(words);
        List<String> actionWords = actionPart(words);

        if (actionWords.size() != 1 && actionWords.size() != 4 && actionWords.size() != 5)
            throw new TSLSyntaxError("Invalid length of words (expected 1, 4 or 5): %s", actionWords);

//        this.entityType = EntityList.getClassFromName(actionWords.get(0));
        this.entityType = EntityList.getClass(new ResourceLocation(actionWords.get(0)));
        this.rawCoordX = actionWords.size() < 4 ? "~" : validateCoordinateExpression(actionWords.get(1));
        this.rawCoordY = actionWords.size() < 4 ? "~" : validateCoordinateExpression(actionWords.get(2));
        this.rawCoordZ = actionWords.size() < 4 ? "~" : validateCoordinateExpression(actionWords.get(3));
        this.rawNbt = actionWords.size() != 5 ? "{}" : actionWords.get(4);

        // Entity type is not found within the list
        if (this.entityType == null)
            throw new TSLSyntaxError("Invalid entity name -> %s", actionWords.get(0));

        // Try to parse NBT and see if it's valid
        try { JsonToNBT.getTagFromJson(this.rawNbt); } catch (NBTException e) {
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
    protected void performAction(EntityPlayerMP player, EventArguments args) {
        String command = String.format("/summon %s %s %s %s %s",
                EntityList.getKey(entityType),
                rawCoordX,
                rawCoordY,
                rawCoordZ,
                replaceExpressions(rawNbt, args));

        player.getServer().getCommandManager()
                .executeCommand(getCommandSender(player, true, true), command);
    }

    @Override
    protected String subtitleEvaluator(String expression, EventArguments args) {
        if (expression.equals("mobName"))
            return EntityList.getTranslationName(EntityList.getKey(entityType));
        return null;
    }

}
