package software.amazon.route53recoverycontrol.safetyrule;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.route53recoverycontrolconfig.Route53RecoveryControlConfigClient;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DescribeSafetyRuleRequest;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DescribeSafetyRuleResponse;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.Route53RecoveryControlConfigException;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.UpdateSafetyRuleRequest;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.UpdateSafetyRuleResponse;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
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

        ResourceModel currentModel = request.getDesiredResourceState();
        ResourceModel previousModel = request.getPreviousResourceState();

        String currentSafetyRuleArn = currentModel.getSafetyRuleArn();

        String name = currentModel.getName();

        if ( currentSafetyRuleArn == null ) { throw new CfnNotFoundException(ResourceModel.TYPE_NAME, name); }

        if (previousModel != null) {
            String previousSafetyRuleArn = previousModel.getSafetyRuleArn();

            if (previousSafetyRuleArn != null) {

                if (!currentSafetyRuleArn.equals(previousSafetyRuleArn)) {
                    logger.log(String.format("%s does not equal %s.", currentSafetyRuleArn, previousSafetyRuleArn));
                    throw new CfnNotFoundException(ResourceModel.TYPE_NAME, String.format("%s does not equal %s.", currentSafetyRuleArn, previousSafetyRuleArn));
                }
            }
        }

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
            .then(progress ->
                proxy.initiate("AWS-Route53RecoveryControl-SafetyRule::Update", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                        .translateToServiceRequest(Translator::translateToUpdateRequest)
                        .makeServiceCall(this::updateSafetyRule)
                        .stabilize(this::isStabilized)
                        .progress()
            )
            .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    private UpdateSafetyRuleResponse updateSafetyRule(
            UpdateSafetyRuleRequest request,
            ProxyClient<Route53RecoveryControlConfigClient> proxyClient
    ) {
        UpdateSafetyRuleResponse response;

        String safetyRuleArn = request.assertionRuleUpdate() != null ? request.assertionRuleUpdate().safetyRuleArn() :
                request.gatingRuleUpdate().safetyRuleArn();

        try {
            response = proxyClient.injectCredentialsAndInvokeV2(request, ClientBuilder.getClient()::updateSafetyRule);
        } catch (Route53RecoveryControlConfigException e) {
            if (e.statusCode() == 404) {
                throw new CfnNotFoundException(ResourceModel.TYPE_NAME,
                        safetyRuleArn, e);
            } else
                throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
        } catch (AwsServiceException e) {
            throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
        }

        logger.log(String.format("%s has successfully been updated.", ResourceModel.TYPE_NAME));

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
            UpdateSafetyRuleRequest updateRequest,
            UpdateSafetyRuleResponse updateResponse,
            ProxyClient<Route53RecoveryControlConfigClient> proxyClient,
            ResourceModel model,
            CallbackContext context
    ) {
        boolean isAsserted = updateResponse.assertionRule() != null;

        ResourceModel updatedModel = Translator.translateFromUpdateResponse(updateResponse);

        model.setSafetyRuleArn(updatedModel.getSafetyRuleArn());
        model.setStatus(updatedModel.getStatus());
        model.setStatus(updatedModel.getStatus());
        model.setControlPanelArn(updatedModel.getControlPanelArn());
        model.setName(updatedModel.getName());
        model.setRuleConfig(updatedModel.getRuleConfig());

        if (isAsserted) {
            model.setAssertionRule(updatedModel.getAssertionRule());
        } else {
            model.setGatingRule(updatedModel.getGatingRule());
        }

        DescribeSafetyRuleResponse response;

        // want to bubble up exceptions
        response = getSafetyRule(DescribeSafetyRuleRequest.builder().safetyRuleArn(model.getSafetyRuleArn()).build(),
                proxyClient);

        String status = isAsserted ? response.assertionRule().statusAsString() :
                response.gatingRule().statusAsString();

        Integer requestWaitTime = isAsserted ? response.assertionRule().waitPeriodMs() :
                response.gatingRule().waitPeriodMs();

        Integer modelWaitTime = isAsserted ? model.getAssertionRule().getWaitPeriodMs() :
                model.getGatingRule().getWaitPeriodMs();


            if (status.equals("DEPLOYED")) {
                if (requestWaitTime.equals(modelWaitTime)) {
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
