# AWS::Route53RecoveryControl::Cluster

AWS Route53 Recovery Control Cluster resource schema

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::Route53RecoveryControl::Cluster",
    "Properties" : {
        "<a href="#name" title="Name">Name</a>" : <i>String</i>,
    }
}
</pre>

### YAML

<pre>
Type: AWS::Route53RecoveryControl::Cluster
Properties:
    <a href="#name" title="Name">Name</a>: <i>String</i>
</pre>

## Properties

#### Name

Name of a Cluster. You can use any non-white space character in the name

_Required_: No

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>64</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the ClusterArn.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### Status

Deployment status of a resource. Status can be one of the following: PENDING, DEPLOYED, PENDING_DELETION.

#### Endpoints

Returns the <code>Endpoints</code> value.

#### ClusterEndpoints

Endpoints for the cluster.

#### ClusterArn

The Amazon Resource Name (ARN) of the cluster.

