package software.amazon.route53recoverycontrol.safetyrule;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import software.amazon.awssdk.services.route53recoverycontrolconfig.Route53RecoveryControlConfigClient;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DescribeSafetyRuleResponse;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.RuleConfig;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest extends AbstractTestBase {

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
        verifyNoMoreInteractions(sdkClient);
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final ReadHandler handler = new ReadHandler();

        final AssertionRule aRule = AssertionRule.builder()
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

        RuleConfig config = RuleConfig.builder()
                .threshold(0)
                .inverted(true)
                .type("ATLEAST")
                .build();

        software.amazon.awssdk.services.route53recoverycontrolconfig.model.AssertionRule asRule =
                software.amazon.awssdk.services.route53recoverycontrolconfig.model.AssertionRule.builder()
                        .name("safetyRule")
                        .status("DEPLOYED")
                        .controlPanelArn("controlPanelArn")
                        .safetyRuleArn("safetyRuleArn")
                        .waitPeriodMs(0)
                        .assertedControls(controls)
                        .ruleConfig(config)
                        .build();

        DescribeSafetyRuleResponse describeResponse = DescribeSafetyRuleResponse.builder()
                .assertionRule(asRule)
                .build();

        doReturn(describeResponse)
                .when(proxy)
                .injectCredentialsAndInvokeV2(
                        ArgumentMatchers.any(),
                        ArgumentMatchers.any()
                );

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }
}
