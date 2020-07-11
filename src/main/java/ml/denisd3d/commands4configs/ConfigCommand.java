package ml.denisd3d.commands4configs;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.UnmodifiableCommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.StringUtils;

import javax.activation.UnsupportedDataTypeException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigCommand
{
    public static ConcurrentHashMap<String, Map<ModConfig.Type, ModConfig>> configsByMod;

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        try
        {
            Field f = ConfigTracker.class.getDeclaredField("configsByMod");
            f.setAccessible(true);
            configsByMod = (ConcurrentHashMap<String, Map<ModConfig.Type, ModConfig>>) f.get(ConfigTracker.INSTANCE);

        } catch (NoSuchFieldException | IllegalAccessException e)
        {
            e.printStackTrace();
        }


        dispatcher.register(
            Commands.literal("config").requires(commandSource -> commandSource.hasPermissionLevel(4))
            .then(Commands.argument("modid", StringArgumentType.word())
                .suggests((context, builder) ->
                {
                    configsByMod.forEach((s, typeModConfigMap) ->
                    {
                        if (builder.getRemaining().isEmpty() || StringUtils.startsWithIgnoreCase(s, builder.getRemaining()))
                            builder.suggest(s);
                    });
                    return builder.buildFuture();
                })
                .then(Commands.argument("side", StringArgumentType.word())
                    .suggests((context, builder) ->
                    {
                        configsByMod.get(context.getArgument("modid", String.class)).keySet().forEach(type -> {
                            if (type == ModConfig.Type.CLIENT)
                                return;
                            if (builder.getRemaining().isEmpty() || StringUtils.startsWithIgnoreCase(type.name(), builder.getRemaining()))
                                builder.suggest(type.name());
                        });
                        return builder.buildFuture();
                    })
                    .then(Commands.literal("get").then(Commands.argument("key", StringArgumentType.string())
                        .suggests((context, builder) ->
                        {
                            getKeys(configsByMod.get(context.getArgument("modid", String.class)).get(ModConfig.Type.valueOf(context.getArgument("side", String.class))).getConfigData(), new ArrayList<>(), new ArrayList<>()).forEach(s ->
                            {
                                if (builder.getRemaining().isEmpty() || StringUtils.startsWithIgnoreCase(s, builder.getRemaining()))
                                    builder.suggest(s);
                            });
                            return builder.buildFuture();
                        })
                        .executes(context ->
                        {
                            CommentedConfig config = configsByMod.get(context.getArgument("modid", String.class)).get(ModConfig.Type.valueOf(context.getArgument("side", String.class))).getConfigData();
                            context.getSource().sendFeedback(new StringTextComponent(context.getArgument("key", String.class) + " = " + config.get(context.getArgument("key", String.class))), false);
                            return 0;
                        })))
                    .then(Commands.literal("set")
                        .then(Commands.argument("key", StringArgumentType.word())
                        .suggests((context, builder) ->
                        {
                            getKeys(configsByMod.get(context.getArgument("modid", String.class)).get(ModConfig.Type.valueOf(context.getArgument("side", String.class))).getConfigData(), new ArrayList<>(), new ArrayList<>()).forEach(s ->
                            {
                                if (builder.getRemaining().isEmpty() || StringUtils.startsWithIgnoreCase(s, builder.getRemaining()))
                                    builder.suggest(s);
                            });
                            return builder.buildFuture();
                        })
                        .then(Commands.argument("value", StringArgumentType.string())
                            .executes(context ->
                            {
                                CommentedConfig config = configsByMod.get(context.getArgument("modid", String.class)).get(ModConfig.Type.valueOf(context.getArgument("side", String.class))).getConfigData();
                                String value = context.getArgument("value", String.class);
                                Object old = config.get(context.getArgument("key", String.class));
                                try
                                {
                                    config.set(context.getArgument("key", String.class), Types.types.entrySet().stream().filter(classFunctionEntry -> old.getClass() == classFunctionEntry.getKey()).findFirst().orElseThrow(UnsupportedDataTypeException::new).getValue().apply(value));
                                }
                                catch (UnsupportedDataTypeException e)
                                {
                                    config.set(context.getArgument("key", String.class), context.getArgument("value", String.class));
                                    context.getSource().sendErrorMessage(new StringTextComponent("/!\\ Unchecked type detected."));
                                    return -1;
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                    context.getSource().sendErrorMessage(new StringTextComponent(e.getMessage()));
                                    return -1;
                                }

                                context.getSource().sendFeedback(new StringTextComponent(context.getArgument("key", String.class) + " set !"), false);

                                return 0;
                            }))
                        )
                    )
                    .then(Commands.literal("add")
                        .then(Commands.argument("key", StringArgumentType.word())
                            .suggests((context, builder) ->
                            {
                                getKeys(configsByMod.get(context.getArgument("modid", String.class)).get(ModConfig.Type.valueOf(context.getArgument("side", String.class))).getConfigData(), new ArrayList<>(), new ArrayList<>()).forEach(s ->
                                {
                                    if (builder.getRemaining().isEmpty() || StringUtils.startsWithIgnoreCase(s, builder.getRemaining()))
                                        builder.suggest(s);
                                });
                                return builder.buildFuture();
                            })
                            .then(Commands.argument("value", StringArgumentType.string())
                                .executes(context ->
                                {
                                    CommentedConfig config = configsByMod.get(context.getArgument("modid", String.class)).get(ModConfig.Type.valueOf(context.getArgument("side", String.class))).getConfigData();
                                    String value = context.getArgument("value", String.class);
                                    Object old = config.get(context.getArgument("key", String.class));

                                    try
                                    {
                                        if (old instanceof List)
                                        {
                                            ((List)old).add(Types.types.entrySet().stream().filter(classFunctionEntry -> ((List)old).get(0).getClass() == classFunctionEntry.getKey()).findFirst().orElseThrow(UnsupportedDataTypeException::new).getValue().apply(value));
                                            config.set(context.getArgument("key", String.class), old);
                                        }
                                        else
                                        {
                                            context.getSource().sendErrorMessage(new StringTextComponent("This key isn't an array."));
                                        }

                                        context.getSource().sendFeedback(new StringTextComponent(context.getArgument("key", String.class) + " updated !"), false);
                                    }
                                    catch (IndexOutOfBoundsException e)
                                    {
                                        context.getSource().sendErrorMessage(new StringTextComponent("Array is empty, Please add at least one value manually"));
                                        return -1;
                                    }
                                    catch (Exception e)
                                    {
                                        context.getSource().sendErrorMessage(new StringTextComponent(e.getMessage()));
                                        return -1;
                                    }

                                    return 0;
                                }
                                )
                            )
                        )
                    )
                    .then(Commands.literal("remove")
                        .then(Commands.argument("key", StringArgumentType.word())
                            .suggests((context, builder) ->
                            {
                                getKeys(configsByMod.get(context.getArgument("modid", String.class)).get(ModConfig.Type.valueOf(context.getArgument("side", String.class))).getConfigData(), new ArrayList<>(), new ArrayList<>()).forEach(s ->
                                {
                                    if (builder.getRemaining().isEmpty() || StringUtils.startsWithIgnoreCase(s, builder.getRemaining()))
                                        builder.suggest(s);
                                });
                                return builder.buildFuture();
                            })
                            .then(Commands.argument("value", StringArgumentType.string())
                                .suggests((context, builder) ->
                                {
                                    CommentedConfig config = configsByMod.get(context.getArgument("modid", String.class)).get(ModConfig.Type.valueOf(context.getArgument("side", String.class))).getConfigData();
                                    List<?> old = config.get(context.getArgument("key", String.class));
                                    old.forEach(o -> builder.suggest(o.toString()));
                                    return builder.buildFuture();
                                })
                                .executes(context ->
                                    {
                                        CommentedConfig config = configsByMod.get(context.getArgument("modid", String.class)).get(ModConfig.Type.valueOf(context.getArgument("side", String.class))).getConfigData();
                                        String value = context.getArgument("value", String.class);
                                        Object old = config.get(context.getArgument("key", String.class));

                                        try
                                        {
                                            if (old instanceof List)
                                            {
                                                ((List<?>)old).remove(Types.types.entrySet().stream().filter(classFunctionEntry -> ((List<?>)old).get(0).getClass() == classFunctionEntry.getKey()).findFirst().orElseThrow(UnsupportedDataTypeException::new).getValue().apply(value));
                                                config.set(context.getArgument("key", String.class), old);
                                            }

                                            context.getSource().sendFeedback(new StringTextComponent(context.getArgument("key", String.class) + " updated !"), false);
                                        }
                                        catch (IndexOutOfBoundsException e)
                                        {
                                            context.getSource().sendErrorMessage(new StringTextComponent("Array is empty, Please add at least one value manually"));
                                            return -1;
                                        }
                                        catch (Exception e)
                                        {
                                            context.getSource().sendErrorMessage(new StringTextComponent(e.getMessage()));
                                            return -1;
                                        }

                                        return 0;
                                    }
                                )
                            )
                        )
                    )
                )
            )
        );
    }

    static List<String> getKeys(UnmodifiableConfig config, List<String> configPath, List<String> result) {
        UnmodifiableCommentedConfig commentedConfig = UnmodifiableCommentedConfig.fake(config);
        return getKeys(commentedConfig, configPath, result);
    }

    private static List<String> getKeys(UnmodifiableCommentedConfig config, List<String> configPath, List<String> result) {
        List<UnmodifiableCommentedConfig.Entry> tablesEntries = new ArrayList<>();
        List<UnmodifiableCommentedConfig.Entry> tableArraysEntries = new ArrayList<>();

        // Writes the "simple" values:
        for (UnmodifiableCommentedConfig.Entry entry : config.entrySet()) {
            final String key = entry.getKey();
            final Object value = entry.getValue();
            final String comment = entry.getComment();
            if (value instanceof UnmodifiableConfig) { //                 !writer.writesInline((UnmodifiableConfig)value)
                tablesEntries.add(entry);
                continue;
            } else if (value instanceof List) {
                List<?> list = (List<?>)value;
                if (!list.isEmpty() && list.get(0) instanceof UnmodifiableConfig) {
                    tableArraysEntries.add(entry);
                    continue;
                }
            }

            result.add(String.join(".", configPath) + "." + key);
        }

        // Writes the tables:
        for (UnmodifiableCommentedConfig.Entry entry : tablesEntries) {
            // Writes the comment, if there is one
            //result.add(String.join(".", configPath) + "." + entry.getKey());

            // Writes the table declaration
            configPath.add(entry.getKey());// path level ++
            // Writes the table's content
            result.addAll(getKeys(entry.<UnmodifiableConfig>getValue(), configPath, result));
            configPath.remove(configPath.size() - 1);// path level --
        }

        // Writes the arrays of tables:
        for (UnmodifiableCommentedConfig.Entry entry : tableArraysEntries) {
            // Writes the comment, if there is one
            //System.out.println("Section entry : " + entry.getComment());

            // Writes the tables
            configPath.add(entry.getKey());// path level ++
            List<Config> tableArray = entry.getValue();
            for (UnmodifiableConfig table : tableArray) {
                result.add(String.join(".", configPath));
                result.addAll(getKeys(table, configPath, result));
            }
            configPath.remove(configPath.size() - 1);// path level --
        }

        return result;
    }

/*
    static void writeNormal(UnmodifiableConfig config, List<String> configPath) {
        UnmodifiableCommentedConfig commentedConfig = UnmodifiableCommentedConfig.fake(config);
        writeNormal(commentedConfig, configPath);
    }

    private static void writeNormal(UnmodifiableCommentedConfig config, List<String> configPath) {
        List<UnmodifiableCommentedConfig.Entry> tablesEntries = new ArrayList<>();
        List<UnmodifiableCommentedConfig.Entry> tableArraysEntries = new ArrayList<>();

        // Writes the "simple" values:
        for (UnmodifiableCommentedConfig.Entry entry : config.entrySet()) {
            final String key = entry.getKey();
            final Object value = entry.getValue();
            final String comment = entry.getComment();
            if (value instanceof UnmodifiableConfig) { //                 !writer.writesInline((UnmodifiableConfig)value)
                tablesEntries.add(entry);
                continue;
            } else if (value instanceof List) {
                List<?> list = (List<?>)value;
                if (!list.isEmpty() && list.get(0) instanceof UnmodifiableConfig) {
                    tableArraysEntries.add(entry);
                    continue;
                }
            }


            System.out.println("Entry : " + key + value);
        }

        // Writes the tables:
        for (UnmodifiableCommentedConfig.Entry entry : tablesEntries) {
            // Writes the comment, if there is one
            System.out.println("Section : " + entry.getKey());

            // Writes the table declaration
            configPath.add(entry.getKey());// path level ++
            // Writes the table's content
            writeNormal(entry.<UnmodifiableConfig>getValue(), configPath);
            configPath.remove(configPath.size() - 1);// path level --
        }

        // Writes the arrays of tables:
        for (UnmodifiableCommentedConfig.Entry entry : tableArraysEntries) {
            // Writes the comment, if there is one
            //System.out.println("Section entry : " + entry.getComment());

            // Writes the tables
            configPath.add(entry.getKey());// path level ++
            List<Config> tableArray = entry.getValue();
            for (UnmodifiableConfig table : tableArray) {
                System.out.println("Section entry 2 : " + String.join(",", configPath));
                writeNormal(table, configPath);
            }
            configPath.remove(configPath.size() - 1);// path level --
        }
    }
 */
}
//                 .suggests((context, builder) -> { ModList.get().getMods().forEach(modInfo -> builder.suggest(modInfo.getModId())); return builder.buildFuture(); })
//ModList.get().getModFileById(context.getArgument("modid", String.class)).getFileProperties().forEach((s, o) ->  builder.suggest(s));
