package com.xiuxian.game.modules.skill.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiuxian.game.common.exception.BusinessException;
import com.xiuxian.game.common.exception.ErrorCode;
import com.xiuxian.game.modules.player.entity.PlayerProfile;
import com.xiuxian.game.modules.player.mapper.PlayerProfileMapper;
import com.xiuxian.game.modules.player.service.PlayerService;
import com.xiuxian.game.modules.skill.entity.PlayerSkill;
import com.xiuxian.game.modules.skill.entity.Skill;
import com.xiuxian.game.modules.skill.entity.SkillShopItem;
import com.xiuxian.game.modules.skill.mapper.PlayerSkillMapper;
import com.xiuxian.game.modules.skill.mapper.SkillMapper;
import com.xiuxian.game.modules.skill.mapper.SkillShopMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SkillShopService {

    private final SkillShopMapper skillShopMapper;
    private final PlayerService playerService;
    private final PlayerProfileMapper playerProfileMapper;
    private final PlayerSkillMapper playerSkillMapper;
    private final SkillMapper skillMapper;

    public List<SkillShopItem> listAvailable() {
        return skillShopMapper.selectAvailable();
    }

    public List<SkillShopItem> getAvailableSkillShopItems() {
        return skillShopMapper.selectAvailable();
    }

    public SkillShopItem getSkillShopItemBySkillId(Integer skillId) {
        return skillShopMapper.selectAvailable().stream()
                .filter(i -> i.getSkillId() != null && i.getSkillId().equals(skillId))
                .findFirst()
                .orElse(null);
    }

    @Transactional
    public void buySkill(Integer shopItemId) {
        PlayerProfile currentPlayer = playerService.getCurrentPlayerProfile();
        PlayerProfile player = playerProfileMapper.selectByIdForUpdate(currentPlayer.getId());
        SkillShopItem item = skillShopMapper.selectById(shopItemId);
        if (item == null || Boolean.FALSE.equals(item.getAvailable())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "商品不存在或不可用");
        }

        Skill skill = skillMapper.selectById(item.getSkillId());
        if (skill == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "技能不存在");
        }
        if (defaultInt(player.getLevel(), 1) < item.getRequiredLevel()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "等级不足");
        }

        PlayerSkill existing = playerSkillMapper.selectByPlayerIdAndSkillId(player.getId(), skill.getId());
        if (existing != null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "已经学习过该技能");
        }
        if (defaultLong(player.getSpiritStones()) < item.getPrice()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "灵石不足");
        }

        player.setSpiritStones(defaultLong(player.getSpiritStones()) - item.getPrice());
        playerService.savePlayerProfile(player);

        LocalDateTime now = LocalDateTime.now();
        int insertedRows = playerSkillMapper.insertIfAbsent(
                player.getId(),
                skill.getId(),
                1,
                0,
                false,
                0,
                now,
                now);
        if (insertedRows == 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "已经学习过该技能");
        }
    }

    @Transactional
    public void sellSkill(Integer playerSkillId) {
        PlayerProfile currentPlayer = playerService.getCurrentPlayerProfile();
        PlayerProfile player = playerProfileMapper.selectByIdForUpdate(currentPlayer.getId());
        PlayerSkill ps = playerSkillMapper.selectByIdForUpdate(playerSkillId);
        if (ps == null || !ps.getPlayerId().equals(player.getId())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "无法出售该技能");
        }
        if (Boolean.TRUE.equals(ps.getEquipped())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请先卸下技能");
        }

        SkillShopItem item = skillShopMapper.selectList(
                new QueryWrapper<SkillShopItem>()
                        .eq("skill_id", ps.getSkillId())
                        .last("LIMIT 1")
        ).stream().findFirst().orElse(null);

        long refund = item != null ? Math.max(1, (long) item.getPrice() / 2) : 100;
        player.setSpiritStones(defaultLong(player.getSpiritStones()) + refund);
        playerService.savePlayerProfile(player);
        playerSkillMapper.deleteById(playerSkillId);
    }

    public void upsertSkillShopItem(SkillShopItem item) {
        if (item.getId() == null) {
            skillShopMapper.insert(item);
        } else {
            skillShopMapper.updateById(item);
        }
    }

    private int defaultInt(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }

    private long defaultLong(Long value) {
        return value == null ? 0L : value;
    }
}
