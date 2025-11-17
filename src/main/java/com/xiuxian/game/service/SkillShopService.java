package com.xiuxian.game.service;

import com.xiuxian.game.entity.PlayerProfile;
import com.xiuxian.game.entity.PlayerSkill;
import com.xiuxian.game.entity.Skill;
import com.xiuxian.game.entity.SkillShopItem;
import com.xiuxian.game.mapper.PlayerProfileMapper;
import com.xiuxian.game.mapper.PlayerSkillMapper;
import com.xiuxian.game.mapper.SkillMapper;
import com.xiuxian.game.mapper.SkillShopMapper;
import com.xiuxian.game.util.GameConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SkillShopService {
    private final SkillShopMapper skillShopMapper;
    private final PlayerProfileMapper playerProfileMapper;
    private final PlayerSkillMapper playerSkillMapper;
    private final SkillMapper skillMapper;
    private final PlayerService playerService;

    public List<SkillShopItem> listAvailable() {
        return skillShopMapper.selectAvailable();
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
        playerProfileMapper.updateById(player);
        // 学习技能（若未学习）
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
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<SkillShopItem>()
                        .eq("skill_id", ps.getSkillId())
                        .last("LIMIT 1")
        ).stream().findFirst().orElse(null);
        long refund = item != null ? Math.max(1, item.getPrice() / 2) : 100; // 没有定价则最低退款
        player.setSpiritStones(player.getSpiritStones() + refund);
        playerProfileMapper.updateById(player);
        playerSkillMapper.deleteById(playerSkillId);
    }
}