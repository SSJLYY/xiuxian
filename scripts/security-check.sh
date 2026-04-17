#!/bin/bash
# OWASP 依赖安全检查脚本

echo "======================================"
echo "OWASP Dependency Check"
echo "======================================"

PROJECT_NAME="xiuxian-game"
REPORT_DIR="./security-reports"
REPORT_FORMAT="HTML"
CVSS_THRESHOLD="7.0"

mkdir -p $REPORT_DIR

echo ""
echo "开始扫描依赖安全漏洞..."
echo "  项目名：$PROJECT_NAME"
echo "  报告目录：$REPORT_DIR"
echo "  CVSS 阈值：$CVSS_THRESHOLD"
echo ""

# 执行 OWASP Dependency Check
mvn org.owasp:dependency-check-maven:check \
  -Dproject.name=$PROJECT_NAME \
  -Dformat=$REPORT_FORMAT \
  -DfailOnCVSS=$CVSS_THRESHOLD \
  -DskipProvidedScope=false \
  -DskipSystemScope=false \
  -DanalysisSkip=false \
  -DenableRetired=false \
  -DassemblyAnalyzerEnabled=true \
  -DarchiveAnalyzerEnabled=true \
  -DéxceptionAnalyzerEnabled=true \
  -DautoconfAnalyzerEnabled=true \
  -DcmakeAnalyzerEnabled=false \
  -DcocoapodsAnalyzerEnabled=false \
  -DcppAnalyzerEnabled=false \
  -DcomposerLockAnalyzerEnabled=false \
  -DcpanfileAnalyzerEnabled=false \
  -DdartAnalyzerEnabled=false \
  -DdartLockAnalyzerEnabled=false \
  -DdebAnalyzerEnabled=false \
  -DelixirScanAnalyzerEnabled=false \
  -DErlangScanAnalyzerEnabled=false \
  -DgoAnalyzerEnabled=false \
  -DgoModAnalyzerEnabled=false \
  -DgoDepAnalyzerEnabled=false \
  -DgradleAnalyzerEnabled=false \
  -DgroovyAnalyzerEnabled=false \
  -DkotlinScriptAnalyzerEnabled=false \
  -DmavenCentralAnalyzerEnabled=true \
  -DnodeAnalyzerEnabled=false \
  -DnodeAuditAnalyzerEnabled=false \
  -DnpmAnalyzerEnabled=false \
  -DoneIconSourceAnalyzerEnabled=false \
  -DopenSSLAnalyzerEnabled=false \
  -DOssIndexAnalyzerEnabled=true \
  -DOssIndexUsername="" \
  -DOssIndexPassword="" \
  -DOssIndexUseCache=true \
  -DpipfileAnalyzerEnabled=false \
  -DpipfileAnalyzerEnabled=false \
  -DpnpmAnalyzerEnabled=false \
  -DpythonAnalyzerEnabled=false \
  -DpythonDistributionAnalyzerEnabled=false \
  -DretireJsAnalyzerEnabled=false \
  -DrubygemsAnalyzerEnabled=false \
  -DbundleAuditAnalyzerEnabled=false \
  -DswiftPackageAnalyzerEnabled=false \
  -DvisualStudioAnalyzerEnabled=false

RESULT=$?

echo ""
echo "======================================"
if [ $RESULT -eq 0 ]; then
  echo "✅ 安全检查通过！未发现高危漏洞"
else
  echo "⚠️  安全检查发现漏洞，请查看报告"
fi
echo "======================================"
echo ""
echo "报告位置:"
echo "  - HTML 报告：$REPORT_DIR/dependency-check-report.html"
echo "  - XML 报告：$REPORT_DIR/dependency-check-report.xml"
echo "  - JSON 报告：$REPORT_DIR/dependency-check-report.json"
echo ""
echo "查看报告: open $REPORT_DIR/dependency-check-report.html"

exit $RESULT
