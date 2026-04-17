# Slack Webhook 配置示例

# 1. 创建 Slack App
# 访问：https://api.slack.com/apps
# 点击 "Add to <Your Workspace>"

# 2. 添加 Incoming WebHooks
# Features → Incoming WebHooks → Activate Webhooks
# Add New Webhook to Workspace

# 3. 获取 Webhook URL
# 格式：https://hooks.slack.com/services/{WORKSPACE_ID}/{BOT_ID}/{SECRET}

# 4. 配置环境变量（替换为你的实际 URL）
# export SLACK_WEBHOOK_URL="YOUR_SLACK_WEBHOOK_URL_HERE"
# export SLACK_CRITICAL_WEBHOOK_URL="YOUR_CRITICAL_ALERTS_WEBHOOK_URL_HERE"
# export SLACK_WARNING_WEBHOOK_URL="YOUR_WARNING_ALERTS_WEBHOOK_URL_HERE"

# 5. 测试 Webhook
# curl -X POST -H 'Content-type: application/json' \
#   --data '{"text":"Hello, World!"}' \
#   $SLACK_WEBHOOK_URL

# 6. 更新 Alertmanager 配置
# 编辑 monitoring/alertmanager/alertmanager.yml
# 将 SLACK_WEBHOOK_URL 替换为实际的 Webhook URL

# Slack 频道说明
# - #alerts - 所有告警通知
# - #critical-alerts - 严重告警（电话通知）
# - #warnings - 警告通知（邮件通知）
# - #deployments - 部署通知

# Slack 通知消息模板示例
# ✅ 部署成功
# {
#   "text": "✅ Deployed xiuxian-game to production",
#   "attachments": [
#     {
#       "color": "good",
#       "fields": [
#         {"title": "Commit", "value": "abc123", "short": true},
#         {"title": "Author", "value": "shaun.sheng", "short": true},
#         {"title": "Duration", "value": "3m 45s", "short": true},
#         {"title": "Status", "value": "Success", "short": true}
#       ]
#     }
#   ]
# }

# ❌ 告警通知
# {
#   "text": "🔴 Critical Alert: High Error Rate",
#   "attachments": [
#     {
#       "color": "danger",
#       "fields": [
#         {"title": "Alert", "value": "HighErrorRate", "short": true},
#         {"title": "Severity", "value": "Critical", "short": true},
#         {"title": "Value", "value": "8.5%", "short": true},
#         {"title": "Threshold", "value": ">5%", "short": true},
#         {"title": "Description", "value": "Error rate exceeds 5% for 5 minutes", "short": false},
#         {"title": "Runbook", "value": "http://wiki.internal/runbook", "short": false}
#       ]
#     }
#   ]
# }
