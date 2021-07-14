# AWS::Route53RecoveryControl::SafetyRule RuleConfig

The rule configuration for an assertion rule or gating rule. This is the criteria that you set for specific assertion controls (routing controls) or gating controls. This configuration specifies how many controls must be enabled after a transaction completes.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#type" title="Type">Type</a>" : <i>String</i>,
    "<a href="#threshold" title="Threshold">Threshold</a>" : <i>Integer</i>,
    "<a href="#inverted" title="Inverted">Inverted</a>" : <i>Boolean</i>
}
</pre>

### YAML

<pre>
<a href="#type" title="Type">Type</a>: <i>String</i>
<a href="#threshold" title="Threshold">Threshold</a>: <i>Integer</i>
<a href="#inverted" title="Inverted">Inverted</a>: <i>Boolean</i>
</pre>

## Properties

#### Type

A rule can be one of the following: ATLEAST, AND, or OR.

_Required_: Yes

_Type_: String

_Allowed Values_: <code>AND</code> | <code>OR</code> | <code>ATLEAST</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Threshold

The value of N, when you specify an ATLEAST rule type. That is, Threshold is the number of controls that must be set when you specify an ATLEAST type.

_Required_: Yes

_Type_: Integer

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Inverted

Logical negation of the rule. If the rule would usually evaluate true, it's evaluated as false, and vice versa.

_Required_: Yes

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

