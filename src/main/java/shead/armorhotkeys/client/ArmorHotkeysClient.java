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

    private static final int ARMOR_START_SLOT = 5;
    private static final int PLAYER_INVENTORY_START = 9;

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

    private void equipAllArmor() {
        if (client.player == null || client.interactionManager == null) return;

        ensureEmptyCursor();

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (!slot.isArmorSlot() || slot == EquipmentSlot.CHEST) continue;

            ItemStack currentlyEquipped = client.player.getEquippedStack(slot);
            if (currentlyEquipped.isEmpty()) {
                for (int i = PLAYER_INVENTORY_START; i < PLAYER_INVENTORY_START + 27; i++) {
                    ItemStack stack = client.player.currentScreenHandler.getSlot(i).getStack();
                    if (!stack.isEmpty() && getSlot(stack) == slot) {
                        clickSlot(i, 0, SlotActionType.PICKUP);
                        clickSlot(getArmorSlotId(slot), 0, SlotActionType.PICKUP);
                        armorMemory.put(slot, i);
                        ensureEmptyCursor();
                        break;
                    }
                }
            }
        }

        ItemStack chestEquipped = client.player.getEquippedStack(EquipmentSlot.CHEST);

        if (chestEquipped.isEmpty()) {
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
            boolean foundChestplate = false;
            for (int i = PLAYER_INVENTORY_START; i < PLAYER_INVENTORY_START + 27; i++) {
                ItemStack stack = client.player.currentScreenHandler.getSlot(i).getStack();
                if (!stack.isEmpty() && getSlot(stack) == EquipmentSlot.CHEST && stack.getItem() != Items.ELYTRA) {
                    clickSlot(i, 0, SlotActionType.PICKUP);
                    clickSlot(getArmorSlotId(EquipmentSlot.CHEST), 0, SlotActionType.PICKUP);
                    clickSlot(i, 0, SlotActionType.PICKUP);

                    armorMemory.put(EquipmentSlot.CHEST, i);
                    ensureEmptyCursor();
                    foundChestplate = true;
                    break;
                }
            }
        }
    }

    private void unequipAll() {
        if (client.player == null || client.interactionManager == null) return;

        ensureEmptyCursor();

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (!slot.isArmorSlot()) continue;

            ItemStack equipped = client.player.getEquippedStack(slot);
            if (equipped.isEmpty()) continue;

            int target = armorMemory.getOrDefault(slot, -1);

            if (target == -1 || !client.player.currentScreenHandler.getSlot(target).getStack().isEmpty()) {
                target = findEmptySlot();
            }

            if (target == -1) {
                client.player.sendMessage(Text.translatable("message.armorhotkeys.no_inventory_space"), true);
                continue;
            }

            clickSlot(getArmorSlotId(slot), 0, SlotActionType.PICKUP);
            clickSlot(target, 0, SlotActionType.PICKUP);
            ensureEmptyCursor();
            armorMemory.put(slot, target);
        }
    }

    private void swapChest() {
        if (client.player == null || client.interactionManager == null) return;

        ensureEmptyCursor();

        ItemStack currentlyEquipped = client.player.getEquippedStack(EquipmentSlot.CHEST);

        if (currentlyEquipped.isEmpty()) {
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
            boolean foundAlternative = false;

            for (int i = PLAYER_INVENTORY_START; i < PLAYER_INVENTORY_START + 27; i++) {
                ItemStack stack = client.player.currentScreenHandler.getSlot(i).getStack();

                if (currentlyEquipped.getItem() == Items.ELYTRA) {
                    if (!stack.isEmpty() && getSlot(stack) == EquipmentSlot.CHEST && stack.getItem() != Items.ELYTRA) {
                        clickSlot(i, 0, SlotActionType.PICKUP);
                        clickSlot(getArmorSlotId(EquipmentSlot.CHEST), 0, SlotActionType.PICKUP);
                        clickSlot(i, 0, SlotActionType.PICKUP);

                        armorMemory.put(EquipmentSlot.CHEST, i);
                        ensureEmptyCursor();
                        foundAlternative = true;
                        break;
                    }
                }
                else {
                    if (!stack.isEmpty() && stack.getItem() == Items.ELYTRA) {
                        clickSlot(i, 0, SlotActionType.PICKUP);
                        clickSlot(getArmorSlotId(EquipmentSlot.CHEST), 0, SlotActionType.PICKUP);
                        clickSlot(i, 0, SlotActionType.PICKUP);

                        armorMemory.put(EquipmentSlot.CHEST, i);
                        ensureEmptyCursor();
                        foundAlternative = true;
                        break;
                    }
                }
            }

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

    private void elytraOnly() {
        if (client.player == null || client.interactionManager == null) return;

        ensureEmptyCursor();

        ItemStack chestEquipped = client.player.getEquippedStack(EquipmentSlot.CHEST);
        boolean alreadyHasElytra = !chestEquipped.isEmpty() && chestEquipped.getItem() == Items.ELYTRA;

        int elytraSlot = -1;

        if (alreadyHasElytra) {
            elytraSlot = armorMemory.getOrDefault(EquipmentSlot.CHEST, -1);

            if (elytraSlot == -1) {

            }
        } else {
            for (int i = PLAYER_INVENTORY_START; i < PLAYER_INVENTORY_START + 27; i++) {
                ItemStack stack = client.player.currentScreenHandler.getSlot(i).getStack();
                if (!stack.isEmpty() && stack.getItem() == Items.ELYTRA) {
                    elytraSlot = i;
                    break;
                }
            }

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

        if (elytraSlot == -1 && !alreadyHasElytra) {
            client.player.sendMessage(Text.translatable("message.armorhotkeys.no_elytra"), true);
            return;
        }

        if (alreadyHasElytra) {
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
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (!slot.isArmorSlot() || slot == EquipmentSlot.CHEST) continue;

                ItemStack equipped = client.player.getEquippedStack(slot);
                if (!equipped.isEmpty()) {
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

            ItemStack currentlyChest = client.player.getEquippedStack(EquipmentSlot.CHEST);

            if (currentlyChest.isEmpty()) {
                clickSlot(elytraSlot, 0, SlotActionType.PICKUP);
                clickSlot(getArmorSlotId(EquipmentSlot.CHEST), 0, SlotActionType.PICKUP);
                armorMemory.put(EquipmentSlot.CHEST, elytraSlot);
                ensureEmptyCursor();
            } else if (currentlyChest.getItem() != Items.ELYTRA) {
                clickSlot(elytraSlot, 0, SlotActionType.PICKUP);
                clickSlot(getArmorSlotId(EquipmentSlot.CHEST), 0, SlotActionType.PICKUP);
                clickSlot(elytraSlot, 0, SlotActionType.PICKUP);

                armorMemory.put(EquipmentSlot.CHEST, elytraSlot);
                ensureEmptyCursor();
            }
        }
    }

    private void elytraArmor() {
        if (client.player == null || client.interactionManager == null) return;

        ensureEmptyCursor();

        int elytraSlot = -1;
        boolean alreadyHasElytra = false;

        ItemStack chestEquipped = client.player.getEquippedStack(EquipmentSlot.CHEST);
        if (!chestEquipped.isEmpty() && chestEquipped.getItem() == Items.ELYTRA) {
            alreadyHasElytra = true;
            elytraSlot = armorMemory.getOrDefault(EquipmentSlot.CHEST, -1);
        }

        if (!alreadyHasElytra) {
            for (int i = PLAYER_INVENTORY_START; i < PLAYER_INVENTORY_START + 27; i++) {
                ItemStack stack = client.player.currentScreenHandler.getSlot(i).getStack();
                if (!stack.isEmpty() && stack.getItem() == Items.ELYTRA) {
                    elytraSlot = i;
                    break;
                }
            }

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

        if (elytraSlot == -1 && !alreadyHasElytra) {
            client.player.sendMessage(Text.translatable("message.armorhotkeys.no_elytra"), true);
            return;
        }

        if (!alreadyHasElytra) {
            if (!chestEquipped.isEmpty() && chestEquipped.getItem() != Items.ELYTRA) {
                clickSlot(elytraSlot, 0, SlotActionType.PICKUP);
                clickSlot(getArmorSlotId(EquipmentSlot.CHEST), 0, SlotActionType.PICKUP);
                clickSlot(elytraSlot, 0, SlotActionType.PICKUP);
                armorMemory.put(EquipmentSlot.CHEST, elytraSlot);
                ensureEmptyCursor();
            } else if (chestEquipped.isEmpty()) {
                clickSlot(elytraSlot, 0, SlotActionType.PICKUP);
                clickSlot(getArmorSlotId(EquipmentSlot.CHEST), 0, SlotActionType.PICKUP);
                armorMemory.put(EquipmentSlot.CHEST, elytraSlot);
                ensureEmptyCursor();
            }
        }

        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack currentlyEquipped = client.player.getEquippedStack(slot);
            if (currentlyEquipped.isEmpty()) {
                for (int i = PLAYER_INVENTORY_START; i < PLAYER_INVENTORY_START + 27; i++) {
                    ItemStack stack = client.player.currentScreenHandler.getSlot(i).getStack();
                    if (!stack.isEmpty() && getSlot(stack) == slot) {
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

    private int findEmptySlot() {
        if (client.player == null) return -1;

        for (int i = PLAYER_INVENTORY_START; i < PLAYER_INVENTORY_START + 27; i++) {
            if (client.player.currentScreenHandler.getSlot(i).getStack().isEmpty()) {
                return i;
            }
        }

        for (int i = 0; i < 9; i++) {
            if (client.player.currentScreenHandler.getSlot(i).getStack().isEmpty()) {
                return i;
            }
        }

        return -1;
    }

    private int getArmorSlotId(EquipmentSlot slot) {
        switch (slot) {
            case FEET: return ARMOR_START_SLOT + 3;
            case LEGS: return ARMOR_START_SLOT + 2;
            case CHEST: return ARMOR_START_SLOT + 1;
            case HEAD: return ARMOR_START_SLOT;
            default: return -1;
        }
    }

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

    private void ensureEmptyCursor() {
        if (client.player != null && client.player.currentScreenHandler != null) {
            if (!client.player.currentScreenHandler.getCursorStack().isEmpty()) {
                int emptySlot = findEmptySlot();
                if (emptySlot != -1) {
                    clickSlot(emptySlot, 0, SlotActionType.PICKUP);
                }
            }
        }
    }

    private EquipmentSlot getSlot(ItemStack stack) {
        Item item = stack.getItem();

        if (item == Items.ELYTRA) return EquipmentSlot.CHEST;

        if (item == Items.LEATHER_HELMET ||
                item == Items.IRON_HELMET ||
                item == Items.GOLDEN_HELMET ||
                item == Items.DIAMOND_HELMET ||
                item == Items.NETHERITE_HELMET) return EquipmentSlot.HEAD;

        if (item == Items.LEATHER_CHESTPLATE ||
                item == Items.IRON_CHESTPLATE ||
                item == Items.GOLDEN_CHESTPLATE ||
                item == Items.DIAMOND_CHESTPLATE ||
                item == Items.NETHERITE_CHESTPLATE) return EquipmentSlot.CHEST;

        if (item == Items.LEATHER_LEGGINGS ||
                item == Items.IRON_LEGGINGS ||
                item == Items.GOLDEN_LEGGINGS ||
                item == Items.DIAMOND_LEGGINGS ||
                item == Items.NETHERITE_LEGGINGS) return EquipmentSlot.LEGS;

        if (item == Items.LEATHER_BOOTS ||
                item == Items.IRON_BOOTS ||
                item == Items.GOLDEN_BOOTS ||
                item == Items.DIAMOND_BOOTS ||
                item == Items.NETHERITE_BOOTS) return EquipmentSlot.FEET;

        return null;
    }
}