package software.amazon.route53recoverycontrol.controlpanel;

import software.amazon.awssdk.services.route53recoverycontrolconfig.model.ListControlPanelsRequest;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.ListControlPanelsResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;


public class ListHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final ListControlPanelsRequest listRequest = Translator.translateToListRequest(request.getNextToken());

        ListControlPanelsResponse response =  proxy.injectCredentialsAndInvokeV2(listRequest, ClientBuilder.getClient()::listControlPanels);

        String nextToken = response.nextToken();

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .resourceModels(Translator.translateFromListRequest(response))
            .nextToken(nextToken)
            .status(OperationStatus.SUCCESS)
            .build();
    }
}
