package shead.armorhotkeys.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public class ArmorHotkeysClient implements ClientModInitializer {

    public static final KeyBinding.Category KEY_CATEGORY = KeyBinding.Category.create(Identifier.of("key.category.sheadarmorhotkeys"));
    private static KeyBinding EQUIP_ARMOR, SWAP_CHEST, ELYTRA_ONLY, ELYTRA_ARMOR, UNEQUIP_ALL;

    private static final Map<EquipmentSlot, Integer> armorMemory = new HashMap<>();
    private static final MinecraftClient client = MinecraftClient.getInstance();

    // Player inventory slot IDs (when the inventory is closed)
    private static final int ARMOR_START_SLOT = 5; // Index of the first armor slot in the player's inventory
    private static final int PLAYER_INVENTORY_START = 9; // Start of the player's inventory

    @Override
    public void onInitializeClient() {
        EQUIP_ARMOR = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.armorhotkeys.equip",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Z,
                KEY_CATEGORY
        ));
        SWAP_CHEST = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.armorhotkeys.swap_chest",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_X,
                KEY_CATEGORY
        ));
        ELYTRA_ONLY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.armorhotkeys.elytra_only",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_C,
                KEY_CATEGORY
        ));
        ELYTRA_ARMOR = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.armorhotkeys.elytra_armor",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                KEY_CATEGORY
        ));
        UNEQUIP_ALL = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.armorhotkeys.unequip_all",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                KEY_CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.interactionManager == null) return;

            if (EQUIP_ARMOR.wasPressed()) equipAllArmor();
            if (UNEQUIP_ALL.wasPressed()) unequipAll();
            if (SWAP_CHEST.wasPressed()) swapChest();
            if (ELYTRA_ONLY.wasPressed()) elytraOnly();
            if (ELYTRA_ARMOR.wasPressed()) elytraArmor();
        });
    }

    /**
     * Equips all available armor from the inventory to the appropriate slots.
     * Specifics: for the chest slot (CHEST), prefers chestplates over elytras,
     * but if elytras are equipped and a chestplate is available - performs a direct swap.
     */
    private void equipAllArmor() {
        if (client.player == null || client.interactionManager == null) return;

        ensureEmptyCursor();

        // For all armor slots (except CHEST)
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (!slot.isArmorSlot() || slot == EquipmentSlot.CHEST) continue;

            ItemStack currentlyEquipped = client.player.getEquippedStack(slot);
            if (currentlyEquipped.isEmpty()) {
                // Find suitable armor in the inventory
                for (int i = PLAYER_INVENTORY_START; i < PLAYER_INVENTORY_START + 27; i++) {
                    ItemStack stack = client.player.currentScreenHandler.getSlot(i).getStack();
                    if (!stack.isEmpty() && getSlot(stack) == slot) {
                        // Move the item to the armor slot
                        clickSlot(i, 0, SlotActionType.PICKUP);
                        clickSlot(getArmorSlotId(slot), 0, SlotActionType.PICKUP);
                        armorMemory.put(slot, i);
                        ensureEmptyCursor();
                        break;
                    }
                }
            }
        }

        // Handle the CHEST slot separately - with direct swap as in swapChest()
        ItemStack chestEquipped = client.player.getEquippedStack(EquipmentSlot.CHEST);

        if (chestEquipped.isEmpty()) {
            // If the slot is empty, first try to equip a chestplate
            boolean foundChestplate = false;
            for (int i = PLAYER_INVENTORY_START; i < PLAYER_INVENTORY_START + 27; i++) {
                ItemStack stack = client.player.currentScreenHandler.getSlot(i).getStack();
                if (!stack.isEmpty() && getSlot(stack) == EquipmentSlot.CHEST && stack.getItem() != Items.ELYTRA) {
                    clickSlot(i, 0, SlotActionType.PICKUP);
                    clickSlot(getArmorSlotId(EquipmentSlot.CHEST), 0, SlotActionType.PICKUP);
                    armorMemory.put(EquipmentSlot.CHEST, i);
                    ensureEmptyCursor();
                    foundChestplate = true;
                    break;
                }
            }

            // If no chestplate is found, try to equip elytras
            if (!foundChestplate) {
                for (int i = PLAYER_INVENTORY_START; i < PLAYER_INVENTORY_START + 27; i++) {
                    ItemStack stack = client.player.currentScreenHandler.getSlot(i).getStack();
                    if (!stack.isEmpty() && stack.getItem() == Items.ELYTRA) {
                        clickSlot(i, 0, SlotActionType.PICKUP);
                        clickSlot(getArmorSlotId(EquipmentSlot.CHEST), 0, SlotActionType.PICKUP);
                        armorMemory.put(EquipmentSlot.CHEST, i);
                        ensureEmptyCursor();
                        break;
                    }
                }
            }
        } else if (chestEquipped.getItem() == Items.ELYTRA) {
            // If elytras are equipped, look for a chestplate to swap (direct exchange)
            boolean foundChestplate = false;
            for (int i = PLAYER_INVENTORY_START; i < PLAYER_INVENTORY_START + 27; i++) {
                ItemStack stack = client.player.currentScreenHandler.getSlot(i).getStack();
                if (!stack.isEmpty() && getSlot(stack) == EquipmentSlot.CHEST && stack.getItem() != Items.ELYTRA) {
                    // Direct swap: elytras <-> chestplate
                    // First, pick up the chestplate from the inventory
                    clickSlot(i, 0, SlotActionType.PICKUP);
                    // Then place it in the armor slot (elytras will move to the cursor)
                    clickSlot(getArmorSlotId(EquipmentSlot.CHEST), 0, SlotActionType.PICKUP);
                    // Now elytras are in the cursor, place them in the slot where the chestplate was
                    clickSlot(i, 0, SlotActionType.PICKUP);

                    armorMemory.put(EquipmentSlot.CHEST, i);
                    ensureEmptyCursor();
                    foundChestplate = true;
                    break;
                }
            }

            // If no chestplate is found, leave the elytras equipped
        }
        // If a chestplate is already equipped - do nothing
    }

    /**
     * Unequips all armor from the player and moves it to the inventory.
     * Uses saved memory slots or searches for empty ones.
     * If there is no space - shows a message.
     */
    private void unequipAll() {
        if (client.player == null || client.interactionManager == null) return;

        ensureEmptyCursor();

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (!slot.isArmorSlot()) continue;

            ItemStack equipped = client.player.getEquippedStack(slot);
            if (equipped.isEmpty()) continue;

            int target = armorMemory.getOrDefault(slot, -1);

            // If there's no saved slot or it's occupied, search for an empty slot
            if (target == -1 || !client.player.currentScreenHandler.getSlot(target).getStack().isEmpty()) {
                target = findEmptySlot();
            }

            if (target == -1) {
                client.player.sendMessage(Text.translatable("message.armorhotkeys.no_inventory_space"), true);
                continue;
            }

            // Unequip the armor
            clickSlot(getArmorSlotId(slot), 0, SlotActionType.PICKUP);
            clickSlot(target, 0, SlotActionType.PICKUP);
            ensureEmptyCursor();
            armorMemory.put(slot, target);
        }
    }

    /**
     * Swaps the contents of the chest slot (CHEST) between elytras and chestplates.
     * If the slot is empty - first searches for a chestplate, then elytras.
     * If something is equipped - searches for an alternative item and performs a direct swap.
     */
    private void swapChest() {
        if (client.player == null || client.interactionManager == null) return;

        ensureEmptyCursor();

        ItemStack currentlyEquipped = client.player.getEquippedStack(EquipmentSlot.CHEST);

        if (currentlyEquipped.isEmpty()) {
            // If the slot is empty, first try to equip a chestplate
            boolean foundChestplate = false;
            for (int i = PLAYER_INVENTORY_START; i < PLAYER_INVENTORY_START + 27; i++) {
                ItemStack stack = client.player.currentScreenHandler.getSlot(i).getStack();
                if (!stack.isEmpty() && getSlot(stack) == EquipmentSlot.CHEST && stack.getItem() != Items.ELYTRA) {
                    clickSlot(i, 0, SlotActionType.PICKUP);
                    clickSlot(getArmorSlotId(EquipmentSlot.CHEST), 0, SlotActionType.PICKUP);
                    armorMemory.put(EquipmentSlot.CHEST, i);
                    ensureEmptyCursor();
                    foundChestplate = true;
                    break;
                }
            }

            // If no chestplate is found, try to equip elytras
            if (!foundChestplate) {
                for (int i = PLAYER_INVENTORY_START; i < PLAYER_INVENTORY_START + 27; i++) {
                    ItemStack stack = client.player.currentScreenHandler.getSlot(i).getStack();
                    if (!stack.isEmpty() && stack.getItem() == Items.ELYTRA) {
                        clickSlot(i, 0, SlotActionType.PICKUP);
                        clickSlot(getArmorSlotId(EquipmentSlot.CHEST), 0, SlotActionType.PICKUP);
                        armorMemory.put(EquipmentSlot.CHEST, i);
                        ensureEmptyCursor();
                        break;
                    }
                }
            }
        } else {
            // If something is equipped, search for an alternative item to swap
            boolean foundAlternative = false;

            for (int i = PLAYER_INVENTORY_START; i < PLAYER_INVENTORY_START + 27; i++) {
                ItemStack stack = client.player.currentScreenHandler.getSlot(i).getStack();

                // If elytras are equipped, look for any chestplate
                if (currentlyEquipped.getItem() == Items.ELYTRA) {
                    if (!stack.isEmpty() && getSlot(stack) == EquipmentSlot.CHEST && stack.getItem() != Items.ELYTRA) {
                        // Direct swap: elytras <-> chestplate
                        // First, pick up the chestplate from the inventory
                        clickSlot(i, 0, SlotActionType.PICKUP);
                        // Then place it in the armor slot (elytras will move to the cursor)
                        clickSlot(getArmorSlotId(EquipmentSlot.CHEST), 0, SlotActionType.PICKUP);
                        // Now elytras are in the cursor, place them in the slot where the chestplate was
                        clickSlot(i, 0, SlotActionType.PICKUP);

                        armorMemory.put(EquipmentSlot.CHEST, i);
                        ensureEmptyCursor();
                        foundAlternative = true;
                        break;
                    }
                }
                // If a chestplate is equipped, look for elytras
                else {
                    if (!stack.isEmpty() && stack.getItem() == Items.ELYTRA) {
                        // Direct swap: chestplate <-> elytras
                        // First, pick up the elytras from the inventory
                        clickSlot(i, 0, SlotActionType.PICKUP);
                        // Then place them in the armor slot (chestplate will move to the cursor)
                        clickSlot(getArmorSlotId(EquipmentSlot.CHEST), 0, SlotActionType.PICKUP);
                        // Now the chestplate is in the cursor, place it in the slot where the elytras were
                        clickSlot(i, 0, SlotActionType.PICKUP);

                        armorMemory.put(EquipmentSlot.CHEST, i);
                        ensureEmptyCursor();
                        foundAlternative = true;
                        break;
                    }
                }
            }

            // If no alternative is found, simply unequip what is equipped
            if (!foundAlternative) {
                int emptySlot = findEmptySlot();
                if (emptySlot != -1) {
                    clickSlot(getArmorSlotId(EquipmentSlot.CHEST), 0, SlotActionType.PICKUP);
                    clickSlot(emptySlot, 0, SlotActionType.PICKUP);
                    armorMemory.put(EquipmentSlot.CHEST, emptySlot);
                    ensureEmptyCursor();
                }
            }
        }
    }

    /**
     * Equips only elytras (unequipping all other armor).
     * If elytras are already equipped - removes the rest of the armor.
     * If chestplates are equipped - performs a direct swap.
     * If no elytras are available - shows a message.
     */
    private void elytraOnly() {
        if (client.player == null || client.interactionManager == null) return;

        ensureEmptyCursor();

        // Check if elytras are already equipped
        ItemStack chestEquipped = client.player.getEquippedStack(EquipmentSlot.CHEST);
        boolean alreadyHasElytra = !chestEquipped.isEmpty() && chestEquipped.getItem() == Items.ELYTRA;

        // Look for elytras in the inventory
        int elytraSlot = -1;

        if (alreadyHasElytra) {
            // If elytras are already equipped, use the saved slot or find the current elytra slot
            elytraSlot = armorMemory.getOrDefault(EquipmentSlot.CHEST, -1);

            // If not found in memory, search for elytras in the inventory (they might already be equipped)
            if (elytraSlot == -1) {
                // Elytras are already equipped but not remembered in memory
                // We'll find an empty slot for them later
            }
        } else {
            // Look for elytras in the inventory
            for (int i = PLAYER_INVENTORY_START; i < PLAYER_INVENTORY_START + 27; i++) {
                ItemStack stack = client.player.currentScreenHandler.getSlot(i).getStack();
                if (!stack.isEmpty() && stack.getItem() == Items.ELYTRA) {
                    elytraSlot = i;
                    break;
                }
            }

            // Also check the hotbar
            if (elytraSlot == -1) {
                for (int i = 0; i < 9; i++) {
                    ItemStack stack = client.player.currentScreenHandler.getSlot(i).getStack();
                    if (!stack.isEmpty() && stack.getItem() == Items.ELYTRA) {
                        elytraSlot = i;
                        break;
                    }
                }
            }
        }

        // If there are no elytras in the inventory or equipped
        if (elytraSlot == -1 && !alreadyHasElytra) {
            client.player.sendMessage(Text.translatable("message.armorhotkeys.no_elytra"), true);
            return;
        }

        if (alreadyHasElytra) {
            // If elytras are already equipped, simply unequip all other armor
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (!slot.isArmorSlot() || slot == EquipmentSlot.CHEST) continue;

                ItemStack equipped = client.player.getEquippedStack(slot);
                if (!equipped.isEmpty()) {
                    int emptySlot = findEmptySlot();
                    if (emptySlot != -1) {
                        clickSlot(getArmorSlotId(slot), 0, SlotActionType.PICKUP);
                        clickSlot(emptySlot, 0, SlotActionType.PICKUP);
                        ensureEmptyCursor();
                    }
                }
            }
        } else {
            // If a chestplate is equipped or the slot is empty, perform a direct swap

            // 1. First, unequip all armor except CHEST (if any)
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (!slot.isArmorSlot() || slot == EquipmentSlot.CHEST) continue;

                ItemStack equipped = client.player.getEquippedStack(slot);
                if (!equipped.isEmpty()) {
                    // Look for a slot for this armor in memory or an empty slot
                    int targetSlot = armorMemory.getOrDefault(slot, -1);
                    if (targetSlot == -1 || !client.player.currentScreenHandler.getSlot(targetSlot).getStack().isEmpty()) {
                        targetSlot = findEmptySlot();
                    }

                    if (targetSlot != -1) {
                        clickSlot(getArmorSlotId(slot), 0, SlotActionType.PICKUP);
                        clickSlot(targetSlot, 0, SlotActionType.PICKUP);
                        armorMemory.put(slot, targetSlot);
                        ensureEmptyCursor();
                    }
                }
            }

            // 2. Handle the CHEST slot - direct swap between chestplate and elytras
            ItemStack currentlyChest = client.player.getEquippedStack(EquipmentSlot.CHEST);

            if (currentlyChest.isEmpty()) {
                // If the CHEST slot is empty, simply equip the elytras
                clickSlot(elytraSlot, 0, SlotActionType.PICKUP);
                clickSlot(getArmorSlotId(EquipmentSlot.CHEST), 0, SlotActionType.PICKUP);
                armorMemory.put(EquipmentSlot.CHEST, elytraSlot);
                ensureEmptyCursor();
            } else if (currentlyChest.getItem() != Items.ELYTRA) {
                // If a chestplate is equipped, perform a direct swap
                // Direct swap: chestplate <-> elytras
                // First, pick up the elytras from the inventory
                clickSlot(elytraSlot, 0, SlotActionType.PICKUP);
                // Then place them in the armor slot (chestplate will move to the cursor)
                clickSlot(getArmorSlotId(EquipmentSlot.CHEST), 0, SlotActionType.PICKUP);
                // Now the chestplate is in the cursor, place it in the slot where the elytras were
                clickSlot(elytraSlot, 0, SlotActionType.PICKUP);

                armorMemory.put(EquipmentSlot.CHEST, elytraSlot);
                ensureEmptyCursor();
            }
            // If elytras are already equipped - do nothing (this case is handled above)
        }
    }

    /**
     * Equips elytras along with the rest of the armor (helmet, leggings, boots).
     * First handles elytras, then equips the remaining armor pieces.
     */
    private void elytraArmor() {
        if (client.player == null || client.interactionManager == null) return;

        ensureEmptyCursor();

        // 1. First, find elytras
        int elytraSlot = -1;
        boolean alreadyHasElytra = false;

        ItemStack chestEquipped = client.player.getEquippedStack(EquipmentSlot.CHEST);
        if (!chestEquipped.isEmpty() && chestEquipped.getItem() == Items.ELYTRA) {
            alreadyHasElytra = true;
            elytraSlot = armorMemory.getOrDefault(EquipmentSlot.CHEST, -1);
        }

        if (!alreadyHasElytra) {
            // Look for elytras in the inventory
            for (int i = PLAYER_INVENTORY_START; i < PLAYER_INVENTORY_START + 27; i++) {
                ItemStack stack = client.player.currentScreenHandler.getSlot(i).getStack();
                if (!stack.isEmpty() && stack.getItem() == Items.ELYTRA) {
                    elytraSlot = i;
                    break;
                }
            }

            // Also check the hotbar
            if (elytraSlot == -1) {
                for (int i = 0; i < 9; i++) {
                    ItemStack stack = client.player.currentScreenHandler.getSlot(i).getStack();
                    if (!stack.isEmpty() && stack.getItem() == Items.ELYTRA) {
                        elytraSlot = i;
                        break;
                    }
                }
            }
        }

        // If there are no elytras in the inventory or equipped
        if (elytraSlot == -1 && !alreadyHasElytra) {
            client.player.sendMessage(Text.translatable("message.armorhotkeys.no_elytra"), true);
            return;
        }

        // 2. Handle elytras (if they are not already equipped)
        if (!alreadyHasElytra) {
            // If the CHEST slot is not empty (a chestplate is equipped), perform a direct swap
            if (!chestEquipped.isEmpty() && chestEquipped.getItem() != Items.ELYTRA) {
                // Direct swap: chestplate <-> elytras
                clickSlot(elytraSlot, 0, SlotActionType.PICKUP);
                clickSlot(getArmorSlotId(EquipmentSlot.CHEST), 0, SlotActionType.PICKUP);
                clickSlot(elytraSlot, 0, SlotActionType.PICKUP);
                armorMemory.put(EquipmentSlot.CHEST, elytraSlot);
                ensureEmptyCursor();
            } else if (chestEquipped.isEmpty()) {
                // If the CHEST slot is empty, simply equip the elytras
                clickSlot(elytraSlot, 0, SlotActionType.PICKUP);
                clickSlot(getArmorSlotId(EquipmentSlot.CHEST), 0, SlotActionType.PICKUP);
                armorMemory.put(EquipmentSlot.CHEST, elytraSlot);
                ensureEmptyCursor();
            }
        }

        // 3. Now equip the rest of the armor (helmet, leggings, boots)
        // Handle HEAD, LEGS, FEET similarly to equipAllArmor()
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack currentlyEquipped = client.player.getEquippedStack(slot);
            if (currentlyEquipped.isEmpty()) {
                // Find suitable armor in the inventory
                for (int i = PLAYER_INVENTORY_START; i < PLAYER_INVENTORY_START + 27; i++) {
                    ItemStack stack = client.player.currentScreenHandler.getSlot(i).getStack();
                    if (!stack.isEmpty() && getSlot(stack) == slot) {
                        // Move the item to the armor slot
                        clickSlot(i, 0, SlotActionType.PICKUP);
                        clickSlot(getArmorSlotId(slot), 0, SlotActionType.PICKUP);
                        armorMemory.put(slot, i);
                        ensureEmptyCursor();
                        break;
                    }
                }
            }
        }
    }

    /**
     * Finds the first empty slot in the player's inventory.
     * First searches the main inventory, then the hotbar.
     * @return ID of an empty slot, or -1 if there is no space
     */
    private int findEmptySlot() {
        if (client.player == null) return -1;

        // Look for an empty slot from left to right, top to bottom in the main inventory
        for (int i = PLAYER_INVENTORY_START; i < PLAYER_INVENTORY_START + 27; i++) {
            if (client.player.currentScreenHandler.getSlot(i).getStack().isEmpty()) {
                return i;
            }
        }

        // If there's no space in the main inventory, search the hotbar from left to right
        for (int i = 0; i < 9; i++) {
            if (client.player.currentScreenHandler.getSlot(i).getStack().isEmpty()) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Returns the armor slot ID in the container (player inventory).
     * @param slot the armor slot type
     * @return the slot ID in the inventory, or -1 if the slot is not an armor slot
     */
    private int getArmorSlotId(EquipmentSlot slot) {
        switch (slot) {
            case FEET: return ARMOR_START_SLOT + 3; // 8
            case LEGS: return ARMOR_START_SLOT + 2; // 7
            case CHEST: return ARMOR_START_SLOT + 1; // 6
            case HEAD: return ARMOR_START_SLOT; // 5
            default: return -1;
        }
    }

    /**
     * Clicks a slot in the player's inventory.
     * @param slotId the slot ID to click
     * @param button the mouse button (0 - left, 1 - right)
     * @param actionType the action type (PICKUP, QUICK_MOVE, etc.)
     */
    private void clickSlot(int slotId, int button, SlotActionType actionType) {
        if (client.interactionManager != null && client.player != null) {
            client.interactionManager.clickSlot(
                    client.player.currentScreenHandler.syncId,
                    slotId,
                    button,
                    actionType,
                    client.player
            );
        }
    }

    /**
     * Ensures the cursor is empty (no item is "held" in hand).
     * If there is something in the cursor, places it in the first available slot.
     */
    private void ensureEmptyCursor() {
        if (client.player != null && client.player.currentScreenHandler != null) {
            // If there is something in the cursor, place it in the first empty slot
            if (!client.player.currentScreenHandler.getCursorStack().isEmpty()) {
                int emptySlot = findEmptySlot();
                if (emptySlot != -1) {
                    clickSlot(emptySlot, 0, SlotActionType.PICKUP);
                }
            }
        }
    }

    /**
     * Determines which armor slot a given item should be placed in.
     * @param stack the item to check
     * @return the corresponding EquipmentSlot, or null if it's not armor
     */
    private EquipmentSlot getSlot(ItemStack stack) {
        Item item = stack.getItem();

        // Elytras
        if (item == Items.ELYTRA) return EquipmentSlot.CHEST;

        // Helmets
        if (item == Items.LEATHER_HELMET ||
                item == Items.IRON_HELMET ||
                item == Items.GOLDEN_HELMET ||
                item == Items.DIAMOND_HELMET ||
                item == Items.NETHERITE_HELMET) return EquipmentSlot.HEAD;

        // Chestplates
        if (item == Items.LEATHER_CHESTPLATE ||
                item == Items.IRON_CHESTPLATE ||
                item == Items.GOLDEN_CHESTPLATE ||
                item == Items.DIAMOND_CHESTPLATE ||
                item == Items.NETHERITE_CHESTPLATE) return EquipmentSlot.CHEST;

        // Leggings
        if (item == Items.LEATHER_LEGGINGS ||
                item == Items.IRON_LEGGINGS ||
                item == Items.GOLDEN_LEGGINGS ||
                item == Items.DIAMOND_LEGGINGS ||
                item == Items.NETHERITE_LEGGINGS) return EquipmentSlot.LEGS;

        // Boots
        if (item == Items.LEATHER_BOOTS ||
                item == Items.IRON_BOOTS ||
                item == Items.GOLDEN_BOOTS ||
                item == Items.DIAMOND_BOOTS ||
                item == Items.NETHERITE_BOOTS) return EquipmentSlot.FEET;

        return null;
    }
}