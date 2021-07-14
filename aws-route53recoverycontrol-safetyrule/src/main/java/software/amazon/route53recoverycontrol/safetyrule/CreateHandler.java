package software.amazon.route53recoverycontrol.safetyrule;

import java.util.List;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.route53recoverycontrolconfig.Route53RecoveryControlConfigClient;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.CreateSafetyRuleRequest;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.CreateSafetyRuleResponse;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DescribeControlPanelRequest;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DescribeControlPanelResponse;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DescribeRoutingControlRequest;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DescribeRoutingControlResponse;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DescribeSafetyRuleRequest;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DescribeSafetyRuleResponse;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.Route53RecoveryControlConfigException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;


public class CreateHandler extends BaseHandlerStd {
    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<Route53RecoveryControlConfigClient> proxyClient,
        final Logger logger) {

        this.logger = logger;

        ResourceModel currentModel = request.getDesiredResourceState();

        boolean isAssertion = isAssertion(currentModel);

        String controlPanelArn = currentModel.getControlPanelArn();

        List<String> routingControls = isAssertion ? currentModel.getAssertionRule().getAssertedControls() : currentModel.getGatingRule().getGatingControls();

        // Control Panel existence check
        DescribeControlPanelRequest controlPanelRequest = DescribeControlPanelRequest.builder().controlPanelArn(controlPanelArn).build();
        describeControlPanel(controlPanelRequest, proxyClient);

        //Routing Controls existence check
        if (routingControls.size() > 0) {
            for (String routingControl : routingControls) {
                DescribeRoutingControlRequest routingControlRequest = DescribeRoutingControlRequest.builder().routingControlArn(routingControl).build();
                getRoutingControl(routingControlRequest, proxyClient);
            }
        }

        // Target Controls existence check
        if (!isAssertion) {
            List<String> targetControls = currentModel.getGatingRule().getTargetControls();
            if (targetControls.size() > 0) {
                for (String targetControl : targetControls) {
                    DescribeRoutingControlRequest routingControlRequest = DescribeRoutingControlRequest.builder().routingControlArn(targetControl).build();
                    getRoutingControl(routingControlRequest, proxyClient);
                }
            }
        }

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then(progress ->
                            proxy.initiate("AWS-Route53RecoveryControl-SafetyRule::Create", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                                    .translateToServiceRequest(Translator::translateToCreateRequest)
                                    .makeServiceCall(this::createSafetyRule)
                                    .stabilize(this::isStabilized)
                                    .progress()
                )
                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));

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

    private boolean isAssertion(ResourceModel model) {
        return model.getAssertionRule() != null;
    }

    private CreateSafetyRuleResponse createSafetyRule(
            CreateSafetyRuleRequest request,
            ProxyClient<Route53RecoveryControlConfigClient> proxyClient
    ) {
        CreateSafetyRuleResponse createResponse;


        try {
            createResponse = proxyClient.injectCredentialsAndInvokeV2(request, ClientBuilder.getClient()::createSafetyRule);
        } catch (Route53RecoveryControlConfigException e) {
            if (e.statusCode() == 409) {
                String name = request.assertionRule() != null ?
                        request.assertionRule().name() : request.gatingRule().name();
                    throw new CfnAlreadyExistsException(ResourceModel.TYPE_NAME,
                            name);
            } else {
                throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
            }
        } catch (AwsServiceException e) {
            throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
        }


        return createResponse;
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
            CreateSafetyRuleRequest createRequest,
            CreateSafetyRuleResponse createResponse,
            ProxyClient<Route53RecoveryControlConfigClient> proxyClient,
            ResourceModel model,
            CallbackContext context
    ) {
        {
            boolean isAssertion = model.getAssertionRule() != null;

            String safetyRuleArn = isAssertion ? createResponse.assertionRule().safetyRuleArn() :
                    createResponse.gatingRule().safetyRuleArn();

            if (safetyRuleArn == null) return false;

            ResourceModel createModel = Translator.translateFromCreateResponse(createResponse);

            model.setName(createModel.getName());
            model.setSafetyRuleArn(createModel.getSafetyRuleArn());
            model.setControlPanelArn(createModel.getControlPanelArn());
            model.setStatus(createModel.getStatus());
            model.setRuleConfig(createModel.getRuleConfig());

            if (isAssertion) {
                model.setAssertionRule(createModel.getAssertionRule());
            } else {
                model.setGatingRule(createModel.getGatingRule());
            }

            DescribeSafetyRuleResponse describeResponse;

            try{
                describeResponse = getSafetyRule(DescribeSafetyRuleRequest.builder().safetyRuleArn(model.getSafetyRuleArn()).build(), proxyClient);
                logger.log(String.format("Describe Response %s", describeResponse));
            } catch (CfnNotFoundException e) {
                logger.log(String.format("%s has not yet stabilized.", ResourceModel.TYPE_NAME));
                return false;
            }

           String status = isAssertion ? describeResponse.assertionRule().statusAsString() : describeResponse.gatingRule().statusAsString();

            return status.equals("DEPLOYED") || status.equals("PENDING");

        }
    }
}
