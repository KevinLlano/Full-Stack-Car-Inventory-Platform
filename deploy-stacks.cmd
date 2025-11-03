@echo off
REM Deploy CloudFormation Stacks for AWS Inventory System

set REGION=us-east-1
set INFRA_STACK=inventory-infrastructure
set PIPELINE_STACK=inventory-pipeline

echo.
echo ============================================
echo Step 1: Deploying Infrastructure Stack
echo ============================================
aws cloudformation create-stack ^
  --stack-name %INFRA_STACK% ^
  --template-body file://cloudformation-infrastructure.yml ^
  --region %REGION% ^
  --capabilities CAPABILITY_NAMED_IAM

if errorlevel 1 (
  echo Error creating infrastructure stack. Trying to update instead...
  aws cloudformation update-stack ^
    --stack-name %INFRA_STACK% ^
    --template-body file://cloudformation-infrastructure.yml ^
    --region %REGION% ^
    --capabilities CAPABILITY_NAMED_IAM
)

echo.
echo Waiting for infrastructure stack to complete (this may take 1-2 minutes)...
aws cloudformation wait stack-create-complete ^
  --stack-name %INFRA_STACK% ^
  --region %REGION%

if errorlevel 1 (
  echo Infrastructure stack update in progress or completed. Continuing...
)

echo.
echo ============================================
echo Step 2: Deploying Pipeline Stack
echo ============================================
aws cloudformation create-stack ^
  --stack-name %PIPELINE_STACK% ^
  --template-body file://cloudformation-pipeline.yml ^
  --region %REGION% ^
  --capabilities CAPABILITY_NAMED_IAM

if errorlevel 1 (
  echo Error creating pipeline stack. Trying to update instead...
  aws cloudformation update-stack ^
    --stack-name %PIPELINE_STACK% ^
    --template-body file://cloudformation-pipeline.yml ^
    --region %REGION% ^
    --capabilities CAPABILITY_NAMED_IAM
)

echo.
echo Waiting for pipeline stack to complete...
aws cloudformation wait stack-create-complete ^
  --stack-name %PIPELINE_STACK% ^
  --region %REGION%

if errorlevel 1 (
  echo Pipeline stack update in progress or completed. Continuing...
)

echo.
echo ============================================
echo Deployment Summary
echo ============================================
echo.
echo Infrastructure Stack Status:
aws cloudformation describe-stacks ^
  --stack-name %INFRA_STACK% ^
  --query "Stacks[0].{StackName:StackName,StackStatus:StackStatus}" ^
  --region %REGION%

echo.
echo Pipeline Stack Status:
aws cloudformation describe-stacks ^
  --stack-name %PIPELINE_STACK% ^
  --query "Stacks[0].{StackName:StackName,StackStatus:StackStatus}" ^
  --region %REGION%

echo.
echo Pipeline URL:
aws cloudformation describe-stacks ^
  --stack-name %PIPELINE_STACK% ^
  --query "Stacks[0].Outputs[?OutputKey=='PipelineUrl'].OutputValue" ^
  --region %REGION% ^
  --output text

echo.
echo ============================================
echo Deployment Complete!
echo ============================================
pause

