# Java Spring Boot API - Deployment on AWS Lambda

## Description
This project is a **RESTful API** developed in **Java with Spring Boot** and deployed on **AWS Lambda**. It serves as a demonstration of implementing a **CI/CD pipeline** and configuring an AWS environment for serverless deployment.

The API returns a **JSON response** to HTTP requests and uses an IAM user for authentication.

## Technologies Used
- **Java 17**
- **Spring Boot 3.x**
- **AWS Lambda**
- **API Gateway**
- **IAM (Identity and Access Management)**
- **GitHub Actions (for CI/CD)**
- **AWS SAM (Serverless Application Model)**

## Prerequisites
- An active **AWS account**
- **Java** installed (JDK 17+)
- **Maven**
- **AWS CLI** configured with appropriate IAM access
- **AWS SAM CLI**
- **Docker** (for local testing)

## Installation and Local Execution
1. Clone the project:
   ```sh
   git clone https://github.com/osloviensole/JavaSpringBootToAwsDemo.git
   cd JavaSpringBootToAwsDemo
   ```

2. Install dependencies and build the project:
   ```sh
   mvn clean install
   ```

3. Start the application locally:
   ```sh
   mvn spring-boot:run
   ```

## Deployment on AWS Lambda
1. Generate a **JAR** file:
   ```sh
   mvn package
   ```
   Follow the instructions to configure your deployment.

## CI/CD with GitHub Actions
A CI/CD pipeline is set up with GitHub Actions to automate:
- **Unit and integration tests**
- **Project build**
- **Deployment to AWS Lambda**

The GitHub Actions workflow is defined in `.github/workflows/deploy.yml`.

## Authentication with IAM
The API uses an IAM user with restricted permissions to access AWS Lambda via API Gateway.

### IAM Configuration
1. Create an IAM user with the necessary permissions.
2. Generate access keys and configure them using `aws configure`.
3. Enable **IAM Authorization** validation in API Gateway.

## Example JSON Response
The API returns a JSON response as follows:
```json
{
  "message": "Welcome to the Spring Boot API deployed on AWS Lambda!",
  "timestamp": "2025-03-24T12:00:00Z"
}
```

## Future Improvements
- Add a DynamoDB database
- Implement JWT authentication
- Improve monitoring with AWS CloudWatch

## Author
[Oslovie Nsole](https://www.linkedin.com/in/oslovie-nsole-8a4621137)

