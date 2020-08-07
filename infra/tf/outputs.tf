output "instance_public_ip" {
  value = module.helpers_spot_instance_ssh.public_ip
}

output "dynamodb_table" {
  value = module.debatable_backend.dynamodb_table_name
}
