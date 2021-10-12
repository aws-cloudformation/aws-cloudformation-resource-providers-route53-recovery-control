package software.amazon.route53recoverycontrol.cluster;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import software.amazon.awssdk.services.route53recoverycontrolconfig.Route53RecoveryControlConfigClient;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.Cluster;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.ClusterEndpoint;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.CreateClusterRequest;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.CreateClusterResponse;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DescribeClusterResponse;
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
                .name("MyFavoriteCluster")
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final List<ClusterEndpoint> clusterEndpointList = new ArrayList<>();

        final ClusterEndpoint clusterEndpoint = ClusterEndpoint.builder()
                .endpoint("jfnvkjnfskjdfnsklefn")
                .region("us-west-2")
                .build();

        clusterEndpointList.add(clusterEndpoint);

        List<String> endpoints = new ArrayList<>();

        endpoints.add("djnijnrviejdcms");
        endpoints.add("sjdncirbfskdjnckjr");

        final Cluster cluster = Cluster.builder()
                .name("MyFavoriteCluster")
                .clusterArn("thisIsAClusterArn")
                .status(("DEPLOYED"))
                .clusterEndpoints(clusterEndpointList)
                .build();

        final CreateClusterResponse createResponse = CreateClusterResponse.builder()
                .cluster(cluster)
                .build();

        final DescribeClusterResponse describeResponse = DescribeClusterResponse.builder()
                .cluster(cluster)
                .build();

        doReturn(createResponse)
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
        assertThat(response.getResourceModel().getStatus()).isEqualTo("DEPLOYED");
        assertThat(response.getResourceModel().getClusterArn()).isEqualTo("thisIsAClusterArn");
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        ArgumentCaptor<CreateClusterRequest> requestCaptor = ArgumentCaptor.forClass(CreateClusterRequest.class);
        verify(proxy, times(3)).injectCredentialsAndInvokeV2(requestCaptor.capture(), ArgumentMatchers.any());
        assertThat(requestCaptor.getAllValues().get(0).hasTags()).isFalse();
    }

    @Test
    public void handleRequest_SimpleSuccessWithTags() {
        final CreateHandler handler = new CreateHandler();
        final List<Tag> tags = ImmutableList.of(Tag.builder().key("key").value("val").build());

        final ResourceModel modelWithTags = ResourceModel.builder()
            .name("MyFavoriteCluster")
            .tags(tags)
            .build();

        final ResourceHandlerRequest<ResourceModel> requestWithTags = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(modelWithTags)
            .build();

        final List<ClusterEndpoint> clusterEndpointList2 = new ArrayList<>();

        final ClusterEndpoint clusterEndpoint2 = ClusterEndpoint.builder()
            .endpoint("jfnvkjnfskjdfnsklefn")
            .region("us-west-2")
            .build();

        clusterEndpointList2.add(clusterEndpoint2);

        List<String> endpoints = new ArrayList<>();

        endpoints.add("djnijnrviejdcms");
        endpoints.add("sjdncirbfskdjnckjr");

        final Cluster cluster2 = Cluster.builder()
            .name("MyFavoriteCluster")
            .clusterArn("thisIsAClusterArn")
            .status(("DEPLOYED"))
            .clusterEndpoints(clusterEndpointList2)
            .build();

        final CreateClusterResponse createResponse = CreateClusterResponse.builder()
            .cluster(cluster2)
            .build();

        final DescribeClusterResponse describeResponse = DescribeClusterResponse.builder()
            .cluster(cluster2)
            .build();

        doReturn(createResponse)
            .doReturn(describeResponse)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            );

        final ProgressEvent<ResourceModel, CallbackContext> responseWithTags = handler.handleRequest(proxy, requestWithTags, new CallbackContext(), proxyClient, logger);

        assertThat(responseWithTags).isNotNull();
        assertThat(responseWithTags.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(responseWithTags.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(responseWithTags.getResourceModel().getName()).isEqualTo(requestWithTags.getDesiredResourceState().getName());
        assertThat(responseWithTags.getResourceModel().getStatus()).isEqualTo("DEPLOYED");
        assertThat(responseWithTags.getResourceModel().getClusterArn()).isEqualTo("thisIsAClusterArn");
        assertThat(responseWithTags.getResourceModels()).isNull();
        assertThat(responseWithTags.getMessage()).isNull();
        assertThat(responseWithTags.getErrorCode()).isNull();

        ArgumentCaptor<CreateClusterRequest> requestCaptor = ArgumentCaptor.forClass(CreateClusterRequest.class);
        verify(proxy, times(3)).injectCredentialsAndInvokeV2(requestCaptor.capture(), ArgumentMatchers.any());
        CreateClusterRequest req = requestCaptor.getAllValues().get(0);
        assertThat(req.hasTags()).isTrue();
        assertThat(req.tags().get(0).key()).isEqualTo(tags.get(0).getKey());
        assertThat(req.tags().get(0).value()).isEqualTo(tags.get(0).getValue());
    }
}
