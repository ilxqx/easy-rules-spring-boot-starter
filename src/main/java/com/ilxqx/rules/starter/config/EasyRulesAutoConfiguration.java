package com.ilxqx.rules.starter.config;

import com.ilxqx.rules.starter.core.PlainRulesEngine;
import com.ilxqx.rules.starter.properties.EasyRulesProperties;
import com.ilxqx.rules.starter.support.EasyRulesTemplate;
import org.jeasy.rules.api.RuleListener;
import org.jeasy.rules.api.RulesEngine;
import org.jeasy.rules.api.RulesEngineListener;
import org.jeasy.rules.api.RulesEngineParameters;
import org.jeasy.rules.core.DefaultRulesEngine;
import org.jeasy.rules.core.InferenceRulesEngine;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto config
 *
 * @author venus
 * @version 1
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(EasyRulesProperties.class)
@ConditionalOnProperty(prefix = "easy.rules", name = "enabled", matchIfMissing = true)
public class EasyRulesAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "easy.rules", name = "rules-engine-type", havingValue = "default")
    @ConditionalOnMissingBean(RulesEngine.class)
    public RulesEngine defaultRulesEngine(EasyRulesProperties properties, ObjectProvider<RuleListener> ruleListeners, ObjectProvider<RulesEngineListener> rulesEngineListeners) {
        DefaultRulesEngine engine = new DefaultRulesEngine(
            new RulesEngineParameters()
                .priorityThreshold(properties.getRulePriorityThreshold())
                .skipOnFirstAppliedRule(properties.isSkipOnFirstAppliedRule())
                .skipOnFirstFailedRule(properties.isSkipOnFirstFailedRule())
                .skipOnFirstNonTriggeredRule(properties.isSkipOnFirstNonTriggeredRule())
        );
        ruleListeners.orderedStream().forEach(engine::registerRuleListener);
        rulesEngineListeners.orderedStream().forEach(engine::registerRulesEngineListener);
        return engine;
    }

    @Bean
    @ConditionalOnProperty(prefix = "easy.rules", name = "rules-engine-type", havingValue = "plain")
    @ConditionalOnMissingBean(RulesEngine.class)
    public RulesEngine plainRulesEngine(EasyRulesProperties properties, ObjectProvider<RuleListener> ruleListeners, ObjectProvider<RulesEngineListener> rulesEngineListeners) {
        PlainRulesEngine engine = new PlainRulesEngine();
        ruleListeners.orderedStream().forEach(engine::registerRuleListener);
        rulesEngineListeners.orderedStream().forEach(engine::registerRulesEngineListener);
        return engine;
    }

    @Bean
    @ConditionalOnProperty(prefix = "easy.rules", name = "rules-engine-type", havingValue = "inference")
    @ConditionalOnMissingBean(RulesEngine.class)
    public RulesEngine inferenceRulesEngine(EasyRulesProperties properties, ObjectProvider<RuleListener> ruleListeners, ObjectProvider<RulesEngineListener> rulesEngineListeners) {
        InferenceRulesEngine engine = new InferenceRulesEngine(
            new RulesEngineParameters()
                .priorityThreshold(properties.getRulePriorityThreshold())
                .skipOnFirstAppliedRule(properties.isSkipOnFirstAppliedRule())
                .skipOnFirstFailedRule(properties.isSkipOnFirstFailedRule())
                .skipOnFirstNonTriggeredRule(properties.isSkipOnFirstNonTriggeredRule())
        );
        ruleListeners.orderedStream().forEach(engine::registerRuleListener);
        rulesEngineListeners.orderedStream().forEach(engine::registerRulesEngineListener);
        return engine;
    }

    @Bean
    @ConditionalOnBean(RulesEngine.class)
    public EasyRulesTemplate easyRulesTemplate(RulesEngine rulesEngine, ApplicationContext applicationContext) {
        return new EasyRulesTemplate(rulesEngine, applicationContext);
    }
}
