package software.amazon.route53recoverycontrol.safetyrule;

import software.amazon.cloudformation.proxy.StdCallbackContext;

@lombok.Getter
@lombok.Setter
@lombok.ToString
@lombok.EqualsAndHashCode(callSuper = true)
public class CallbackContext extends StdCallbackContext {
    String safetyRuleArn;
    String status;
    String name;
    String controlPanelArn;
    RuleConfig ruleConfig;
    AssertionRule assertionRule;
    GatingRule gatingRule;
}
