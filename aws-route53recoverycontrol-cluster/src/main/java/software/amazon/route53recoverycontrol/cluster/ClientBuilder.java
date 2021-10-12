package software.amazon.route53recoverycontrol.cluster;


import java.net.URI;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.route53recoverycontrolconfig.Route53RecoveryControlConfigClient;
import software.amazon.cloudformation.LambdaWrapper;

public class ClientBuilder {

  public static Route53RecoveryControlConfigClient getClient() {
      URI myUri = URI.create("https://route53-recovery-control-config.us-west-2.amazonaws.com");

      return Route53RecoveryControlConfigClient.builder()
              .endpointOverride(myUri)
              .region(Region.US_WEST_2)
              .httpClient(LambdaWrapper.HTTP_CLIENT)
              .build();
  }
}
