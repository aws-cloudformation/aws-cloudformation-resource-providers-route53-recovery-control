package software.amazon.route53recoverycontrol.cluster;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.route53recoverycontrolconfig.Route53RecoveryControlConfigClient;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DeleteClusterRequest;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DeleteClusterResponse;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DescribeClusterRequest;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DescribeClusterResponse;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.Route53RecoveryControlConfigException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class DeleteHandler extends BaseHandlerStd {
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
                proxy.initiate("AWS-Route53RecoveryControl-Cluster::Delete", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                    .translateToServiceRequest(Translator::translateToDeleteRequest)
                    .makeServiceCall(this::deleteCluster)
                    .stabilize((awsRequest, awsResponse, client, model, context) -> isStabilized(model, client))
                    .progress()
            )
            .then(progress -> ProgressEvent.defaultSuccessHandler(null));
    }

    private DeleteClusterResponse deleteCluster (
            DeleteClusterRequest request,
            ProxyClient<Route53RecoveryControlConfigClient> proxyClient
    ) {
        DeleteClusterResponse response = null;

        try {
            response = proxyClient.injectCredentialsAndInvokeV2(request, proxyClient.client()::deleteCluster);
        } catch (Route53RecoveryControlConfigException e) {
            if (e.statusCode() == 404) {
                throw new CfnNotFoundException(ResourceModel.TYPE_NAME, request.clusterArn(), e);
            }
        } catch (AwsServiceException e) {
            throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
        }

        return response;
    }

    private boolean isStabilized(
            ResourceModel model,
            ProxyClient<Route53RecoveryControlConfigClient> proxyClient
    ) {
        String clusterArn = model.getClusterArn();

        DescribeClusterResponse response;

        try{
            response = describeCluster(DescribeClusterRequest.builder().clusterArn(clusterArn).build(), proxyClient);
        } catch (CfnNotFoundException e) {
            logger.log(String.format("%s has stabilized.", ResourceModel.TYPE_NAME));
            return true;
        }

        if (response != null && response.cluster().statusAsString().equals("PENDING_DELETION")) {
            logger.log(String.format("%s has not yet stabilized.", ResourceModel.TYPE_NAME));
        } else {
            logger.log(String.format("%s was not successfully marked for deletion.", ResourceModel.TYPE_NAME));
        }
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
