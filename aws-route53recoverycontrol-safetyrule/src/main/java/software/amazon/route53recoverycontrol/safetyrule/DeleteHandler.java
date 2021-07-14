package software.amazon.route53recoverycontrol.safetyrule;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.route53recoverycontrolconfig.Route53RecoveryControlConfigClient;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DeleteSafetyRuleRequest;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DeleteSafetyRuleResponse;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DescribeSafetyRuleRequest;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DescribeSafetyRuleResponse;
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
                proxy.initiate("AWS-Route53RecoveryControl-SafetyRule::Delete", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                    .translateToServiceRequest(Translator::translateToDeleteRequest)
                    .makeServiceCall(this::deleteSafetyRule)
                        .stabilize(this::isStabilized)
                    .progress()
            )
            .then(progress -> ProgressEvent.defaultSuccessHandler(null));
    }

    private DeleteSafetyRuleResponse deleteSafetyRule(
            DeleteSafetyRuleRequest request,
            ProxyClient<Route53RecoveryControlConfigClient> proxyClient
    ) {
        DeleteSafetyRuleResponse response = null;

        try{
            response = proxyClient.injectCredentialsAndInvokeV2(request, ClientBuilder.getClient()::deleteSafetyRule);
        } catch (Route53RecoveryControlConfigException e) {
            if (e.statusCode() == 404) {
                throw new CfnNotFoundException(ResourceModel.TYPE_NAME, request.safetyRuleArn(), e);
            }
        } catch (AwsServiceException e) {
            throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
        }

        return response;
    }

    private DescribeSafetyRuleResponse getSafetyRule(
            DescribeSafetyRuleRequest request,
            ProxyClient<Route53RecoveryControlConfigClient> proxyClient
    ) {
        DescribeSafetyRuleResponse response;

        try {
            response = proxyClient.injectCredentialsAndInvokeV2(request, ClientBuilder.getClient()::describeSafetyRule);
        } catch (Route53RecoveryControlConfigException e) {
            if (e.statusCode() == 404) {
                throw new CfnNotFoundException(ResourceModel.TYPE_NAME,
                        request.safetyRuleArn(), e);
            } else
                throw new CfnGeneralServiceException(request.safetyRuleArn(), e);
        } catch (final AwsServiceException e) {
            throw new CfnGeneralServiceException(request.safetyRuleArn(), e);
        }

        return response;
    }

    private boolean isStabilized(
            DeleteSafetyRuleRequest deleteRequest,
            DeleteSafetyRuleResponse deleteResponse,
            ProxyClient<Route53RecoveryControlConfigClient> proxyClient,
            ResourceModel model,
            CallbackContext context
    ) {
        boolean isAssertion = model.getAssertionRule() != null;

        String safetyRuleArn = model.getSafetyRuleArn();

        DescribeSafetyRuleResponse response;

        try{
            response = getSafetyRule(DescribeSafetyRuleRequest.builder()
                    .safetyRuleArn(safetyRuleArn).build(), proxyClient);
        } catch (CfnNotFoundException e) {
            logger.log(String.format("%s has stabilized.", ResourceModel.TYPE_NAME));
            return true;
        }

        if (response != null) {
            String status = isAssertion ? response.assertionRule().statusAsString() :
                    response.gatingRule().statusAsString();
            if (status.equals("PENDING_DELETION")) {
                logger.log(String.format("%s has not yet stabilized.", ResourceModel.TYPE_NAME));
            } else {
                logger.log(String.format("%s was not successfully marked for deletion.", ResourceModel.TYPE_NAME));
            }
        }
        return false;
    }
}
