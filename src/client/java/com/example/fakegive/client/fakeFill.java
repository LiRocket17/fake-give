package com.example.fakegive.client;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;

public class fakeFill implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, commandBuildContext) -> {
			dispatcher.register(ClientCommands.literal("fakefill")
				.executes(context -> {
					context.getSource().sendFeedback(Component.literal("/fakefill <start> <end> <block>"));
					return 0;
				})
				.then(ClientCommands.argument("start", BlockPosArgument.blockPos())
					.then(ClientCommands.argument("end", BlockPosArgument.blockPos())
						.then(ClientCommands.argument("block", BlockStateArgument.block(commandBuildContext))
							.executes(fakeFill::executeFakeFill)
						)
					)
				)
			);
		});
	}

	private static int executeFakeFill(CommandContext<FabricClientCommandSource> context) {
		var source = context.getSource();
		var commandSource = new net.minecraft.commands.CommandSourceStack(
				CommandSource.NULL,
				source.getPosition(),
				source.getRotation(),
				null, permission -> true, "", net.minecraft.network.chat.Component.empty(), null, null
		);
		var world = Minecraft.getInstance().level;
		var start = context.getArgument("start", Coordinates.class);
		var startVec = start.getPosition(commandSource);
		BlockPos startPos = BlockPos.containing(startVec.x, startVec.y, startVec.z);
		var end = context.getArgument("end", Coordinates.class);
		var endVec = end.getPosition(commandSource);
		BlockPos endPos = BlockPos.containing(endVec.x, endVec.y, endVec.z);
		var block = context.getArgument("block", BlockInput.class);
		for (BlockPos pos : BlockPos.betweenClosed(startPos, endPos)) {
			world.setBlock(pos, block.getState(), 2);
		}
		return 1;
	}
}