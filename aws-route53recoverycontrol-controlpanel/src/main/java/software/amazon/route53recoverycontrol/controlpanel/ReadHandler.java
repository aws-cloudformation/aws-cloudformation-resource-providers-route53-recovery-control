package software.amazon.route53recoverycontrol.controlpanel;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.route53recoverycontrolconfig.Route53RecoveryControlConfigClient;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DescribeControlPanelRequest;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DescribeControlPanelResponse;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.Route53RecoveryControlConfigException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class ReadHandler extends BaseHandlerStd {
    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<Route53RecoveryControlConfigClient> proxyClient,
        final Logger logger) {

        this.logger = logger;

        return proxy.initiate("AWS-Route53RecoveryControl-ControlPanel::Read", proxyClient, request.getDesiredResourceState(), callbackContext)
            .translateToServiceRequest(Translator::translateToReadRequest)
            .makeServiceCall(this::describeControlPanel)
            .done(awsResponse -> ProgressEvent.defaultSuccessHandler(Translator.translateFromReadResponse(awsResponse)));
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
}
