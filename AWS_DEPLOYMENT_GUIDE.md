# AWS CloudFormation & CodePipeline Deployment Guide

This guide explains how to deploy your Java Spring Boot inventory system using AWS CloudFormation and CodePipeline.

## Files Created

1. **buildspec.yml** - Build instructions for CodeBuild
2. **cloudformation-infrastructure.yml** - Infrastructure resources (S3, IAM roles)
3. **cloudformation-pipeline.yml** - CI/CD pipeline (CodePipeline, CodeBuild)

## Prerequisites

1. AWS CLI installed and configured
2. GitHub repository with your code
3. AWS Account with sufficient permissions

## Cost-Effective Setup

This setup uses the **cheapest AWS options**:
- **CodeBuild**: BUILD_GENERAL1_SMALL compute type (free tier: 100 build minutes/month)
- **S3**: Standard storage with lifecycle policy (30-day expiration)
- **CodePipeline**: Free tier: 1 active pipeline/month
- **No EC2 or RDS** - This is just for building and storing artifacts

## Deployment Steps

### Step 1: Create GitHub Connection (One-time setup via Console)

Since GitHub tokens in CloudFormation templates aren't secure, AWS recommends using CodeStar Connections:

1. Go to AWS Console → CodePipeline → Settings → Connections
2. Click "Create connection"
3. Choose "GitHub" as provider
4. Name it (e.g., "github-connection")
5. Click "Connect to GitHub" and authorize
6. Copy the Connection ARN (looks like: `arn:aws:codestar-connections:us-east-1:123456789012:connection/abc-123`)

### Step 2: Deploy Infrastructure Stack

```bash
aws cloudformation create-stack ^
  --stack-name inventory-infrastructure ^
  --template-body file://cloudformation-infrastructure.yml ^
  --capabilities CAPABILITY_NAMED_IAM ^
  --region us-east-1
```

Wait for the stack to complete:
```bash
aws cloudformation wait stack-create-complete --stack-name inventory-infrastructure --region us-east-1
```

### Step 3: Deploy Pipeline Stack

Update the parameters with your values:

```bash
aws cloudformation create-stack ^
  --stack-name inventory-pipeline ^
  --template-body file://cloudformation-pipeline.yml ^
  --parameters ^
    ParameterKey=InfrastructureStackName,ParameterValue=inventory-infrastructure ^
    ParameterKey=GitHubOwner,ParameterValue=YourGitHubUsername ^
    ParameterKey=GitHubRepo,ParameterValue=aws-inventory-system ^
    ParameterKey=GitHubBranch,ParameterValue=main ^
    ParameterKey=GitHubConnectionArn,ParameterValue=YOUR_CONNECTION_ARN_FROM_STEP1 ^
  --region us-east-1
```

Wait for the stack to complete:
```bash
aws cloudformation wait stack-create-complete --stack-name inventory-pipeline --region us-east-1
```

### Step 4: Verify Deployment

Get the pipeline URL:
```bash
aws cloudformation describe-stacks ^
  --stack-name inventory-pipeline ^
  --query "Stacks[0].Outputs[?OutputKey=='PipelineUrl'].OutputValue" ^
  --output text ^
  --region us-east-1
```

## What Happens Next

1. Push code to your GitHub repository
2. CodePipeline automatically detects changes
3. CodeBuild runs Maven build (`mvn clean package`)
4. Build artifacts are stored in S3
5. You can download the JAR file from S3 or add a deploy stage later

## Viewing Resources

**Infrastructure Stack:**
```bash
aws cloudformation describe-stacks --stack-name inventory-infrastructure --region us-east-1
```

**Pipeline Stack:**
```bash
aws cloudformation describe-stacks --stack-name inventory-pipeline --region us-east-1
```

**View Pipeline Status:**
```bash
aws codepipeline get-pipeline-state --name inventory-pipeline-pipeline --region us-east-1
```

## Clean Up (To Avoid Charges)

When you're done testing, delete stacks in reverse order:

```bash
# Delete pipeline stack first
aws cloudformation delete-stack --stack-name inventory-pipeline --region us-east-1

# Wait for deletion
aws cloudformation wait stack-delete-complete --stack-name inventory-pipeline --region us-east-1

# Delete infrastructure stack
aws cloudformation delete-stack --stack-name inventory-infrastructure --region us-east-1

# Manually empty and delete S3 bucket if needed
aws s3 rm s3://inventory-infrastructure-artifacts-YOUR_ACCOUNT_ID --recursive
```

## Troubleshooting

**Build Fails:**
- Check CodeBuild logs in AWS Console
- Verify buildspec.yml is in repository root
- Ensure Java 17 is compatible with your code

**Pipeline Doesn't Trigger:**
- Verify GitHub connection is active
- Check CodePipeline execution history
- Ensure branch name matches

**Permission Errors:**
- Verify IAM roles have correct permissions
- Check you used `--capabilities CAPABILITY_NAMED_IAM` flag

## Next Steps (Optional)

To extend this setup:
1. Add deployment stage (Elastic Beanstalk, ECS, or EC2)
2. Add test stage in pipeline
3. Add manual approval stage before deploy
4. Set up SNS notifications for build status

## Cost Monitoring

Monitor your AWS costs:
```bash
aws ce get-cost-and-usage --time-period Start=2025-11-01,End=2025-11-30 --granularity MONTHLY --metrics BlendedCost
```

**Expected monthly costs for testing:**
- ~$0-5 (mostly free tier if under 100 build minutes/month)

