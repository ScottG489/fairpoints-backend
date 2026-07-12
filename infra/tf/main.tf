provider "aws" {
  region = "us-west-2"
}

terraform {
  backend "s3" {
    bucket = "tfstate-fairpoints-backend"
    key    = "app.tfstate"
    region = "us-west-2"
  }
}

module "helpers_spot_instance_ssh" {
  source        = "ScottG489/helpers/aws//modules/spot_instance_ssh"
  version       = "0.1.12"
  name          = var.domain_name
  public_key    = var.public_key
  instance_type = var.instance_type
  spot_price    = var.spot_price
  spot_type     = var.spot_type
  volume_size   = var.volume_size
}

module "fairpoints_backend" {
  source      = "./modules/fairpoints_backend_core"
  domain_name = var.domain_name
  public_ip   = module.helpers_spot_instance_ssh.public_ip
  table_name  = "Channels"
}

// TODO: It's hacky but since we use but don't manage the hosted zone. The frontend project does.
//module "helpers_route53_domain_name_servers" {
//  source  = "ScottG489/helpers/aws//modules/route53_domain_name_servers"
//  version = "0.0.4"
//  route53_zone_name = module.fairpoints_backend.r53_zone_name
//  route53_zone_name_servers = module.fairpoints_backend.r53_zone_name_servers
//}
