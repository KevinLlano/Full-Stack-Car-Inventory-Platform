output "rds_endpoint" {
  description = "RDS instance endpoint"
  value       = aws_db_instance.backend_db.address
}

output "ebs_env_url" {
  description = "Elastic Beanstalk environment URL"
  value       = aws_elastic_beanstalk_environment.env.endpoint_url
}

output "s3_bucket_name" {
  description = "S3 bucket name for app"
  value       = aws_s3_bucket.ebs_app_bucket.bucket
}
