package com.blakebr0.ironjetpacks.crafting.recipe;

import com.blakebr0.ironjetpacks.crafting.ModRecipeSerializers;
import com.blakebr0.ironjetpacks.item.JetpackItem;
import com.blakebr0.ironjetpacks.mixins.ShapedRecipeAccesser;
import com.google.gson.JsonObject;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

public class JetpackUpgradeRecipe extends ShapedRecipe {
    public JetpackUpgradeRecipe(Identifier id, String group, int recipeWidth, int recipeHeight, DefaultedList<Ingredient> inputs, ItemStack output) {
        super(id, group, recipeWidth, recipeHeight, inputs, output);
    }
    
    @Override
    public ItemStack craft(CraftingInventory inv) {
        ItemStack jetpack = inv.getInvStack(4);
        ItemStack result = this.getOutput().copy();
        
        if (!jetpack.isEmpty() && jetpack.getItem() instanceof JetpackItem) {
            CompoundTag tag = jetpack.getTag();
            if (tag != null) {
                result.setTag(tag);
                return result;
            }
        }
        
        return result;
    }
    
    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.CRAFTING_JETPACK_UPGRADE.get();
    }
    
    public static class Serializer implements RecipeSerializer<JetpackUpgradeRecipe> {
        @Override
        public JetpackUpgradeRecipe read(Identifier recipeId, JsonObject json) {
            ShapedRecipe recipe = RecipeSerializer.SHAPED.read(recipeId, json);
            return new JetpackUpgradeRecipe(recipeId, ((ShapedRecipeAccesser) recipe).getGroup(), recipe.getWidth(), recipe.getHeight(), recipe.getPreviewInputs(), recipe.getOutput());
        }
        
        @Override
        public JetpackUpgradeRecipe read(Identifier recipeId, PacketByteBuf buffer) {
            int i = buffer.readVarInt();
            int j = buffer.readVarInt();
            String s = buffer.readString(32767);
            DefaultedList<Ingredient> inputs = DefaultedList.ofSize(i * j, Ingredient.EMPTY);
            
            for (int k = 0; k < inputs.size(); k++) {
                inputs.set(k, Ingredient.fromPacket(buffer));
            }
            
            ItemStack output = buffer.readItemStack();
            return new JetpackUpgradeRecipe(recipeId, s, i, j, inputs, output);
        }
        
        @Override
        public void write(PacketByteBuf buffer, JetpackUpgradeRecipe recipe) {
            buffer.writeVarInt(recipe.getWidth());
            buffer.writeVarInt(recipe.getHeight());
            buffer.writeString(((ShapedRecipeAccesser) recipe).getGroup());
            
            for (Ingredient ingredient : recipe.getPreviewInputs()) {
                ingredient.write(buffer);
            }
            
            buffer.writeItemStack(recipe.getOutput());
        }
    }
}
