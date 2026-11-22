package cn.enaium.fabricpvz.trainermod;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** 一次性脚本：把 TrainerMod.addPottedPlant 替换为按当前花园场景判定的版本。 */
public final class PatchTrainerAddPlant {
    public static void main(String[] args) throws IOException {
        Path path = Paths.get(
                "src/main/java/cn/enaium/fabricpvz/trainermod/TrainerMod.java");
        String src = Files.readString(path, StandardCharsets.UTF_8);

        String old = """
                private static void addPottedPlant(boolean theMarigold, boolean theFullGrown) {
                        if (sApp.mZenGarden == null || sBoard == null) {
                            show(sBoard, "请在花园里使用");
                            return;
                        }
                        if (sApp.mZenGarden.isZenGardenFull(true)) {
                            show(sBoard, "当前花园已满");
                            return;
                        }
                        PottedPlant aPottedPlant = new PottedPlant();
                        if (theMarigold) {
                            aPottedPlant.initializePottedPlant(cn.enaium.fabricpvz.portable.lawn.SeedType.SEED_MARIGOLD);
                            // 金盏花随机颜色（白/橙/红/浅绿四色变体）
                            aPottedPlant.mDrawVariation = cn.enaium.fabricpvz.portable.pvzp.PvzpCommon.randRangeInt(
                                    DrawVariation.VARIATION_MARIGOLD_WHITE,
                                    DrawVariation.VARIATION_MARIGOLD_LIGHT_GREEN);
                        } else {
                            aPottedPlant.initializePottedPlant(ZenGarden.pickRandomSeedType());
                            if (theFullGrown) {
                                aPottedPlant.mPlantAge = PottedPlantAge.PLANTAGE_FULL;
                            }
                        }
                        sApp.mZenGarden.addPottedPlant(aPottedPlant);
                        show(sBoard, theMarigold ? "已添加一株金盏花幼苗"
                                : theFullGrown ? "已添加一株随机大花" : "已添加一株随机幼苗");
                    }""";

        String replacement = """
                private static void addPottedPlant(boolean theMarigold, boolean theFullGrown) {
                        if (sApp.mZenGarden == null || sBoard == null) {
                            show(sBoard, "请在花园里使用");
                            return;
                        }
                        var aGarden = sApp.mZenGarden;
                        int aGardenType = aGarden.mGardenType;
                        boolean aAquarium = aGardenType == cn.enaium.fabricpvz.portable.lawn.GardenType.GARDEN_AQUARIUM;
                        // 按当前花园场景判定：水族馆禁止金盏花与幼苗（只能加大花，且随机
                        // 出水生植物）；已满按当前场景的格位占用数（mWhichZenGarden）计，
                        // 不再只看温室（GARDEN_MAIN）
                        if (aAquarium && (theMarigold || !theFullGrown)) {
                            show(sBoard, "水族馆只能添加随机大花");
                            return;
                        }
                        if (aGarden.isZenGardenFull(true) && !aAquarium && !isMushroomGarden(aGardenType)) {
                            show(sBoard, "当前花园已满");
                            return;
                        }
                        int aUsedInGarden = countPlantsInGarden(aGardenType);
                        if (aUsedInGarden >= ZEN_GARDEN_CAPACITY) {
                            show(sBoard, "当前花园已满");
                            return;
                        }
                        int aSeedType;
                        if (theMarigold) {
                            aSeedType = cn.enaium.fabricpvz.portable.lawn.SeedType.SEED_MARIGOLD;
                        } else {
                            // 随机树种：水族馆只出水生植物，蘑菇园只出非水生，温室不限
                            do {
                                aSeedType = ZenGarden.pickRandomSeedType();
                            } while (aAquarium != cn.enaium.fabricpvz.portable.lawn.Plant.isAquatic(aSeedType));
                        }
                        int aIndex = sApp.mPlayerInfo.mNumPottedPlants;
                        PottedPlant aPottedPlant = sApp.mPlayerInfo.mPottedPlant[aIndex];
                        aPottedPlant.initializePottedPlant(aSeedType);
                        if (theMarigold) {
                            // 金盏花随机颜色（白/橙/红/浅绿四色变体）
                            aPottedPlant.mDrawVariation = cn.enaium.fabricpvz.portable.pvzp.PvzpCommon.randRangeInt(
                                    DrawVariation.VARIATION_MARIGOLD_WHITE,
                                    DrawVariation.VARIATION_MARIGOLD_LIGHT_GREEN);
                        } else if (theFullGrown) {
                            aPottedPlant.mPlantAge = PottedPlantAge.PLANTAGE_FULL;
                        }
                        aPottedPlant.mWhichZenGarden = aGardenType;
                        int[] aSpot = findOpenSpotInGarden(aGardenType);
                        aPottedPlant.mX = aSpot[0];
                        aPottedPlant.mY = aSpot[1];
                        sApp.mPlayerInfo.mNumPottedPlants++;
                        Plant aPlant = aGarden.placePottedPlant(aIndex);
                        if (sApp.getDialog(cn.enaium.fabricpvz.portable.lawn.Dialogs.DIALOG_STORE) == null) {
                            sBoard.doPlantingEffects(aPottedPlant.mX, aPottedPlant.mY, aPlant);
                        }
                        show(sBoard, theMarigold ? "已添加一株金盏花幼苗"
                                : theFullGrown ? "已添加一株随机大花" : "已添加一株随机幼苗");
                    }

                    /** 花园单场景容量（与 findOpenZenGardenSpot 的 8x4 网格一致）。 */
                    private static final int ZEN_GARDEN_CAPACITY =
                            cn.enaium.fabricpvz.portable.lawn.ZenGarden.ZEN_MAX_GRIDSIZE_X
                                    * cn.enaium.fabricpvz.portable.lawn.ZenGarden.ZEN_MAX_GRIDSIZE_Y;

                    /** 是否蘑菇园场景。 */
                    private static boolean isMushroomGarden(int theGardenType) {
                        return theGardenType == cn.enaium.fabricpvz.portable.lawn.GardenType.GARDEN_MUSHROOM;
                    }

                    /** 统计指定花园（mWhichZenGarden）已占用的格位数。 */
                    private static int countPlantsInGarden(int theGardenType) {
                        int aUsed = 0;
                        for (int i = 0; i < sApp.mPlayerInfo.mNumPottedPlants; i++) {
                            if (sApp.mPlayerInfo.mPottedPlant[i].mWhichZenGarden == theGardenType) {
                                aUsed++;
                            }
                        }
                        return aUsed;
                    }

                    /**
                     * 在指定花园的 8x4 网格里找一个空位（与 findOpenZenGardenSpot 同规
                     * 则：跳过被 Crazy Dave 台词遮住的左上区域与已被当前园植物占用的格）。
                     */
                    private static int[] findOpenSpotInGarden(int theGardenType) {
                        for (int x = 0; x < cn.enaium.fabricpvz.portable.lawn.ZenGarden.ZEN_MAX_GRIDSIZE_X; x++) {
                            for (int y = 0; y < cn.enaium.fabricpvz.portable.lawn.ZenGarden.ZEN_MAX_GRIDSIZE_Y; y++) {
                                boolean aCoveredByDave = sApp.mCrazyDaveMessageIndex != -1 && (x < 2 || y < 1);
                                if (aCoveredByDave) {
                                    continue;
                                }
                                boolean aOccupied = false;
                                for (int i = 0; i < sApp.mPlayerInfo.mNumPottedPlants; i++) {
                                    PottedPlant aPottedPlant = sApp.mPlayerInfo.mPottedPlant[i];
                                    if (aPottedPlant.mWhichZenGarden == theGardenType
                                            && aPottedPlant.mX == x && aPottedPlant.mY == y) {
                                        aOccupied = true;
                                        break;
                                    }
                                }
                                if (!aOccupied) {
                                    return new int[]{x, y};
                                }
                            }
                        }
                        return new int[]{0, 0};
                    }""";

        if (!src.contains(old)) {
            System.out.println("PATCH TARGET NOT FOUND");
            return;
        }
        Files.writeString(path, src.replace(old, replacement), StandardCharsets.UTF_8);
        System.out.println("patched");
    }
}
