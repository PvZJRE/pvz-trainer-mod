package io.github.pvzjre.trainermod.mixin;

import io.github.pvzjre.portable.lawn.GameMode;
import io.github.pvzjre.trainermod.TrainerMod;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 特效页"排山倒海"：mouseDownWithPlant 里"种一格种满一整列"的判定（逐行
 * canPlantAt 通过即补种）——原本只认排山倒海模式；OR 入开关后普通关卡种下
 * 同样铺满一整列（静态目标：handler 亦 static）。
 */
@Mixin(targets = "io.github.pvzjre.portable.lawn.BoardInput")
public class TrainerBoardInputMixin {

    @ModifyExpressionValue(method = "mouseDownWithPlant(Lio/github/pvzjre/portable/lawn/Board;III)V",
            at = @At(value = "FIELD", target = "Lio/github/pvzjre/portable/lawn/LawnApp;mGameMode:I"))
    private static int trainermod$column(int theOriginal) {
        return TrainerMod.columnPlantEnabled()
                ? GameMode.GAMEMODE_CHALLENGE_COLUMN : theOriginal;
    }
}
