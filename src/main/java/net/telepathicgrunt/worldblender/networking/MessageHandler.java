package net.telepathicgrunt.worldblender.networking;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.telepathicgrunt.worldblender.WorldBlender;
import net.telepathicgrunt.worldblender.blocks.WBPortalTileEntity;


public class MessageHandler
{
	//setup channel to send packages through
	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel DEFAULT_CHANNEL = NetworkRegistry.newSimpleChannel(
																			new ResourceLocation(WorldBlender.MODID, "networking"), 
																			() -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals
																		);

	/*
	 * Register the channel so it exists
	 */
	public static void init()
	{
		int channelID = -1;
		DEFAULT_CHANNEL.registerMessage(++channelID, UpdateTECooldownPacket.class, UpdateTECooldownPacket::compose, UpdateTECooldownPacket::parse, UpdateTECooldownPacket.Handler::handle);
	}

	/*
	 * updates the tileentity state for all clients from server
	 * 
	 * Packet to send to client and how the client will respond
	 * 
	 * Holds cooldown when sending to all clients 
	 */
	public static class UpdateTECooldownPacket
	{
		private float cooldown = 0;
		private BlockPos pos = null;

		public static void sendToClient(BlockPos pos, float cooldown)
		{
			MessageHandler.DEFAULT_CHANNEL.send(PacketDistributor.ALL.noArg(), new UpdateTECooldownPacket(pos, cooldown));
		}


		/*
		 * Sets block location and resource location
		 */
		public UpdateTECooldownPacket(BlockPos pos, float cooldownIn)
		{
			this.pos = pos;
			this.cooldown = cooldownIn;
		}


		/*
		 * How the server will read the packet. 
		 */
		public static UpdateTECooldownPacket parse(final PacketBuffer buf)
		{
			return new UpdateTECooldownPacket(buf.readBlockPos(), buf.readFloat());
		}
		

		/*
		 * creates the packet buffer and sets its values
		 */
		public static void compose(final UpdateTECooldownPacket pkt, final PacketBuffer buf)
		{
			buf.writeBlockPos(pkt.pos);
			buf.writeFloat(pkt.cooldown);
		}

		
		/*
		 * What the client will do with the packet
		 */
		public static class Handler
		{
			//this is what gets run on the client
			public static void handle(final UpdateTECooldownPacket pkt, final Supplier<NetworkEvent.Context> ctx)
			{
				Minecraft.getInstance().deferTask(() -> {
					@SuppressWarnings("resource")
					WBPortalTileEntity te = (WBPortalTileEntity)Minecraft.getInstance().world.getTileEntity(pkt.pos);
					te.setCoolDown(pkt.cooldown);
				});
				ctx.get().setPacketHandled(true);
			}
		}
	}
	
	
//	/**
//	 * Client requests new TE packet so they know what state it is in
//	 */
//	public static class RequestUpdateTECooldownPacket implements Message {
//
//		private BlockPos pos;
//		private String dimension;
//		
//		public RequestUpdateTECooldownPacket(BlockPos pos, String dimension) {
//			this.pos = pos;
//			this.dimension = dimension;
//		}
//		
//		public RequestUpdateTECooldownPacket(WBPortalTileEntity te) {
//			this(te.getPos(), te.getWorld().dimension.getType().getRegistryName().toString());
//		}
//		
//		public RequestUpdateTECooldownPacket() {
//		}
//
//
//		/*
//		 * How the server will read the packet. 
//		 */
//		public static RequestUpdateTECooldownPacket parse(final PacketBuffer buf)
//		{
//			return new RequestUpdateTECooldownPacket(buf.readBlockPos(), buf.readString());
//		}
//		
//
//		/*
//		 * creates the packet buffer and sets its values
//		 */
//		public static void compose(final RequestUpdateTECooldownPacket pkt, final PacketBuffer buf)
//		{
//			buf.writeBlockPos(pkt.pos);
//			buf.writeString(pkt.dimension);
//		}
//
//		
//		public static class Handler{
//		
//			public UpdateTECooldownPacket onMessage(RequestUpdateTECooldownPacket message, MessageContext ctx) {
//				World world = Minecraft.getInstance().getIntegratedServer().getWorld(DimensionType.byName(new ResourceLocation(message.dimension)));
//				WBPortalTileEntity te = (WBPortalTileEntity)world.getTileEntity(message.pos);
//				if (te != null) {
//					return new UpdateTECooldownPacket(message.pos, te.getCoolDown());
//				} else {
//					return null;
//				}
//			}
//		}
//
//		@Override
//		public String getString()
//		{
//			return null;
//		}
//
//	}
}