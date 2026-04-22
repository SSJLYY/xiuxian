export { skillsService } from './SkillsService.js';
export { skillsUI } from './SkillsUI.js';

export function mountSkillsGlobals() {
    window.skillsUI = skillsUI;

    window.switchSkillTab = function(tab) {
        return skillsUI.switchSkillTab(tab);
    };

    window.loadMySkills = async function() {
        return skillsUI.loadMySkills();
    };

    window.loadAvailableSkills = async function() {
        return skillsUI.loadAvailableSkills();
    };

    window.learnSkill = async function(skillId) {
        return skillsUI.learnSkill(skillId);
    };

    window.equipSkill = async function(playerSkillId, slotNumber) {
        return skillsUI.equipSkill(playerSkillId, slotNumber);
    };

    window.unequipSkill = async function(playerSkillId) {
        return skillsUI.unequipSkill(playerSkillId);
    };

    window.upgradeSkill = async function(playerSkillId) {
        return skillsUI.upgradeSkill(playerSkillId);
    };

    window.switchComboTab = function(tab) {
        return skillsUI.switchComboTab(tab);
    };

    window.loadCombos = async function(availableOnly = true) {
        return skillsUI.loadCombos(availableOnly);
    };

    return skillsUI;
}
