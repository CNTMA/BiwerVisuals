package com.biwer.biwervisuals;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * AutoTotem: автоматически кладёт тотем бессмертия в левую руку (offhand).
 *
 * Работает через штатный клиентский метод MultiPlayerGameMode#handleContainerInput
 * с ContainerInput.SWAP и кнопкой 40 (слот offhand). Миксины не нужны.
 */
public final class AutoTotem {
    private static int cooldown = 0;

    private AutoTotem() {
    }

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.level == null || client.gameMode == null) return;

            if (!BiwerVisualsClient.CONFIG.autoTotemEnabled) {
                cooldown = 0;
                return;
            }

            if (cooldown > 0) {
                cooldown--;
                return;
            }

            // Не трогаем инвентарь, пока открыт любой экран (сундук, крафт, чат и т.д.).
            if (client.screen != null) return;

            // Работаем только со своим инвентарём (containerId = 0, слоты InventoryMenu).
            if (client.player.containerMenu != client.player.inventoryMenu) return;
            if (!client.player.isAlive() || client.player.isSpectator()) return;

            Inventory inventory = client.player.getInventory();

            // В левой руке уже тотем — ничего не делаем.
            ItemStack offhand = inventory.getItem(Inventory.SLOT_OFFHAND);
            if (!offhand.isEmpty() && offhand.getItem() == Items.TOTEM_OF_UNDYING) return;

            int inventorySlot = findTotemSlot(inventory);
            if (inventorySlot == -1) return;

            int menuSlot = toMenuSlot(inventorySlot);
            if (menuSlot == -1) return;

            // SWAP + кнопка 40 меняет местами выбранный слот с offhand.
            client.gameMode.handleContainerInput(
                    client.player.containerMenu.containerId,
                    menuSlot,
                    Inventory.SLOT_OFFHAND,
                    ContainerInput.SWAP,
                    client.player
            );

            // Небольшая задержка, чтобы сервер успел обработать обмен.
            cooldown = Math.max(1, Math.min(20, BiwerVisualsClient.CONFIG.autoTotemDelay));
        });
    }

    private static int findTotemSlot(Inventory inventory) {
        // 0..8 — хотбар, 9..35 — основной инвентарь.
        for (int i = 0; i < Inventory.INVENTORY_SIZE; i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && stack.getItem() == Items.TOTEM_OF_UNDYING) {
                return i;
            }
        }
        return -1;
    }

    private static int toMenuSlot(int inventorySlot) {
        if (inventorySlot >= 0 && inventorySlot < 9) {
            return inventorySlot + 36; // хотбар: InventoryMenu.USE_ROW_SLOT_START (36..44)
        }
        if (inventorySlot >= 9 && inventorySlot < Inventory.INVENTORY_SIZE) {
            return inventorySlot;      // основной инвентарь: 9..35
        }
        return -1;
    }

    public static void toggle(Minecraft client) {
        BiwerVisualsClient.CONFIG.autoTotemEnabled = !BiwerVisualsClient.CONFIG.autoTotemEnabled;
        cooldown = 0;

        if (client.player != null) {
            client.player.sendSystemMessage(Component.literal(
                    "§e[AutoTotem] " + (BiwerVisualsClient.CONFIG.autoTotemEnabled ? "§aВКЛ" : "§cВЫКЛ")
            ));
        }
    }
}
