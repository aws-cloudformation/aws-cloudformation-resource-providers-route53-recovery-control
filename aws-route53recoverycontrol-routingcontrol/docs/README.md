# AWS::Route53RecoveryControl::RoutingControl

AWS Route53 Recovery Control Routing Control resource schema .

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::Route53RecoveryControl::RoutingControl",
    "Properties" : {
        "<a href="#controlpanelarn" title="ControlPanelArn">ControlPanelArn</a>" : <i>String</i>,
        "<a href="#name" title="Name">Name</a>" : <i>String</i>,
        "<a href="#clusterarn" title="ClusterArn">ClusterArn</a>" : <i>String</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::Route53RecoveryControl::RoutingControl
Properties:
    <a href="#controlpanelarn" title="ControlPanelArn">ControlPanelArn</a>: <i>String</i>
    <a href="#name" title="Name">Name</a>: <i>String</i>
    <a href="#clusterarn" title="ClusterArn">ClusterArn</a>: <i>String</i>
</pre>

## Properties

#### ControlPanelArn

The Amazon Resource Name (ARN) of the control panel.

_Required_: Yes

_Type_: String

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### Name

The name of the routing control. You can use any non-white space character in the name.

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>64</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### ClusterArn

Arn associated with Control Panel

_Required_: Yes

_Type_: String

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the RoutingControlArn.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### RoutingControlArn

The Amazon Resource Name (ARN) of the routing control.

#### Status

The deployment status of the routing control. Status can be one of the following: PENDING, DEPLOYED, PENDING_DELETION.

