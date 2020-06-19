package com.oakleyplugins.treefeller;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;


import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public final class TreeFeller extends JavaPlugin implements Listener {
    public static ArrayList<Material> logs = new ArrayList<>();
    public static ArrayList<Material> Leaves = new ArrayList<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        Material[] mat = Material.values();
        for (Material m : mat) {
            if (m.toString().contains("LOG") & !(m.toString().contains("STRIPPED"))) {
                logs.add(m);
            }
            if (m.toString().contains("LEAVES")) {
                Leaves.add(m);
            }
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if (e.getPlayer().getGameMode() != GameMode.CREATIVE) {
            Material type = e.getBlock().getType();
            if (logs.contains(type)) {
                Block block = e.getBlock();
                ArrayList<Block> blocks = new ArrayList<>(check(around(block.getLocation())));
                do {
                    ArrayList<Block> temp = new ArrayList<>(blocks);
                    for (Block b : temp) {
                        for (Block b1 : check(around(b.getLocation()))) {
                            if (!(blocks.contains(b1))) {
                                blocks.add(b1);
                            }
                        }
                    }
                } while (!checkList(blocks));
                boolean hasLeaves = false;
                int delay = 1;
                for (Block b : blocks) {
                    if (Leaves.contains(b.getType())) {
                        hasLeaves = true;
                    }
                }
                if (hasLeaves) {
                    for (Block b : blocks) {
                        if (!(Leaves.contains(b.getType()))) {
                            Bukkit.getScheduler().runTaskLater(this, () -> {
                                if (e.getPlayer().getInventory().firstEmpty() != -1) {
                                    e.getPlayer().getInventory().addItem(new ItemStack(b.getType()));
                                } else {
                                    e.getPlayer().getWorld().dropItemNaturally(e.getPlayer().getLocation(), new ItemStack(b.getType()));
                                }
                                e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 1f);
                                b.setType(Material.AIR);
                            }, delay);
                            delay++;
                        }
                    }
                }
                if (hasLeaves) {
                    e.setDropItems(false);
                }
            }
        }
    }

    public static ArrayList<Block> around(Location location) {
        ArrayList<Block> b = new ArrayList<>();
        for (int x = location.getBlockX() - 1; x <= location.getBlockX() + 1; x++) {
            for (int y = location.getBlockY() - 1; y <= location.getBlockY() + 1; y++) {
                for (int z = location.getBlockZ() - 1; z <= location.getBlockZ() + 1; z++) {
                    b.add(location.getWorld().getBlockAt(x, y, z));
                }
            }
        }
        return b;
    }

    public static ArrayList<Block> check(ArrayList<Block> b) {
        ArrayList<Block> all = new ArrayList<>();
        for (Block block : b) {
            ArrayList<Block> log = new ArrayList<>();
            ArrayList<Block> Leaf = new ArrayList<>();
            if (logs.contains(block.getType())) {
                log.add(block);
            } else if (Leaves.contains(block.getType())) {
                Leaves data = (Leaves) block.getBlockData();
                if (!(data.isPersistent()) & data.getDistance() < 2) {
                    Leaf.add(block);
                }
            }
            all.addAll(log);
            all.addAll(Leaf);
        }
        return all;
    }

    public static boolean checkList(ArrayList<Block> blocks) {
        ArrayList<Block> temp = new ArrayList<>();
        for (Block b : blocks) {
            temp.addAll(check(around(b.getLocation())));
        }
        return blocks.containsAll(temp);
    }
}
