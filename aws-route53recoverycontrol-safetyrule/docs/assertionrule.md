# AWS::Route53RecoveryControl::SafetyRule AssertionRule

An assertion rule enforces that, when a routing control state is changed, that the criteria set by the rule configuration is met. Otherwise, the change to the routing control is not accepted.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#waitperiodms" title="WaitPeriodMs">WaitPeriodMs</a>" : <i>Integer</i>,
    "<a href="#assertedcontrols" title="AssertedControls">AssertedControls</a>" : <i>[ String, ... ]</i>
}
</pre>

### YAML

<pre>
<a href="#waitperiodms" title="WaitPeriodMs">WaitPeriodMs</a>: <i>Integer</i>
<a href="#assertedcontrols" title="AssertedControls">AssertedControls</a>: <i>
      - String</i>
</pre>

## Properties

#### WaitPeriodMs

An evaluation period, in milliseconds (ms), during which any request against the target routing controls will fail. This helps prevent "flapping" of state. The wait period is 5000 ms by default, but you can choose a custom value.

_Required_: Yes

_Type_: Integer

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### AssertedControls

The routing controls that are part of transactions that are evaluated to determine if a request to change a routing control state is allowed. For example, you might include three routing controls, one for each of three AWS Regions.

_Required_: Yes

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

