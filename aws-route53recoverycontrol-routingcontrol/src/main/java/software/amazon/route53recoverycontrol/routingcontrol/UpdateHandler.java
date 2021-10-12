package software.amazon.route53recoverycontrol.routingcontrol;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.route53recoverycontrolconfig.Route53RecoveryControlConfigClient;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DescribeRoutingControlRequest;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DescribeRoutingControlResponse;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.Route53RecoveryControlConfigException;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.UpdateRoutingControlRequest;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.UpdateRoutingControlResponse;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnNotUpdatableException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class UpdateHandler extends BaseHandlerStd {
    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<Route53RecoveryControlConfigClient> proxyClient,
        final Logger logger) {

        this.logger = logger;

        final ResourceModel currentModel = request.getDesiredResourceState();
        final ResourceModel previousModel = request.getPreviousResourceState();

        if (currentModel.getRoutingControlArn() == null ) { throw new CfnNotFoundException(ResourceModel.TYPE_NAME, currentModel.getControlPanelArn()); }
        // Ensure not updating ReadOnly properties
        if (previousModel != null) {
            if (!currentModel.getRoutingControlArn().equals(previousModel.getRoutingControlArn())) {
                throw new CfnNotUpdatableException(ResourceModel.TYPE_NAME, request.getDesiredResourceState().getRoutingControlArn());
            }
        }

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
            .then(progress ->
                proxy.initiate("AWS-Route53RecoveryControl-RoutingControl::Update::first", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                    .translateToServiceRequest(Translator::translateToUpdateRequest)
                    .makeServiceCall(this::updateRoutingControl)
                    .stabilize((awsRequest, awsResponse, client, model, context) -> isStabilized(awsResponse, client, model))
                    .progress())
            .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    private UpdateRoutingControlResponse updateRoutingControl(
            UpdateRoutingControlRequest request,
            ProxyClient<Route53RecoveryControlConfigClient> proxyClient
    ) {
        UpdateRoutingControlResponse response;

        try{
            response = proxyClient.injectCredentialsAndInvokeV2(request, proxyClient.client()::updateRoutingControl);
        } catch (Route53RecoveryControlConfigException e) {
            if (e.statusCode() == 404) {
                throw new CfnNotFoundException(ResourceModel.TYPE_NAME,
                        request.routingControlArn(), e);
            } else
                throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
        } catch (AwsServiceException e) {
            throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
        }

        logger.log(String.format("%s has successfully been updated.", ResourceModel.TYPE_NAME));
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

    private boolean isStabilized(
            UpdateRoutingControlResponse updateResponse,
            ProxyClient<Route53RecoveryControlConfigClient> proxyClient,
            ResourceModel model
    ) {

        DescribeRoutingControlResponse response;

        // want to bubble up exceptions
        response = getRoutingControl(DescribeRoutingControlRequest.builder()
                .routingControlArn(updateResponse.routingControl().routingControlArn()).build(),
                proxyClient);

        if (response.routingControl().statusAsString().equals("DEPLOYED")) {
            String name = response.routingControl().name();
            if (name.equals(model.getName())) {
                logger.log(String.format("%s has stabilized.", ResourceModel.TYPE_NAME));
                return true;
            }
        } else {
            logger.log(String.format("%s has not yet stabilized.", ResourceModel.TYPE_NAME));
            return false;
        }

        return false;
    }
}
