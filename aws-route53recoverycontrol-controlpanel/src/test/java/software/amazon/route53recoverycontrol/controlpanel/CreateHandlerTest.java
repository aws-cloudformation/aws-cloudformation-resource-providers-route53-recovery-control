package software.amazon.route53recoverycontrol.controlpanel;

import java.time.Duration;
import java.util.List;

import software.amazon.awssdk.services.route53recoverycontrolconfig.Route53RecoveryControlConfigClient;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.ControlPanel;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.CreateClusterRequest;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.CreateControlPanelRequest;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.CreateControlPanelResponse;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DescribeClusterRequest;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DescribeClusterResponse;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DescribeControlPanelResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.google.common.collect.ImmutableList;

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
        verifyNoMoreInteractions(sdkClient);
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final CreateHandler handler = new CreateHandler();

        final ResourceModel model = ResourceModel.builder()
                .clusterArn("aClusterArn")
                .name("myControlPanel")
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ControlPanel controlPanel = ControlPanel.builder()
                .clusterArn("aClusterArn")
                .name("myControlPanel")
                .defaultControlPanel((true))
                .routingControlCount(1)
                .status("DEPLOYED")
                .controlPanelArn("ControlPanelArn")
                .build();

        final DescribeClusterResponse clusterResponse = DescribeClusterResponse.builder().build();

        final CreateControlPanelResponse createResponse = CreateControlPanelResponse.builder()
                .controlPanel(controlPanel)
                .build();

        final DescribeControlPanelResponse describeResponse = DescribeControlPanelResponse.builder().
                controlPanel(controlPanel).build();

        doReturn(clusterResponse)
                .doReturn(createResponse)
                .doReturn(describeResponse)
                .when(proxy)
                .injectCredentialsAndInvokeV2(
                        ArgumentMatchers.any(),
                        ArgumentMatchers.any()
                );

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel().getName()).isEqualTo(request.getDesiredResourceState().getName());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_SimpleSuccessWithTags() {
        final CreateHandler handler = new CreateHandler();
        final List<Tag> tags = ImmutableList.of(Tag.builder().key("key").value("val").build());

        final ResourceModel model = ResourceModel.builder()
            .clusterArn("aClusterArn")
            .name("myControlPanel")
            .tags(tags)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ControlPanel controlPanel = ControlPanel.builder()
            .clusterArn("aClusterArn")
            .name("myControlPanel")
            .defaultControlPanel((true))
            .routingControlCount(1)
            .status("DEPLOYED")
            .controlPanelArn("ControlPanelArn")
            .build();

        final DescribeClusterResponse clusterResponse = DescribeClusterResponse.builder().build();

        final CreateControlPanelResponse createResponse = CreateControlPanelResponse.builder()
            .controlPanel(controlPanel)
            .build();

        final DescribeControlPanelResponse describeResponse = DescribeControlPanelResponse.builder().
            controlPanel(controlPanel).build();

        doReturn(clusterResponse)
            .doReturn(createResponse)
            .doReturn(describeResponse)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            );

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel().getName()).isEqualTo(request.getDesiredResourceState().getName());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        ArgumentCaptor<CreateControlPanelRequest> requestCaptor = ArgumentCaptor.forClass(CreateControlPanelRequest.class);
        verify(proxy, times(4)).injectCredentialsAndInvokeV2(requestCaptor.capture(), ArgumentMatchers.any());
        CreateControlPanelRequest req = requestCaptor.getAllValues().get(1);
        assertThat(req.hasTags()).isTrue();
        assertThat(req.tags().get(0).key()).isEqualTo(tags.get(0).getKey());
        assertThat(req.tags().get(0).value()).isEqualTo(tags.get(0).getValue());
    }
}
