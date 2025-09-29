variable "app_version" {
  description = "Application version label4"
  type        = string
  default     = "v8"  # Updated version to deploy
}

// Database config
data "aws_vpc" "default" {
  default = true
}

resource "aws_security_group" "db_sg" {
  name        = "rds_security_group"
  description = "Allow inbound PostgreSQL access"
  vpc_id      = data.aws_vpc.default.id

  ingress {
    from_port   = 5432
    to_port     = 5432
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"] // test only
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_db_instance" "backend_db" {
  engine                 = "postgres"
  instance_class         = "db.t4g.micro"
  username               = "postgres"
  password               = ""
  allocated_storage      = 20
  skip_final_snapshot    = true
  publicly_accessible    = true
  vpc_security_group_ids = [aws_security_group.db_sg.id]
}

resource "aws_s3_bucket" "ebs_app_bucket" {
  bucket        = "ebsbucket-framework"
  force_destroy = true
}

resource "aws_s3_object" "app_zip" {
  bucket = aws_s3_bucket.ebs_app_bucket.bucket
  key    = "app-${var.app_version}.zip"
  source = ""
}

// IAM Role & Instance Profile for Elastic Beanstalk EC2
resource "aws_iam_role" "eb_role" {
  name = "eb-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Principal = {
        Service = "ec2.amazonaws.com"
      }
      Action = "sts:AssumeRole"
    }]
  })
}

resource "aws_iam_role_policy_attachment" "eb_role_attach" {
  role       = aws_iam_role.eb_role.name
  policy_arn = "arn:aws:iam::aws:policy/AWSElasticBeanstalkWebTier"
}

resource "aws_iam_instance_profile" "eb_instance_profile" {
  name = "eb-instance-profile"
  role = aws_iam_role.eb_role.name
}

resource "aws_elastic_beanstalk_application" "app" {
  name        = "my-ebs-app"
  description = "Spring Boot app on Beanstalk"
}

resource "aws_elastic_beanstalk_application_version" "app_version" {
  application = aws_elastic_beanstalk_application.app.name
  name        = var.app_version  
  bucket      = aws_s3_bucket.ebs_app_bucket.bucket
  key         = aws_s3_object.app_zip.key
}

resource "aws_elastic_beanstalk_environment" "env" {
  name                = "my-ebs-env"
  application         = aws_elastic_beanstalk_application.app.name
  solution_stack_name = "64bit Amazon Linux 2 v3.8.2 running Corretto 17"

  version_label = aws_elastic_beanstalk_application_version.app_version.name  # Using the latest version

  setting {
    namespace = "aws:elasticbeanstalk:application:environment"
    name      = "SPRING_DATASOURCE_URL"
    value     = "jdbc:postgresql://${aws_db_instance.backend_db.address}:5432/postgres"
  }

  setting {
    namespace = "aws:autoscaling:launchconfiguration"
    name      = "InstanceType"
    value     = "t2.micro"
  }

  setting {
    namespace = "aws:autoscaling:launchconfiguration"
    name      = "IamInstanceProfile"
    value     = aws_iam_instance_profile.eb_instance_profile.name
  }

  setting {
    namespace = "aws:elasticbeanstalk:application:environment"
    name      = "SPRING_DATASOURCE_USERNAME"
    value     = "postgres"
  }

  setting {
    namespace = "aws:elasticbeanstalk:environment:process:default"
    name      = "ProxyServer"
    value     = "none"
  }

  setting {
    namespace = "aws:elasticbeanstalk:application:environment"
    name      = "SERVER_PORT"
    value     = "8080"
  }

  setting {
    namespace = "aws:elasticbeanstalk:environment:process:default"
    name      = "Port"
    value     = "8080"
  }

  setting {
    namespace = "aws:elasticbeanstalk:application:environment"
    name      = "SPRING_DATASOURCE_PASSWORD"
    value     = ""
  }

  setting {
    namespace = "aws:elasticbeanstalk:application"
    name      = "Application Healthcheck URL"
    value     = "/"
  }

  setting {
    namespace = "aws:elasticbeanstalk:environment"
    name      = "EnvironmentType"
    value     = "SingleInstance"
  }
}
