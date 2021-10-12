package software.amazon.route53recoverycontrol.cluster;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.route53recoverycontrolconfig.Route53RecoveryControlConfigClient;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.CreateClusterRequest;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.CreateClusterResponse;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DescribeClusterRequest;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DescribeClusterResponse;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.Route53RecoveryControlConfigException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;


public class CreateHandler extends BaseHandlerStd {
    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<Route53RecoveryControlConfigClient> proxyClient,
        final Logger logger) {

        this.logger = logger;

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
            .then(progress ->
                proxy.initiate("AWS-Route53RecoveryControl-Cluster::Create", proxyClient,progress.getResourceModel(), progress.getCallbackContext())
                    .translateToServiceRequest(Translator::translateToCreateRequest)
                    .makeServiceCall(this::createCluster)
                        .stabilize(this::isStabilized)
                    .progress()
                )
            .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    private CreateClusterResponse createCluster(
            CreateClusterRequest request,
            ProxyClient<Route53RecoveryControlConfigClient> proxyClient
    ) {
        CreateClusterResponse response;

        try {
            response = proxyClient.injectCredentialsAndInvokeV2(request, proxyClient.client()::createCluster);
        } catch (Route53RecoveryControlConfigException e) {
            if (e.statusCode() == 409) {
                throw new CfnAlreadyExistsException(ResourceModel.TYPE_NAME, request.clusterName());
            } else {
                throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
            }
        } catch (AwsServiceException e) {
            throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
        }

        return response;
    }

    private boolean isStabilized(
            CreateClusterRequest createRequest,
            CreateClusterResponse createResponse,
            ProxyClient<Route53RecoveryControlConfigClient> proxyClient,
            ResourceModel model,
            CallbackContext context
    ) {
        if (createResponse.cluster().clusterArn() == null) {
            logger.log(String.format("%s has not yet stabilized. Cluster arn is null", ResourceModel.TYPE_NAME));
            return false;
        }

        String clusterArn = createResponse.cluster().clusterArn();
        model.setClusterArn(clusterArn);

        DescribeClusterResponse describeResponse;

        try{
            describeResponse = describeCluster(DescribeClusterRequest.builder().clusterArn(clusterArn).build(), proxyClient);
            logger.log(String.format("Describe Response %s", describeResponse));
        } catch (CfnNotFoundException e) {
            logger.log(String.format("%s has not yet stabilized.", ResourceModel.TYPE_NAME));
            return false;
        }

        if (describeResponse.cluster().hasClusterEndpoints()) {
            if (describeResponse.cluster().statusAsString().equals("DEPLOYED") ||
            describeResponse.cluster().statusAsString().equals("PENDING")) {
                logger.log(String.format("%s has stabilized.", ResourceModel.TYPE_NAME));
                return true;
            }
        }

        logger.log(String.format("%s has not yet stabilized.", ResourceModel.TYPE_NAME));
        return false;

    }

    private DescribeClusterResponse describeCluster (
            DescribeClusterRequest request,
            ProxyClient<Route53RecoveryControlConfigClient> proxyClient
    ) {
        DescribeClusterResponse response;

        try {
            response = proxyClient.injectCredentialsAndInvokeV2(request, ClientBuilder.getClient()::describeCluster);
        } catch (Route53RecoveryControlConfigException e) {
            if (e.statusCode() == 404) {
                throw new CfnNotFoundException(ResourceModel.TYPE_NAME,
                        request.clusterArn(), e);
            } else
                throw new CfnGeneralServiceException(request.clusterArn(), e);
        } catch (final AwsServiceException e) {
            throw new CfnGeneralServiceException(request.clusterArn(), e);
        }

        return response;
    }
}
