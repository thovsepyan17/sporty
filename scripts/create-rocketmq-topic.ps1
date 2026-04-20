# Creates topic "bet-settlements" on the local Docker RocketMQ broker.
# Run after: docker-compose up -d
# Usage: .\scripts\create-rocketmq-topic.ps1
#
# (RocketMQ Dashboard "Add topic" often fails; this uses the official mqadmin CLI.)

$ErrorActionPreference = "Stop"

# Stop PowerShell from interpreting flags meant for mqadmin
docker --% exec sporty-rocketmq-broker sh mqadmin updateTopic -n rocketmq-namesrv:9876 -t bet-settlements -c DefaultCluster -r 4 -w 4

if ($LASTEXITCODE -ne 0) {
  Write-Error "Failed to create topic. Is sporty-rocketmq-broker running?"
  exit $LASTEXITCODE
}

Write-Host "Topic 'bet-settlements' is ready. Refresh RocketMQ Dashboard -> Topic tab."
