package software.amazon.route53recoverycontrol.routingcontrol;

import software.amazon.awssdk.services.route53recoverycontrolconfig.model.CreateRoutingControlRequest;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DeleteRoutingControlRequest;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DescribeRoutingControlRequest;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DescribeRoutingControlResponse;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.ListRoutingControlsRequest;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.ListRoutingControlsResponse;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.UpdateRoutingControlRequest;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
  static CreateRoutingControlRequest translateToCreateRequest(final ResourceModel model) {
    return CreateRoutingControlRequest.builder()
            .routingControlName(model.getName())
            .controlPanelArn(model.getControlPanelArn())
            .clusterArn((model.getClusterArn()))
            .build();
  }

  /**
   * Request to read a resource
   * @param model resource model
   * @return awsRequest the aws service request to describe a resource
   */
  static DescribeRoutingControlRequest translateToReadRequest(final ResourceModel model) {
    return DescribeRoutingControlRequest.builder()
            .routingControlArn(model.getRoutingControlArn())
            .build();
  }

  /**
   * Translates resource object from sdk into a resource model
   * @param response the aws service describe resource response
   * @return model resource model
   */
  static ResourceModel translateFromReadResponse(final DescribeRoutingControlResponse response) {
    return ResourceModel.builder()
            .name(response.routingControl().name())
            .controlPanelArn(response.routingControl().controlPanelArn())
            .status(response.routingControl().statusAsString())
            .routingControlArn(response.routingControl().routingControlArn())
            .clusterArn(response.routingControl().controlPanelArn())
            .build();
  }

  /**
   * Request to delete a resource
   * @param model resource model
   * @return awsRequest the aws service request to delete a resource
   */
  static DeleteRoutingControlRequest translateToDeleteRequest(final ResourceModel model) {
    return DeleteRoutingControlRequest.builder()
            .routingControlArn(model.getRoutingControlArn())
            .build();
  }

  /**
   * Request to update properties of a previously created resource
   * @param model resource model
   * @return awsRequest the aws service request to modify a resource
   */
  static UpdateRoutingControlRequest translateToUpdateRequest(final ResourceModel model) {
    return UpdateRoutingControlRequest.builder()
            .routingControlArn(model.getRoutingControlArn())
            .routingControlName(model.getName())
            .build();
  }

  /**
   * Request to list resources
   * @param nextToken token passed to the aws service list resources request
   * @return awsRequest the aws service request to list resources within aws account
   */
  static ListRoutingControlsRequest translateToListRequest(ResourceModel model, String nextToken) {
    return ListRoutingControlsRequest.builder()
            .controlPanelArn(model.getControlPanelArn())
            .nextToken(nextToken)
            .build();
  }

  /**
   * Translates resource objects from sdk into a resource model (primary identifier only)
   * @param response the aws service describe resource response
   * @return list of resource models
   */
  static List<ResourceModel> translateFromListRequest(final ListRoutingControlsResponse response) {
    return streamOfOrEmpty(response.routingControls())
        .map(routingControl -> ResourceModel.builder()
            .routingControlArn(routingControl.routingControlArn())
            .build())
        .collect(Collectors.toList());
  }

  private static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
    return Optional.ofNullable(collection)
        .map(Collection::stream)
        .orElseGet(Stream::empty);
  }
}
