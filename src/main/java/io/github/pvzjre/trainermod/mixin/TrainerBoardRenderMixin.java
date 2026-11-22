package io.github.pvzjre.trainermod.mixin;

import io.github.pvzjre.trainermod.TrainerMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 特效页"暴风雨之夜/种子雨"的雨幕绘制注册：drawGameObjects 里 RENDER_ITEM_STORM
 * 的注册判定原本只认两个真实模式——OR 入开关后普通关卡同样注册雨幕渲染项，
 * 绘制本体由 TrainerChallengeMixin 的 drawWeather 注入接管（BoardRender 是包私有
 * 类，targets 字符串指定；静态目标的 handler 亦 static）。
 */
@Mixin(targets = "io.github.pvzjre.portable.lawn.BoardRender")
public class TrainerBoardRenderMixin {

    @Redirect(method = "drawGameObjects(Lio/github/pvzjre/portable/lawn/Board;"
            + "Lio/github/pvzjre/portable/sexy/graphics/Graphics;)V",
            at = @At(value = "INVOKE",
                    target = "Lio/github/pvzjre/portable/lawn/LawnApp;isStormyNightLevel()Z"))
    private static boolean trainermod$stormItem(io.github.pvzjre.portable.lawn.LawnApp theApp) {
        return theApp.isStormyNightLevel()
                || TrainerMod.stormyNightEnabled() || TrainerMod.seedRainEnabled();
    }
}
