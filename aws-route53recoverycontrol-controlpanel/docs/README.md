# AWS::Route53RecoveryControl::ControlPanel

AWS Route53 Recovery Control Control Panel resource schema .

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::Route53RecoveryControl::ControlPanel",
    "Properties" : {
        "<a href="#clusterarn" title="ClusterArn">ClusterArn</a>" : <i>String</i>,
        "<a href="#name" title="Name">Name</a>" : <i>String</i>,
    }
}
</pre>

### YAML

<pre>
Type: AWS::Route53RecoveryControl::ControlPanel
Properties:
    <a href="#clusterarn" title="ClusterArn">ClusterArn</a>: <i>String</i>
    <a href="#name" title="Name">Name</a>: <i>String</i>
</pre>

## Properties

#### ClusterArn

Cluster to associate with the Control Panel

_Required_: Yes

_Type_: String

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### Name

The name of the control panel. You can use any non-white space character in the name.

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>64</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the ControlPanelArn.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### ControlPanelArn

The Amazon Resource Name (ARN) of the cluster.

#### Status

The deployment status of control panel. Status can be one of the following: PENDING, DEPLOYED, PENDING_DELETION.

#### RoutingControlCount

Count of associated routing controls

#### DefaultControlPanel

A flag that Amazon Route 53 Application Recovery Controller sets to true to designate the default control panel for a cluster. When you create a cluster, Amazon Route 53 Application Recovery Controller creates a control panel, and sets this flag for that control panel. If you create a control panel yourself, this flag is set to false.

