package software.amazon.route53recoverycontrol.controlpanel;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.route53recoverycontrolconfig.Route53RecoveryControlConfigClient;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.CreateControlPanelRequest;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.CreateControlPanelResponse;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DescribeClusterRequest;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DescribeClusterResponse;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DescribeControlPanelRequest;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DescribeControlPanelResponse;
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

        DescribeClusterRequest describeClusterRequest = getDescribeClusterRequest(request.getDesiredResourceState());

        // Check to make sure cluster exists
        getCluster(describeClusterRequest, proxyClient);


        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
            .then(progress ->
                proxy.initiate("AWS-Route53RecoveryControl-ControlPanel::Create", proxyClient,progress.getResourceModel(), progress.getCallbackContext())
                    .translateToServiceRequest(Translator::translateToCreateRequest)
                    .makeServiceCall(this::createControlPanel)
                    .stabilize((awsRequest, awsResponse, client, model, context) -> isStabilized(awsResponse, client, model))
                    .progress(0)
                )
            .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    private DescribeClusterResponse getCluster(
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

    private DescribeClusterRequest getDescribeClusterRequest(
            ResourceModel model
    ) {
        return DescribeClusterRequest.builder()
                .clusterArn(model.getClusterArn())
                .build();
    }

    private CreateControlPanelResponse createControlPanel(
            CreateControlPanelRequest request,
            ProxyClient<Route53RecoveryControlConfigClient> proxyClient
    ) {
        CreateControlPanelResponse response;

        try {
            response = proxyClient.injectCredentialsAndInvokeV2(request, ClientBuilder.getClient()::createControlPanel);
        } catch (Route53RecoveryControlConfigException e) {
            if (e.statusCode() == 409) {
                throw new CfnAlreadyExistsException(ResourceModel.TYPE_NAME, request.controlPanelName());
            } else {
                throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
            }
        } catch (AwsServiceException e) {
            throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
        }

        return response;
    }

    private DescribeControlPanelResponse describeControlPanel(
            DescribeControlPanelRequest request,
            ProxyClient<Route53RecoveryControlConfigClient> proxyClient
    ) {
        DescribeControlPanelResponse response;

        try {
            response = proxyClient.injectCredentialsAndInvokeV2(request, ClientBuilder.getClient()::describeControlPanel);
        } catch (Route53RecoveryControlConfigException e) {
            if (e.statusCode() == 404) {
                throw new CfnNotFoundException(ResourceModel.TYPE_NAME,
                        request.controlPanelArn(), e);
            } else
                throw new CfnGeneralServiceException(request.controlPanelArn(), e);
        } catch (final AwsServiceException e) {
            throw new CfnGeneralServiceException(request.controlPanelArn(), e);
        }

        return response;
    }

    boolean isStabilized(
            CreateControlPanelResponse createResponse,
            ProxyClient<Route53RecoveryControlConfigClient> proxyClient,
            ResourceModel model
    ) {
        String controlPanelArn = createResponse.controlPanel().controlPanelArn();

        if (controlPanelArn == null) return false;

        model.setControlPanelArn(controlPanelArn);

        DescribeControlPanelResponse response;

        // if something goes wrong we want to bubble up the exception exception
        response = describeControlPanel(DescribeControlPanelRequest.builder().controlPanelArn(controlPanelArn).build(), proxyClient);

        if (response.controlPanel().statusAsString().equals("DEPLOYED")) {
            logger.log(String.format("%s has stabilized.", ResourceModel.TYPE_NAME));
            return true;
        } else {
            logger.log(String.format("%s has not yet stabilized.", ResourceModel.TYPE_NAME));
            return false;
        }
    }
}
