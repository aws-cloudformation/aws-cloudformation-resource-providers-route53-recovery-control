package software.amazon.route53recoverycontrol.routingcontrol;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.route53recoverycontrolconfig.Route53RecoveryControlConfigClient;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DeleteRoutingControlRequest;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DeleteRoutingControlResponse;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DescribeRoutingControlRequest;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DescribeRoutingControlResponse;
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
                proxy.initiate("AWS-Route53RecoveryControl-RoutingControl::Delete", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                    .translateToServiceRequest(Translator::translateToDeleteRequest)
                    .makeServiceCall(this::deleteRoutingControl)
                    .stabilize((awsRequest, awsResponse, client, model, context) -> isStabilized(client, model))
                    .progress()
            )
            .then(progress -> ProgressEvent.defaultSuccessHandler(null));
    }

    private DeleteRoutingControlResponse deleteRoutingControl(
            DeleteRoutingControlRequest request,
            ProxyClient<Route53RecoveryControlConfigClient> proxyClient
    ) {

        DeleteRoutingControlResponse response;

        try {
            response = proxyClient.injectCredentialsAndInvokeV2(request, proxyClient.client()::deleteRoutingControl);
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

    private DescribeRoutingControlResponse getRoutingControl(
            DescribeRoutingControlRequest request,
            ProxyClient<Route53RecoveryControlConfigClient> proxyClient
    ) {
        DescribeRoutingControlResponse response;

        try {
            response = proxyClient.injectCredentialsAndInvokeV2(request,proxyClient.client()::describeRoutingControl);
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
            ProxyClient<Route53RecoveryControlConfigClient> proxyClient,
            ResourceModel model
    ) {
        String routingControlArn = model.getRoutingControlArn();

        DescribeRoutingControlResponse response;

        try{
            response = getRoutingControl(DescribeRoutingControlRequest.builder()
                    .routingControlArn(routingControlArn).build(), proxyClient);
        } catch (CfnNotFoundException e) {
            logger.log(String.format("%s has stabilized.", ResourceModel.TYPE_NAME));
            return true;
        }

        if (response != null && response.routingControl().statusAsString().equals("PENDING_DELETION")) {
            logger.log(String.format("%s has not yet stabilized.", ResourceModel.TYPE_NAME));
        } else {
            logger.log(String.format("%s was not successfully marked for deletion.", ResourceModel.TYPE_NAME));
        }
        return false;
    }
}
