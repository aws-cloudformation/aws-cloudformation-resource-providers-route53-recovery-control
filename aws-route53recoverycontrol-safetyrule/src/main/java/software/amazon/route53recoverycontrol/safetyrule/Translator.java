package software.amazon.route53recoverycontrol.safetyrule;

import software.amazon.awssdk.services.route53recoverycontrolconfig.model.AssertionRuleUpdate;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.CreateSafetyRuleRequest;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.CreateSafetyRuleResponse;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DeleteSafetyRuleRequest;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DescribeSafetyRuleRequest;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DescribeSafetyRuleResponse;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.GatingRuleUpdate;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.ListSafetyRulesRequest;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.ListSafetyRulesResponse;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.NewAssertionRule;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.NewGatingRule;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.RuleConfig;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.UpdateSafetyRuleRequest;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.UpdateSafetyRuleResponse;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class is a centralized placeholder for
 *  - api request construction
 *  - object translation to/from aws sdk
 *  - resource model construction for read/list handlers
 */

public class Translator {

  /**
   * Request to create a resource
   * @param model resource model
   * @return awsRequest the aws service request to create a resource
   */
  static CreateSafetyRuleRequest translateToCreateRequest(final ResourceModel model) {

    if (model.getAssertionRule() != null) {
      return CreateSafetyRuleRequest.builder()
              .assertionRule(buildCreateAssertionRuleRequest(model))
              .build();
    }

    return CreateSafetyRuleRequest.builder()
            .gatingRule(buildCreateGatingRuleRequest(model))
            .build();
  }

  static NewGatingRule buildCreateGatingRuleRequest(ResourceModel model) {
    return NewGatingRule.builder()
            .controlPanelArn(model.getControlPanelArn())
            .name(model.getName())
            .gatingControls(model.getGatingRule().getGatingControls())
            .waitPeriodMs(model.getGatingRule().getWaitPeriodMs())
            .ruleConfig(buildRuleConfigRequest(model.getRuleConfig()))
            .targetControls(model.getGatingRule().getTargetControls())
            .build();
  }

  static NewAssertionRule buildCreateAssertionRuleRequest(ResourceModel model) {
    return NewAssertionRule.builder()
            .controlPanelArn(model.getControlPanelArn())
            .name(model.getName())
            .assertedControls(model.getAssertionRule().getAssertedControls())
            .waitPeriodMs(model.getAssertionRule().getWaitPeriodMs())
            .ruleConfig(buildRuleConfigRequest(model.getRuleConfig()))
            .build();
  }

  static RuleConfig buildRuleConfigRequest(final software.amazon.route53recoverycontrol.safetyrule.RuleConfig ruleConfig) {
    return RuleConfig.builder()
            .inverted(ruleConfig.getInverted())
            .threshold(ruleConfig.getThreshold())
            .type(ruleConfig.getType())
            .build();
  }

  static ResourceModel translateFromCreateResponse(final CreateSafetyRuleResponse response) {

    if (response.assertionRule() != null) {
      software.amazon.awssdk.services.route53recoverycontrolconfig.model.AssertionRule aRule = response.assertionRule();

      return ResourceModel.builder()
              .controlPanelArn(aRule.controlPanelArn())
              .safetyRuleArn(aRule.safetyRuleArn())
              .name(aRule.name())
              .status(aRule.statusAsString())
              .ruleConfig(translateFromReadResponseRuleConfig(aRule.ruleConfig()))
              .assertionRule(translateFromReadResponseAssertionRule(aRule))
              .build();
    }

    software.amazon.awssdk.services.route53recoverycontrolconfig.model.GatingRule gRule = response.gatingRule();
    return ResourceModel.builder()
            .controlPanelArn(gRule.controlPanelArn())
            .safetyRuleArn(gRule.safetyRuleArn())
            .name(gRule.name())
            .status(gRule.statusAsString())
            .ruleConfig(translateFromReadResponseRuleConfig(gRule.ruleConfig()))
            .gatingRule(translateFromReadResponseGatingRule(response.gatingRule()))
            .build();
  }

  /**
   * Request to read a resource
   * @param model resource model
   * @return awsRequest the aws service request to describe a resource
   */
  static DescribeSafetyRuleRequest translateToReadRequest(final ResourceModel model) {
    return DescribeSafetyRuleRequest.builder()
            .safetyRuleArn(model.getSafetyRuleArn())
            .build();
  }

  /**
   * Translates resource object from sdk into a resource model
   * @param response the aws service describe resource response
   * @return model resource model
   */
  static ResourceModel translateFromReadResponse(final DescribeSafetyRuleResponse response) {
    if (response.assertionRule() != null) {
      software.amazon.awssdk.services.route53recoverycontrolconfig.model.AssertionRule aRule = response.assertionRule();
      return ResourceModel.builder()
              .name(aRule.name())
              .controlPanelArn(aRule.controlPanelArn())
              .status(aRule.statusAsString())
              .safetyRuleArn(aRule.safetyRuleArn())
              .ruleConfig(translateFromReadResponseRuleConfig(aRule.ruleConfig()))
              .assertionRule(translateFromReadResponseAssertionRule(aRule))
              .build();
    }

    software.amazon.awssdk.services.route53recoverycontrolconfig.model.GatingRule gRule = response.gatingRule();
    return ResourceModel.builder()
            .name(gRule.name())
            .safetyRuleArn(gRule.safetyRuleArn())
            .controlPanelArn(gRule.controlPanelArn())
            .status(gRule.statusAsString())
            .ruleConfig(translateFromReadResponseRuleConfig(gRule.ruleConfig()))
            .gatingRule(translateFromReadResponseGatingRule(gRule))
            .build();
  }

  static AssertionRule translateFromReadResponseAssertionRule(final software.amazon.awssdk.services.route53recoverycontrolconfig.model.AssertionRule assertionRule) {
    return AssertionRule.builder()
            .waitPeriodMs(assertionRule.waitPeriodMs())
            .assertedControls(assertionRule.assertedControls())
            .build();
  }

  static GatingRule translateFromReadResponseGatingRule(final software.amazon.awssdk.services.route53recoverycontrolconfig.model.GatingRule gatingRule) {
    return GatingRule.builder()
            .gatingControls(gatingRule.gatingControls())
            .targetControls(gatingRule.targetControls())
            .waitPeriodMs(gatingRule.waitPeriodMs())
            .build();
  }

  static software.amazon.route53recoverycontrol.safetyrule.RuleConfig translateFromReadResponseRuleConfig(final RuleConfig ruleConfig) {
    return software.amazon.route53recoverycontrol.safetyrule.RuleConfig.builder()
            .inverted(ruleConfig.inverted())
            .threshold(ruleConfig.threshold())
            .type(ruleConfig.typeAsString())
            .build();
  }

  /**
   * Request to delete a resource
   * @param model resource model
   * @return awsRequest the aws service request to delete a resource
   */
  static DeleteSafetyRuleRequest translateToDeleteRequest(final ResourceModel model) {
    return DeleteSafetyRuleRequest.builder()
            .safetyRuleArn(model.getSafetyRuleArn())
            .build();
  }

  /**
   * Request to update properties of a previously created resource
   * @param model resource model
   * @return awsRequest the aws service request to modify a resource
   */
  static UpdateSafetyRuleRequest translateToUpdateRequest(final ResourceModel model) {
    if (model.getAssertionRule() != null) {
      return UpdateSafetyRuleRequest.builder()
              .assertionRuleUpdate(translateToUpdateRequestAssertionRule(model))
              .build();
    }

    return UpdateSafetyRuleRequest.builder()
            .gatingRuleUpdate(translateToUpdateRequestGatingRule(model))
            .build();
  }

  static AssertionRuleUpdate translateToUpdateRequestAssertionRule(ResourceModel model) {
    return AssertionRuleUpdate.builder()
            .name(model.getName())
            .safetyRuleArn(model.getSafetyRuleArn())
            .waitPeriodMs(model.getAssertionRule().getWaitPeriodMs())
            .build();
  }

  static GatingRuleUpdate translateToUpdateRequestGatingRule( ResourceModel model) {
    return GatingRuleUpdate.builder()
            .name(model.getName())
            .safetyRuleArn(model.getSafetyRuleArn())
            .waitPeriodMs(model.getGatingRule().getWaitPeriodMs())
            .build();
  }


  static ResourceModel translateFromUpdateResponse(UpdateSafetyRuleResponse response) {
    if (response.assertionRule() != null) {
      software.amazon.awssdk.services.route53recoverycontrolconfig.model.AssertionRule aRule = response.assertionRule();

      return ResourceModel.builder()
              .controlPanelArn(aRule.controlPanelArn())
              .safetyRuleArn(aRule.safetyRuleArn())
              .status(aRule.statusAsString())
              .name(aRule.name())
              .ruleConfig(translateFromReadResponseRuleConfig(aRule.ruleConfig()))
              .assertionRule(translateFromReadResponseAssertionRule(aRule))
              .build();
    }

    software.amazon.awssdk.services.route53recoverycontrolconfig.model.GatingRule gRule = response.gatingRule();
    return ResourceModel.builder()
            .controlPanelArn(gRule.controlPanelArn())
            .safetyRuleArn(gRule.safetyRuleArn())
            .status(gRule.statusAsString())
            .name(gRule.name())
            .ruleConfig(translateFromReadResponseRuleConfig(gRule.ruleConfig()))
            .gatingRule(translateFromReadResponseGatingRule(gRule))
            .build();
  }

  /**
   * Request to list resources
   * @param nextToken token passed to the aws service list resources request
   * @return awsRequest the aws service request to list resources within aws account
   */
  static ListSafetyRulesRequest translateToListRequest(ResourceModel model, final String nextToken) {
      return ListSafetyRulesRequest.builder()
              .controlPanelArn(model.getControlPanelArn())
              .nextToken(nextToken)
              .build();

  }

  /**
   * Translates resource objects from sdk into a resource model (primary identifier only)
   * @param response the aws service describe resource response
   * @return list of resource models
   */
  static List<ResourceModel> translateFromListRequest(final ListSafetyRulesResponse response) {
    return streamOfOrEmpty(response.safetyRules())
        .map(safetyRule -> {
          if (safetyRule.assertion() != null) {
            software.amazon.awssdk.services.route53recoverycontrolconfig.model.AssertionRule rule = safetyRule.assertion();
            return ResourceModel.builder()
                    .safetyRuleArn(rule.safetyRuleArn())
                    .status(rule.statusAsString())
                    .controlPanelArn(rule.controlPanelArn())
                    .name(rule.name())
                    .ruleConfig(translateFromReadResponseRuleConfig(rule.ruleConfig()))
                    .assertionRule(translateFromListRequestAssertionRule(safetyRule.assertion()))
                    .build();
          }
          software.amazon.awssdk.services.route53recoverycontrolconfig.model.GatingRule gRule = safetyRule.gating();
          return ResourceModel.builder()
                  .safetyRuleArn(gRule.safetyRuleArn())
                  .name(gRule.name())
                  .controlPanelArn(gRule.controlPanelArn())
                  .status(gRule.statusAsString())
                  .ruleConfig(translateFromReadResponseRuleConfig(gRule.ruleConfig()))
                  .gatingRule(translateFromListResponseGatingRule(gRule))
                  .build();
        })
        .collect(Collectors.toList());
  }

  static AssertionRule translateFromListRequestAssertionRule(software.amazon.awssdk.services.route53recoverycontrolconfig.model.AssertionRule assertionRule) {
    return AssertionRule.builder()
            .assertedControls(assertionRule.assertedControls())
            .waitPeriodMs(assertionRule.waitPeriodMs())
            .build();
  }

  static GatingRule translateFromListResponseGatingRule(software.amazon.awssdk.services.route53recoverycontrolconfig.model.GatingRule gatingRule) {
    return GatingRule.builder()
            .targetControls(gatingRule.targetControls())
            .gatingControls(gatingRule.gatingControls())
            .waitPeriodMs(gatingRule.waitPeriodMs())
            .build();
  }

  private static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
    return Optional.ofNullable(collection)
        .map(Collection::stream)
        .orElseGet(Stream::empty);
  }
}
