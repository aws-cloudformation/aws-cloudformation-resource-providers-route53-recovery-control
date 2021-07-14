package software.amazon.route53recoverycontrol.routingcontrol;

import software.amazon.awssdk.services.route53recoverycontrolconfig.model.ListRoutingControlsRequest;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.ListRoutingControlsResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.ArrayList;
import java.util.List;

public class ListHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final List<ResourceModel> models = new ArrayList<>();

        final ListRoutingControlsRequest listRequest = Translator.translateToListRequest(
                request.getDesiredResourceState(),
                request.getNextToken()
        );

       ListRoutingControlsResponse response = proxy.injectCredentialsAndInvokeV2(listRequest, ClientBuilder.getClient()::listRoutingControls);

        String nextToken = response.nextToken();

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .resourceModels(Translator.translateFromListRequest(response))
            .nextToken(nextToken)
            .status(OperationStatus.SUCCESS)
            .build();
    }
}
