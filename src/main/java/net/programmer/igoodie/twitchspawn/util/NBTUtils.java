package net.programmer.igoodie.twitchspawn.util;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.programmer.igoodie.twitchspawn.tslanguage.EventArguments;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;

/**
 * NBT Utility class. Used to replace expressions with actual event
 * argument values in items or entities display name (such as displaying the donator name
 * as a Entity name)
 * @author JimiIT92
 */
public class NBTUtils {

    /**
     * Replace the TSL expressions inside the NBT Tags
     * @param textComponent Original Text Component
     * @param args Event Arguments
     * @return Original Text with replaced expressions
     */
    public static ITextComponent ReplaceExpressions(ITextComponent textComponent, EventArguments args) {
        if(textComponent instanceof TranslationTextComponent) {
            return new TranslationTextComponent(ReplaceExpressions(textComponent.getString(), args));
        }
        else
            return new StringTextComponent(ReplaceExpressions(textComponent.getString(), args));
    }

    /**
     * Replace the TSL expressions inside the NBT Tags
     * @param oldText Original Text
     * @param args Event Arguments
     * @return Original Text with replaced expressions
     */
    public static String ReplaceExpressions(String oldText, EventArguments args) {
        String newText = oldText;

        newText = newText.replaceAll("\\$\\{actor}", args.actorNickname);
        newText = newText.replaceAll("\\$\\{streamer}", args.streamerNickname);
        newText = newText.replaceAll("\\$\\{amount}", String.valueOf(args.donationAmount));
        newText = newText.replaceAll("\\$\\{amount_i}", String.valueOf((int)args.donationAmount));
        newText = newText.replaceAll("\\$\\{amount_f}", String.valueOf((float)args.donationAmount));
        newText = newText.replaceAll("\\$\\{currency}", args.donationCurrency);
        newText = newText.replaceAll("\\$\\{month}", String.valueOf(args.subscriptionMonths));
        newText = newText.replaceAll("\\$\\{viewers}", String.valueOf(args.viewerCount));
        newText = newText.replaceAll("\\$\\{raiders}", String.valueOf(args.raiderCount));
        newText = newText.replaceAll("\\$\\{time}", new SimpleDateFormat("HH:mm:ss").format(new Date()));

        return newText;
    }
}
