/*
 * 叙事系统初始数据
 * 包含：对话树、对话节点、NPC日常对话、传说条目、离线事件
 * Date: 2026-03-23
 */

USE xiuxian_game;

SET NAMES utf8mb4;

-- ====================================================================
-- 对话树数据
-- ====================================================================

INSERT INTO `dialogue_trees` VALUES
-- 第一幕：初入仙途
(1, 1, 'su_xuan_qing_first_meeting', '玉简之缘', '青云镇口', 'mysterious_warm', 1, NULL, NULL, NULL, NULL, 0, 10, 1, NOW(), NOW()),
(2, 2, 'jian_wuhen_first_meeting', '师兄弟初遇', '苏玄清洞府前', 'cold_curious', 2, NULL, NULL, NULL, NULL, 0, 9, 1, NOW(), NOW()),
(3, 5, 'bai_lu_first_meeting', '灵兽之缘', '天剑宗后山', 'warm_insightful', 6, NULL, NULL, NULL, NULL, 0, 8, 1, NOW(), NOW()),
(4, 1, 'su_xuan_qing_cultivation_guide', '修炼入门', '苏玄清洞府', 'patient_guiding', 1, NULL, NULL, NULL, '["met_su_xuan_qing"]', 0, 8, 1, NOW(), NOW()),
(5, 1, 'su_xuan_qing_breakthrough_prep', '突破前夜', '苏玄清洞府', 'serious_caring', 8, NULL, NULL, NULL, NULL, 0, 9, 1, NOW(), NOW()),

-- 第二幕：宗门风云
(6, 1, 'su_xuan_qing_secret_hint', '师尊的沉默', '苏玄清洞府', 'heavy_mysterious', 12, NULL, NULL, NULL, '["broke_through_once"]', 0, 8, 1, NOW(), NOW()),
(7, 3, 'lin_wan_er_invitation', '万法阁之邀', '天剑宗走廊', 'warm_intriguing', 11, NULL, NULL, NULL, NULL, 0, 7, 1, NOW(), NOW()),

-- 日常对话（重复）
(8, 1, 'su_xuan_qing_daily', '师尊闲谈', '苏玄清洞府', 'warm_casual', 1, NULL, NULL, NULL, NULL, 1, 1, 1, NOW(), NOW()),
(9, 6, 'old_chen_daily', '老陈闲聊', '青云镇集市', 'casual_humorous', 1, NULL, NULL, NULL, NULL, 1, 1, 1, NOW(), NOW());

-- ====================================================================
-- 对话节点数据 - 苏玄清首次会面
-- ====================================================================

INSERT INTO `dialogue_nodes` (`dialogue_tree_id`, `node_key`, `node_type`, `speaker`, `text`, `portrait`, `next_node_key`, `parent_node_key`, `sort_order`, `set_flags`, `set_reputation`) VALUES
-- 根节点：苏玄清开口
(1, 'start', 'dialogue', '苏玄清', '你就是激活这枚玉简的人？', 'su_curious', 'player_choice_1', NULL, 0, NULL, NULL),

-- 玩家选择
(1, 'player_choice_1', 'choice', '玩家', '如何回应？', NULL, NULL, NULL, 0, NULL, NULL),

-- 选项1：正直
(1, 'choice_righteous', 'choice', '玩家', '这是你的东西？我还给你。', NULL, 'su_response_righteous', 'player_choice_1', 1, NULL, '{"1": 5}'),
(1, 'su_response_righteous', 'dialogue', '苏玄清', '（笑着摇头）它不是我的了。或者说……它从来就不是我的。', 'su_warm', 'su_response_righteous_2', NULL, 0, NULL, NULL),
(1, 'su_response_righteous_2', 'dialogue', '苏玄清', '它等了很久。等一个合适的人。', 'su_serious', 'merge_path', NULL, 0, '["first_impression_righteous"]', NULL),

-- 选项2：好奇
(1, 'choice_curious', 'choice', '玩家', '这玉简是什么？你怎么知道是我？', NULL, 'su_response_curious', 'player_choice_1', 2, NULL, '{"1": 3}'),
(1, 'su_response_curious', 'dialogue', '苏玄清', '（举步走近，目光锐利）好问题。', 'su_sharp', 'su_response_curious_2', NULL, 0, NULL, NULL),
(1, 'su_response_curious_2', 'dialogue', '苏玄清', '因为这枚玉简在过去三百年里，从未对任何人产生过反应。', 'su_serious', 'merge_path', NULL, 0, '["first_impression_curious"]', NULL),

-- 选项3：警觉
(1, 'choice_cautious', 'choice', '玩家', '……（保持沉默，握紧玉简。）', NULL, 'su_response_cautious', 'player_choice_1', 3, NULL, '{"1": 4}'),
(1, 'su_response_cautious', 'dialogue', '苏玄清', '（停步，微微一笑）不说话？好。不说话的人往往更值得信任。', 'su_warm', 'su_response_cautious_2', NULL, 0, NULL, NULL),
(1, 'su_response_cautious_2', 'dialogue', '苏玄清', '我叫苏玄清。你可以叫我……师父。如果你愿意的话。', 'su_warm', 'merge_path', NULL, 0, '["first_impression_cautious"]', NULL),

-- 汇合节点
(1, 'merge_path', 'dialogue', '苏玄清', '我住在镇子东边的山脚下。如果你想学修炼……明天日出前到那里找我。', 'su_calm', 'merge_path_2', NULL, 0, NULL, NULL),
(1, 'merge_path_2', 'dialogue', '苏玄清', '（转身走了两步，又停下）对了——别吃那玉简。上一个碰到它的人……嗯。不提了。', 'su_back', NULL, NULL, 0, '["met_su_xuan_qing"]', NULL);

-- ====================================================================
-- 对话节点数据 - 剑无痕初遇
-- ====================================================================

INSERT INTO `dialogue_nodes` (`dialogue_tree_id`, `node_key`, `node_type`, `speaker`, `text`, `portrait`, `next_node_key`, `parent_node_key`, `sort_order`, `set_flags`, `set_reputation`) VALUES
(2, 'start', 'dialogue', '剑无痕', '所以这就是新来的？', 'jian_scan', 'jian_scan_2', NULL, 0, NULL, NULL),
(2, 'jian_scan_2', 'dialogue', '剑无痕', '（上下打量）练气二层。嗯。', 'jian_unimpressed', 'jian_scan_3', NULL, 0, NULL, NULL),
(2, 'jian_scan_3', 'dialogue', '剑无痕', '师父，您确定没看走眼？', 'jian_mock', 'player_choice', NULL, 0, NULL, NULL),
(2, 'player_choice', 'choice', '玩家', '如何回应剑无痕？', NULL, NULL, NULL, 0, NULL, NULL),
(2, 'choice_polite', 'choice', '玩家', '师兄好，请多指教。', NULL, 'jian_polite_response', 'player_choice', 1, NULL, '{"2": 3}'),
(2, 'jian_polite_response', 'dialogue', '剑无痕', '（微微皱眉）客气话就不必了。看实力。三天后的宗门试炼，别给我丢人。', 'jian_cold', 'end', NULL, 0, NULL, NULL),
(2, 'choice_confident', 'choice', '玩家', '实力不够，可以练。态度不够，练也白练。', NULL, 'jian_confident_response', 'player_choice', 2, NULL, '{"2": 5}'),
(2, 'jian_confident_response', 'dialogue', '剑无痕', '（挑眉）呵。有意思。那我们看看，是你嘴硬还是剑硬。三天后见。', 'jian_amused', 'end', NULL, 0, NULL, NULL),
(2, 'choice_naive', 'choice', '玩家', '……你是谁？', NULL, 'jian_naive_response', 'player_choice', 3, NULL, '{"2": 2}'),
(2, 'jian_naive_response', 'dialogue', '剑无痕', '（愣了一下，然后笑了一声）行吧，看来师父什么都没跟你说。', 'jian_laugh', 'jian_naive_2', NULL, 0, NULL, NULL),
(2, 'jian_naive_2', 'dialogue', '剑无痕', '我是剑无痕。天剑宗内门首席弟子。你可以理解为……你未来的天花板。', 'jian_arrogant', 'end', NULL, 0, NULL, NULL);

-- ====================================================================
-- 对话节点数据 - 白鹿真人初遇
-- ====================================================================

INSERT INTO `dialogue_nodes` (`dialogue_tree_id`, `node_key`, `node_type`, `speaker`, `text`, `portrait`, `next_node_key`, `parent_node_key`, `sort_order`, `set_flags`, `set_reputation`) VALUES
(3, 'start', 'dialogue', '白鹿真人', '（蹲在灵猫旁边）嘿，小家伙，你爪子好了没？', 'bai_lu_warm', 'bai_lu_noticed', NULL, 0, NULL, NULL),
(3, 'bai_lu_noticed', 'dialogue', '白鹿真人', '哦？你就是苏老头的新弟子？（站起身，拍拍膝盖上的土）', 'bai_lu_curious', 'bai_lu_observe', NULL, 0, NULL, NULL),
(3, 'bai_lu_observe', 'dialogue', '白鹿真人', '这只小猫跟着你呢。它不跟一般人。', 'bai_lu_insight', 'player_choice', NULL, 0, NULL, NULL),
(3, 'player_choice', 'choice', '玩家', '如何回应？', NULL, NULL, NULL, 0, NULL, NULL),
(3, 'choice_humble', 'choice', '玩家', '它受伤了，我只是帮了它一下。', NULL, 'bai_lu_humble_response', 'player_choice', 1, NULL, '{"5": 4}'),
(3, 'bai_lu_humble_response', 'dialogue', '白鹿真人', '帮了一下？（笑着摇头）年轻人，你对"帮"的理解太轻了。', 'bai_lu_smile', 'bai_lu_humble_2', NULL, 0, NULL, NULL),
(3, 'bai_lu_humble_2', 'dialogue', '白鹿真人', '它不只是留下了——它选择了你。灵兽的直觉比人准得多。好好待它。', 'bai_lu_serious', 'end', NULL, 0, NULL, NULL),
(3, 'choice_utility', 'choice', '玩家', '我想把它训练成战斗灵兽。', NULL, 'bai_lu_utility_response', 'player_choice', 2, NULL, '{"5": 1}'),
(3, 'bai_lu_utility_response', 'dialogue', '白鹿真人', '（表情变了一下，然后恢复）战斗灵兽？嗯……', 'bai_lu_caution', 'bai_lu_utility_2', NULL, 0, NULL, NULL),
(3, 'bai_lu_utility_2', 'dialogue', '白鹿真人', '灵兽不只是武器。你记住这一点，以后会感谢我的。', 'bai_lu_warning', 'end', NULL, 0, NULL, NULL),
(3, 'choice_casual', 'choice', '玩家', '它跟来就来吧，我不介意多个伴。', NULL, 'bai_lu_casual_response', 'player_choice', 3, NULL, '{"5": 5}'),
(3, 'bai_lu_casual_response', 'dialogue', '白鹿真人', '（咧嘴笑）好！我就喜欢这种态度。', 'bai_lu_happy', 'bai_lu_casual_2', NULL, 0, NULL, NULL),
(3, 'bai_lu_casual_2', 'dialogue', '白鹿真人', '你看，它尾巴翘起来了——它也喜欢你。', 'bai_lu_warm', 'end', NULL, 0, NULL, NULL);

-- ====================================================================
-- 对话节点数据 - 修炼入门
-- ====================================================================

INSERT INTO `dialogue_nodes` (`dialogue_tree_id`, `node_key`, `node_type`, `speaker`, `text`, `portrait`, `next_node_key`, `parent_node_key`, `sort_order`, `set_flags`) VALUES
(4, 'start', 'dialogue', '苏玄清', '准备好了？盘膝坐下。闭上眼。', 'su_calm', 'cultivate_step_1', NULL, 0, NULL),
(4, 'cultivate_step_1', 'dialogue', '苏玄清', '感受灵气。像溪水流过手指一样——不要用力去抓，让它自然流过。', 'su_guiding', 'cultivate_step_2', NULL, 0, NULL),
(4, 'cultivate_step_2', 'dialogue', '旁白', '一股微凉的气流从四面八方汇聚，轻轻拂过你的皮肤……', NULL, 'cultivate_step_3', NULL, 0, NULL),
(4, 'cultivate_step_3', 'dialogue', '苏玄清', '（煮茶中，头也不回）感觉到了？那就是灵气。从今天起，它会成为你的一部分。', 'su_calm', 'cultivate_end', NULL, 0, NULL),
(4, 'cultivate_end', 'dialogue', '苏玄清', '修行路漫漫。慢慢来，别急。为师在这等你。', 'su_warm', NULL, NULL, 0, '["started_cultivation"]');

-- ====================================================================
-- 对话节点数据 - 突破前夜
-- ====================================================================

INSERT INTO `dialogue_nodes` (`dialogue_tree_id`, `node_key`, `node_type`, `speaker`, `text`, `portrait`, `next_node_key`, `parent_node_key`, `sort_order`) VALUES
(5, 'start', 'dialogue', '苏玄清', '（看了你一眼）气息比昨天稳了。快要圆满了？', 'su_observing', 'breakthrough_step_2', NULL, 0),
(5, 'breakthrough_step_2', 'dialogue', '苏玄清', '练气十层圆满之后，你需要面对一样东西——心魔。', 'su_serious', 'breakthrough_step_3', NULL, 0),
(5, 'breakthrough_step_3', 'dialogue', '苏玄清', '心魔不是怪物。它是你内心深处……你最不愿面对的那个自己。', 'su_deep', 'breakthrough_step_4', NULL, 0),
(5, 'breakthrough_step_4', 'dialogue', '苏玄清', '（沉默良久）每个人都要过这一关。为师当年也……算了，不提往事。', 'su_memory', 'breakthrough_step_5', NULL, 0),
(5, 'breakthrough_step_5', 'dialogue', '苏玄清', '记住——心魔说的每一句话，都不是真的。但它们听起来会像是真的。', 'su_warning', 'breakthrough_end', NULL, 0),
(5, 'breakthrough_end', 'dialogue', '苏玄清', '准备好了就告诉我。不急。', 'su_warm', NULL, NULL, 0);

-- ====================================================================
-- 对话节点数据 - 师尊的沉默（第二幕）
-- ====================================================================

INSERT INTO `dialogue_nodes` (`dialogue_tree_id`, `node_key`, `node_type`, `speaker`, `text`, `portrait`, `next_node_key`, `parent_node_key`, `sort_order`, `set_flags`) VALUES
(6, 'start', 'dialogue', '苏玄清', '（你走进洞府时，苏玄清正对着一面空白的墙壁出神。）', 'su_distant', 'secret_hint_2', NULL, 0, NULL),
(6, 'secret_hint_2', 'dialogue', '苏玄清', '……回来了？修炼进展不错。（停顿）筑基……嗯。', 'su_vague', 'secret_hint_3', NULL, 0, NULL),
(6, 'secret_hint_3', 'dialogue', '苏玄清', '（转身煮茶，背对着你）你知道吗，这个世界上有些地方……不是不想去，是不能去。', 'su_heavy', 'secret_hint_4', NULL, 0, NULL),
(6, 'secret_hint_4', 'dialogue', '苏玄清', '天剑宗的剑冢，万法阁的藏经阁最上层，妖兽林深处的黑雾谷……', 'su_listing', 'secret_hint_5', NULL, 0, NULL),
(6, 'secret_hint_5', 'dialogue', '苏玄清', '（递给你一杯茶）别去。至少……不是现在。', 'su_warning', NULL, NULL, 0, '["su_mentioned_forbidden_places"]');

-- ====================================================================
-- 对话节点数据 - 林婉儿邀请
-- ====================================================================

INSERT INTO `dialogue_nodes` (`dialogue_tree_id`, `node_key`, `node_type`, `speaker`, `text`, `portrait`, `next_node_key`, `parent_node_key`, `sort_order`, `set_flags`, `set_reputation`) VALUES
(7, 'start', 'dialogue', '林婉儿', '（抱着一摞书从走廊拐角出来，差点撞上你）啊！——对不起对不起！', 'lin_surprised', 'lin_meet_2', NULL, 0, NULL, NULL),
(7, 'lin_meet_2', 'dialogue', '林婉儿', '（扶稳书本，好奇地打量你）你是……苏长老的弟子？练气期就突破到筑基了？', 'lin_curious', 'lin_meet_3', NULL, 0, NULL, NULL),
(7, 'lin_meet_3', 'dialogue', '林婉儿', '了不起！我听说苏长老已经三百年没收弟子了。你是第一个。', 'lin_warm', 'lin_invite', NULL, 0, NULL, NULL),
(7, 'lin_invite', 'dialogue', '林婉儿', '对了，我叫林婉儿，是万法阁的。三天后万法阁有一个法术交流会——你要不要来？', 'lin_inviting', 'player_choice', NULL, 0, NULL, NULL),
(7, 'player_choice', 'choice', '玩家', '如何回应？', NULL, NULL, NULL, 0, NULL, NULL),
(7, 'choice_go', 'choice', '玩家', '法术交流会？听起来很有趣，我去。', NULL, 'lin_go_response', 'player_choice', 1, '["agreed_wanfa_exchange"]', '{"3": 5}'),
(7, 'lin_go_response', 'dialogue', '林婉儿', '太好了！（眼睛亮了起来）那我帮你登记。万法阁在天剑宗东边，有个很大的藏书楼——你不会找不到的！', 'lin_happy', 'lin_go_2', NULL, 0, NULL, NULL),
(7, 'lin_go_2', 'dialogue', '林婉儿', '（低头翻书，自言自语）对了……那本《万法总纲》放哪了……（抬头）啊你还在？没事没事，三天后见！', 'lin_absent', NULL, NULL, 0, NULL, NULL),
(7, 'choice_maybe', 'choice', '玩家', '我考虑一下，三天后给你答复。', NULL, 'lin_maybe_response', 'player_choice', 2, NULL, '{"3": 2}'),
(7, 'lin_maybe_response', 'dialogue', '林婉儿', '当然可以！不着急。（微笑）万法阁的门永远为好学之人敞开。', 'lin_understanding', NULL, NULL, 0, NULL, NULL);

-- ====================================================================
-- 对话节点数据 - 苏玄清日常对话（可重复）
-- ====================================================================

INSERT INTO `dialogue_nodes` (`dialogue_tree_id`, `node_key`, `node_type`, `speaker`, `text`, `portrait`, `next_node_key`, `parent_node_key`, `sort_order`) VALUES
(8, 'start', 'dialogue', '苏玄清', '（煮茶中）来了？坐。茶刚泡好。', 'su_warm', NULL, NULL, 0);

-- ====================================================================
-- 对话节点数据 - 老陈日常对话（可重复）
-- ====================================================================

INSERT INTO `dialogue_nodes` (`dialogue_tree_id`, `node_key`, `node_type`, `speaker`, `text`, `portrait`, `next_node_key`, `parent_node_key`, `sort_order`) VALUES
(9, 'start', 'dialogue', '老陈', '嘿，年轻人！要药材不？自己种的。灵气保证足！（竖起大拇指）', 'chen_grin', 'chen_2', NULL, 0),
(9, 'chen_2', 'dialogue', '老陈', '什么？你说市场价更便宜？那你去市场买啊！（嗑瓜子）……开玩笑的，我给你打八折。', 'chen_joking', NULL, NULL, 0);

-- ====================================================================
-- NPC日常对话池
-- ====================================================================

INSERT INTO `npc_daily_dialogues` (`npc_id`, `text`, `conditions`, `priority`) VALUES
-- 苏玄清日常对话
(1, '起了？今日灵气尚可，适合修炼。别浪费了。', NULL, 3),
(1, '（煮茶中）夜深了还修炼？年轻人精力旺盛是好事……但茶凉了就不好喝了。', '{"time": "night"}', 2),
(1, '你的气息比昨天稳了。快要圆满了？……别急。急不得。', '{"level_gte": 8}', 4),
(1, '你的灵兽看起来不太开心。为师不是说了——灵兽和弟子一样，都要上心。', '{"pet_hunger_lte": 20}', 5),
(1, '两天没见。去哪了？（停顿）……不是为师想你了。是茶凉了没人喝。', '{"days_since_login_gte": 2}', 6),
(1, '突破之后，感觉如何？（不等回答）都会过去的。不论是好是坏。', '{"has_flag": "broke_through_once"}', 3),

-- 剑无痕日常对话
(2, '又来了？行吧。别拖后腿就行。', NULL, 2),
(2, '（擦拭佩剑）你的剑法……还需要多练。我说的是实话。', NULL, 1),
(2, '（你走近时，剑无痕抬头看了你一眼，又低下头继续擦剑）……有事？', NULL, 3),

-- 林婉儿日常对话
(3, '（从书堆后面探出头）哦？是你啊。稍等，让我找到这一页……好了！怎么了？', NULL, 2),
(3, '你知道吗？万法阁有一本书，最后一页是空白的。不是没写完——是故意留白的。', '{"min_relation": 41}', 4),
(3, '别担心灵石不够，办法总比困难多。至少……大概吧。大概率吧。六四开。', NULL, 3),

-- 老陈日常对话
(6, '走了又来了？', NULL, 1),
(6, '修炼累了喝点茶。别问我哪来的茶叶，问就是自己种的。', NULL, 2),
(6, '飞升？那玩意儿啊……（嗑瓜子）就跟考试一样，考上了不一定好，考不上也不一定坏。关键是——你想不想考。', '{"min_relation": 21}', 5),
(6, '（看着远方）你看那片云，像不像一只手？（停顿）……算了，我老眼昏花。', NULL, 3);

-- ====================================================================
-- 传说条目数据
-- ====================================================================

INSERT INTO `lore_entries` (`lore_key`, `title`, `content`, `lore_layer`, `category`, `related_npcs`, `related_lore_keys`, `discover_condition`, `min_realm`, `min_level`, `icon`, `sort_order`) VALUES
-- 表面层
('L001', '苍玄界概览', '苍玄界由五大灵脉支撑，灵脉交汇处形成仙城。世界分为凡人居住的下界和修仙者争夺的上界，以天堑分隔。修仙者通过吸收灵气提升修为，目标是渡天劫、飞升仙界。', '表面', '世界', NULL, NULL, '自动获得', NULL, 1, 'fa-globe', 1),
('L002', '五大灵脉', '五条灵脉分布在苍玄界各处，分别以金、木、水、火、土五行命名。每条灵脉都是一个灵气极为充沛的区域，围绕灵脉建立的仙城是修仙者的聚集地。', '表面', '世界', NULL, '["L009"]', '天剑宗藏经阁一楼', NULL, 1, 'fa-fire', 2),
('L003', '四大宗门', '天剑宗——以剑入道，追求极致力量；万法阁——博采众长，研究万法之源；幽冥殿——以身为炉，炼化万物；灵兽山——人兽共生，追求自然和谐。四大宗门维持着苍玄界的秩序。', '表面', '宗门', NULL, '["L007"]', '天剑宗入门仪式', NULL, 1, 'fa-landmark', 3),
('L004', '天剑宗', '苍玄界最强的宗门之一，建立在一条中型灵脉之上，已有千年历史。宗门以剑道闻名，弟子修炼讲究一剑破万法。内门弟子驻于山腰，外门弟子分布于山脚的青云镇。', '表面', '宗门', '["苏玄清","剑无痕"]', '["L005"]', '天剑宗入门仪式', NULL, 1, 'fa-bolt', 4),
('L005', '封魔之战', '三千年前的远古战争。传说当时有"魔物"入侵苍玄界，四大宗门联手封印。战争留下了巨大的伤疤——天堑。幽冥殿被认为与"魔物"有染，被逐出中原。', '参与', '事件', NULL, '["L009","L010"]', '妖兽林深处探索', '筑基期', 11, 'fa-skull-crossbones', 5),
('L006', '灵兽起源', '灵兽并非普通的野兽——它们是远古时代就存在的生灵，部分灵兽拥有数千年甚至更久的记忆。灵兽山的白鹿真人似乎知道更多……', '参与', '世界', '["白鹿真人"]', NULL, '白鹿真人好感度>60', '筑基期', 11, 'fa-paw', 6),
('L007', '万法阁秘辛', '万法阁的创始人原本是幽冥殿的弟子，因理念不合叛逃。两宗的恩怨已延续三千年。林婉儿的身世似乎与此有关……', '参与', '宗门', '["林婉儿"]', NULL, '林婉儿好感度>60', '筑基期', 11, 'fa-book', 7),
('L008', '苏玄清的过去', '苏玄清，天剑宗外门长老。表面上是一个温和的退休老者，实际上实力深不可测。三百年前，他在一次事件中失去了挚友——他唯一输过的对手。他在青云镇的任务似乎不只是教导弟子……', '参与', '人物', '["苏玄清"]', NULL, '苏玄清好感度>80', '金丹期', 16, 'fa-user-secret', 8),
('L009', '五灵脉的秘密', '五条灵脉并非自然形成——而是上古五位大能以自身道基铸就。这意味着每条灵脉都蕴含着一位远古强者的毕生修为。灵脉的波动似乎在逐年增强……', '深层', '世界', NULL, '["L010"]', '金丹期 + 收集L002+L005', '金丹期', 16, 'fa-yin-yang', 9),
('L010', '苍玄仙帝', '苍玄界本身是一位陨落的仙人的尸骸化成——苍玄仙帝。十万年前，仙帝陨落，其身躯化为山川大地，其灵力化为灵脉，其残存的意识……至今仍在沉睡。', '深层', '世界', NULL, '["L011"]', '金丹期 + 探索青云镇古井', '金丹期', 16, 'fa-crown', 10),
('L011', '天劫真相', '天劫不是天道对修士的考验——而是苍玄仙帝残存意识对"可能的继承者"的筛选。每一次天劫，都是仙帝在寻找一个能承载其意志的容器。', '深层', '世界', NULL, '["L012"]', '元婴期 + 收集L009+L010', '元婴期', 20, 'fa-bolt', 11),
('L012', '飞升的真相', '所谓飞升，其实是被仙帝残意识吞噬——真正的永生，需要找到另一条路。散修联盟的"老陈"似乎知道更多……', '深层', '世界', '["老陈"]', NULL, '老陈好感度>90', '元婴期', 20, 'fa-dove', 12);

-- ====================================================================
-- 离线事件叙事情景
-- ====================================================================

INSERT INTO `offline_narrative_events` (`event_key`, `title`, `narrative`, `probability`, `min_offline_hours`, `min_realm`, `min_level`, `set_flag`, `unlock_dialogue_key`, `npc_relation_change`, `sort_order`) VALUES
('injured_spirit_fox', '受伤的灵狐', '你离线期间，一只受伤的灵狐偷偷溜进了你的洞府。它蜷缩在你的修炼蒲团旁，等你回来时，它已经用灵力为自己疗了伤——但看起来很虚弱。', 0.010, 8, NULL, 1, 'found_injured_fox', NULL, '{"5": 3}', 1),
('mysterious_letter', '神秘来信', '你的洞府门缝里塞了一封信。没有署名，只有一句话——"剑冢之下，有你想要的东西。切记：不是所有锁都该打开。"', 0.005, 24, '筑基期', 11, 'received_mysterious_letter', NULL, NULL, 2),
('old_chen_tea', '老陈的茶', '老陈来过你的洞府。桌上多了一壶茶和一张纸条——"修炼累了喝点茶。别问我哪来的茶叶，问就是自己种的。（画了一个笑脸）"', 0.015, 4, NULL, 1, NULL, NULL, '{"6": 5}', 3),
('spirit_vine_bloom', '灵藤花开', '洞府外的一株灵藤在你离线期间开了花。灵花散发着淡雅的香气，闻到它的人会感到修炼时灵气流转更加顺畅。', 0.008, 12, NULL, 1, 'spirit_vine_bloomed', NULL, NULL, 4),
('sword_echo', '剑鸣之夜', '深夜，你的佩剑突然自行发出嗡鸣。剑身微微发光，上面浮现出几个模糊的古字。你辨认出其中两个字——"……醒来……"', 0.003, 48, '筑基期', 11, 'sword_resonance', NULL, NULL, 5);
