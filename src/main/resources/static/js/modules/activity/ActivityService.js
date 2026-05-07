import { gameAPI } from '../../core/api/GameApi.js';
import { toast } from '../../components/Toast.js';

const PROGRESS_TARGET_KEYS = ['target', 'requiredValue', 'requiredProgress', 'goal', 'progressTarget'];
const SCORE_TARGET_KEYS = ['scoreTarget', 'requiredScore', 'targetScore', 'maxScore'];

function toNumber(value, fallback = 0) {
    if (typeof value === 'number' && Number.isFinite(value)) {
        return value;
    }
    if (typeof value === 'string' && value.trim() !== '') {
        const parsed = Number(value);
        if (Number.isFinite(parsed)) {
            return parsed;
        }
    }
    return fallback;
}

function safeParseJson(rawValue) {
    if (rawValue == null) {
        return null;
    }
    if (typeof rawValue === 'object') {
        return rawValue;
    }
    if (typeof rawValue !== 'string') {
        return null;
    }

    const trimmed = rawValue.trim();
    if (!trimmed || (!trimmed.startsWith('{') && !trimmed.startsWith('['))) {
        return null;
    }

    try {
        return JSON.parse(trimmed);
    } catch {
        return null;
    }
}

function pickNumber(source, keys) {
    if (!source || typeof source !== 'object') {
        return null;
    }

    for (const key of keys) {
        const value = toNumber(source[key], Number.NaN);
        if (Number.isFinite(value)) {
            return value;
        }
    }

    return null;
}

function parseProgress(progressRaw) {
    if (progressRaw == null) {
        return { value: 0, score: 0 };
    }
    if (typeof progressRaw === 'number') {
        return { value: progressRaw, score: 0 };
    }

    const parsed = safeParseJson(progressRaw);
    if (parsed && typeof parsed === 'object' && !Array.isArray(parsed)) {
        return {
            value: toNumber(parsed.value, 0),
            score: toNumber(parsed.score, 0)
        };
    }

    const rawText = String(progressRaw).trim();
    if (!rawText) {
        return { value: 0, score: 0 };
    }
    if (/^-?\d+$/.test(rawText)) {
        return { value: Number(rawText), score: 0 };
    }

    const valueMatch = rawText.match(/"value"\s*:\s*(-?\d+)/);
    const scoreMatch = rawText.match(/"score"\s*:\s*(-?\d+)/);
    return {
        value: valueMatch ? Number(valueMatch[1]) : 0,
        score: scoreMatch ? Number(scoreMatch[1]) : 0
    };
}

function parseRules(rulesRaw) {
    const rules = safeParseJson(rulesRaw);
    return {
        target: pickNumber(rules, PROGRESS_TARGET_KEYS),
        scoreTarget: pickNumber(rules, SCORE_TARGET_KEYS)
    };
}

function normalizeRewardType(rawType) {
    const normalized = String(rawType || '').trim().toUpperCase();
    if (!normalized) {
        return '';
    }
    if (normalized === 'SPIRIT_STONE' || normalized === 'STONE' || normalized === 'STONES') {
        return 'SPIRIT_STONES';
    }
    return normalized;
}

function describeReward(reward) {
    const type = normalizeRewardType(reward?.itemType ?? reward?.type ?? reward?.rewardType);
    const itemId = reward?.itemId ?? reward?.id ?? reward?.rewardId;
    const quantity = toNumber(reward?.quantity ?? reward?.amount ?? reward?.count ?? reward?.value, 0);

    if (!type || quantity <= 0) {
        return '';
    }

    switch (type) {
        case 'SPIRIT_STONES':
            return `灵石 x${quantity}`;
        case 'EXP':
            return `经验 x${quantity}`;
        case 'ITEM':
            return `${itemId == null ? '物品' : `物品#${itemId}`} x${quantity}`;
        case 'EQUIPMENT':
            return `${itemId == null ? '装备' : `装备#${itemId}`} x${quantity}`;
        default:
            return `${type} x${quantity}`;
    }
}

function buildRewardDescription(rewardsRaw) {
    const parsed = safeParseJson(rewardsRaw);
    const rewards = Array.isArray(parsed)
        ? parsed
        : parsed && typeof parsed === 'object' && Array.isArray(parsed.rewards)
            ? parsed.rewards
            : parsed && typeof parsed === 'object'
                ? [parsed]
                : [];

    const rewardDescriptions = rewards.map(describeReward).filter(Boolean);
    if (rewardDescriptions.length > 0) {
        return rewardDescriptions.join(', ');
    }

    if (typeof rewardsRaw === 'string' && rewardsRaw.trim()) {
        return rewardsRaw;
    }

    return '暂无奖励';
}

export class ActivityService {
    constructor() {
        this.activities = [];
        this.myActivities = [];
        this.progressRecords = [];
    }

    async refreshData() {
        try {
            const [activitiesResponse, progressResponse] = await Promise.all([
                gameAPI.getAllActivities(),
                gameAPI.getMyActivityProgress()
            ]);

            if (!activitiesResponse?.success) {
                throw new Error(activitiesResponse?.message || '加载活动失败');
            }
            if (!progressResponse?.success) {
                throw new Error(progressResponse?.message || '加载活动进度失败');
            }

            const progressRecords = progressResponse.data || [];
            const progressMap = new Map(
                progressRecords.map(record => [Number(record.activityId), record])
            );

            this.progressRecords = progressRecords;
            this.activities = (activitiesResponse.data || [])
                .map(activity => this.normalizeActivity(activity, progressMap.get(Number(activity.id))))
                .sort((left, right) => new Date(right.startTime) - new Date(left.startTime));
            this.myActivities = this.activities
                .filter(activity => activity.participated)
                .sort((left, right) =>
                    Number(right.canClaim) - Number(left.canClaim) ||
                    Number(right.completed) - Number(left.completed) ||
                    new Date(right.startTime) - new Date(left.startTime));

            return {
                activities: this.activities,
                myActivities: this.myActivities
            };
        } catch (error) {
            toast.error(`加载活动失败：${error.message}`);
            throw error;
        }
    }

    async getActivities() {
        if (this.activities.length === 0) {
            await this.refreshData();
        }
        return this.activities;
    }

    async getMyActivities() {
        if (this.activities.length === 0 && this.myActivities.length === 0) {
            await this.refreshData();
        }
        return this.myActivities;
    }

    async participateActivity(activityId) {
        try {
            const response = await gameAPI.participateActivity(activityId);
            if (!response?.success) {
                throw new Error(response?.message || '参与活动失败');
            }
            toast.success(response.message || '参与活动成功');
            await this.refreshData();
            return response.data;
        } catch (error) {
            toast.error(`参与活动失败：${error.message}`);
            throw error;
        }
    }

    async claimReward(activityId) {
        try {
            const response = await gameAPI.claimActivityReward(activityId);
            if (!response?.success) {
                throw new Error(response?.message || '领取奖励失败');
            }
            toast.success(response.message || '领取奖励成功');
            await this.refreshData();
            return response.data;
        } catch (error) {
            toast.error(`领取奖励失败：${error.message}`);
            throw error;
        }
    }

    getActivityById(activityId) {
        return this.activities.find(activity => Number(activity.id) === Number(activityId)) || null;
    }

    normalizeActivity(activity, progressRecord = null) {
        const progressInfo = parseProgress(progressRecord?.progress);
        const rulesInfo = parseRules(activity?.rules);
        const usesScore = Number.isFinite(rulesInfo.scoreTarget) && rulesInfo.scoreTarget > 0;
        const target = usesScore
            ? rulesInfo.scoreTarget
            : Number.isFinite(rulesInfo.target) && rulesInfo.target > 0
                ? rulesInfo.target
                : 0;
        const currentProgress = usesScore ? progressInfo.score : progressInfo.value;
        const completed = Boolean(progressRecord?.completed) || (target > 0 && currentProgress >= target);

        return {
            ...activity,
            participated: Boolean(progressRecord),
            claimed: Boolean(progressRecord?.rewarded),
            completed,
            canClaim: Boolean(progressRecord) && completed && !progressRecord?.rewarded,
            usesScore,
            progress: currentProgress,
            rawProgressValue: progressInfo.value,
            score: progressInfo.score,
            target,
            progressDisplay: target > 0 ? `${currentProgress}/${target}` : `${currentProgress}`,
            progressPercent: target > 0
                ? Math.max(0, Math.min(100, Math.round((currentProgress / target) * 100)))
                : completed
                    ? 100
                    : 0,
            rewardDescription: buildRewardDescription(activity?.rewards)
        };
    }
}

export const activityService = new ActivityService();
