package software.amazon.route53recoverycontrol.safetyrule;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import software.amazon.awssdk.services.route53recoverycontrolconfig.Route53RecoveryControlConfigClient;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DescribeSafetyRuleResponse;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.RuleConfig;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.UpdateSafetyRuleResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<Route53RecoveryControlConfigClient> proxyClient;

    @Mock
    Route53RecoveryControlConfigClient sdkClient;

    @BeforeEach
    public void setup() {
        proxy = spy(new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis()));
        sdkClient = mock(Route53RecoveryControlConfigClient.class);
        proxyClient = MOCK_PROXY(proxy, sdkClient);
    }

    @AfterEach
    public void tear_down() {
        verify(sdkClient, atLeastOnce()).serviceName();
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    public void handleRequest_SimpleSuccess() {
        final UpdateHandler handler = new UpdateHandler();

        final AssertionRule aRule = AssertionRule.builder()
                .waitPeriodMs(5)
                .build();

        final ResourceModel model = ResourceModel.builder()
                .name("safetyRuleName")
                .safetyRuleArn("safetyRuleArn")
                .assertionRule(aRule)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        List<String> controls = new ArrayList<>();

        controls.add("endpoint1");
        controls.add("endpoint2");

        RuleConfig config = RuleConfig.builder()
                .type("AND")
                .inverted(false)
                .threshold(1)
                .build();

        software.amazon.awssdk.services.route53recoverycontrolconfig.model.AssertionRule respRule =
                software.amazon.awssdk.services.route53recoverycontrolconfig.model.AssertionRule.builder()
                        .controlPanelArn("controlPanelArn")
                        .status("DEPLOYED")
                        .name("safetyRuleName")
                        .waitPeriodMs(5)
                        .assertedControls(controls)
                        .safetyRuleArn("safetyRuleArn")
                        .ruleConfig(config)
                        .build();

        UpdateSafetyRuleResponse updateResponse = UpdateSafetyRuleResponse.builder()
                .assertionRule(respRule)
                .build();

        ResourceModel updatedModel = Translator.translateFromUpdateResponse(updateResponse);

        DescribeSafetyRuleResponse describeSafetyRuleResponse = DescribeSafetyRuleResponse.builder()
                .assertionRule(respRule)
                .build();

        CallbackContext context = new CallbackContext();
        context.setName(updatedModel.getName());
        context.setSafetyRuleArn(updatedModel.getSafetyRuleArn());
        context.setStatus(updatedModel.getStatus());
        context.setControlPanelArn(updatedModel.getControlPanelArn());
        context.setRuleConfig(updatedModel.getRuleConfig());
        context.setAssertionRule(updatedModel.getAssertionRule());

        doReturn(updateResponse)
                .doReturn(describeSafetyRuleResponse)
                .when(proxy)
                .injectCredentialsAndInvokeV2(
                        ArgumentMatchers.any(),
                        ArgumentMatchers.any()
                );

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, context, proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }
}
