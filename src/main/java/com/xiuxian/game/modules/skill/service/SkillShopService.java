package com.xiuxian.game.modules.skill.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiuxian.game.modules.player.entity.PlayerProfile;
import com.xiuxian.game.modules.skill.entity.PlayerSkill;
import com.xiuxian.game.modules.skill.entity.Skill;
import com.xiuxian.game.modules.skill.entity.SkillShopItem;
import com.xiuxian.game.modules.player.service.PlayerService;
import com.xiuxian.game.modules.skill.mapper.PlayerSkillMapper;
import com.xiuxian.game.modules.skill.mapper.SkillMapper;
import com.xiuxian.game.modules.skill.mapper.SkillShopMapper;
import com.xiuxian.game.common.util.GameConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 技能商店服务
 * 模块边界：通过PlayerService访问玩家数据
 *
 * @author shaun.sheng
 */
@Service
@RequiredArgsConstructor
public class SkillShopService {

    private final SkillShopMapper skillShopMapper;
    private final PlayerService playerService;   // 模块边界：通过PlayerService访问玩家数据
    private final PlayerSkillMapper playerSkillMapper;
    private final SkillMapper skillMapper;

    /**
     * 获取所有可购买的技能商店条目
     */
    public List<SkillShopItem> listAvailable() {
        return skillShopMapper.selectAvailable();
    }

    /**
     * 供ShopService使用：获取所有可购买的技能商店条目（别名方法）
     */
    public List<SkillShopItem> getAvailableSkillShopItems() {
        return skillShopMapper.selectAvailable();
    }

    /**
     * 供ShopService使用：按skillId查找技能商店条目
     *
     * @param skillId 技能ID
     * @return 技能商店条目，不存在返回null
     */
    public SkillShopItem getSkillShopItemBySkillId(Integer skillId) {
        // 先尝试直接通过主键查找
        SkillShopItem item = skillShopMapper.selectById(skillId);
        if (item != null) {
            return item;
        }
        // 再通过skill_id字段查找
        List<SkillShopItem> allItems = skillShopMapper.selectAvailable();
        return allItems.stream()
                .filter(i -> i.getSkillId().equals(skillId))
                .findFirst()
                .orElse(null);
    }

    @Transactional
    public void buySkill(Integer shopItemId) {
        PlayerProfile player = playerService.getCurrentPlayerProfile();
        SkillShopItem item = skillShopMapper.selectById(shopItemId);
        if (item == null || Boolean.FALSE.equals(item.getAvailable())) {
            throw new IllegalArgumentException("商品不存在或不可用");
        }
        Skill skill = skillMapper.selectById(item.getSkillId());
        if (skill == null) throw new IllegalArgumentException("技能不存在");
        if (player.getLevel() < item.getRequiredLevel()) {
            throw new IllegalArgumentException(GameConstants.ERROR_REQUIREMENTS_NOT_MET + ": 等级不足");
        }
        if (player.getSpiritStones() < item.getPrice()) {
            throw new IllegalArgumentException(GameConstants.ERROR_INSUFFICIENT_RESOURCES + ": 灵石不足");
        }
        // 扣除灵石
        player.setSpiritStones(player.getSpiritStones() - item.getPrice());
        playerService.savePlayerProfile(player);
        // 学习技能（若未学习过）
        PlayerSkill existing = playerSkillMapper.selectByPlayerIdAndSkillId(player.getId(), skill.getId());
        if (existing == null) {
            PlayerSkill ps = PlayerSkill.builder()
                    .playerId(player.getId())
                    .skillId(skill.getId())
                    .level(1)
                    .experience(0)
                    .equipped(false)
                    .slotNumber(0)
                    .build();
            playerSkillMapper.insert(ps);
        }
    }

    @Transactional
    public void sellSkill(Integer playerSkillId) {
        PlayerProfile player = playerService.getCurrentPlayerProfile();
        PlayerSkill ps = playerSkillMapper.selectById(playerSkillId);
        if (ps == null || !ps.getPlayerId().equals(player.getId())) {
            throw new IllegalArgumentException(GameConstants.ERROR_INVALID_OPERATION + ": 无法出售该技能");
        }
        if (Boolean.TRUE.equals(ps.getEquipped())) {
            throw new IllegalArgumentException(GameConstants.ERROR_INVALID_OPERATION + ": 请先卸下技能");
        }
        SkillShopItem item = skillShopMapper.selectList(
                new QueryWrapper<SkillShopItem>()
                        .eq("skill_id", ps.getSkillId())
                        .last("LIMIT 1")
        ).stream().findFirst().orElse(null);
        long refund = item != null ? Math.max(1, (long) item.getPrice() / 2) : 100;
        player.setSpiritStones(player.getSpiritStones() + refund);
        playerService.savePlayerProfile(player);
        playerSkillMapper.deleteById(playerSkillId);
    }

    /**
     * 管理员：新增或更新技能商店条目
     */
    public void upsertSkillShopItem(SkillShopItem item) {
        if (item.getId() == null) {
            skillShopMapper.insert(item);
        } else {
            skillShopMapper.updateById(item);
        }
    }
}
