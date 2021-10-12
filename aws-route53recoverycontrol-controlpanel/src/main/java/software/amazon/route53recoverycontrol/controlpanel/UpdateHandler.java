package software.amazon.route53recoverycontrol.controlpanel;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.route53recoverycontrolconfig.Route53RecoveryControlConfigClient;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DescribeControlPanelRequest;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DescribeControlPanelResponse;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.Route53RecoveryControlConfigException;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.UpdateControlPanelRequest;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.UpdateControlPanelResponse;
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

        if (currentModel.getControlPanelArn() == null ) { throw new CfnNotFoundException(ResourceModel.TYPE_NAME, currentModel.getControlPanelArn()); }
        // Ensure not updating ReadOnly properties
        if (previousModel != null) {
            if (!currentModel.getControlPanelArn().equals(previousModel.getControlPanelArn())) {
                throw new CfnNotUpdatableException(ResourceModel.TYPE_NAME, request.getDesiredResourceState().getControlPanelArn());
            }
        }

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
            .then(progress ->
                proxy.initiate("AWS-Route53RecoveryControl-ControlPanel::Update", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                    .translateToServiceRequest(Translator::translateToUpdateRequest)
                    .makeServiceCall(this::updateControlPanel)
                    .stabilize((awsRequest, awsResponse, client, model, context) -> (isStabilized(awsResponse, client, model)))
                    .progress(0))
            .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    UpdateControlPanelResponse updateControlPanel(
            UpdateControlPanelRequest request,
            ProxyClient<Route53RecoveryControlConfigClient> proxyClient
    ) {
        UpdateControlPanelResponse response;

        try {
            response = proxyClient.injectCredentialsAndInvokeV2(request, proxyClient.client()::updateControlPanel);
        } catch (Route53RecoveryControlConfigException e) {
            if (e.statusCode() == 404) {
                throw new CfnNotFoundException(ResourceModel.TYPE_NAME,
                        request.controlPanelArn(), e);
            } else
                throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
        } catch (AwsServiceException e) {
            throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
        }

        logger.log(String.format("%s has successfully been updated.", ResourceModel.TYPE_NAME));
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
            UpdateControlPanelResponse createResponse,
            ProxyClient<Route53RecoveryControlConfigClient> proxyClient,
            ResourceModel model
    ) {

        DescribeControlPanelResponse describeResponse;

        // want to bubble up exceptions
        describeResponse = describeControlPanel(DescribeControlPanelRequest.builder().controlPanelArn(model.getControlPanelArn()).build(), proxyClient);

        if (describeResponse.controlPanel().statusAsString().equals("DEPLOYED")) {
            String name = describeResponse.controlPanel().name();
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
