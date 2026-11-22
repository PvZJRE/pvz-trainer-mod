package io.github.pvzjre.trainermod.mixin;

import io.github.pvzjre.portable.lawn.Zombie;
import io.github.pvzjre.portable.lawn.ZombiePhase;
import io.github.pvzjre.trainermod.TrainerMod;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 僵尸页两个 ZombieBehaviourA 上的注入：
 * <ul>
 * <li>"不扔小鬼"：巨人的投掷触发判定 mHasObject && 血量过半 && 距离足够
 *     ——修改 mHasObject 的读取（opcode 限定 GETFIELD：该方法里另有 mHasObject = false
 *     的写入，PUTFIELD 是 void 指令不能被表达式修改），开关开启时恒 false，
 *     巨人不再抛小鬼（砸击等其余行为不变）；</li>
 * <li>"从不爆炸"：小丑盒的爆箱检查在 updateZombieJackInTheBox 头部（mPhaseCounter
 *     每 tick 先递减再检查，TICK 尾部压回永远晚一步）——HEAD 注入把 RUNNING 阶段的
 *     计时顶回 100，检查永远不满足，永不爆箱。</li>
 * </ul>
 */
@Mixin(io.github.pvzjre.portable.lawn.ZombieBehaviourA.class)
public class TrainerGargantuarMixin {

    @ModifyExpressionValue(method = "updateZombieGargantuar(Lio/github/pvzjre/portable/lawn/Zombie;)V",
            at = @At(value = "FIELD", target = "Lio/github/pvzjre/portable/lawn/Zombie;mHasObject:Z",
                    opcode = Opcodes.GETFIELD))
    private static boolean trainermod$noImpThrow(boolean theOriginal) {
        return TrainerMod.zombieNoImpThrowEnabled() ? false : theOriginal;
    }

    @Inject(method = "updateZombieJackInTheBox(Lio/github/pvzjre/portable/lawn/Zombie;)V",
            at = @At("HEAD"))
    private static void trainermod$neverExplode(Zombie theZombie, CallbackInfo ci) {
        if (TrainerMod.zombieNeverExplodeEnabled()
                && theZombie.mZombiePhase == ZombiePhase.PHASE_JACK_IN_THE_BOX_RUNNING
                && theZombie.mPhaseCounter < 100) {
            theZombie.mPhaseCounter = 100; // 顶住爆箱计时，永远不触发
        }
    }
}
