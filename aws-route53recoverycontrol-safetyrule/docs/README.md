# AWS::Route53RecoveryControl::SafetyRule

Resource schema for AWS Route53 Recovery Control basic constructs and validation rules.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::Route53RecoveryControl::SafetyRule",
    "Properties" : {
        "<a href="#assertionrule" title="AssertionRule">AssertionRule</a>" : <i><a href="assertionrule.md">AssertionRule</a></i>,
        "<a href="#gatingrule" title="GatingRule">GatingRule</a>" : <i><a href="gatingrule.md">GatingRule</a></i>,
        "<a href="#name" title="Name">Name</a>" : <i>String</i>,
        "<a href="#controlpanelarn" title="ControlPanelArn">ControlPanelArn</a>" : <i>String</i>,
        "<a href="#ruleconfig" title="RuleConfig">RuleConfig</a>" : <i><a href="ruleconfig.md">RuleConfig</a></i>,
        "<a href="#tags" title="Tags">Tags</a>" : <i>[ <a href="tag.md">Tag</a>, ... ]</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::Route53RecoveryControl::SafetyRule
Properties:
    <a href="#assertionrule" title="AssertionRule">AssertionRule</a>: <i><a href="assertionrule.md">AssertionRule</a></i>
    <a href="#gatingrule" title="GatingRule">GatingRule</a>: <i><a href="gatingrule.md">GatingRule</a></i>
    <a href="#name" title="Name">Name</a>: <i>String</i>
    <a href="#controlpanelarn" title="ControlPanelArn">ControlPanelArn</a>: <i>String</i>
    <a href="#ruleconfig" title="RuleConfig">RuleConfig</a>: <i><a href="ruleconfig.md">RuleConfig</a></i>
    <a href="#tags" title="Tags">Tags</a>: <i>
      - <a href="tag.md">Tag</a></i>
</pre>

## Properties

#### AssertionRule

An assertion rule enforces that, when a routing control state is changed, that the criteria set by the rule configuration is met. Otherwise, the change to the routing control is not accepted.

_Required_: No

_Type_: <a href="assertionrule.md">AssertionRule</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### GatingRule

A gating rule verifies that a set of gating controls evaluates as true, based on a rule configuration that you specify. If the gating rule evaluates to true, Amazon Route 53 Application Recovery Controller allows a set of routing control state changes to run and complete against the set of target controls.

_Required_: No

_Type_: <a href="gatingrule.md">GatingRule</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Name

The name for the safety rule.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ControlPanelArn

The Amazon Resource Name (ARN) of the control panel.

_Required_: No

_Type_: String

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### RuleConfig

The rule configuration for an assertion rule or gating rule. This is the criteria that you set for specific assertion controls (routing controls) or gating controls. This configuration specifies how many controls must be enabled after a transaction completes.

_Required_: No

_Type_: <a href="ruleconfig.md">RuleConfig</a>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### Tags

A collection of tags associated with a resource

_Required_: No

_Type_: List of <a href="tag.md">Tag</a>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the SafetyRuleArn.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### SafetyRuleArn

The Amazon Resource Name (ARN) of the safety rule.

#### Status

The deployment status of the routing control. Status can be one of the following: PENDING, DEPLOYED, PENDING_DELETION.

