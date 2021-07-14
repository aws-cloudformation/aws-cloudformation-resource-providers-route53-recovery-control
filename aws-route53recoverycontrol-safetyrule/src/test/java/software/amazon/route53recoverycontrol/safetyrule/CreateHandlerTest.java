package software.amazon.route53recoverycontrol.safetyrule;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import software.amazon.awssdk.services.route53recoverycontrolconfig.Route53RecoveryControlConfigClient;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.CreateSafetyRuleResponse;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DescribeControlPanelResponse;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DescribeRoutingControlResponse;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DescribeSafetyRuleResponse;
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


@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest extends AbstractTestBase {

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
        final CreateHandler handler = new CreateHandler();

        List<String> controls = new ArrayList<>();
        controls.add("endpoint1");
        controls.add("endpoint2");

        RuleConfig config = RuleConfig.builder()
                .type("OR")
                .threshold(5)
                .inverted(false)
                .build();

        final AssertionRule aRule = AssertionRule.builder()
                .assertedControls(controls)
                .waitPeriodMs(1)
                .build();

        final ResourceModel model = ResourceModel.builder()
                .name("assertionRule")
                .controlPanelArn("controlPanelArn")
                .ruleConfig(config)
                .assertionRule(aRule)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final software.amazon.awssdk.services.route53recoverycontrolconfig.model.RuleConfig ruleConfig =
                software.amazon.awssdk.services.route53recoverycontrolconfig.model.RuleConfig.builder()
                        .type("OR")
                        .threshold(5)
                        .inverted(false)
                        .build();

        final software.amazon.awssdk.services.route53recoverycontrolconfig.model.AssertionRule aRespRule =
                software.amazon.awssdk.services.route53recoverycontrolconfig.model.AssertionRule.builder()
                .name("assertionRule")
                .controlPanelArn("controlPanelArn")
                .assertedControls(controls)
                .waitPeriodMs(1)
                .ruleConfig(ruleConfig)
                .status("DEPLOYED")
                .safetyRuleArn("safetyRuleArn")
                .build();

        CreateSafetyRuleResponse createResponse = CreateSafetyRuleResponse.builder()
                .assertionRule(aRespRule)
                .build();

        ResourceModel createModel = Translator.translateFromCreateResponse(createResponse);

        CallbackContext context = new CallbackContext();
        context.setSafetyRuleArn("safetyRuleArn");
        context.setControlPanelArn("controlPanelArn");
        context.setStatus("DEPLOYED");
        context.setName(createModel.getName());
        context.setRuleConfig(createModel.getRuleConfig());
        context.setAssertionRule(createModel.getAssertionRule());

        DescribeControlPanelResponse cpResponse = DescribeControlPanelResponse.builder().build();

        DescribeRoutingControlResponse rcResponse = DescribeRoutingControlResponse.builder().build();

        DescribeRoutingControlResponse rc2Response = DescribeRoutingControlResponse.builder().build();

        DescribeSafetyRuleResponse describeResponse = DescribeSafetyRuleResponse.builder()
                .assertionRule(aRespRule)
                .build();

        doReturn(cpResponse)
                .doReturn(rcResponse)
                .doReturn(rc2Response)
                .doReturn(createResponse)
                .doReturn(describeResponse)
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
