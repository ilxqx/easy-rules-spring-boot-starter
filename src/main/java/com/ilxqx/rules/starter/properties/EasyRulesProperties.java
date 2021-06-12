package com.ilxqx.rules.starter.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Objects;

/**
 * easy rules config properties for rules engine.
 *
 * @author venus
 * @version 1
 */
@ConfigurationProperties(prefix = "easy.rules")
public class EasyRulesProperties {

    /**
     * enable easy rule
     */
    private boolean enabled = true;
    /**
     * The rulePriorityThreshold parameter tells the engine to skip next rules if priority exceeds the defined threshold.
     */
    private int rulePriorityThreshold = Integer.MAX_VALUE;
    /**
     * The skipOnFirstAppliedRule parameter tells the engine to skip next rules when a rule is applied.
     */
    private boolean skipOnFirstAppliedRule = false;
    /**
     * The skipOnFirstFailedRule parameter tells the engine to skip next rules when a rule fails.
     */
    private boolean skipOnFirstFailedRule = false;
    /**
     * The skipOnFirstNonTriggeredRule parameter tells the engine to skip next rules when a rule is not triggered.
     */
    private boolean skipOnFirstNonTriggeredRule = false;
    /**
     * The rulesEngineType parameter tells autoconfiguration which engine implementation to use.
     */
    private RulesEngineType rulesEngineType;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getRulePriorityThreshold() {
        return rulePriorityThreshold;
    }

    public void setRulePriorityThreshold(int rulePriorityThreshold) {
        this.rulePriorityThreshold = rulePriorityThreshold;
    }

    public boolean isSkipOnFirstAppliedRule() {
        return skipOnFirstAppliedRule;
    }

    public void setSkipOnFirstAppliedRule(boolean skipOnFirstAppliedRule) {
        this.skipOnFirstAppliedRule = skipOnFirstAppliedRule;
    }

    public boolean isSkipOnFirstFailedRule() {
        return skipOnFirstFailedRule;
    }

    public void setSkipOnFirstFailedRule(boolean skipOnFirstFailedRule) {
        this.skipOnFirstFailedRule = skipOnFirstFailedRule;
    }

    public boolean isSkipOnFirstNonTriggeredRule() {
        return skipOnFirstNonTriggeredRule;
    }

    public void setSkipOnFirstNonTriggeredRule(boolean skipOnFirstNonTriggeredRule) {
        this.skipOnFirstNonTriggeredRule = skipOnFirstNonTriggeredRule;
    }

    public RulesEngineType getRulesEngineType() {
        return rulesEngineType;
    }

    public void setRulesEngineType(RulesEngineType rulesEngineType) {
        this.rulesEngineType = rulesEngineType;
    }

    enum RulesEngineType {
        DEFAULT,
        PLAIN,
        INFERENCE
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EasyRulesProperties that = (EasyRulesProperties) o;
        return enabled == that.enabled && rulePriorityThreshold == that.rulePriorityThreshold && skipOnFirstAppliedRule == that.skipOnFirstAppliedRule && skipOnFirstFailedRule == that.skipOnFirstFailedRule && skipOnFirstNonTriggeredRule == that.skipOnFirstNonTriggeredRule && rulesEngineType == that.rulesEngineType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled, rulePriorityThreshold, skipOnFirstAppliedRule, skipOnFirstFailedRule, skipOnFirstNonTriggeredRule, rulesEngineType);
    }
}
