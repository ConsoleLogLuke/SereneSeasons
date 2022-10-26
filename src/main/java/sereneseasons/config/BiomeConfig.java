/*******************************************************************************
 * Copyright 2014-2017, the Biomes O' Plenty Team
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International Public License.
 *
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/.
 ******************************************************************************/
package sereneseasons.config;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.reflect.TypeToken;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import sereneseasons.config.json.BiomeData;
import sereneseasons.util.biome.BiomeUtil;
import sereneseasons.util.config.JsonUtil;

import java.io.File;
import java.util.List;
import java.util.Map;

public class BiomeConfig
{
    // We use a HashMap for maximum performance as JsonUtil#getOrCreateConfigFile will return a LinkedHashMap
    public static final Map<ResourceLocation, BiomeData> biomeDataMap = Maps.newHashMap();

    public static void init(File configDir)
    {
        Map<String, BiomeData> defaultBiomeData = Maps.newHashMap();
        addBlacklistedBiomes(defaultBiomeData);
        addTropicalBiomes(defaultBiomeData);

        biomeDataMap.clear();

        Map<String, BiomeData> tmpBiomeDataMap = JsonUtil.getOrCreateConfigFile(configDir, "biome_info.json", defaultBiomeData, new TypeToken<Map<String, BiomeData>>(){}.getType());

        if (tmpBiomeDataMap != null && !tmpBiomeDataMap.isEmpty())
        {
            // We convert our keys to ResourceLocations here as to avoid calling `ResourceLocation#toString()` everywhere
            // This reduces CPU overhead and garbage collector pressure
            for (Map.Entry<String, BiomeData> entry : tmpBiomeDataMap.entrySet())
            {
                biomeDataMap.put(new ResourceLocation(entry.getKey()), entry.getValue());
            }
        }
    }

    public static boolean enablesSeasonalEffects(RegistryKey<Biome> biome)
    {
        if (biome == null)
        {
            return true;
        }

        ResourceLocation name = biome.location();
        if (biomeDataMap.containsKey(name))
        {
            return biomeDataMap.get(name).enableSeasonalEffects;
        }

        return true;
    }

    public static boolean usesTropicalSeasons(RegistryKey<Biome> key, Biome biome)
    {
        if (key == null && biome == null)
        {
            throw new RuntimeException("Failed to check if a biome uses tropical seasons!");
        }

        if (key != null)
        {
            ResourceLocation name = key.location();
            if (biomeDataMap.containsKey(name))
            {
                return biomeDataMap.get(name).useTropicalSeasons;
            }
        }

        Biome realBiome = biome;
        if (realBiome == null)
        {
            realBiome = BiomeUtil.getBiome(key);
        }
        return realBiome.getBaseTemperature() > 0.8F;
    }

    public static boolean usesTropicalSeasons(RegistryKey<Biome> key)
    {
        return usesTropicalSeasons(key, null);
    }

    public static boolean infertileBiome(RegistryKey<Biome> biome)
    {
        List<String> infertileBiomes = Lists.newArrayList("biomesoplenty:wasteland");

        String name = biome.location().toString();

        if (infertileBiomes.contains(name))
        {
            return true;
        }

        return false;
    }

    public static boolean lessColorChange(RegistryKey<Biome> biome)
    {
        if (biome == null)
        {
            return false;
        }

        List<String> lessColorChangeBiomes = Lists.newArrayList("minecraft:swamp", "minecraft:swamp_hills",
                "biomesoplenty:mystic_grove", "biomesoplenty:mystic_plains", "biomesoplenty:tundra", "biomesoplenty:tundra_basin", "biomesoplenty:tundra_bog",
                "biomesoplenty:ominous_woods", "biomesoplenty:ominous_mire");

        String name = biome.location().toString();
        if (lessColorChangeBiomes.contains(name))
        {
            return true;
        }

        return false;
    }

    private static void addBlacklistedBiomes(Map<String, BiomeData> map)
    {
        List<String> blacklistedBiomes = Lists.newArrayList("minecraft:mushroom_fields", "minecraft:mushroom_fields_shore", "minecraft:ocean",
                "minecraft:deep_ocean", "minecraft:frozen_ocean", "minecraft:deep_frozen_ocean", "minecraft:cold_ocean", "minecraft:deep_cold_ocean",
                "minecraft:lukewarm_ocean", "minecraft:deep_lukewarm_ocean", "minecraft:warm_ocean", "minecraft:deep_warm_ocean", "minecraft:river",
                "minecraft:the_void",

                "biomesoplenty:origin_valley", "biomesoplenty:rainbow_hills");

        for (String biomeName : blacklistedBiomes)
        {
            if (!map.containsKey(biomeName))
                map.put(biomeName, new BiomeData(false, false));
            else
                map.get(biomeName).enableSeasonalEffects = false;
        }
    }

    private static void addTropicalBiomes(Map<String, BiomeData> map)
    {
        List<String> tropicalBiomes = Lists.newArrayList("minecraft:swamp", "minecraft:swamp_hills", "minecraft:warm_ocean", "minecraft:deep_warm_ocean");

        for (String biomeName : tropicalBiomes)
        {
            if (!map.containsKey(biomeName))
                map.put(biomeName, new BiomeData(true, true));
            else
                map.get(biomeName).useTropicalSeasons = true;
        }
    }
}
