package com.ilxqx.rules.starter.core;

import com.ilxqx.rules.starter.exception.RuleEvaluateException;
import com.ilxqx.rules.starter.exception.RuleExecutionException;
import org.jeasy.rules.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The plain engine implementation, which can throw an exception when rules encounter an exception.
 *
 * @author venus
 * @version 1
 */
public class PlainRulesEngine implements RulesEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlainRulesEngine.class);

    List<RuleListener> ruleListeners;
    List<RulesEngineListener> rulesEngineListeners;

    /**
     * Create a new {@link PlainRulesEngine} with default parameters.
     */
    public PlainRulesEngine() {
        this.ruleListeners = new ArrayList<>();
        this.rulesEngineListeners = new ArrayList<>();
    }

    /**
     * Return the rules engine parameters.
     *
     * @return The rules engine parameters
     */
    @Override
    public RulesEngineParameters getParameters() {
        // no op
        return null;
    }

    /**
     * Return the list of registered rule listeners.
     *
     * @return the list of registered rule listeners
     */
    @Override
    public List<RuleListener> getRuleListeners() {
        return this.ruleListeners;
    }

    /**
     * Return the list of registered rules engine listeners.
     *
     * @return the list of registered rules engine listeners
     */
    @Override
    public List<RulesEngineListener> getRulesEngineListeners() {
        return this.rulesEngineListeners;
    }

    public void registerRuleListener(RuleListener ruleListener) {
        ruleListeners.add(ruleListener);
    }

    public void registerRuleListeners(List<RuleListener> ruleListeners) {
        this.ruleListeners.addAll(ruleListeners);
    }

    public void registerRulesEngineListener(RulesEngineListener rulesEngineListener) {
        rulesEngineListeners.add(rulesEngineListener);
    }

    public void registerRulesEngineListeners(List<RulesEngineListener> rulesEngineListeners) {
        this.rulesEngineListeners.addAll(rulesEngineListeners);
    }

    @Override
    public void fire(Rules rules, Facts facts) {
        triggerListenersBeforeRules(rules, facts);
        doFire(rules, facts);
        triggerListenersAfterRules(rules, facts);
    }

    void doFire(Rules rules, Facts facts) {
        if (rules.isEmpty()) {
            LOGGER.warn("No rules registered! Nothing to apply");
            return;
        }
        log(rules);
        log(facts);
        LOGGER.debug("Rules evaluation started");
        for (Rule rule : rules) {
            final String name = rule.getName();
            final int priority = rule.getPriority();
            if (!shouldBeEvaluated(rule, facts)) {
                LOGGER.debug("Rule '{}' has been skipped before being evaluated", name);
                continue;
            }
            boolean evaluationResult = false;
            try {
                evaluationResult = rule.evaluate(facts);
            } catch (RuntimeException exception) {
                LOGGER.error("Rule '{}' evaluated with error: {}", name, exception.getMessage());
                triggerListenersOnEvaluationError(rule, facts, exception);
                // throw exception
                throw new RuleEvaluateException("Rule '" + name + "' evaluated with error: " + exception.getMessage(), exception);
            }
            if (evaluationResult) {
                LOGGER.debug("Rule '{}' triggered", name);
                triggerListenersAfterEvaluate(rule, facts, true);
                try {
                    triggerListenersBeforeExecute(rule, facts);
                    rule.execute(facts);
                    LOGGER.debug("Rule '{}' performed successfully", name);
                    triggerListenersOnSuccess(rule, facts);
                } catch (Exception exception) {
                    if (exception instanceof InvocationTargetException && exception.getCause() != null && exception.getCause() instanceof Exception) {
                        exception = (Exception) exception.getCause();
                    }
                    LOGGER.warn("Rule '{}' performed with error: {}", name, exception.getMessage());
                    triggerListenersOnFailure(rule, exception, facts);
                    // throw exception
                    throw new RuleExecutionException("Rule '" + name + "' performed with error: " + exception.getMessage(), exception);
                }
            } else {
                LOGGER.debug("Rule '{}' has been evaluated to false, it has not been executed", name);
                triggerListenersAfterEvaluate(rule, facts, false);
            }
        }
    }

    private void log(Rules rules) {
        LOGGER.debug("Registered rules:");
        for (Rule rule : rules) {
            LOGGER.debug("Rule { name = '{}', description = '{}', priority = '{}'}",
                rule.getName(), rule.getDescription(), rule.getPriority());
        }
    }

    private void log(Facts facts) {
        LOGGER.debug("Known facts:");
        for (Fact<?> fact : facts) {
            LOGGER.debug("{}", fact);
        }
    }

    @Override
    public Map<Rule, Boolean> check(Rules rules, Facts facts) {
        triggerListenersBeforeRules(rules, facts);
        Map<Rule, Boolean> result = doCheck(rules, facts);
        triggerListenersAfterRules(rules, facts);
        return result;
    }

    private Map<Rule, Boolean> doCheck(Rules rules, Facts facts) {
        LOGGER.debug("Checking rules");
        Map<Rule, Boolean> result = new HashMap<>();
        for (Rule rule : rules) {
            if (shouldBeEvaluated(rule, facts)) {
                result.put(rule, rule.evaluate(facts));
            }
        }
        return result;
    }

    private void triggerListenersOnFailure(final Rule rule, final Exception exception, Facts facts) {
        ruleListeners.forEach(ruleListener -> ruleListener.onFailure(rule, facts, exception));
    }

    private void triggerListenersOnSuccess(final Rule rule, Facts facts) {
        ruleListeners.forEach(ruleListener -> ruleListener.onSuccess(rule, facts));
    }

    private void triggerListenersBeforeExecute(final Rule rule, Facts facts) {
        ruleListeners.forEach(ruleListener -> ruleListener.beforeExecute(rule, facts));
    }

    private boolean triggerListenersBeforeEvaluate(Rule rule, Facts facts) {
        return ruleListeners.stream().allMatch(ruleListener -> ruleListener.beforeEvaluate(rule, facts));
    }

    private void triggerListenersAfterEvaluate(Rule rule, Facts facts, boolean evaluationResult) {
        ruleListeners.forEach(ruleListener -> ruleListener.afterEvaluate(rule, facts, evaluationResult));
    }

    private void triggerListenersOnEvaluationError(Rule rule, Facts facts, Exception exception) {
        ruleListeners.forEach(ruleListener -> ruleListener.onEvaluationError(rule, facts, exception));
    }

    private void triggerListenersBeforeRules(Rules rule, Facts facts) {
        rulesEngineListeners.forEach(rulesEngineListener -> rulesEngineListener.beforeEvaluate(rule, facts));
    }

    private void triggerListenersAfterRules(Rules rule, Facts facts) {
        rulesEngineListeners.forEach(rulesEngineListener -> rulesEngineListener.afterExecute(rule, facts));
    }

    private boolean shouldBeEvaluated(Rule rule, Facts facts) {
        return triggerListenersBeforeEvaluate(rule, facts);
    }
}
