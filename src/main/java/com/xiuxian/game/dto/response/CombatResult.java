package com.xiuxian.game.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 战斗结果 DTO
 *
 * <p>用于替代 {@code Map<String, Object>} 弱类型返回，提供编译期类型安全保证。</p>
 *
 * <p>单次战斗和批量战斗共用此 DTO：</p>
 * <ul>
 *   <li>单次战斗：totalBattles=1，wins=0或1</li>
 *   <li>批量战斗：totalBattles=N，wins 为实际胜利次数</li>
 * </ul>
 *
 * @author xiuxian
 * @version 1.0
 */
@Data
@Builder
public class CombatResult {

    // ---- 战斗统计 ----

    /** 总战斗次数 */
    private int totalBattles;

    /** 胜利次数 */
    private int wins;

    /** 失败次数 */
    private int losses;

    /** 胜率（0.0 ~ 1.0） */
    private double winRate;

    /** 平均回合数 */
    private double averageRounds;

    // ---- 奖励 ----

    /** 获得总经验 */
    private long totalExpGained;

    /** 获得总灵石 */
    private long totalSpiritStonesGained;

    // ---- 战斗详情 ----

    /** 战斗日志（单次战斗的回合记录） */
    private List<String> battleLog;

    // ---- 怪物信息 ----

    /** 怪物名称 */
    private String monsterName;

    /** 怪物等级 */
    private Integer monsterLevel;

    /** 怪物类型（普通/精英/BOSS） */
    private String monsterType;

    // ---- 战斗后玩家状态 ----

    /** 玩家当前等级 */
    private Integer playerLevel;

    /** 玩家当前经验 */
    private long playerExp;

    /** 玩家当前灵石 */
    private long playerSpiritStones;

    /** 是否有装备掉落（单次战斗） */
    private Integer droppedEquipmentId;

    // ---- 兼容字段（保留供 Controller 序列化时使用） ----

    /**
     * 单次战斗结果字符串："WIN" 或 "LOSE"
     * 批量战斗时通过 wins/losses 判断
     */
    private String result;

    /** 单次战斗回合数（批量战斗中为最后一次的回合数） */
    private int rounds;
}
