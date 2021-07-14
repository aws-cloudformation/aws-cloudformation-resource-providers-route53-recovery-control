package software.amazon.route53recoverycontrol.cluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import software.amazon.awssdk.services.route53recoverycontrolconfig.model.CreateClusterRequest;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DeleteClusterRequest;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DescribeClusterRequest;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DescribeClusterResponse;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.ListClustersRequest;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.ListClustersResponse;

/**
 * This class is a centralized placeholder for
 *  - api request construction
 *  - object translation to/from aws sdk
 *  - resource model construction for read/list handlers
 */

public class Translator {

  /**
   * Request to create a resource
   * @param model resource model
   * @return awsRequest the aws service request to create a resource
   */
  static CreateClusterRequest translateToCreateRequest(final ResourceModel model) {
    return CreateClusterRequest.builder()
            .clusterName(model.getName())
            .build();
  }

  /**
   * Request to read a resource
   * @param model resource model
   * @return awsRequest the aws service request to describe a resource
   */
  static DescribeClusterRequest translateToReadRequest(final ResourceModel model) {
    return DescribeClusterRequest.builder()
            .clusterArn(model.getClusterArn())
            .build();
  }

  /**
   * Translates resource object from sdk into a resource model
   * @param response the aws service describe resource response
   * @return model resource model
   */
  static ResourceModel translateFromReadResponse(final DescribeClusterResponse response) {
    return ResourceModel.builder()
            .clusterArn(response.cluster().clusterArn())
            .clusterEndpoints(buildModelClusterEndpoints(response.cluster().clusterEndpoints()))
            .name(response.cluster().name())
            .status(response.cluster().statusAsString())
            .build();
  }

  static List<ClusterEndpoint> buildModelClusterEndpoints(List<software.amazon.awssdk.services.route53recoverycontrolconfig.model.ClusterEndpoint> endpoints) {
    List<ClusterEndpoint> clusterEndpoints = new ArrayList<>();

    for(software.amazon.awssdk.services.route53recoverycontrolconfig.model.ClusterEndpoint endpoint : endpoints) {
      clusterEndpoints.add(
              ClusterEndpoint.builder()
                      .endpoint(endpoint.endpoint())
                      .region(endpoint.region())
                      .build()
      );
    }
    return clusterEndpoints;
  }

  /**
   * Request to delete a resource
   * @param model resource model
   * @return awsRequest the aws service request to delete a resource
   */
  static DeleteClusterRequest translateToDeleteRequest(final ResourceModel model) {
    return DeleteClusterRequest.builder()
            .clusterArn(model.getClusterArn())
            .build();
  }

  /**
   * Request to list resources
   * @param nextToken token passed to the aws service list resources request
   * @return awsRequest the aws service request to list resources within aws account
   */
  static ListClustersRequest translateToListRequest(final String nextToken) {
    return ListClustersRequest.builder()
            .nextToken(nextToken)
            .build();
  }

  /**
   * Translates resource objects from sdk into a resource model (primary identifier only)
   * @param response the aws service describe resource response
   * @return list of resource models
   */
  static List<ResourceModel> translateFromListRequest(final ListClustersResponse response) {
    return streamOfOrEmpty(response.clusters())
        .map(cluster -> ResourceModel.builder()
            .clusterArn(cluster.clusterArn())
            .build())
        .collect(Collectors.toList());
  }

  private static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
    return Optional.ofNullable(collection)
        .map(Collection::stream)
        .orElseGet(Stream::empty);
  }
}
