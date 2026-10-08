package com.example.fakegive.client;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

public class fakeGive implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, commandBuildContext) -> {
            dispatcher.register(ClientCommands.literal("fakegive")
                    .executes(context -> {
                        context.getSource().sendFeedback(Component.literal("/fakegive <item> [count] [slot]"));
                        return 0;
                    })
                    .then(ClientCommands.argument("item", ItemArgument.item(commandBuildContext))
                            .executes(fakeGive::executeFakeGive)
                            .then(ClientCommands.argument("amount", IntegerArgumentType.integer(1))
                                    .executes(fakeGive::executeFakeGive)
                                    .then(ClientCommands.argument("slot", IntegerArgumentType.integer(0, 40))
                                            .executes(fakeGive::executeFakeGive)
                                    )
                            )
                    )
            );
        });
    }

    private static int executeFakeGive(CommandContext<FabricClientCommandSource> context) {
        var player = Minecraft.getInstance().player;
        if (player == null) return 0;
        int amount = 1;
        try {
            amount = IntegerArgumentType.getInteger(context, "amount");
        } catch (IllegalArgumentException _) {
        }
        ItemInput itemInput = ItemArgument.getItem(context, "item");
        ItemStack itemStack;
        try {
            itemStack = itemInput.createItemStack(amount);
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException e) {
            throw new RuntimeException("Failed to generate item stack from template", e);
        }
        int slot;
        try {
            slot = IntegerArgumentType.getInteger(context, "slot");
            player.getInventory().setItem(slot, itemStack);
        } catch (Exception _) {
            slot = player.getInventory().getSelectedSlot();
            player.getInventory().setSelectedItem(itemStack);
        }
        try {
            var itemTemplate = ItemStackTemplate.fromNonEmptyStack(itemStack);
            var message = Component.literal(itemInput.item().value().toString())
                .withStyle(style -> style.withHoverEvent(
                    new HoverEvent.ShowItem(itemTemplate)
                ));
            context.getSource().sendFeedback(Component.literal("made ghost %s in slot %d".formatted(message.getString(), slot)));
        } catch (Exception _) {
            context.getSource().sendFeedback(Component.literal("made server ghost"));
        }
        return 1;
    }
}