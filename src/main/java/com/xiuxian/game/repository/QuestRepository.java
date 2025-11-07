package com.xiuxian.game.repository;

import com.xiuxian.game.entity.Quest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuestRepository extends JpaRepository<Quest, Integer> {
    List<Quest> findByType(Quest.QuestType type);
}