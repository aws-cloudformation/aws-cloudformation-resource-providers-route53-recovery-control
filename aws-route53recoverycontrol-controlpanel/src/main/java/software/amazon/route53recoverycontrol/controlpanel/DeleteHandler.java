package software.amazon.route53recoverycontrol.controlpanel;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DeleteControlPanelRequest;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DeleteControlPanelResponse;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DescribeControlPanelRequest;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DescribeControlPanelResponse;
import software.amazon.awssdk.services.route53recoverycontrolconfig.Route53RecoveryControlConfigClient;
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
                proxy.initiate("AWS-Route53RecoveryControl-ControlPanel::Delete", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                    .translateToServiceRequest(Translator::translateToDeleteRequest)
                    .makeServiceCall(this::deleteControlPanel)
                    .stabilize((awsRequest, awsResponse, client, model, context) -> isStabilized(model, client))
                    .progress(0)
            )
            .then(progress -> ProgressEvent.defaultSuccessHandler(null));
    }

    private DeleteControlPanelResponse deleteControlPanel(
            DeleteControlPanelRequest request,
            ProxyClient<Route53RecoveryControlConfigClient> proxyClient
    ) {
        DeleteControlPanelResponse response = null;

        try {
            response = proxyClient.injectCredentialsAndInvokeV2(request, proxyClient.client()::deleteControlPanel);
        } catch (Route53RecoveryControlConfigException e) {
            if (e.statusCode() == 404) {
                throw new CfnNotFoundException(ResourceModel.TYPE_NAME, request.controlPanelArn(), e);
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

    private boolean isStabilized(
            ResourceModel model,
            ProxyClient<Route53RecoveryControlConfigClient> proxyClient
    ) {
        String controlPanelArn = model.getControlPanelArn();

        DescribeControlPanelResponse response;

        try{
            response = describeControlPanel(DescribeControlPanelRequest.builder().controlPanelArn(controlPanelArn).build(), proxyClient);
        } catch (CfnNotFoundException e) {
            logger.log(String.format("%s has stabilized.", ResourceModel.TYPE_NAME));
            return true;
        }

        if (response != null && response.controlPanel().statusAsString().equals("PENDING_DELETION")) {
            logger.log(String.format("%s has not yet stabilized.", ResourceModel.TYPE_NAME));
        } else {
            logger.log(String.format("%s was not successfully marked for deletion.", ResourceModel.TYPE_NAME));
        }
        return false;
    }
}
