package com.walrusone.skywarsreloaded.nms.v1_8_R2;

import com.walrusone.skywarsreloaded.game.signs.SWRSign;
import com.walrusone.skywarsreloaded.nms.NMS;
import net.minecraft.server.v1_8_R2.*;
import net.minecraft.server.v1_8_R2.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R2.PacketPlayOutTitle.EnumTitleAction;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.craftbukkit.v1_8_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.BlockIterator;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class NMSHandler implements NMS {
    public NMSHandler() {
    }

    @Override
    public SWRSign createSWRSign(String name, org.bukkit.Location location) {
        return new SWRSign82(name, location);
    }

    public boolean removeFromScoreboardCollection(Scoreboard scoreboard) {
        return false;
    }

    public void respawnPlayer(Player player) {
        ((org.bukkit.craftbukkit.v1_8_R2.CraftServer) Bukkit.getServer()).getHandle().moveToWorld(((CraftPlayer) player).getHandle(), 0, false);
    }

    public void sendParticles(World world, String type, float x, float y, float z, float offsetX, float offsetY, float offsetZ, float data, int amount) {
        net.minecraft.server.v1_8_R2.EnumParticle particle = net.minecraft.server.v1_8_R2.EnumParticle.valueOf(type);
        net.minecraft.server.v1_8_R2.PacketPlayOutWorldParticles particles = new net.minecraft.server.v1_8_R2.PacketPlayOutWorldParticles(particle, false, x, y, z, offsetX, offsetY, offsetZ, data, amount, new int[]{1});
        for (Player player : world.getPlayers()) {
            CraftPlayer start = (CraftPlayer) player;
            EntityPlayer target = start.getHandle();
            PlayerConnection connect = target.playerConnection;
            connect.sendPacket(particles);
        }
    }

    public FireworkEffect getFireworkEffect(Color one, Color two, Color three, Color four, Color five, FireworkEffect.Type type) {
        return FireworkEffect.builder().flicker(false).withColor(one, two, three, four).withFade(five).with(type).trail(true).build();
    }

    public void sendTitle(Player player, int fadein, int stay, int fadeout, String title, String subtitle) {
        PlayerConnection pConn = ((CraftPlayer) player).getHandle().playerConnection;
        PacketPlayOutTitle pTitleInfo = new PacketPlayOutTitle(EnumTitleAction.TIMES, (IChatBaseComponent) null, fadein, stay, fadeout);
        pConn.sendPacket(pTitleInfo);
        if (subtitle != null) {
            subtitle = subtitle.replaceAll("%player%", player.getDisplayName());
            subtitle = ChatColor.translateAlternateColorCodes('&', subtitle);
            IChatBaseComponent iComp = ChatSerializer.a("{\"text\": \"" + subtitle + "\"}");
            PacketPlayOutTitle pSubtitle = new PacketPlayOutTitle(EnumTitleAction.SUBTITLE, iComp);
            pConn.sendPacket(pSubtitle);
        }
        if (title != null) {
            title = title.replaceAll("%player%", player.getDisplayName());
            title = ChatColor.translateAlternateColorCodes('&', title);
            IChatBaseComponent iComp = ChatSerializer.a("{\"text\": \"" + title + "\"}");
            PacketPlayOutTitle pTitle = new PacketPlayOutTitle(EnumTitleAction.TITLE, iComp);
            pConn.sendPacket(pTitle);
        }
    }

    public void playGameSound(Location loc, String paramEnumName, String paramCategory, float paramVolume, float paramPitch, boolean paramIsCustom) {
        paramEnumName = this.getSoundTranslation(paramEnumName);
        if (!paramIsCustom) {
            loc.getWorld().playSound(loc, Sound.valueOf(paramEnumName), paramVolume, paramPitch);
        }
    }

    private String getSoundTranslation(String paramEnumName) {
        switch (paramEnumName) {
            case "ENTITY_PLAYER_DEATH":
                return "HURT_FLESH";
            default:
                return paramEnumName;
        }
    }

    public void sendActionBar(Player p, String msg) {
        String s = ChatColor.translateAlternateColorCodes('&', msg);
        IChatBaseComponent icbc = ChatSerializer.a("{\"text\": \"" + s + "\"}");
        net.minecraft.server.v1_8_R2.PacketPlayOutChat bar = new net.minecraft.server.v1_8_R2.PacketPlayOutChat(icbc, (byte) 2);
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(bar);
    }

    public String getItemName(org.bukkit.inventory.ItemStack item) {
        return org.bukkit.craftbukkit.v1_8_R2.inventory.CraftItemStack.asNMSCopy(item).getName();
    }

    public org.bukkit.inventory.ItemStack getMainHandItem(Player player) {
        return player.getInventory().getItemInHand();
    }

    public org.bukkit.inventory.ItemStack getOffHandItem(Player player) {
        return null;
    }

    public org.bukkit.inventory.ItemStack getItemStack(Material material, List<String> lore, String message) {
        org.bukkit.inventory.ItemStack addItem = new org.bukkit.inventory.ItemStack(material, 1);
        ItemMeta addItemMeta = addItem.getItemMeta();
        addItemMeta.setDisplayName(message);
        addItemMeta.setLore(lore);
        addItemMeta.addItemFlags(ItemFlag.values());
        addItem.setItemMeta(addItemMeta);
        return addItem;
    }

    public org.bukkit.inventory.ItemStack getItemStack(org.bukkit.inventory.ItemStack item, List<String> lore, String message) {
        org.bukkit.inventory.ItemStack addItem = item.clone();
        ItemMeta addItemMeta = addItem.getItemMeta();
        addItemMeta.setDisplayName(message);
        addItemMeta.setLore(lore);
        addItemMeta.addItemFlags(ItemFlag.values());
        addItem.setItemMeta(addItemMeta);
        return addItem;
    }

    public boolean isValueParticle(String string) {
        return true;
    }

    public void updateSkull(Skull skull, java.util.UUID uuid) {
        skull.setSkullType(SkullType.PLAYER);
        skull.setOwner(Bukkit.getOfflinePlayer(uuid).getName());
    }

    public void setMaxHealth(Player player, int health) {
        player.setMaxHealth(health);
    }

    public void spawnDragon(World world, Location loc) {
        WorldServer w = ((CraftWorld) world).getHandle();
        EntityEnderDragon dragon = new EntityEnderDragon(w);
        dragon.setLocation(loc.getX(), loc.getY(), loc.getZ(), w.random.nextFloat() * 360.0F, 0.0F);
        w.addEntity(dragon);
    }


    public Entity spawnFallingBlock(Location loc, Material mat, boolean damage) {
        FallingBlock block = loc.getWorld().spawnFallingBlock(loc, mat, (byte) 0);
        block.setDropItem(false);
        EntityFallingBlock fb = ((org.bukkit.craftbukkit.v1_8_R2.entity.CraftFallingSand) block).getHandle();
        fb.a(damage);
        return block;
    }

    public void playEnderChestAction(Block block, boolean open) {
        Location location = block.getLocation();
        WorldServer world = ((CraftWorld) location.getWorld()).getHandle();
        BlockPosition position = new BlockPosition(location.getX(), location.getY(), location.getZ());
        TileEntityEnderChest ec = (TileEntityEnderChest) world.getTileEntity(position);
        world.playBlockAction(position, ec.w(), 1, open ? 1 : 0);
    }

    public void setEntityTarget(Entity ent, Player player) {
        EntityCreature entity = (EntityCreature) ((org.bukkit.craftbukkit.v1_8_R2.entity.CraftEntity) ent).getHandle();
        entity.setGoalTarget(((CraftPlayer) player).getHandle(), null, false);
    }

    public void updateSkull(SkullMeta meta1, Player player) {
        meta1.setOwner(player.getName());
    }

    public ChunkGenerator getChunkGenerator() {
        return new ChunkGenerator() {
            @Override
            public List<BlockPopulator> getDefaultPopulators(World world) {
                return Arrays.asList(new BlockPopulator[0]);
            }

            @Override
            public boolean canSpawn(World world, int x, int z) {
                return true;
            }

            @Override
            public byte[] generate(World world, Random random, int x, int z) {
                return new byte[32768];
            }

            @Override
            public Location getFixedSpawnLocation(World world, Random random) {
                return new Location(world, 0.0D, 64.0D, 0.0D);
            }
        };
    }

    public boolean checkMaterial(FallingBlock fb, Material mat) {
        return fb.getMaterial().equals(mat);
    }

    public org.bukkit.scoreboard.Objective getNewObjective(Scoreboard scoreboard, String criteria, String DisplayName) {
        return scoreboard.registerNewObjective(DisplayName, criteria);
    }

    public void setGameRule(World world, String rule, String bool) {
        world.setGameRuleValue(rule, bool);
    }

    public boolean headCheck(Block h1) {
        return h1.getType() == Material.SKULL;
    }

    public org.bukkit.inventory.ItemStack getBlankPlayerHead() {
        return new org.bukkit.inventory.ItemStack(Material.SKULL_ITEM, 1, (short) 3);
    }

    public int getVersion() {
        return 8;
    }

    public org.bukkit.inventory.ItemStack getMaterial(String item) {
        if (item.equalsIgnoreCase("SKULL_ITEM")) {
            return new org.bukkit.inventory.ItemStack(Material.SKULL_ITEM, 1, (short) 1);
        }
        return new org.bukkit.inventory.ItemStack(Material.valueOf(item), 1);
    }


    public org.bukkit.inventory.ItemStack getColorItem(String mat, byte color) {
        if (mat.equalsIgnoreCase("wool"))
            return new org.bukkit.inventory.ItemStack(Material.WOOL, 1,  color);
        if (mat.equalsIgnoreCase("glass"))
            return new org.bukkit.inventory.ItemStack(Material.STAINED_GLASS, 1,  color);
        if (mat.equalsIgnoreCase("banner")) {
            return new org.bukkit.inventory.ItemStack(Material.BANNER, 1,  color);
        }
        return new org.bukkit.inventory.ItemStack(Material.STAINED_GLASS, 1,  color);
    }


    public void setBlockWithColor(World world, int x, int y, int z, Material mat, byte cByte) {
        world.getBlockAt(x, y, z).setType(mat);
        world.getBlockAt(x, y, z).setData(cByte);
    }


    public void deleteCache() {
    }


    public Block getHitBlock(ProjectileHitEvent event) {
        BlockIterator iterator = new BlockIterator(event.getEntity().getWorld(), event.getEntity().getLocation().toVector(), event.getEntity().getVelocity().normalize(), 0.0D, 4);
        Block hitBlock = null;
        while (iterator.hasNext()) {
            hitBlock = iterator.next();
            if (hitBlock.getType() != Material.AIR) {
                break;
            }
        }
        return hitBlock;
    }

    @Override
    public void sendJSON(Player sender, String json) {
        final IChatBaseComponent icbc = IChatBaseComponent.ChatSerializer.a(json);
        final PacketPlayOutChat chat = new PacketPlayOutChat(icbc);
        ((CraftPlayer) sender).getHandle().playerConnection.sendPacket(chat);
    }

    @Override
    public boolean isHoldingTotem(Player player) {
        return false;
    }

    @Override
    public void applyTotemEffect(Player player) {

    }
}
