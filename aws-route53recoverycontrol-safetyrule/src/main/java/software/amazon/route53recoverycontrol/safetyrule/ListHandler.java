package software.amazon.route53recoverycontrol.safetyrule;

import software.amazon.awssdk.services.route53recoverycontrolconfig.model.ListSafetyRulesRequest;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.ListSafetyRulesResponse;
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

        final ListSafetyRulesRequest listRequest = Translator.translateToListRequest(request.getDesiredResourceState(), request.getNextToken());

        ListSafetyRulesResponse listResponse = proxy.injectCredentialsAndInvokeV2(listRequest, ClientBuilder.getClient()::listSafetyRules);

        String nextToken = listResponse.nextToken();

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .resourceModels(Translator.translateFromListRequest(listResponse))
            .nextToken(nextToken)
            .status(OperationStatus.SUCCESS)
            .build();
    }
}
