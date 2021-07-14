package software.amazon.route53recoverycontrol.routingcontrol;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.route53recoverycontrolconfig.Route53RecoveryControlConfigClient;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.CreateRoutingControlRequest;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.CreateRoutingControlResponse;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DescribeClusterRequest;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DescribeControlPanelRequest;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DescribeControlPanelResponse;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DescribeRoutingControlRequest;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DescribeRoutingControlResponse;
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
        String clusterArn = request.getDesiredResourceState().getClusterArn();
        String controlPanelArn = request.getDesiredResourceState().getControlPanelArn();

        // Pre-Existence check - we don't care about the response, just that it exists
        getCluster(DescribeClusterRequest.builder()
                .clusterArn(clusterArn).build(), proxyClient);

        describeControlPanel(
                DescribeControlPanelRequest.builder().controlPanelArn(controlPanelArn).build(), proxyClient
        );

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
            .then(progress ->
                proxy.initiate("AWS-Route53RecoveryControl-RoutingControl::Create", proxyClient,progress.getResourceModel(), progress.getCallbackContext())
                    .translateToServiceRequest(Translator::translateToCreateRequest)
                    .makeServiceCall(this::createRoutingControl)
                    .stabilize((awsRequest, awsResponse, client, model, context) -> isStabilized(awsResponse, client, model, context))
                    .progress()
                )
            .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    private void getCluster(
            DescribeClusterRequest request,
            ProxyClient<Route53RecoveryControlConfigClient> proxyClient
    ) {

        try {
            proxyClient.injectCredentialsAndInvokeV2(request, proxyClient.client()::describeCluster);
        } catch (Route53RecoveryControlConfigException e) {
            if (e.statusCode() == 404) {
                throw new CfnNotFoundException(ResourceModel.TYPE_NAME,
                        request.clusterArn(), e);
            } else
                throw new CfnGeneralServiceException(request.clusterArn(), e);
        } catch (final AwsServiceException e) {
            throw new CfnGeneralServiceException(request.clusterArn(), e);
        }
    }

    private CreateRoutingControlResponse createRoutingControl(
            CreateRoutingControlRequest request,
            ProxyClient<Route53RecoveryControlConfigClient> proxyClient
    ) {
        CreateRoutingControlResponse response;

        try{
            response = proxyClient.injectCredentialsAndInvokeV2(request, ClientBuilder.getClient()::createRoutingControl);
        } catch (Route53RecoveryControlConfigException e) {
            if (e.statusCode() == 409) {
                throw new CfnAlreadyExistsException(ResourceModel.TYPE_NAME, request.routingControlName());
            } else {
                throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
            }
        } catch (AwsServiceException e) {
            throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
        }

        return response;
    }

    private DescribeRoutingControlResponse getRoutingControl(
            DescribeRoutingControlRequest request,
            ProxyClient<Route53RecoveryControlConfigClient> proxyClient
    ) {
        DescribeRoutingControlResponse response;

        try {
            response = proxyClient.injectCredentialsAndInvokeV2(request, ClientBuilder.getClient()::describeRoutingControl);
        } catch (Route53RecoveryControlConfigException e) {
            if (e.statusCode() == 404) {
                throw new CfnNotFoundException(ResourceModel.TYPE_NAME,
                        request.routingControlArn(), e);
            } else
                throw new CfnGeneralServiceException(request.routingControlArn(), e);
        } catch (final AwsServiceException e) {
            throw new CfnGeneralServiceException(request.routingControlArn(), e);
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

    private boolean isStabilized(
            CreateRoutingControlResponse createResponse,
            ProxyClient<Route53RecoveryControlConfigClient> proxyClient,
            ResourceModel model,
            CallbackContext context
    ) {
        {
            String routingControlArn = createResponse.routingControl().routingControlArn();

            if (routingControlArn == null) return false;

            model.setRoutingControlArn(routingControlArn);

            DescribeRoutingControlResponse response;

            // if something goes wrong we want to bubble up the exception exception
            response = getRoutingControl(DescribeRoutingControlRequest.builder()
                    .routingControlArn(createResponse.routingControl().routingControlArn()).build(), proxyClient);

            if (response.routingControl().statusAsString().equals("DEPLOYED")) {
                logger.log(String.format("%s has stabilized.", ResourceModel.TYPE_NAME));
                return true;
            } else {
                logger.log(String.format("%s has not yet stabilized.", ResourceModel.TYPE_NAME));
                return false;
            }
        }
    }
}
