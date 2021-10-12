package software.amazon.route53recoverycontrol.controlpanel;


import software.amazon.awssdk.services.route53recoverycontrolconfig.model.CreateControlPanelRequest;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DeleteControlPanelRequest;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DescribeControlPanelRequest;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.DescribeControlPanelResponse;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.ListControlPanelsRequest;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.ListControlPanelsResponse;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.UpdateControlPanelRequest;
import software.amazon.awssdk.services.route53recoverycontrolconfig.model.Tag;

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
  static CreateControlPanelRequest translateToCreateRequest(final ResourceModel model) {
    CreateControlPanelRequest.Builder requestBuilder = CreateControlPanelRequest.builder();
    if (model.getTags() != null) {
      requestBuilder.tags(buildCreateControlPanelRequestTags(model.getTags()));
    }
    requestBuilder.clusterArn(model.getClusterArn()).controlPanelName(model.getName());
    return requestBuilder.build();
  }

  /**
   * Request to read a resource
   * @param model resource model
   * @return awsRequest the aws service request to describe a resource
   */
  static DescribeControlPanelRequest translateToReadRequest(final ResourceModel model) {
    return DescribeControlPanelRequest.builder()
            .controlPanelArn(model.getControlPanelArn())
            .build();
  }

  /**
   * Translates resource object from sdk into a resource model
   * @param response the aws service describe resource response
   * @return model resource model
   */
  static ResourceModel translateFromReadResponse(final DescribeControlPanelResponse response) {
    return ResourceModel.builder()
        .clusterArn(response.controlPanel().clusterArn())
            .defaultControlPanel(response.controlPanel().defaultControlPanel())
            .controlPanelArn(response.controlPanel().controlPanelArn())
            .name(response.controlPanel().name())
            .status(response.controlPanel().statusAsString())
        .build();
  }

  /**
   * Request to delete a resource
   * @param model resource model
   * @return awsRequest the aws service request to delete a resource
   */
  static DeleteControlPanelRequest translateToDeleteRequest(final ResourceModel model) {
    return DeleteControlPanelRequest.builder()
            .controlPanelArn(model.getControlPanelArn())
            .build();
  }

  /**
   * Request to update properties of a previously created resource
   * @param model resource model
   * @return awsRequest the aws service request to modify a resource
   */
  static UpdateControlPanelRequest translateToUpdateRequest(final ResourceModel model) {
    return UpdateControlPanelRequest.builder()
            .controlPanelArn(model.getControlPanelArn())
            .controlPanelName(model.getName())
            .build();
  }

  /**
   * Request to list resources
   * @param nextToken token passed to the aws service list resources request
   * @return awsRequest the aws service request to list resources within aws account
   */
  static ListControlPanelsRequest translateToListRequest(final String nextToken) {
    return ListControlPanelsRequest.builder()
            .nextToken(nextToken)
            .build();
  }

  /**
   * Translates resource objects from sdk into a resource model (primary identifier only)
   * @param response the aws service describe resource response
   * @return list of resource models
   */
  static List<ResourceModel> translateFromListRequest(final ListControlPanelsResponse response) {
    return streamOfOrEmpty(response.controlPanels())
        .map(controlPanel -> ResourceModel.builder()
            .controlPanelArn(controlPanel.controlPanelArn())
            .build())
        .collect(Collectors.toList());
  }

  static List<Tag> buildCreateControlPanelRequestTags(List<software.amazon.route53recoverycontrol.controlpanel.Tag> tags) {
    return tags.stream()
        .map(modelTag -> Tag.builder()
            .key(modelTag.getKey())
            .value(modelTag.getValue())
            .build())
        .collect(Collectors.toList());
  }

  private static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
    return Optional.ofNullable(collection)
        .map(Collection::stream)
        .orElseGet(Stream::empty);
  }
}
