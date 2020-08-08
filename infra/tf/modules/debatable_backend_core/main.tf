resource "aws_route53_zone" "r53_zone" {
  name = var.domain_name
}

resource "aws_route53_record" "r53_record_A_api" {
  zone_id = aws_route53_zone.r53_zone.id
  name = "api"
  records = [
    var.public_ip
  ]
  ttl = 300
  type = "A"
}

resource "aws_dynamodb_table" "dynamodb_table" {
  name = var.table_name
  read_capacity = 5
  write_capacity = 5
  hash_key = "TopicId"
  range_key = "ViewpointCreatedTimestamp"

  attribute {
    name = "TopicId"
    type = "S"
  }

  attribute {
    name = "ViewpointCreatedTimestamp"
    type = "S"
  }
}

