# MemoryLane(Anime Pictures): Highly Available Containerized Web Application

## Overview

MemoryLane is a highly available containerized web application deployed within a custom AWS VPC spanning multiple Availability Zones. The application provides a dashboard for viewing images stored in an Amazon S3 bucket with pagination support, and allows users to upload new images to the bucket.

## Architecture

### AWS Infrastructure

- **VPC**: Custom Virtual Private Cloud spanning multiple Availability Zones
- **Compute**: Amazon ECS with Fargate for containerized application
- **Storage**: Amazon S3 bucket for image storage
- **CI/CD**: GitHub Actions for CI/CD pipeline in conjunction with AWS CodeDeploy
- **Container Registry**: Amazon ECR for storing container images
- **Load Balancing**: Application Load Balancer for distributing traffic

### High Availability & Scaling

- Multiple Availability Zones deployment
- Auto Scaling based on demand
- Blue/Green deployment strategy for zero-downtime updates

## Features

- **Image Dashboard**: View all images stored in the S3 bucket
- **Pagination**: Efficiently browse through large image collections
- **Image Upload**: Securely upload new images to the S3 bucket

## Technical Stack

- **Backend**: Java application running on an HTTP server
- **Frontend**: HTML, CSS, and JavaScript
- **Containerization**: Docker
- **Build Tool**: Maven
- **JDK Version**: 21

## Deployment

### CI/CD Pipeline

Our application uses a fully automated CI/CD pipeline with GitHub Actions for continuous integration and deployment.

#### Pipeline Stages

1. **Build & Push**:
   - Triggered on pushes to the `main` branch or pull requests
   - Builds the Java application using Maven
   - Creates a Docker image
   - Pushes the image to Amazon ECR

2. **Deploy** (Blue/Green Deployment):
   - Triggered after successful build & push (only for main branch)
   - Registers a new ECS task definition
   - Creates AppSpec file for CodeDeploy
   - Initiates a blue/green deployment via AWS CodeDeploy


## AWS Resources

- **ECS Cluster**: Managed with Fargate
- **ECS Service**: Configured for blue/green deployment
- **Task Definition**: `memory3-task` (2 vCPU, 4GB RAM)
- **ECR Repository**: `memory3`
- **CodeDeploy Application**: `memory3-application`
- **CodeDeploy Deployment Group**: `memory3-deployment-group`

## Infrastructure as Code

The AWS infrastructure is provisioned using CloudFormation with GitSync feature. The CloudFormation templates are managed in another repository and automatically synced with AWS.

## Security

- **VPC**: Custom VPC with public and private subnets
- **IAM Roles**: Least privilege principle applied
- **Secrets Management**: All sensitive information stored in GitHub Secrets
- **Network Security**: Security groups and Network ACLs to control traffic

## Prerequisites

- AWS Account with appropriate permissions
- GitHub repository with Actions enabled
- Docker installed (for local development)
- AWS CLI configured (for local development)
- JDK 17/21
- Maven

## Local Development

1. Clone the repository
2. Create a local `.env` file with necessary environment variables
3. Run `mvn spring-boot:run` to start the application locally
4. Access the application at `http://localhost:8080`

## Environment Variables

The following environment variables are required:

- `AWS_ACCESS_KEY_ID`: AWS access key
- `AWS_SECRET_ACCESS_KEY`: AWS secret key
- `AWS_REGION`: AWS region (default: us-east-1)
- `S3_BUCKET_NAME`: Name of the S3 bucket for image storage


## Monitoring and Logging

- CloudWatch Logs for application logs
- CloudWatch Metrics for performance monitoring
- CloudWatch Alarms for notifications

## Troubleshooting

### Common Issues

1. **Deployment Failures**:
- Check CodeDeploy logs
- Verify task definition is correct
- Ensure IAM roles have proper permissions

2. **Image Upload Failures**:
- Verify S3 bucket permissions
- Check network connectivity
- Validate file size and type

3. **Application Performance**:
- Monitor CPU and memory usage
- Check for database bottlenecks
- Verify load balancer health checks


