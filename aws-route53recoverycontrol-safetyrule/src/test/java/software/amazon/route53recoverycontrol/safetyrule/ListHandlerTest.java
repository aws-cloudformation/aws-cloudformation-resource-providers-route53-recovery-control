package software.amazon.route53recoverycontrol.safetyrule;

import software.amazon.awssdk.services.route53recoverycontrolconfig.model.ListSafetyRulesResponse;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.Rule;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.RuleConfig;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class ListHandlerTest {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final ListHandler handler = new ListHandler();

        final software.amazon.route53recoverycontrol.safetyrule.AssertionRule aRule = AssertionRule.builder()
                .build();

        final ResourceModel model = ResourceModel.builder()
                .assertionRule(aRule)
                .safetyRuleArn("safetyRuleArn")
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        List<String> controls = new ArrayList<>();

        controls.add("endpoint1");
        controls.add("endpoint2");

        software.amazon.awssdk.services.route53recoverycontrolconfig.model.RuleConfig config = RuleConfig.builder()
                .inverted(true)
                .threshold(2)
                .type("AND")
                .build();

        final software.amazon.awssdk.services.route53recoverycontrolconfig.model.AssertionRule rule =
                software.amazon.awssdk.services.route53recoverycontrolconfig.model.AssertionRule.builder()
                        .safetyRuleArn("safetyRuleArn")
                        .waitPeriodMs(0)
                        .assertedControls(controls)
                        .controlPanelArn("controlPanelArn")
                        .name("mySafetyRule")
                        .status("DEPLOYED")
                        .ruleConfig(config)
                        .build();

        List<Rule> assertionRules = new ArrayList<>();

        Rule rRule = Rule.builder()
                .assertion(rule)
                .build();

        assertionRules.add(rRule);

        ListSafetyRulesResponse listResponse = ListSafetyRulesResponse.builder()
                .nextToken("nextToken")
                .safetyRules(assertionRules)
                .build();

        doReturn(listResponse)
                .when(proxy)
                .injectCredentialsAndInvokeV2(
                        ArgumentMatchers.any(),
                        ArgumentMatchers.any()
                );

        final ProgressEvent<ResourceModel, CallbackContext> response =
            handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNotNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }
}
